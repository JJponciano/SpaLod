import { ref } from "vue";
import * as ApiGeo from "./api-geo";

const features = [];
const datasets = [];
const catalogs = ref([]);
const sparqlQueries = ref([]);

const visibilityChangeSubscribers = [];
const clickSubscribers = [];

let nbSparqlQueries = 0;

init();

export async function init() {
  const allGeos = await ApiGeo.getAllCatalogs();

  const needRefresh = addCatalogs(allGeos.map(({ metadatas }) => metadatas));

  if (needRefresh) {
    // need to refresh the datasets of the catalogs
    for (const catalog of catalogs.value) {
      await addDatasets(catalog);
    }
  }
}

async function addCatalogs(metadatas) {
  let needRefresh = true;
  for (const { catalog: catalogId, label } of metadatas) {
    if (!catalogs.value.find(({ id }) => id === catalogId)) {
      const catalog = {
        id: catalogId,
        label,
        datasets: [],
        expanded: false,
        visible: false,
      };
      catalogs.value.push(catalog);
      needRefresh = false;
    }
  }
  return needRefresh;
}

async function addDatasets(catalog) {
  const catalogDatasets = await ApiGeo.getAllDatasets(catalog.id);

  for (const {
    metadatas: { dataset: datasetId, label },
  } of catalogDatasets) {
    if (!datasets.find(({ id }) => id === datasetId)) {
      const dataset = {
        id: datasetId,
        catalogId: catalog.id,
        label,
        features: [],
        expanded: false,
        visible: false,
      };
      datasets.push(dataset);

      catalog.datasets.push(dataset);
    }
  }
}

async function addFeatures(dataset) {
  const catalogFeatures = await ApiGeo.getAllFeatures(dataset.id);

  for (const {
    metadatas: { feature: featureId, label },
  } of catalogFeatures) {
    const feature = {
      id: featureId,
      label,
      visible: false,
      datasetId: dataset.id,
      catalogId: dataset.catalogId,
    };
    features.push(feature);

    dataset.features.push(feature);
  }
}

export function getAllFeatures() {
  return features;
}

export function getAllCatalogs() {
  return catalogs.value;
}

export function getAllSparqlQueries() {
  return sparqlQueries.value;
}

export function expandCatalog(catalogId) {
  const catalog = catalogs.value.find(({ id }) => id === catalogId);

  if (catalog) {
    catalog.expanded = !catalog.expanded;

    if (catalog.expanded && catalog.datasets.length === 0) {
      addDatasets(catalog);
    }
  }
}

export async function expandDataset(datasetId) {
  const dataset = datasets.find(({ id }) => id === datasetId);

  if (dataset) {
    dataset.expanded = !dataset.expanded;

    if (dataset.expanded && dataset.features.length === 0) {
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

  // const start = Date.now();

  for (const featureId of featureIds) {
    const feature = features.find(({ id }) => id === featureId);

    if (!catalog) {
      catalog = catalogs.value.find(({ id }) => id === feature.catalogId);
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
        features.splice(features.indexOf(feature), 1);
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

  // console.log("time elapsed: ", Date.now() - start);

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

      for (const datasetWkt of datasetWkts) {
        const feature = copyFeatures.find(
          ({ id }) => id === datasetWkt.metadatas.feature
        );

        if (feature) {
          feature.wkt = {
            geo: datasetWkt.geo,
            type: datasetWkt.type,
          };
        } else {
          const feature = {
            id: datasetWkt.metadatas.feature,
            datasetId: dataset.id,
            catalogId: dataset.catalogId,
            wkt: { geo: datasetWkt.geo, type: datasetWkt.type },
          };
          copyFeatures.push(feature);
          features.push(feature);
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

      const catalog = catalogs.value.find(({ id }) => id === dataset.catalogId);

      if (catalog) {
        catalog.datasets.splice(catalog.datasets.indexOf(dataset), 1);
      }
    }
  }
}

export async function setCatalogVisibility(catalogId, visible, remove = false) {
  const catalog = catalogs.value.find(({ id }) => id === catalogId);

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
          const feature = features.find(
            ({ id }) => id === featureWkt.metadatas.feature
          );

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
            features.push(feature);
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
      catalogs.value.splice(catalogs.value.indexOf(catalog), 1);
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
  const feature = features.find(({ id }) => id === featureId);

  if (feature && feature.visible) {
    clickSubscribers.forEach((x) => x(featureId));
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

export function addSparqlQueryResult(res, queryName) {
  nbSparqlQueries++;

  let queriesCatalog = catalogs.value.find(({ id }) => id === `custom-queries`);

  if (!queriesCatalog) {
    queriesCatalog = {
      id: `custom-queries`,
      label: "Custom queries",
      type: "SPARQL_QUERY",
      datasets: [],
      expanded: true,
      visible: true,
    };
    catalogs.value.push(queriesCatalog);
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

    features.push(feature);
    dataset.features.push(feature);
    tabFeatures.push(feature);
  }

  visibilityChangeSubscribers.forEach((x) => x(tabFeatures));
}

export function reset() {
  features.splice(0, features.length);
  catalogs.value.splice(0, catalogs.value.length);
  sparqlQueries.value.splice(0, sparqlQueries.value.length);

  clickSubscribers.splice(0, clickSubscribers.length);
  visibilityChangeSubscribers.splice(0, visibilityChangeSubscribers.length);
}
