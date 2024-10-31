import { ref } from "vue";

const features = ref([]);
const visibilityChangeSubscribers = [];
const clickSubscribers = [];

export function getAllFeatures() {
  return features.value;
}

export function addFeatures(newFeatures) {
  features.value.push(
    ...newFeatures.map((x) => ({
      visible: false,
      id: x,
    }))
  );
}

export function setFeatureVisibility(featureId, visible) {
  const feature = features.value.find(({ id }) => id === featureId);

  if (feature) {
    feature.visible = visible;

    visibilityChangeSubscribers.forEach((x) => x(feature));
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
