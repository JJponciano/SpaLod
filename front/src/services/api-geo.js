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

  return getGeoData(
    result.results.bindings.map((x) => ({
      key: x.key.value,
      value: x.value.value,
    }))
  );
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
        itemRes.geo = property.split(" ").map((x) =>
          Number(
            x
              .trim()
              .replace(/POINT\(/i, "")
              .replace(")", "")
          )
        );
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

export async function updateFeature(id, key, value, needInsert = false) {
  const verb = needInsert ? "insert" : "update";
  return $fetch(
    `api/geo/feature/${verb}?id=${encodeURIComponent(
      id
    )}&key=${encodeURIComponent(key)}&value=${encodeURIComponent(value)}`
  ).then((x) => x.json());
}
