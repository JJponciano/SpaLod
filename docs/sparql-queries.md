# Querying GraphDB and API using SPARQL and cURL

This guide demonstrates how to query GraphDB and an API using `curl` and SPARQL queries. It will explain how to retrieve all graphs with their HTML and OWL URLs from GraphDB and how to get feature properties from a GeoSPARQL-enabled API.

## 1. GRAPHDB: Retrieve All Graphs with HTML and OWL URLs

The following `curl` command is used to retrieve all graphs in the GraphDB repository, along with their associated HTML and OWL URLs.

### Command:

```bash
curl -X POST \
  -H "Content-Type: application/sparql-query" \
  --data-binary 'PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> 
                 PREFIX spalod: <http://spalod/> 
                 SELECT ?graph ?html_url ?owl_url 
                 WHERE { 
                   GRAPH ?graph { 
                     ?graph spalod:hasHTML ?html_url ; 
                            spalod:hasOWL ?owl_url . 
                   } 
                 }' \
  http://localhost:7200/repositories/Spalod
```

### Explanation:
1. **Prefixes:**
   - `xsd`: Refers to XML Schema data types (used for typed literals in RDF).
   - `spalod`: A custom namespace representing the SPALOD ontology.

2. **Query Structure:**
   - **`GRAPH ?graph`**: This part of the query iterates over all named graphs in the dataset.
   - **`?graph spalod:hasHTML ?html_url`**: For each graph, it retrieves the URL of the HTML map associated with the graph.
   - **`?graph spalod:hasOWL ?owl_url`**: It also retrieves the URL of the OWL ontology for each graph.

3. **Endpoint:**
   - The request is sent to the SPARQL endpoint of the GraphDB repository at `http://localhost:7200/repositories/Spalod`.

### Result:
The query will return a list of all graphs with their associated HTML and OWL URLs.

---

## 2. Querying GeoSPARQL API: Get Features and Geometry

The following `curl` command retrieves all features and their geometries in Well-Known Text (WKT) format from a GeoSPARQL-enabled API.

### Command:

```bash
curl -X POST http://localhost:8000/api/sparql-query/ \
    -H "Content-Type: application/json" \
    -H "Authorization: Token 79f5571c8c44563a82ce13c395f1982e18d7be5d" \
    -d '{
        "query": "PREFIX geo: <http://www.opengis.net/ont/geosparql#> PREFIX ns2: <https://registry.gdi-de.org/id/hamburg/> PREFIX ex: <http://example.org/ns#> SELECT ?feature ?wkt WHERE { ?feature a geo:Feature ; geo:hasGeometry ?geom . ?geom geo:asWKT ?wkt . }",
        "graph": "http://spalod/52688e21-0a69-456f-ab39-36b2fe36db1a"
    }'
```

### Explanation:
1. **Prefixes:**
   - `geo`: The GeoSPARQL namespace, used to define geographic features and geometries.
   - `ns2`: Custom namespace for the dataset.
   - `ex`: Example namespace used for this query.

2. **Query Structure:**
   - **`?feature a geo:Feature`**: Selects all features in the dataset that are classified as GeoSPARQL features.
   - **`geo:hasGeometry ?geom`**: Retrieves the geometry associated with each feature.
   - **`geo:asWKT ?wkt`**: Extracts the Well-Known Text (WKT) representation of the geometry.

3. **Authentication:**
   - The `Authorization` header includes a token for API authentication (`Token 79f5571c8c44563a82ce13c395f1982e18d7be5d`).

4. **Endpoint:**
   - The query is sent to the SPARQL endpoint at `http://localhost:8000/api/sparql-query/`.

### Result:
This query returns all GeoSPARQL features along with their WKT geometries.

---

## 3. Querying GeoSPARQL API: Get Features and All Properties

If you want to retrieve all properties associated with a feature, the following command can be used:

### Command:

```bash
curl -X POST http://localhost:8000/api/sparql-query/ \
    -H "Content-Type: application/json" \
    -H "Authorization: Token 79f5571c8c44563a82ce13c395f1982e18d7be5d" \
    -d '{"query": "PREFIX geo: <http://www.opengis.net/ont/geosparql#> PREFIX ns2: <https://registry.gdi-de.org/id/hamburg/> PREFIX ex: <http://example.org/ns#> SELECT ?feature ?property ?value WHERE { ?feature a geo:Feature ; ?property ?value . }", "graph": "http://spalod/52688e21-0a69-456f-ab39-36b2fe36db1a"}'
```

### Explanation:
1. **`?property ?value`**: This part of the query retrieves all properties and their values associated with each feature. It allows you to explore all the data linked to the features.

### Result:
This query returns all features along with all their properties and values.

---