# **SPARQL API Testing Documentation**

## **Authentication and API Usage**

### **1. User Registration**
Register a new user:

```bash
curl -X POST http://127.0.0.1:8000/auth/registration/ -d "username=JJ&password1=GNybRXbC563&password2=GNybRXbC563"
```

### **2. User Login**
Log in to obtain an authentication token:

```bash
curl -X POST http://127.0.0.1:8000/auth/login/ -d "username=JJ&password=GNybRXbC563"
```

#### **Expected Response:**
```json
{"key":"9b2b164be957dcfc9dcb399f91acc06d4b0f4228"}
```

---

## **3. Authentication Token**
All API requests require authentication using a token. Replace the provided token with your actual token.

```plaintext
Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228
```

---

## **4. Uploading a GeoJSON File**
Uploads a GeoJSON file with metadata.

```sh
curl -X POST http://127.0.0.1:8000/api/upload-file/ \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228" \
-F "file=@/path/to/your/file.geojson" \
-F "metadata={\"catalog\": \"catalog_name_TEST\", \"title\": \"dataset title\", \"description\": \"des\", \"distribution\": \"distri\", \"publisher\": \"publi\"}"
```

---

## **5. Executing SPARQL Queries**
### **5.1 Query All Features**
Retrieves all features with optional labels.

```sh
curl -X POST http://localhost:8000/api/sparql-query/ \
-H "Content-Type: application/json" \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228" \
-d '{"query": "SELECT ?feature ?label WHERE { ?feature a geosparql:Feature . OPTIONAL { ?feature rdfs:label ?label } }"}'
```

### **5.2 Query All Catalogs**
Retrieves all available catalogs.

```sh
curl -X POST http://localhost:8000/api/sparql-query/ \
-H "Content-Type: application/json" \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228" \
-d '{"query": "SELECT ?catalog ?label WHERE { ?catalog a dcat:Catalog . OPTIONAL { ?catalog rdfs:label ?label  } }"}'
```

---

## **6. Retrieving Catalogs and Datasets**
### **6.1 Get All Catalogs**
```sh
curl -X GET http://localhost:8000/api/geo/all/catalog \
-H "Content-Type: application/json" \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228"
```

### **6.2 Get All Datasets of a Catalog**
🔹 **Tip:** Use only the last part after `#` (without `https://geovast3d.com/ontologies/spalod#`).

```sh
curl -X GET "http://localhost:8000/api/geo/all/dataset?catalog_id=catalog_name_TEST" \
-H "Content-Type: application/json" \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228"
```

---

## **7. Retrieving Features**
### **7.1 Get All Features of a Dataset**
🔹 **Use dataset ID directly without the full URI.**

```sh
curl -X GET "http://localhost:8000/api/geo/all/feature?dataset_id=caf6f079-0df2-4cb1-95ff-14f87e56a597" \
-H "Content-Type: application/json" \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228"
```

### **7.2 Get WKT of All Features in a Dataset**
```sh
curl -X GET "http://localhost:8000/api/geo/getwkt?dataset_id=caf6f079-0df2-4cb1-95ff-14f87e56a597" \
-H "Content-Type: application/json" \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228"
```

### **7.3 Get Only the WKT of a Single Feature**
```sh
curl -X GET "http://localhost:8000/api/geo/getfeaturewkt?id=feature3ec7318b-6317-4c26-85b7-eb7ab3455063" \
-H "Content-Type: application/json" \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228"
```

### **7.4 Get All Information About a Feature**
```sh
curl -X GET "http://localhost:8000/api/geo/feature?id=feature3ec7318b-6317-4c26-85b7-eb7ab3455063" \
-H "Content-Type: application/json" \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228"
```

---

## **8. Deleting a Feature**
Deletes everything linked to the given **subject URI**.  
🔹 **Use the full URI because it can be anything.**

```sh
curl -X GET "http://localhost:8000/api/geo/delete?id=https://registry.gdi-de.org/feature3ec7318b-6317-4c26-85b7-eb7ab3455063" \
-H "Content-Type: application/json" \
-H "Authorization: Token 9b2b164be957dcfc9dcb399f91acc06d4b0f4228"
```

---

## **📌 Notes**
- **Replace the `Authorization` token** with your own.
- **For dataset and catalog IDs, use the correct format**:
  - **Catalogs:** Use only the last part after `#`.
  - **Features & Datasets:** Use full ID.
- **Ensure that API endpoints are correct** (`http://localhost:8000` assumes a local server).