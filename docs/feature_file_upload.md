# 📍 SPALOD API – Feature and File Upload

This section explains how to:

- ✅ Add a new geospatial feature (e.g., a point on the map)
- 📎 Attach a file to a feature with automatic semantic linking

---

## 🧭 Add a New Geospatial Feature

This endpoint creates a new feature and:

- Creates the catalog and dataset if they don't exist
- Creates a `FeatureCollection` under the dataset
- Stores geometry as GeoSPARQL WKT
- Optionally adds RDF metadata to the feature

### 🔗 Endpoint

`POST /api/geo/feature/new`

### 🔐 Required Headers

- `Authorization: Token <your_token>`
- `Content-Type: application/json`

### 🧪 Example Request

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
````

---

## 📎 Attach a File to a Feature

This endpoint allows uploading **any file** and links it to a feature using the appropriate semantic property, based on file extension.

### 🔗 Endpoint

`POST /api/geo/feature/addfile`

### 📂 Semantic Mapping by File Type

| File Extensions                          | Semantic Property          |
| ---------------------------------------- | -------------------------- |
| `.glb`, `.gltf`, `.ply`, `.obj`, `.fbx`  | `spalod:has3D`             |
| `.las`, `.laz`                           | `spalod:hasPointCloud`     |
| `.mp4`, `.webm`                          | `spalod:hasVideo`          |
| `.pdf`, `.docx`, `.pptx`, `.txt`, `.doc` | `spalod:hasDocument`       |
| *(all others)*                           | `spalod:hasFile` (default) |

### 🔐 Required Fields

* `feature_id` – URI of the feature
* `file` – Multipart file upload

### 🧪 Example Request

```bash
curl -X POST http://127.0.0.1:8000/api/geo/feature/addfile \
-H "Authorization: Token 63644bad468695c215d7d77ef8186ea6658a4cfa" \
-F "feature_id=https://geovast3d.com/ontologies/spalod#Test_Dataset/collection/feature/00df4906-c3f6-48d9-85cd-f34389eb1741" \
-F "file=@model.glb"
```

---

### ✅ Tested

* **Date:** 2025-07-10
* **Tested by:** Jean-Jacques Ponciano

---

