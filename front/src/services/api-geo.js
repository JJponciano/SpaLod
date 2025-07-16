import { $fetch } from "./api";

export async function getAllCatalogs() {
  const result = await $fetch("api/geo/catalog/all").then((x) => x.json());

  return getGeoData(result);
}

export async function getAllDatasets(catalogId) {
  const result = await $fetch(
    `api/geo/dataset/all?catalog_id=${encodeURIComponent(catalogId)}`
  ).then((x) => x.json());

  return getGeoData(result);
}

export async function getAllDatasetsFromCatalogName(catalogName) {
  const result = await $fetch(
    `api/geo/dataset/all?catalog_name=${encodeURIComponent(catalogName)}`
  ).then((x) => x.json());

  return getGeoData(result);
}

export async function getAllFeatures(datasetId) {
  const result = await $fetch(
    `api/geo/feature/all?dataset_id=${encodeURIComponent(datasetId)}`
  ).then((x) => x.json());

  return getGeoData(result);
}

export async function getAllCatalogWkt(catalogId) {
  const result = await $fetch(
    `api/geo/catalog/all/wkt?catalog_id=${encodeURIComponent(catalogId)}`
  ).then((x) => x.json());

  return getGeoData(result);
}

export async function getAllDatasetWkt(datasetId) {
  const result = await $fetch(
    `api/geo/dataset/all/wkt?dataset_id=${encodeURIComponent(datasetId)}`
  ).then((x) => x.json());

  return getGeoData(result);
}

export async function getFeatureWkt(featureId, catalogId) {
  const result = await $fetch(
    `api/geo/feature/wkt?id=${encodeURIComponent(
      featureId
    )}&catalog_id=${encodeURIComponent(catalogId)}`
  ).then((x) => x.json());

  return getGeoData(result);
}

export async function getCatalog(catalogId) {
  const result = await $fetch(
    `api/geo/catalog?id=${encodeURIComponent(catalogId)}`
  ).then((x) => x.json());

  return getGeoData(result);
}

export async function getDataset(datasetId) {
  const result = await $fetch(
    `api/geo/dataset?id=${encodeURIComponent(datasetId)}`
  ).then((x) => x.json());

  return getGeoData(result);
}

export async function filterDataset(filterStr) {
  const result = await $fetch(
    `api/geo/dataset/filter?filter_str=${encodeURIComponent(filterStr)}`
  ).then((x) => x.json());

  return getGeoData(result);
}

export async function getFeature(featureId) {
  const result = await $fetch(
    `api/geo/feature?id=${encodeURIComponent(featureId)}`
  ).then((x) => x.json());

  return getGeoData(result);
}

export async function sparqlQuery(query) {
  const result = await $fetch("/api/sparql-query/", {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ query }),
  }).then((x) => x.json());

  return getGeoData(result);
}

export async function getGeoData(results) {
  const res = [];

  for (const result of results) {
    const itemRes = {
      metadatas: {},
    };
    for (const header of Object.keys(result)) {
      const property = result[header].value;
      if (property.toUpperCase().startsWith("LINESTRING")) {
        itemRes.type = "LINESTRING";
        itemRes.geo = property.split(",").map((x) =>
          x
            .trim()
            .replace(/LINESTRING \(/i, "")
            .replace(")", "")
            .split(" ")
            .map((y) => Number(y))
        );
      } else if (property.toUpperCase().startsWith("MULTILINESTRING")) {
        itemRes.type = "MULTILINESTRING";
        itemRes.geo = property.split(/\).*?,.*?\(/).map((x) =>
          x.split(",").map((y) =>
            y
              .trim()
              .replace(/MULTILINESTRING \(\(/i, "")
              .replace("))", "")
              .split(" ")
              .map((z) => Number(z))
          )
        );
      } else if (property.toUpperCase().startsWith("POLYGON")) {
        itemRes.type = "POLYGON";
        itemRes.geo = property.split(",").map((x) =>
          x
            .trim()
            .replace(/POLYGON \(\(/i, "")
            .replace("))", "")
            .split(" ")
            .map((y) => Number(y))
        );
      } else if (property.toUpperCase().startsWith("POINT")) {
        itemRes.type = "POINT";
        itemRes.geo = /POINT\s*?\((-?[\d\.]+) (-?[\d\.]+)\)/i
          .exec(property)
          .slice(1);
      }

      itemRes.metadatas[header] = property;
    }

    res.push(itemRes);
  }

  for (const metadatas of res.map((x) => x.metadatas)) {
    if (metadatas.key?.includes("hasPointcloud")) {
      const pointcloudResult = await getFeature(metadatas.value);
      const pointcloudId = pointcloudResult
        .map((x) => x.metadatas)
        .filter((x) => x.key?.includes("pointcloud_id"))[0]?.value;
      const pointcloudUuid = pointcloudResult
        .map((x) => x.metadatas)
        .filter((x) => x.key?.includes("pointcloud_uuid"))[0]?.value;
      const pointcloudUrl = `${
        import.meta.env.VITE_APP_FLYVAST_POINTCLOUD_VIEWER_BASE_URL
      }${pointcloudId}/${pointcloudUuid}`;

      res.push({
        pointcloudUrl,
      });
    }
  }

  return res;
}

export async function removeFeature(id) {
  return $fetch(`api/geo/feature/delete?id=${encodeURIComponent(id)}`).then(
    (x) => x.json()
  );
}

export async function removeDataset(id) {
  return $fetch(`api/geo/dataset/delete?id=${encodeURIComponent(id)}`).then(
    (x) => x.json()
  );
}

export async function removeCatalog(id) {
  return $fetch(`api/geo/catalog/delete?id=${encodeURIComponent(id)}`).then(
    (x) => x.json()
  );
}

export async function deleteFeatureProperty(featureId, key, value) {
  return $fetch(
    `api/geo/generic/delete?s=${encodeURIComponent(
      featureId
    )}&p=${encodeURIComponent(key)}&o=${encodeURIComponent(value)}`
  );
}

export async function updateFeature(id, key, value, needInsert = false) {
  const verb = needInsert ? "insert" : "update";
  return $fetch(
    `api/geo/feature/${verb}?id=${encodeURIComponent(
      id
    )}&key=${encodeURIComponent(key)}&value=${encodeURIComponent(value)}`
  ).then((x) => x.json());
}

export async function addFeature(
  lat,
  lng,
  label,
  catalogName,
  datasetName,
  metadata
) {
  let formData = new FormData();
  formData.append("lat", lat);
  formData.append("lng", lng);
  formData.append("label", label);
  formData.append("catalog_name", catalogName);
  formData.append("dataset_name", datasetName);
  formData.append("metadata", JSON.stringify(metadata));

  return $fetch("/api/geo/feature/new", {
    method: "POST",
    body: formData,
  }).then((x) => x.json());
}
