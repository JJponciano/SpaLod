import { ref } from "vue";
import {
  getAllGeo,
  getCatalogWkt,
  getFeatureWkt,
  getAllCatalogFeatures,
} from "./api-geo";

const features = [];
const catalogs = ref([]);
const sparqlQueries = ref([]);

const visibilityChangeSubscribers = [];
const clickSubscribers = [];

let nbSparqlQueries = 0;

init();

export async function init() {
  const allGeos = await getAllGeo();

  addCatalogs(allGeos.map(({ metadatas }) => metadatas));
}

async function addCatalogs(metadatas) {
  for (const { catalog: catalogId } of metadatas) {
    if (!catalogs.value.find(({ id }) => id === catalogId)) {
      const catalog = {
        id: catalogId,
        features: [],
        expanded: false,
        visible: false,
      };
      catalogs.value.push(catalog);
    }
  }
}

async function addFeatures(catalog) {
  const catalogFeatures = await getAllCatalogFeatures(catalog.id);

  for (const {
    metadatas: { feature: featureId },
  } of catalogFeatures) {
    const feature = {
      visible: false,
      id: featureId,
      catalogId: catalog.id,
    };
    features.push(feature);

    catalog.features.push(feature);
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

    if (catalog.expanded && catalog.features.length === 0) {
      addFeatures(catalog);
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

  let copyFeatures;

  const tabFeatures = [];
  let catalog = null;
  let allFeaturesVisible = true;

  const start = Date.now();

  for (const featureId of featureIds) {
    const feature = features.find(({ id }) => id === featureId);

    if (feature) {
      feature.visible = visible && !remove;

      allFeaturesVisible = allFeaturesVisible && feature.visible;

      if (!catalog) {
        catalog = catalogs.value.find(({ id }) => id === feature.catalogId);

        if (remove) {
          copyFeatures = catalog.features.slice();
        }
      }

      if (!feature.visible && catalog.visible) {
        catalog.visible = false;
      }

      if (feature.visible && !feature.wkt) {
        const featureWkt = await getFeatureWkt(feature.id, feature.catalogId);

        feature.wkt = {
          geo: featureWkt[0].geo,
          type: featureWkt[0].type,
        };
      }

      if (remove) {
        features.splice(features.indexOf(feature), 1);
        copyFeatures.splice(copyFeatures.indexOf(feature), 1);
      }

      tabFeatures.push(feature);
    }
  }

  if (remove) {
    catalog.features = copyFeatures;
  }

  if (!catalog.visible && catalog.features.every((x) => x.visible)) {
    catalog.visible = true;
  }

  console.log("time elapsed: ", Date.now() - start);

  visibilityChangeSubscribers.forEach((x) => x(tabFeatures, remove));
}

export async function setCatalogVisibility(catalogId, visible, remove = false) {
  const catalog = catalogs.value.find(({ id }) => id === catalogId);

  if (catalog) {
    visible = visible && !remove;

    catalog.visible = visible;

    if (
      visible &&
      (catalog.features.length === 0 ||
        catalog.features.some(({ wkt }) => !wkt))
    ) {
      const catalogWkts = await getCatalogWkt(catalog.id);

      const copyFeatures = catalog.features.slice();

      for (const catalogWkt of catalogWkts) {
        const feature = copyFeatures.find(
          ({ id }) => id === catalogWkt.metadatas.feature
        );

        if (feature) {
          feature.wkt = {
            geo: catalogWkt.geo,
            type: catalogWkt.type,
          };
        } else {
          const feature = {
            id: catalogWkt.metadatas.feature,
            catalogId: catalog.id,
            wkt: { geo: catalogWkt.geo, type: catalogWkt.type },
          };
          copyFeatures.push(feature);
          features.push(feature);
        }
      }

      catalog.features = copyFeatures;
    }

    setFeatureVisibility(
      catalog.features.map(({ id }) => id),
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

export function addSparqlQueryResult(res) {
  nbSparqlQueries++;

  const catalogId = `sparql-query-${nbSparqlQueries}`;

  const catalog = {
    id: catalogId,
    type: "SPARQL_QUERY",
    raw: res,
    features: [],
    expanded: true,
    visible: true,
  };
  catalogs.value.push(catalog);

  const tabFeatures = [];

  let i = 0;
  for (const { geo, type } of res.filter((x) => x.geo)) {
    i++;
    const feature = {
      id: `${nbSparqlQueries}-feature-${i}`,
      wkt: { geo, type },
      catalogId,
      visible: true,
    };

    features.push(feature);
    catalog.features.push(feature);
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
