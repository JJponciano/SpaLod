# SpaLod
Spatial data management with semantic web technology and Linked Open Data (LOD)
## Introduction

SpaLOD addresses the increasingly complex challenge brought about by the rapid growth of geospatial data, which expands by at least 20% every year. This has resulted in an enormous increase in data heterogeneity, creating complexities in structure and vocabulary variations. The vocabularies in use depend heavily on the application domain and the language in which the data is described, making integration and unification a daunting task.

In light of these challenges and harnessing the potential of Semantic Web technologies, numerous approaches have emerged to group these data into knowledge graphs. These knowledge graphs enable efficient data linking, ease sharing, and enhance maintenance. However, they also bring forth the daunting task of data homogenization due to the non-unified data structures and vocabulary variations.

To overcome this problem of homogenization, we present SpaLOD, a comprehensive framework designed to efficiently group heterogeneous spatial data into a single knowledge base. The knowledge base is rooted in an ontology connected to Schema.org and DCAT-AP, providing a data structure compatible with GeoSPARQL. One of the unique strengths of SpaLOD is its ability to integrate geospatial data independently of their original language. This is made possible by translating them using advanced Neural Machine Translation.

SpaLOD sets a new benchmark in the field of geospatial data, enabling a universal sharing platform and fostering collaboration between different states and organizations globally. Through SpaLOD, we envision a future where geospatial data can be universally used, integrated, and shared, regardless of their original structure and language.



## Docker 

Clone the repository and go inside:
```bash
git clone https://github.com/JJponciano/SpaLod.git
cd SpaLod
```

Build the docker image and run the container
```bash
docker build -t spalod .
docker run -p 8080:8080 -d -i --name spalod-container spalod
```
Run the server:

```bash
docker exec spalod-container /home/spalod/spalod.sh
```

## Installation 
Clone the repository and go inside:
```bash
git clone https://github.com/JJponciano/SpaLod.git
cd SpaLod
```
Install dependencies
```bash
mvn install:install-file \
-Dfile=/home/spalod/libs/pisemantic-1.0-SNAPSHOT.jar \
-DpomFile=/home/spalod/pom.xml \
-DgroupId=info.ponciano.lab \
-DartifactId=pisemantic \
-Dversion=1.0 \
-Dpackaging=jar

mvn install:install-file \
-Dfile=/home/spalod/libs/pitools-1.0-SNAPSHOT.jar \
-DpomFile=/home/spalod/pom.xml \
-DgroupId=info.ponciano.lab \
-DartifactId=pitools \
-Dversion=1.0-SNAPSHOT\
-Dpackaging=jar
```
Build the binaries
```bash
mvn package
```
Run the server:
```bash
java -jar target/spalod-0.0.1-SNAPSHOT.jar
```

## Example of request:
```
SELECT ?s ?p ?o WHERE {?s ?p ?o. ?d rdf:type dcat:Dataset . ?d spalod:hasFeature ?s}

SELECT ?d ?item ?coords WHERE { ?d rdf:type dcat:Dataset . ?d spalod:hasFeature ?f. ?f geosparql:hasGeometry ?g. ?g geosparql:asWKT ?coords . ?f spalod:itemlabel ?item}
SELECT ?d ?item ?coordinates WHERE { ?d rdf:type dcat:Dataset . ?d spalod:hasFeature ?f. ?f geosparql:hasGeometry ?g. ?g geosparql:asWKT ?coordinates . ?f spalod:itemlabel ?item}
SELECT ?d ?itemID ?itemLabel ?coordinates WHERE { ?d rdf:type dcat:Dataset . ?d spalod:hasFeature ?itemID. ?itemID geosparql:hasGeometry ?g. ?g geosparql:asWKT ?coordinates . ?itemID spalod:itemlabel ?itemLabel}


SELECT ?f ?g ?wkt ?fp ?o WHERE { spalod:67504af5-5d32-4815-ae53-fb879f4bb0c7 spalod:hasFeature  ?f. ?f geosparql:hasGeometry ?g. ?g geosparql:asWKT ?wkt . ?f ?fp ?o}

```