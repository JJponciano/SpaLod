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

export function getGeoData(sparqlResult) {
  const headers = sparqlResult.head.vars;

  const res = [];

  for (const binding of sparqlResult.results.bindings) {
    const itemRes = {
      metadatas: {},
    };
    for (const header of headers) {
      if (binding[header].value.startsWith("LINESTRING")) {
        itemRes.type = "LINESTRING";
        itemRes.geo = binding[header].value.split(",").map((x) =>
          x
            .trim()
            .replace("LINESTRING (", "")
            .replace(")", "")
            .split(" ")
            .map((y) => Number(y))
        );
      } else if (binding[header].value.startsWith("MULTILINESTRING")) {
        itemRes.type = "MULTILINESTRING";
        itemRes.geo = binding[header].value.split(/\).*?,.*?\(/).map((x) =>
          x.split(",").map((y) =>
            y
              .trim()
              .replace("MULTILINESTRING ((", "")
              .replace("))", "")
              .split(" ")
              .map((z) => Number(z))
          )
        );
      } else {
        itemRes.metadatas[header] = binding[header].value;
      }
    }

    res.push(itemRes);
  }

  return res;
}
