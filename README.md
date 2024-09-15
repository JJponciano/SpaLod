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

1. Log in to get an authentication token:
   ```bash
   curl -X POST http://127.0.0.1:8000/api/auth/login/ -d "username=Falk&password=GNybRXbC563"
   ```

2. Run a SPARQL query:
   ```bash
   curl -X POST http://127.0.0.1:8000/api/sparql-query/ \
   -H "Content-Type: application/json" \
   -H "Authorization: Token 1a2926e119b4560da1faa48d3aead3a2ce1a5f78" \
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
-H "Authorization: Token 1a2926e119b4560da1faa48d3aead3a2ce1a5f78" \
-F "file=@/Volumes/poncianoCloud/workspace/data/bkg/bkg_map/radnetz_use_case/data/epsg_4326/part1/de_hh_up_freizeitroute2_EPSG_4326.json " \
-F "metadata={\"description\": \"This is a shapefile\", \"source\": \"Survey XYZ\"}"

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