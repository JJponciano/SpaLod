import { ref } from "vue";
import * as ApiGeo from "./api-geo";

const features = {};
const datasets = [];
const catalogs = [];
const sparqlQueries = ref([]);

const visibilityChangeSubscribers = [];
const clickSubscribers = [];
const doubleClickSubscribers = [];
const labelChangeSubscribers = [];

let nbSparqlQueries = 0;

init();

export function getGeoItems() {
  const geoItems = [];

  for (const catalog of catalogs) {
    geoItems.push({
      type: "catalog",
      item: catalog,
    });

    if (catalog.expanded) {
      for (const dataset of catalog.datasets) {
        geoItems.push({
          type: "dataset",
          item: dataset,
        });

        if (dataset.expanded) {
          for (const feature of dataset.features) {
            geoItems.push({
              type: "feature",
              item: feature,
            });
          }
        }
      }
    }
  }

  return geoItems;
}

export async function init() {
  const allGeos = await ApiGeo.getAllCatalogs();

  addCatalogs(allGeos.map(({ metadatas }) => metadatas));
}

export async function refreshGeoData() {
  const allGeos = await ApiGeo.getAllCatalogs();

  addCatalogs(allGeos.map(({ metadatas }) => metadatas));

  for (const catalog of catalogs) {
    if (catalog.expanded) {
      await addDatasets(catalog);
    }
  }
}

async function addCatalogs(metadatas) {
  for (const { catalog: catalogId, label } of metadatas) {
    if (!catalogs.find(({ id }) => id === catalogId)) {
      const catalog = {
        id: catalogId,
        label,
        datasets: [],
        expanded: false,
        visible: false,
      };
      catalogs.push(catalog);
    }
  }
}

async function addDatasets(catalog) {
  const catalogDatasets = await ApiGeo.getAllDatasets(catalog.id);

  for (const {
    metadatas: { dataset: datasetId, label },
  } of catalogDatasets) {
    let dataset = datasets.find(({ id }) => id === datasetId);

    if (!dataset) {
      dataset = {
        id: datasetId,
        catalogId: catalog.id,
        label: label || "",
        features: [],
        expanded: false,
        visible: false,
      };
      datasets.push(dataset);
      catalog.datasets.push(dataset);
    }

    if (dataset.label === undefined) {
      dataset.label = label || "";
    }
  }
}

async function addFeatures(dataset) {
  let start = Date.now();
  const catalogFeatures = await ApiGeo.getAllFeatures(dataset.id);
  console.log("time elapsed1: ", Date.now() - start);

  start = Date.now();
  for (const {
    metadatas: { feature: featureId, label },
  } of catalogFeatures) {
    let feature = features[featureId];

    if (!feature) {
      feature = {
        id: featureId,
        label,
        visible: false,
        datasetId: dataset.id,
        catalogId: dataset.catalogId,
      };
      features[featureId] = feature;
      dataset.features.push(feature);
    }

    if (feature.label === undefined) {
      feature.label = label || "";
    }
  }
  console.log("time elapsed2: ", Date.now() - start);
}

export function getAllFeatures() {
  return features;
}

export function getAllCatalogs() {
  return catalogs;
}

export function getAllSparqlQueries() {
  return sparqlQueries.value;
}

export async function expandCatalog(catalogId) {
  const catalog = catalogs.find(({ id }) => id === catalogId);

  if (catalog) {
    catalog.expanded = !catalog.expanded;

    if (
      catalog.expanded &&
      (catalog.datasets.length === 0 ||
        catalog.datasets.some(({ label }) => label === undefined))
    ) {
      await addDatasets(catalog);
    }
  }
}

export async function expandDataset(datasetId) {
  const dataset = datasets.find(({ id }) => id === datasetId);

  if (dataset) {
    dataset.expanded = !dataset.expanded;

    if (
      dataset.expanded &&
      (dataset.features.length === 0 ||
        (dataset.type !== "SPARQL_QUERY" &&
          dataset.features.some(({ label }) => label === undefined)))
    ) {
      await addFeatures(dataset);
    }
  }
}

export async function setFeatureVisibility(
  featureIds,
  visible,
  remove = false
) {
  if (!Array.isArray(featureIds)) {
    featureIds = [featureIds];
  }

  const tabFeatures = [];
  let featureDataset = {};
  let allFeaturesVisible = true;
  let catalog = null;

  const start = Date.now();

  for (const featureId of featureIds) {
    const feature = features[featureId];

    if (!catalog) {
      catalog = catalogs.find(({ id }) => id === feature.catalogId);
    }

    if (feature) {
      feature.visible = visible && !remove;

      allFeaturesVisible = allFeaturesVisible && feature.visible;

      if (!featureDataset[feature.datasetId]) {
        featureDataset[feature.datasetId] = datasets.find(
          ({ id }) => id === feature.datasetId
        );

        if (remove) {
          featureDataset[feature.datasetId].copyFeatures =
            featureDataset[feature.datasetId].features.slice();
        }
      }

      if (!feature.visible && featureDataset[feature.datasetId].visible) {
        featureDataset[feature.datasetId].visible = false;
      }

      if (!feature.visible && catalog.visible) {
        catalog.visible = false;
      }

      if (feature.visible && !feature.wkt) {
        const featureWkt = await ApiGeo.getFeatureWkt(
          feature.id,
          feature.catalogId
        );

        feature.wkt = {
          geo: featureWkt[0].geo,
          type: featureWkt[0].type,
        };
      }

      if (remove) {
        delete features[featureId];
        featureDataset[feature.datasetId].copyFeatures.splice(
          featureDataset[feature.datasetId].copyFeatures.indexOf(feature),
          1
        );
      }

      tabFeatures.push(feature);
    }
  }

  if (remove) {
    for (const datasetId of Object.keys(featureDataset)) {
      featureDataset[datasetId].features =
        featureDataset[datasetId].copyFeatures;
    }
  }

  for (const datasetId of Object.keys(featureDataset)) {
    if (
      !featureDataset[datasetId].visible &&
      featureDataset[datasetId].features.every((x) => x.visible)
    ) {
      featureDataset[datasetId].visible = true;
    }
  }

  if (catalog && !catalog.visible && catalog.datasets.every((x) => x.visible)) {
    catalog.visible = true;
  }

  console.log("time elapsed: ", Date.now() - start);

  visibilityChangeSubscribers.forEach((x) => x(tabFeatures, remove));
}

export async function setDatasetVisibility(datasetId, visible, remove = false) {
  const dataset = datasets.find(({ id }) => id === datasetId);

  if (dataset) {
    visible = visible && !remove;

    dataset.visible = visible;

    if (
      visible &&
      (dataset.features.length === 0 ||
        dataset.features.some(({ wkt }) => wkt === undefined))
    ) {
      const datasetWkts = await ApiGeo.getAllDatasetWkt(dataset.id);

      const copyFeatures = dataset.features.slice();
      const copyFeaturesMap = copyFeatures.reduce((acc, x) => {
        acc[x.id] = x;
        return acc;
      }, {});

      for (const datasetWkt of datasetWkts) {
        let feature = copyFeaturesMap[datasetWkt.metadatas.feature];

        if (feature) {
          feature.wkt = {
            geo: datasetWkt.geo,
            type: datasetWkt.type,
          };
        } else {
          feature = {
            id: datasetWkt.metadatas.feature,
            datasetId: dataset.id,
            catalogId: dataset.catalogId,
            wkt: { geo: datasetWkt.geo, type: datasetWkt.type },
          };
          copyFeatures.push(feature);
          features[datasetWkt.metadatas.feature] = feature;
        }
      }

      dataset.features = copyFeatures;
    }

    setFeatureVisibility(
      dataset.features.map(({ id }) => id),
      visible,
      remove
    );

    if (remove) {
      datasets.splice(datasets.indexOf(dataset), 1);

      const catalog = catalogs.find(({ id }) => id === dataset.catalogId);

      if (catalog) {
        catalog.datasets.splice(catalog.datasets.indexOf(dataset), 1);
      }

      if (dataset.type !== "SPARQL_QUERY") {
        await ApiGeo.removeDataset(dataset.id);
      }
    }
  }
}

export async function setCatalogVisibility(catalogId, visible, remove = false) {
  const catalog = catalogs.find(({ id }) => id === catalogId);

  if (catalog) {
    visible = visible && !remove;

    catalog.visible = visible;

    if (visible) {
      const catalogFeatures = catalog.datasets
        .map(({ features }) => features)
        .flat();

      if (
        catalogFeatures.length === 0 ||
        catalogFeatures.some(({ wkt }) => !wkt)
      ) {
        const featureWkts = await ApiGeo.getAllCatalogWkt(catalog.id);

        for (const featureWkt of featureWkts) {
          const feature = features[featureWkt.metadatas.feature];

          if (feature) {
            feature.wkt = {
              geo: featureWkt.geo,
              type: featureWkt.type,
            };
          } else {
            let dataset = datasets.find(
              ({ id }) => id === featureWkt.metadatas.dataset
            );

            if (!dataset) {
              dataset = {
                id: featureWkt.metadatas.dataset,
                catalogId: catalog.id,
                features: [],
                expanded: false,
                visible: true,
              };
              datasets.push(dataset);

              catalog.datasets.push(dataset);
            }

            const feature = {
              id: featureWkt.metadatas.feature,
              datasetId: featureWkt.metadatas.dataset,
              catalogId: catalog.id,
              wkt: { geo: featureWkt.geo, type: featureWkt.type },
            };
            features[featureWkt.metadatas.feature] = feature;
            dataset.features.push(feature);
          }
        }
      }
    }

    setFeatureVisibility(
      catalog.datasets
        .map(({ features }) => features.map(({ id }) => id))
        .flat(),
      visible,
      remove
    );

    if (remove) {
      catalogs.splice(catalogs.indexOf(catalog), 1);

      if (catalog.type !== "SPARQL_QUERY") {
        await ApiGeo.removeCatalog(catalog.id);
      }
    }
  }
}

export function subscribeFeatureVisibiltyChange(func) {
  visibilityChangeSubscribers.push(func);

  const unsubscribe = () => {
    visibilityChangeSubscribers.splice(
      visibilityChangeSubscribers.indexOf((x) => x === func),
      1
    );
  };

  return unsubscribe;
}

export function triggerFeatureClick(featureId) {
  const feature = features[featureId];

  if (feature && feature.visible) {
    clickSubscribers.forEach((x) => x(featureId));
  }
}

export function triggerFeatureDoubleClick(featureId) {
  const feature = features[featureId];

  if (feature && feature.visible) {
    doubleClickSubscribers.forEach((x) => x(featureId));
  }
}

export function subscribeFeatureClick(func) {
  clickSubscribers.push(func);

  const unsubscribe = () => {
    clickSubscribers.splice(
      clickSubscribers.indexOf((x) => x === func),
      1
    );
  };

  return unsubscribe;
}

export function subscribeFeatureDoubleClick(func) {
  doubleClickSubscribers.push(func);

  const unsubscribe = () => {
    doubleClickSubscribers.splice(
      doubleClickSubscribers.indexOf((x) => x === func),
      1
    );
  };

  return unsubscribe;
}

export function subscribeLabelChange(func) {
  labelChangeSubscribers.push(func);

  const unsubscribe = () => {
    labelChangeSubscribers.splice(
      labelChangeSubscribers.indexOf((x) => x === func),
      1
    );
  };

  return unsubscribe;
}

export function addSparqlQueryResult(res, queryName) {
  nbSparqlQueries++;

  let queriesCatalog = catalogs.find(({ id }) => id === `custom-queries`);

  if (!queriesCatalog) {
    queriesCatalog = {
      id: `custom-queries`,
      label: "Custom queries",
      type: "SPARQL_QUERY",
      datasets: [],
      expanded: true,
      visible: true,
    };
    catalogs.push(queriesCatalog);
  }

  const datasetId = `sparql-query-${nbSparqlQueries}`;

  const dataset = {
    id: datasetId,
    type: "SPARQL_QUERY",
    label: queryName || `Custom Query ${nbSparqlQueries}`,
    raw: res,
    features: [],
    catalogId: queriesCatalog.id,
    expanded: true,
    visible: true,
  };
  datasets.push(dataset);
  queriesCatalog.datasets.push(dataset);

  const tabFeatures = [];

  let i = 0;
  for (const { geo, type } of res.filter((x) => x.geo)) {
    i++;
    const feature = {
      id: `${nbSparqlQueries}-feature-${i}`,
      wkt: { geo, type },
      datasetId,
      catalogId: queriesCatalog.id,
      visible: true,
    };

    features[feature.id] = feature;
    dataset.features.push(feature);
    tabFeatures.push(feature);
  }

  visibilityChangeSubscribers.forEach((x) => x(tabFeatures));
}

const featureUdates = {};

export function updateFeature(featureId, key, value, needInsert = false) {
  const feature = features[featureId];

  if (feature) {
    if (
      key === "rdfs:label" ||
      key === "http://www.w3.org/2000/01/rdf-schema#label"
    ) {
      feature.label = value;
      labelChangeSubscribers.forEach((x) => x());
    }

    if (!featureUdates[featureId]) {
      featureUdates[featureId] = {};
    }
    if (needInsert) {
      ((featureId) => {
        featureUdates[featureId].pendingInsert = true;
        ApiGeo.updateFeature(featureId, key, value, true).then(() => {
          featureUdates[featureId].pendingInsert = false;
        });
      })(featureId);
    } else {
      const updateFeatureDelayed = (featureId) => {
        if (featureUdates[featureId]) {
          clearTimeout(featureUdates[featureId]);
        }

        featureUdates[featureId] = setTimeout(() => {
          if (featureUdates[featureId].pendingInsert) {
            updateFeatureDelayed(featureId);
            return;
          }

          ApiGeo.updateFeature(featureId, key, value, needInsert);

          featureUdates[featureId] = null;
        }, 200);
      };

      updateFeatureDelayed(featureId);
    }
  }
}

export function reset() {
  Object.keys(features).forEach((key) => {
    delete features[key];
  });
  datasets.splice(0, datasets.length);
  catalogs.splice(0, catalogs.length);
  sparqlQueries.value.splice(0, sparqlQueries.value.length);

  clickSubscribers.splice(0, clickSubscribers.length);
  visibilityChangeSubscribers.splice(0, visibilityChangeSubscribers.length);
  labelChangeSubscribers.splice(0, labelChangeSubscribers.length);
}
