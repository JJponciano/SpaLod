import { ref } from "vue";

const features = ref([]);
const catalogs = ref([]);
const visibilityChangeSubscribers = [];
const clickSubscribers = [];

export function getAllFeatures() {
  return features.value;
}

export function getAllCatalogs() {
  return catalogs.value;
}

export function addFeatures(metadatas) {
  for (const { feature: featureId, catalog: catalogId } of metadatas) {
    const feature = {
      visible: false,
      id: featureId,
      catalogId,
    };
    features.value.push(feature);

    if (!catalogs.value.find(({ id }) => id === catalogId)) {
      catalogs.value.push({
        id: catalogId,
        features: [],
      });
    }

    catalogs.value.find(({ id }) => id === catalogId).features.push(feature);
  }
}

export function setFeatureVisibility(featureIds, visible, remove = false) {
  if (!Array.isArray(featureIds)) {
    featureIds = [featureIds];
  }

  const tabFeatures = [];

  for (const featureId of featureIds) {
    const feature = features.value.find(({ id }) => id === featureId);

    if (feature) {
      feature.visible = visible && !remove;

      if (remove) {
        features.value.splice(features.value.indexOf(feature), 1);

        const catalog = catalogs.value.find(
          ({ id }) => id === feature.catalogId
        );
        catalog.features.splice(catalog.features.indexOf(feature), 1);
      }

      tabFeatures.push(feature);
    }
  }

  visibilityChangeSubscribers.forEach((x) => x(tabFeatures, remove));
}

export function setCatalogVisibility(catalogId, visible, remove = false) {
  const catalog = catalogs.value.find(({ id }) => id === catalogId);

  if (catalog) {
    visible = visible && !remove;

    // catalog.features.forEach((feature) =>
    //   setFeatureVisibility(feature.id, visible, remove)
    // );
    setFeatureVisibility(
      catalog.features.map(({ id }) => id),
      visible,
      remove
    );

    if (remove) {
      catalogs.value.splice(catalogs.value.indexOf(catalog));
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
  const feature = features.value.find(({ id }) => id === featureId);

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

export function clearFeatures() {
  features.value.splice(0, features.value.length);
}
