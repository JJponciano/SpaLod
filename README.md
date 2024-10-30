Here’s the updated README with instructions on installing Miniconda and setting up the environment using the `environment.yml` file.

# Spalod Setup Guide

## Prerequisites

### 1. Install Miniconda

First, you'll need to install Miniconda. You can download and install it from [here](https://docs.conda.io/en/latest/miniconda.html) by selecting the appropriate installer for your system (Windows, macOS, or Linux).

After installation, verify that Conda is installed by running:

```bash
conda --version
```

### 2. Set Up the Conda Environment

Once Miniconda is installed, use the provided `environment.yml` file to set up the environment.

To create the Conda environment from `environment.yml`, run:

```bash
conda env create -f environment.yml
#or
conda create -n spalod_env=3.10
conda activate spalod_env
pip install django djangorestframework dj-rest-auth social-auth-app-django django-allauth pydeck rdflib pyproj folium shapely
pip install laspy shapely

```

This will install all the dependencies, including Python and Django, as specified in the `environment.yml` file.

### 3. Activate the Environment

After the environment is created, activate it using:

```bash
conda activate spalod_env
```

Make sure to use the exact environment name from the `environment.yml` file.

## Project Setup

1. With the environment activated, navigate to the project directory and run the migrations:

```bash
python manage.py makemigrations spalod_app
python manage.py migrate
python manage.py createsuperuser
```

2. Start the Django development server:

```bash
python manage.py runserver
```

## GraphDB

Ensure GraphDB is running and accessible at:

```
http://localhost:7200/repositories/Spalod
```

## Authentication and SPARQL Query
1. Register:

```bash
curl -X POST http://127.0.0.1:8000/auth/registration/ -d "username=JJ&password1=GNybRXbC563&password2=GNybRXbC563"
```
2. Log in to get an authentication token:

```bash
curl -X POST http://127.0.0.1:8000/auth/login/ -d "username=JJ&password=GNybRXbC563"
```
You get something like:
```bash
{"key":"79f5571c8c44563a82ce13c395f1982e18d7be5d"}% 
```
3. Run a SPARQL query:

```bash
curl -X POST http://127.0.0.1:8000/api/sparql-query/ \
-H "Content-Type: application/json" \
-H "Authorization: Token 79f5571c8c44563a82ce13c395f1982e18d7be5d" \
-d '{"query": "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 10"}'
```

## Update Ontology with Property Mappings

To update the ontology by linking new and old properties and adding validation timestamps:

```bash
curl -X POST http://127.0.0.1:8000/api/update-ontology/ \
-H "Authorization: Token 1a2926e119b4560da1faa48d3aead3a2ce1a5f78" \
-H "Content-Type: application/json" \
-d '{
      "mappings": [
        {
          "new_property": "http://your-ontology/newProperty1",
          "old_property": "http://your-ontology/oldProperty1"
        },
        {
          "new_property": "http://your-ontology/newProperty2",
          "old_property": "http://your-ontology/oldProperty2"
        }
      ]
    }'
```

## Query Properties

To query all properties that either have a `hasBeenValidatedBy` status of "none" or do not have the property:

```bash
curl -X GET http://127.0.0.1:8000/api/query-properties/ \
-H "Authorization: Token 1a2926e119b4560da1faa48d3aead3a2ce1a5f78"
```

## File Upload with Metadata

To upload a file with associated metadata:

```bash
curl -X POST http://127.0.0.1:8000/api/upload-file/ \
-H "Authorization: Token 79f5571c8c44563a82ce13c395f1982e18d7be5d" \
-F "file=@/Volumes/poncianoCloud/workspace/data/bkg/bkg_map/radnetz_use_case/data/epsg_4326/part1/de_hh_up_freizeitroute2_EPSG_4326.json " \
-F "metadata={\"description\": \"This is a shapefile\", \"source\": \"Survey XYZ\"}"
```

 you get :

 ```json
 {"message":"File uploaded and ontology processed successfully.","uuid":"04287a39-053d-435e-8064-a7664604edb9","ontology_url":"/media/uploads/04287a39-053d-435e-8064-a7664604edb9/04287a39-053d-435e-8064-a7664604edb9_ontology.owl","map_url":"/media/uploads/04287a39-053d-435e-8064-a7664604edb9/04287a39-053d-435e-8064-a7664604edb9_map.html"}% 
 ```
Then you can download :
```bash
curl -O http://127.0.0.1:8000/media/uploads/04287a39-053d-435e-8064-a7664604edb9/04287a39-053d-435e-8064-a7664604edb9_map.html
```

### Upload pointcloud
```bash
curl -X POST http://127.0.0.1:8000/api/upload-file/ \
-H "Authorization: Token 79f5571c8c44563a82ce13c395f1982e18d7be5d" \
-F "file=@/Volumes/poncianoCloud/workspace/data/bkg/DemodatenHackathon2024/1km_565_5934.las" \
-F "metadata={\"description\": \"Poincloud\", \"source\": \"BKG\"}"
```

## To update the ontology mapping

```bash
curl -X POST http://127.0.0.1:8000/api/update-ontology/ \
-H "Authorization: Token 1a2926e119b4560da1faa48d3aead3a2ce1a5f78" \
-H "Content-Type: application/json" \
-d '{
      "mappings": [
        {
          "new_property": "http://your-ontology/newProperty1",
          "old_property": "http://your-ontology/oldProperty1"
        },
        {
          "new_property": "http://your-ontology/newProperty2",
          "old_property": "http://your-ontology/oldProperty2"
        }
      ]
    }'
```

## GRAPHDB: get all graph with HTML and OWl
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

## sparql-query
```bash
curl -X POST http://localhost:8000/api/sparql-query/ \
    -H "Content-Type: application/json" \
    -H "Authorization: Token 79f5571c8c44563a82ce13c395f1982e18d7be5d" \
    -d '{
        "query": "PREFIX geo: <http://www.opengis.net/ont/geosparql#> PREFIX ns2: <https://registry.gdi-de.org/id/hamburg/> PREFIX ex: <http://example.org/ns#> SELECT ?feature ?wkt WHERE { ?feature a geo:Feature ; geo:hasGeometry ?geom . ?geom geo:asWKT ?wkt . }",
        "graph": "http://spalod/52688e21-0a69-456f-ab39-36b2fe36db1a"
    }'
    
```bash
curl -X POST http://localhost:8000/api/sparql-query/ \
    -H "Content-Type: application/json" \
    -H "Authorization: Token 79f5571c8c44563a82ce13c395f1982e18d7be5d" \
    -d '{"query": "PREFIX geo: <http://www.opengis.net/ont/geosparql#> PREFIX ns2: <https://registry.gdi-de.org/id/hamburg/> PREFIX ex: <http://example.org/ns#> SELECT ?feature ?property ?value WHERE { ?feature a geo:Feature ; ?property ?value . }", "graph": "http://spalod/52688e21-0a69-456f-ab39-36b2fe36db1a"}'
```

