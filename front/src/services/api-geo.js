import { $fetch } from "./api";

export async function getAllGeo() {
  const result = await $fetch("api/geo/all/catalog").then((x) => x.json());

  return getGeoData(
    result.results.bindings.map((x) => ({ catalog: x.catalog.value }))
  );
}

export async function getAllCatalogFeatures(catalogId) {
  const result = await $fetch(
    `api/geo/all/feature?catalog_id=${catalogId}`
  ).then((x) => x.json());

  return getGeoData(result);
}

export async function getCatalogWkt(catalogId) {
  const result = await $fetch(`api/geo/getwkt?catalog_id=${catalogId}`).then(
    (x) => x.json()
  );

  return getGeoData(result);
}

export async function getFeatureWkt(featureId, catalogId) {
  const result = await $fetch(
    `api/geo/getfeaturewkt?id=${featureId}&catalog_id=${catalogId}`
  ).then((x) => x.json());

  return getGeoData(result);
}

export async function getCatalog(catalogId) {
  const result = await $fetch(`api/geo/catalog?id=${catalogId}`).then((x) =>
    x.json()
  );

  return getGeoData(
    result.results.bindings.map((x) => ({
      key: x.key.value,
      value: x.value.value,
    })),
    catalogId
  );
}

export async function getFeature(id, catalogId) {
  const result = await $fetch(
    `api/geo/feature?id=${id}&catalog_id=${catalogId}`
  ).then((x) => x.json());

  return getGeoData(result, catalogId);
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

export async function getGeoData(results, catalogId) {
  const res = [];

  for (const result of results) {
    const itemRes = {
      metadatas: {},
    };
    for (const header of Object.keys(result)) {
      if (result[header].toUpperCase().startsWith("LINESTRING")) {
        itemRes.type = "LINESTRING";
        itemRes.geo = result[header].split(",").map((x) =>
          x
            .trim()
            .replace(/LINESTRING \(/i, "")
            .replace(")", "")
            .split(" ")
            .map((y) => Number(y))
        );
      } else if (result[header].toUpperCase().startsWith("MULTILINESTRING")) {
        itemRes.type = "MULTILINESTRING";
        itemRes.geo = result[header].split(/\).*?,.*?\(/).map((x) =>
          x.split(",").map((y) =>
            y
              .trim()
              .replace(/MULTILINESTRING \(\(/i, "")
              .replace("))", "")
              .split(" ")
              .map((z) => Number(z))
          )
        );
      } else if (result[header].toUpperCase().startsWith("POLYGON")) {
        itemRes.type = "POLYGON";
        itemRes.geo = result[header].split(",").map((x) =>
          x
            .trim()
            .replace(/POLYGON \(\(/i, "")
            .replace("))", "")
            .split(" ")
            .map((y) => Number(y))
        );
      } else if (result[header].toUpperCase().startsWith("POINT")) {
        itemRes.type = "POINT";
        itemRes.geo = result[header].split(" ").map((x) =>
          Number(
            x
              .trim()
              .replace(/POINT\(/i, "")
              .replace(")", "")
          )
        );
      } else {
        itemRes.metadatas[header] = result[header];
      }
    }

    res.push(itemRes);
  }

  for (const metadatas of res.map((x) => x.metadatas)) {
    if (metadatas.key?.includes("hasPointcloud")) {
      const pointcloudResult = await getFeature(metadatas.value, catalogId);
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
  return $fetch(`api/geo/feature/delete?id=${id}`).then((x) => x.json());
}

export async function removeCatalog(id) {
  return $fetch(`api/geo/catalog/delete?id=${id}`).then((x) => x.json());
}
