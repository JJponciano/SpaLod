# Spalod Setup Guide

## Prerequisites

### 1. Install Miniconda

You will need Miniconda to manage dependencies and set up your environment. Download and install Miniconda by selecting the appropriate installer for your system (Windows, macOS, or Linux) from [here](https://docs.conda.io/en/latest/miniconda.html).

After installation, confirm that Conda is installed:

```bash
conda --version
```

### 2. Set Up the Conda Environment

Once Miniconda is installed, use the provided `environment.yml` file to create the environment. Alternatively, you can manually create the environment and install dependencies.

#### Method 1: Using `environment.yml`

To create the environment directly:

```bash
conda env create -f environment.yml
```

#### Method 2: Manual Environment Creation

To create the environment manually:

```bash
conda create -n spalod_env python=3.10
conda activate spalod_env
pip install django djangorestframework dj-rest-auth social-auth-app-django django-allauth pydeck rdflib pyproj folium shapely laspy tqdm
```

### 3. Activate the Environment

Activate the Conda environment using:

```bash
conda activate spalod_env
```

Make sure to use the exact environment name if specified differently in the `environment.yml` file.

## Project Setup

1. With the environment activated, navigate to the project directory and run migrations:

```bash
python manage.py makemigrations spalod_app
python manage.py migrate
python manage.py createsuperuser
```

2. Start the Django development server:

```bash
python manage.py runserver
```

## GraphDB Configuration

Ensure that GraphDB is running and accessible at:

```
http://localhost:7200/repositories/Spalod
```

## Authentication and API Usage

### 1. User Registration

Register a new user:

```bash
curl -X POST http://127.0.0.1:8000/auth/registration/ -d "username=jj&password1=GNybRXbC563&password2=GNybRXbC563"
```

### 2. User Login

Log in to obtain an authentication token:

```bash
curl -X POST http://127.0.0.1:8000/auth/login/ -d "username=jj&password=GNybRXbC563"
```

Expected response:

```json
{"key":"9b2b164be957dcfc9dcb399f91acc06d4b0f4228"}
```

### 3. Running a SPARQL Query

Example SPARQL query request:

```bash
curl -X POST http://127.0.0.1:8000/api/sparql-query/ \
-H "Content-Type: application/json" \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228" \
-d '{"query": "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 10"}'
```

## Ontology Update with Property Mappings

To update ontology mappings with new properties and validation timestamps:

```bash
curl -X POST http://127.0.0.1:8000/api/update-ontology/ \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228" \
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

To retrieve properties that have a `hasBeenValidatedBy` status of "none" or are missing this property:

```bash
curl -X GET http://127.0.0.1:8000/api/query-properties/ \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228"
```

## File Upload with Metadata

Example file upload with associated metadata:

```bash
curl -X POST http://127.0.0.1:8000/api/upload-file/ \
-H "Authorization: Token b9208365e4bbd3500888a6deb7dcb3e6d05c05a3" \
-F "file=@/Volumes/poncianoCloud/workspace/DATA/i3mainz/bkg/test.geojson" \
-F "metadata={\"catalog\": \"catalog_name_TEST-KEV\", \"title\": \"dataset titel \", \"description\": \"des\", \"distribution\": \"distri\", \"publisher\": \"publi\"}"

curl -X POST http://127.0.0.1:8000/api/upload-file/ \
-H "Authorization: Token b9208365e4bbd3500888a6deb7dcb3e6d05c05a3" \
-F "file=@/Volumes/poncianoCloud/workspace/DATA/i3mainz/bkg/spalod_test_01/1km_565_5934.las" \
-F "metadata={\"catalog\": \"catalog_name_TEST-KEV\", \"title\": \"dataset titel \", \"description\": \"des\", \"distribution\": \"distri\", \"publisher\": \"publi\"}"

curl -X POST http://127.0.0.1:8000/api/upload-file/ \
-H "Authorization: Token b9208365e4bbd3500888a6deb7dcb3e6d05c05a3" \
-F "file=@/Volumes/poncianoCloud/workspace/2025/data/bkg/spalod_test_01/query.json " \
-F "metadata={\"catalog\": \"catalog_name_TEST\", \"title\": \"dataset titel \", \"description\": \"des\", \"distribution\": \"distri\", \"publisher\": \"publi\"}"
```

Response:

```json
{
  "message": "File uploaded and ontology processed successfully.",
  "uuid": "04287a39-053d-435e-8064-a7664604edb9",
  "ontology_url": "/media/uploads/04287a39-053d-435e-8064-a7664604edb9/04287a39-053d-435e-8064-a7664604edb9_ontology.owl",
  "map_url": "/media/uploads/04287a39-053d-435e-8064-a7664604edb9/04287a39-053d-435e-8064-a7664604edb9_map.html"
}
```

To download:

```bash
curl -O http://127.0.0.1:8000/media/uploads/04287a39-053d-435e-8064-a7664604edb9/04287a39-053d-435e-8064-a7664604edb9_map.html
```

### Uploading a Point Cloud

```bash
curl -X POST http://127.0.0.1:8000/api/upload-file/ \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228" \
-F "file=@/path/to/pointcloud.las" \
-F "metadata={\"description\": \"Pointcloud\", \"source\": \"BKG\"}"
```

## Running SPARQL Queries

Example SPARQL query:

```bash
curl -X POST http://localhost:8000/api/sparql-query/ \
-H "Content-Type: application/json" \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228" \
-d '{"query": "PREFIX geo: <http://www.opengis.net/ont/geosparql#> PREFIX ns2: <https://registry.gdi-de.org/id/hamburg/> PREFIX ex: <http://example.org/ns#> SELECT ?feature ?property ?value WHERE { ?feature a geo:Feature ; ?property ?value . }"}'
```

## Miscellaneous Endpoints

### Retrieve a Catalog

```bash
curl -X GET "http://127.0.0.1:8000/api/geo/catalog?id=http://spalod/catalog_acc53514-6a2f-4521-afa5-40783906d4ba" \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228"
```

### Delete a Catalog

```bash
curl -X GET "http://127.0.0.1:8000/api/geo/delete?id=https://geovast3d.com/ontologies/spalod%23909ef21f-2af6-4240-aacc-4b3ed3320e18" \
-H "Authorization: Token b9208365e4bbd3500888a6deb7dcb3e6d05c05a3"
```

### Retrieve Feature Information

```bash
curl -X GET "http://127.0.0.1:8000/api/geo/feature?id=https://registry.gdi-de.org/feature002755b6-8a27-4e58-a1e7-34635993696e&catalog_id=http://spalod/catalog_652579e3-1a45-4f6d-b55f-c4f1c1bfce20" \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228"
```

### Retrieve WKT of a Feature

```bash
curl -X GET "http://127.0.0.1:8000/api/geo/getfeaturewkt?id=https://registry.gdi-de.org/feature002755b6-8a27-4e58-a1e7-34635993696e&catalog_id=http://spalod/catalog_652579e3-1a45-4f6d-b55f-c4f1c1bfce20" \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228"
```

### Retrieve All WKT of All Catalog Features

```bash
curl -X GET "http://127.0.0.1:8000/api/geo/getwkt?catalog_id=http://spalod/catalog_652579e3-1a45-4f6d-b55f-c4f1c1bf

ce20" \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228"
```

### Retrieve All Catalogs

```bash
curl -X GET "http://127.0.0.1:8000/api/geo/all" \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228"
```

---

# Add a new feature
```bash
curl -X POST http://127.0.0.1:8000/api/geo/feature/new \
-H "Authorization: Token 63644bad468695c215d7d77ef8186ea6658a4cfa" \
-H "Content-Type: application/json" \
-d '{
  "label": "Test Point A",
  "lat": 49.756,
  "lng": 6.641,
  "catalog_name": "Test Catalog",
  "dataset_name": "Test Dataset",
  "metadata": {
    "http://purl.org/dc/terms/creator": "Jean-Jacques Ponciano",
    "http://purl.org/dc/terms/date": "2025-06-20"
  }
}'
```

```bash
curl -X POST http://127.0.0.1:8000/api/geo/feature/add/file \
-H "Authorization: Token 63644bad468695c215d7d77ef8186ea6658a4cfa" \
-F "feature_id=https://geovast3d.com/ontologies/spalod#Test_Dataset/collection/feature/00df4906-c3f6-48d9-85cd-f34389eb1741" \
-F "file=@/Users/jean-jacquesponciano/Downloads/model.glb"
```

---

### 🔍 Get Feature Properties

You can retrieve all RDF properties linked to a specific feature using the API.

**Request:**

```bash
curl -X GET \
"http://127.0.0.1:8000/api/geo/feature?id=https://geovast3d.com/ontologies/spalod%233de4eb5f-71d6-410e-b3d4-20b103417a74" \
-H "Authorization: Token b9208365e4bbd3500888a6deb7dcb3e6d05c05a3"
```

> 📝 Note: Ensure that the feature URI is URL-encoded (`#` → `%23`).
