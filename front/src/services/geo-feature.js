import * as ApiGeo from "./api-geo";

const features = {};
const featureUdates = {};

const visibilityChangeSubscribers = [];
const clickSubscribers = [];
const doubleClickSubscribers = [];
const labelChangeSubscribers = [];
const dataRefreshSuscribers = [];

export async function addFeatures(dataset) {
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

  if (feature) {
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

export function subscribeDataRefresh(func) {
  dataRefreshSuscribers.push(func);

  const unsubscribe = () => {
    dataRefreshSuscribers.splice(
      dataRefreshSuscribers.indexOf((x) => x === func),
      1
    );
  };

  return unsubscribe;
}

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

  clickSubscribers.splice(0, clickSubscribers.length);
  visibilityChangeSubscribers.splice(0, visibilityChangeSubscribers.length);
  labelChangeSubscribers.splice(0, labelChangeSubscribers.length);
}
