import { $fetch } from "./api";

export async function getAllGeo() {
  const req = await $fetch("api/geo/all");
  const result = await req.json();

  return result.results.bindings.map(({ feature, wkt }) => {
    const res = {
      id: feature.value,
    };

    // TODO: handle other types
    if (wkt.value.startsWith("LINESTRING")) {
      res.type = "LINESTRING";

      res.geo = wkt.value.split(",").map((x) =>
        x
          .trim()
          .replace("LINESTRING (", "")
          .replace(")", "")
          .split(" ")
          .map((y) => Number(y))
      );
    } else if (wkt.value.startsWith("MULTILINESTRING")) {
      res.type = "MULTILINESTRING";

      res.geo = wkt.value.split(/\).*?,.*?\(/).map((x) =>
        x.split(",").map((y) =>
          y
            .trim()
            .replace("MULTILINESTRING ((", "")
            .replace("))", "")
            .split(" ")
            .map((z) => Number(z))
        )
      );
    }

    return res;
  });
}

export async function getFeature(id) {
  const req = await $fetch(`api/geo/feature?id=${id}`);
  const result = await req.json();

  return result.results.bindings.map(({ key, value }) => ({
    key: key.value,
    value: value.value,
  }));
}
