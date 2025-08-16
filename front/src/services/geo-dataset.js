import * as ApiGeo from "./api-geo";

const datasets = [];

export async function addDatasets(catalog) {
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

export function triggerDatasetDoubleClick(datasetId) {
  const dataset = datasets.find(({ id }) => id === datasetId);

  if (dataset) {
    doubleClickSubscribers.forEach((x) => x(datasetId));
  }
}

export function reset() {
  datasets.splice(0, datasets.length);
}
