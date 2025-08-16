import { ref } from "vue";
import * as ApiGeo from "./api-geo";

const catalogs = [];

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

  dataRefreshSuscribers.forEach((x) => x());
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

export function getAllCatalogs() {
  return catalogs;
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

export function reset() {
  catalogs.splice(0, catalogs.length);
}
