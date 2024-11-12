import { ref } from "vue";
import {
  getAllGeo,
  getCatalogWkt,
  getFeatureWkt,
  getAllCatalogFeatures,
} from "./api-geo";

const features = ref([]);
const catalogs = ref([]);
const sparqlQueries = ref([]);

const visibilityChangeSubscribers = [];
const clickSubscribers = [];

let nbSparqlQueries = 0;

init();

async function init() {
  const allGeos = await getAllGeo();

  addCatalogs(allGeos.map(({ metadatas }) => metadatas));
}

async function addCatalogs(metadatas) {
  for (const { catalog: catalogId } of metadatas) {
    const catalog = {
      id: catalogId,
      features: [],
      expanded: false,
      visible: false,
    };
    catalogs.value.push(catalog);
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
    features.value.push(feature);

    catalog.features.push(feature);
  }
}

export function getAllFeatures() {
  return features.value;
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

  const tabFeatures = [];

  for (const featureId of featureIds) {
    const feature = features.value.find(({ id }) => id === featureId);

    if (feature) {
      feature.visible = visible && !remove;

      const catalog = catalogs.value.find(({ id }) => id === feature.catalogId);

      if (!feature.visible && catalog.visible) {
        catalog.visible = false;
      } else if (!catalog.visible && catalog.features.every((x) => x.visible)) {
        catalog.visible = true;
      }

      if (feature.visible && !feature.wkt) {
        const featureWkt = await getFeatureWkt(feature.id, feature.catalogId);

        feature.wkt = {
          geo: featureWkt[0].geo,
          type: featureWkt[0].type,
        };
      }

      if (remove) {
        features.value.splice(features.value.indexOf(feature), 1);
        catalog.features.splice(catalog.features.indexOf(feature), 1);
      }

      tabFeatures.push(feature);
    }
  }

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

      for (const catalogWkt of catalogWkts) {
        const feature = catalog.features.find(
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
          catalog.features.push(feature);
          features.value.push(feature);
        }
      }
    }

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

    features.value.push(feature);
    catalog.features.push(feature);
    tabFeatures.push(feature);
  }

  visibilityChangeSubscribers.forEach((x) => x(tabFeatures));
}

export function reset() {
  features.value.splice(0, features.value.length);
  catalogs.value.splice(0, catalogs.value.length);
  sparqlQueries.value.splice(0, sparqlQueries.value.length);

  clickSubscribers.splice(0, clickSubscribers.length);
  visibilityChangeSubscribers.splice(0, visibilityChangeSubscribers.length);
}
