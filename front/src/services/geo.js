import { $fetch } from "./api";

export async function getAllGeo() {
  const req = await $fetch("api/geo/all");
  const result = await req.json();

  return getGeoData(result);
}

export async function getFeature(id) {
  const req = await $fetch(`api/geo/feature?id=${id}`);
  const result = await req.json();

  return getGeoData(result);
}

export async function getGeoData(results) {
  const res = [];

  for (const result of results) {
    const itemRes = {
      metadatas: {},
    };
    for (const header of Object.keys(result)) {
      if (result[header].startsWith("LINESTRING")) {
        itemRes.type = "LINESTRING";
        itemRes.geo = result[header].split(",").map((x) =>
          x
            .trim()
            .replace("LINESTRING (", "")
            .replace(")", "")
            .split(" ")
            .map((y) => Number(y))
        );
      } else if (result[header].startsWith("MULTILINESTRING")) {
        itemRes.type = "MULTILINESTRING";
        itemRes.geo = result[header].split(/\).*?,.*?\(/).map((x) =>
          x.split(",").map((y) =>
            y
              .trim()
              .replace("MULTILINESTRING ((", "")
              .replace("))", "")
              .split(" ")
              .map((z) => Number(z))
          )
        );
      } else if (result[header].startsWith("POLYGON")) {
        itemRes.type = "POLYGON";
        itemRes.geo = result[header].split(",").map((x) =>
          x
            .trim()
            .replace("POLYGON ((", "")
            .replace("))", "")
            .split(" ")
            .map((y) => Number(y))
        );
      } else {
        itemRes.metadatas[header] = result[header];
      }
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
