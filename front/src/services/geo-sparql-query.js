import { ref } from "vue";

const sparqlQueries = ref([]);

let nbSparqlQueries = 0;

export function getAllSparqlQueries() {
  return sparqlQueries.value;
}

export function addSparqlQueryResult(res, queryName) {
  nbSparqlQueries++;

  let queriesCatalog = catalogs.find(({ id }) => id === `custom-queries`);

  if (!queriesCatalog) {
    queriesCatalog = {
      id: `custom-queries`,
      label: "Custom queries",
      type: "SPARQL_QUERY",
      datasets: [],
      expanded: true,
      visible: true,
    };
    catalogs.push(queriesCatalog);
  }

  const datasetId = `sparql-query-${nbSparqlQueries}`;

  const dataset = {
    id: datasetId,
    type: "SPARQL_QUERY",
    label: queryName || `Custom Query ${nbSparqlQueries}`,
    raw: res,
    features: [],
    catalogId: queriesCatalog.id,
    expanded: true,
    visible: true,
  };
  datasets.push(dataset);
  queriesCatalog.datasets.push(dataset);

  const tabFeatures = [];

  let i = 0;
  for (const { geo, type } of res.filter((x) => x.geo)) {
    i++;
    const feature = {
      id: `${nbSparqlQueries}-feature-${i}`,
      label: `feature-${i}`,
      wkt: { geo, type },
      datasetId,
      catalogId: queriesCatalog.id,
      visible: true,
    };

    features[feature.id] = feature;
    dataset.features.push(feature);
    tabFeatures.push(feature);
  }

  visibilityChangeSubscribers.forEach((x) => x(tabFeatures));
}

export function reset() {
  sparqlQueries.value.splice(0, sparqlQueries.value.length);
}
