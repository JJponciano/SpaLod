# SpaLod
Spatial data management with semantic web technology and Linked Open Data (LOD)
[![Spalod Docker Image](https://github.com/JJponciano/SpaLod/actions/workflows/docker-image.yml/badge.svg?branch=main)](https://github.com/JJponciano/SpaLod/actions/workflows/docker-image.yml)
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


```