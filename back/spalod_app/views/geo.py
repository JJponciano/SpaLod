import os
import threading
from urllib.parse import quote_plus,urlparse
from django.conf import settings
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rdflib import URIRef

from .upload import send_to_flyvast

from ..utils.env import get_env_settings
from ..utils.GraphDBManager import GraphDBManager,NS
import re, uuid, json

from rdflib import URIRef
from ..models import VocabularyMapping
from ..views.matching import label_from_uri

from shapely.wkt import loads as load_wkt
from shapely.geometry import mapping as geom_mapping
from django.http import HttpResponse
from collections import OrderedDict

def resolve_dataset_iri(user_id: int, feature_id: str):
    """Resolve which dataset owns this feature """
    g = GraphDBManager(user_id)
    fid = (feature_id or "").strip().strip("<>")

    sparql = f"""
        SELECT ?ds
        WHERE {{
          ?ds a <http://www.w3.org/ns/dcat#Dataset> .
          BIND( IRI(CONCAT(STR(?ds), "/collection")) AS ?col )
          ?col ?_ <{fid}> .
        }}
    """
    res = g.query_graphdb(sparql)
    rows = res if isinstance(res, list) else res.get("results", {}).get("bindings", [])
    return rows[0]["ds"]["value"] if rows else None

class GeoGetAllCatalogsView(APIView):
    def get(self, request, *args, **kwargs):

        print("::::::: GeoGetAllCatalogsView :::::::")
        user_id = request.user.id
        graph_manager = GraphDBManager(user_id)
        sparql_query="""
            SELECT ?catalog ?label
            WHERE { 
                ?catalog a dcat:Catalog . 
                OPTIONAL { ?catalog rdfs:label ?label  } 
            }
        """
        try:
            results = graph_manager.query_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
    
class GeoGetDatasetOfCatalogView(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetDatasetOfCatalogView :::::::")
        catalog_id = request.query_params.get('catalog_id')
        catalog_name = request.query_params.get('catalog_name')
        user_id = request.user.id
        graph_manager = GraphDBManager(user_id)
        
        if catalog_id:
            sparql_query=f"""
                SELECT ?dataset ?label
                WHERE {{ 
                    <{catalog_id}>  dcat:dataset ?dataset.
                    OPTIONAL {{ ?dataset rdfs:label ?label }} 
                }}
            """
        else:
            sparql_query=f"""
                SELECT ?label
                WHERE {{ 
                    ?catalog a dcat:Catalog .
                    ?catalog dcat:dataset ?dataset.
                    OPTIONAL {{ ?dataset rdfs:label ?label }} 
                    FILTER (str(?catalog) = "https://geovast3d.com/ontologies/spalod#{catalog_name}")
                }}
            """
        try:
            results = graph_manager.query_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
        
class GeoFilterDatasetByMetadata(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoFilterDatasetByMetadata :::::::")
        filter_str = request.query_params.get('filter_str')
        user_id = request.user.id
        graph_manager = GraphDBManager(user_id)
        sparql_query=f"""
            select * where {{
                ?dataset a <http://www.w3.org/ns/dcat#Dataset>.
                OPTIONAL {{ ?dataset <http://www.w3.org/2000/01/rdf-schema#label> ?label }}
                ?dataset ?p ?o .
                FILTER (contains(?o, '{filter_str}'))
                VALUES ?p {{ 
                    <http://purl.org/dc/terms/description> 
                    <http://www.w3.org/2000/01/rdf-schema#label> 
                    <http://purl.org/dc/terms/distribution>
                    <http://purl.org/dc/terms/publisher>
                }}
            }}
        """
        try:
            results = graph_manager.query_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)     


class GeoGetAllFeaturesOfDatasetView(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetAllFeaturesOfDatasetView :::::::")
        dataset_id = request.query_params.get('dataset_id')
        user_id = request.user.id
        graph_manager = GraphDBManager(user_id)
        sparql_query=f"""
            SELECT ?feature ?label
            WHERE {{ 
                <{dataset_id}> geosparql:hasFeatureCollection ?fc.
                ?fc  geosparql:hasFeature ?feature . 
                OPTIONAL {{ ?feature rdfs:label ?label  }} 
            }}
        """
        try:
            results = graph_manager.query_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
        


# class GeoGetAllFeaturesOfCatalogView(APIView):
#     def get(self, request, *args, **kwargs):
#         print("::::::: GeoGeGeoWKTbyCatalogtAllView :::::::")
#         catalog_id = request.query_params.get('catalog_id')
        
#         sparql_query = """     
#             PREFIX geosparql: <http://www.opengis.net/ont/geosparql#> 
#             PREFIX spalod: <http://spalod/> 
#             SELECT ?feature
#             WHERE { 
#                 ?feature a geosparql:Feature . 
#             }
#         """

#         # Add the SPARQL query to request data for use in SparqlQueryAPIView
#         request.data['query'] = sparql_query
#         request.data['catalog_id'] = catalog_id

#         # Instantiate SparqlQueryAPIView and directly call its `post` method
#         sparql_view = SparqlQueryAPIView()
#         return sparql_view.post(request, *args, **kwargs)
        

class GeoGetCatalog(APIView):

    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetCatalog :::::::")
        id = request.query_params.get('id')
        return Response({'error':"Not yet implemented"}, status=status.HTTP_501_NOT_IMPLEMENTED)

        # sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod")
        # self.spalod = Namespace("http://spalod/")

        # graph_general = self.spalod.General

        # sparql.setQuery(f"""     
        #     SELECT ?key ?value
        #     WHERE {{
        #         GRAPH <{graph_general}> {{ 
        #             <{id}> ?key ?value .
        #         }}
        #     }}
        # """)
        # sparql.setReturnFormat(JSON)
        
        # try:
        #     results = sparql.query().convert()
        #     return Response(results, status=status.HTTP_200_OK)
        # except Exception as e:
        #     return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)

class GeoDatasetWKT(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoWKT :::::::")
        dataset_id = request.query_params.get('dataset_id')
        sparql_query = f"""     
             SELECT ?feature ?label ?wkt WHERE {{
                <{dataset_id}>  geosparql:hasFeatureCollection ?fc. 
                ?fc  geosparql:hasFeature ?feature .
                ?feature geosparql:hasGeometry ?geom . 
                ?geom geosparql:asWKT ?wkt . 
                OPTIONAL {{ ?feature rdfs:label ?label  }} 
            }}
            
        """
        try:
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)
            results = graph_manager.query_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
        
class GeoCatalogWKT(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoWKT :::::::")
        catalog_id = request.query_params.get('catalog_id')
        sparql_query = f"""     
             SELECT ?feature ?label ?wkt ?dataset WHERE {{
                <{catalog_id}> dcat:dataset ?dataset. 
                ?dataset geosparql:hasFeatureCollection ?fc. 
                ?fc  geosparql:hasFeature ?feature .
                ?feature geosparql:hasGeometry ?geom . 
                ?geom geosparql:asWKT ?wkt . 
                OPTIONAL {{ ?feature rdfs:label ?label  }} 
            }}
            
        """
        try:
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)
            results = graph_manager.query_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
    
class GeoGetFeatureWKT(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetFeature :::::::")
        id = request.query_params.get('id')
        sparql_query = f"""     
             SELECT ?wkt WHERE {{
                <{id}> geosparql:hasGeometry ?geom . 
                ?geom geosparql:asWKT ?wkt . 
            }}
        """
        try:
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)
            results = graph_manager.query_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
        

        # user_id = request.user.id
        # graph_manager = GraphDBManager(user_id)
        # # Add the SPARQL query to request data for use in SparqlQueryAPIView
        # request.data['query'] = sparql_query
        # request.data['catalog_id'] = catalog_id

        # # Instantiate SparqlQueryAPIView and directly call its `post` method
        # sparql_view = SparqlQueryAPIView()
        # return sparql_view.post(request, *args, **kwargs)
    
class GeoGetItem(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetItem :::::::")
        
        id = request.query_params.get('id')
        print(f"[INFO] ID:{id}")

        ds = resolve_dataset_iri(request.user.id, id)
        print(f"[INFO] dataset_iri for feature {id} => {ds}")

        sparql_query = f"""     
             SELECT ?key ?value ?label WHERE {{
                <{id}> ?key ?value . 
                OPTIONAL {{ <{id}> rdfs:label ?label  }} 

            }}
        """
        print(f"[INFO]{sparql_query}")
        try:
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)
            results = graph_manager.query_graphdb(sparql_query)
            try:
                dataset_iri = ds
                map_new_uri = {}
                if dataset_iri:
                    qs = VocabularyMapping.objects.filter(user=request.user, dataset_iri=dataset_iri)
                    map_new_uri = {m.original_uri: (m.new_uri or "") for m in qs}

                if isinstance(results, list):
                    bindings = results
                else:
                    bindings = results.get("results", {}).get("bindings", [])

                for b in bindings:
                    key_val = b.get("key", {}).get("value")
                    if not key_val:
                        continue

                    mapped_uri = map_new_uri.get(key_val, "")
                    if mapped_uri:
                        display_label = label_from_uri(mapped_uri)  
                    else:
                        display_label = key_val.split("#")[-1] if "#" in key_val else key_val.rsplit("/", 1)[-1]

                    b["displayKey"] = {"type": "literal", "value": display_label}

            except Exception:
                pass
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
        
class GeoUpdateFeatureItem(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoUpdateFeatureItem :::::::")
        
        id = request.query_params.get('id')
        key = request.query_params.get('key')
        value = request.query_params.get('value')
        
        sparql_query = f"""     
            DELETE {{
                <{id}> <{key}> ?value .
            }}
            INSERT {{
                <{id}> <{key}> "{value}" .
            }}
            WHERE {{
                <{id}> <{key}> ?value .
            }}
        """
        try:
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)
            results = graph_manager.update_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)

class GeoInsertFeatureItem(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoInsertFeatureItem :::::::")
        
        id = request.query_params.get('id')
        key = request.query_params.get('key')
        value = request.query_params.get('value')
        
        try:
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)
            results = graph_manager.insert_graphdb(id, key, value)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)

class GeoRemoveID(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoRemoveID :::::::")
        id = request.query_params.get('id')
        user_id = request.user.id
        graph_manager = GraphDBManager(user_id)
        try:
            response=graph_manager.delete_all(id)
            print(response)
            return Response({'message': f'Elements with ID {id} has been successfully deleted.'}, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)


class GeoGenericDelete(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGenericDelete :::::::")
        
        s = request.query_params.get('s')
        p = request.query_params.get('p')
        o = request.query_params.get('o')
        
        if o.startswith("http://") or o.startswith("https://"):
            sparql_query = f"""
                DELETE {{
                    <{s}> <{p}> ?o .
                }}
                WHERE {{
                    <{s}> <{p}> ?o .
                    FILTER (?o IN ("{o}", <{o}>))
                }}
            """
        else:
            sparql_query = f"""
                DELETE {{
                    <{s}> <{p}> "{o}" .
                }}
                WHERE {{
                    <{s}> <{p}> "{o}" .
                }}
            """
        
        try:
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)
            results = graph_manager.update_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
        
class GeoFeatureAddFile(APIView):
    """
    Uploads a file and attaches it to a feature using a semantic SPALOD property.

    Property depends on file type:
    - .las, .laz → spalod:hasPointCloud
    - .glb, .gltf, .ply, .obj, .fbx → spalod:has3D
    - .mp4, .webm → spalod:hasVideo
    - .pdf, .docx, .doc, .pptx, .txt → spalod:hasDocument
    - otherwise → spalod:hasFile

    Required fields:
    - feature_id (URI)
    - file (multipart/form-data)

    Tested: ✅ Yes
    Test date: 2025-07-10
    Tested by: Jean-Jacques Ponciano
    """
    def post(self, request, *args, **kwargs):
        print("::::::: GeoFeatureAddFile :::::::")

        feature_id = request.data.get('feature_id')
        file = request.FILES.get('file')

        if not feature_id or not file:
            return Response({'error': 'feature_id and file are required.'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)
            file_ext = os.path.splitext(file.name)[1].lower()
            
            if file_ext.endswith('las') or file_ext.endswith('laz') or file_ext.endswith('xyz'):
                pointcloud_uuid = file.flyvast_pointcloud["pointcloud_uuid"]
                pointcloud_id = file.flyvast_pointcloud["pointcloud_id"]
                file_uuid = f"{pointcloud_id}{pointcloud_uuid}"
                print("[INFO] Pointcloud detected !")
                t = threading.Thread(
                    target=send_to_flyvast,
                    args=[file],
                    daemon=True,
                )
                t.start()
            else:
                file_uuid = str(uuid.uuid4())
            
            filename = f"{file_uuid}_{file.name}".replace(" ", "+")
            upload_dir = os.path.join(settings.MEDIA_ROOT, 'uploads', file_uuid)
            os.makedirs(upload_dir, exist_ok=True)
            file_path = os.path.join(upload_dir, filename)

            # Save file
            with open(file_path, 'wb+') as dest:
                for chunk in file.chunks():
                    dest.write(chunk)

            # Public URL
            spalod_url = get_env_settings("SPALOD_URL")
            file_url = f"/media/uploads/{file_uuid}/{filename}"
            full_url = f"{spalod_url}{file_url}"

            # Determine semantic property based on extension
            ext_map = {
                # pointcloud
                ".las": "hasPointCloud",
                ".laz": "hasPointCloud",
                
                # 3D models
                ".glb": "has3D",
                ".gltf": "has3D",
                ".ply": "has3D",
                ".obj": "has3D",
                ".fbx": "has3D",
                
                # video
                ".mp4": "hasVideo",
                ".webm": "hasVideo",
                
                # PDF
                ".pdf": "hasPdf",
                
                # documents
                ".docx": "hasDocument",
                ".doc": "hasDocument",
                ".pptx": "hasDocument",
                ".ppt": "hasDocument",
                ".txt": "hasDocument",
                
                # images
                ".png": "hasImage",
                ".jpg": "hasImage",
                ".jpeg": "hasImage",
                ".svg": "hasImage",
                ".bmp": "hasImage",
                ".ico": "hasImage",
                ".webp": "hasImage",
                ".apng": "hasImage",
                ".avif": "hasImage"
            }
            predicate_key = ext_map.get(file_ext, "hasFile")
            predicate_uri = NS["SPALOD"][predicate_key]

            # Add triple to graph
            feature_uri = URIRef(feature_id)
            triple = (feature_uri, predicate_uri, URIRef(full_url))
            graph_manager.upload_to_graphdb([triple])

            return Response({
                'message': f'File uploaded and linked using {predicate_key}.',
                'file_url': file_url,
                'uri': str(feature_uri)
            }, status=status.HTTP_201_CREATED)

        except Exception as e:
            print(f"[ERROR] {e}")
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)


class GeoDatasetReplaceMetadata(APIView):
    """
    Adds or replaces the metadata of an existing dataset.

    It removes existing dcterms:* metadata triples from the dataset,
    stores the new XML metadata file in the dataset folder,
    adds the metadata_file_url,
    and inserts the new metadata fields.
    """

    def post(self, request, *args, **kwargs):
        print("::::::: GeoDatasetReplaceMetadata :::::::")

        dataset_id = request.data.get("dataset_id")
        metadata_file = request.FILES.get("metadata_file")
        metadata = request.data.get("metadata")

        if not dataset_id or not metadata_file or not metadata:
            return Response(
                {"error": "dataset_id, metadata_file and metadata are required."},
                status=status.HTTP_400_BAD_REQUEST
            )

        metadata_file_extension = os.path.splitext(metadata_file.name)[1].lower()

        if metadata_file_extension != ".xml":
            return Response(
                {"error": "Only .xml metadata files are supported."},
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            metadata = json.loads(metadata)
        except ValueError:
            return Response(
                {"error": "Invalid JSON for metadata."},
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)

            old_metadata_query = f"""
                SELECT ?value WHERE {{
                    <{dataset_id}> dcterms:metadata_file_url ?value .
                }}
            """
            old_metadata_results = graph_manager.query_graphdb(old_metadata_query)

            old_metadata_file_url = None
            if old_metadata_results:
                old_metadata_file_url = old_metadata_results[0].get("value", {}).get("value")

            file_url_for_upload_dir = old_metadata_file_url

            if not file_url_for_upload_dir:
                dataset_file_query = f"""
                    SELECT ?file WHERE {{
                        <{dataset_id}> spalod:hasFile ?file .
                        FILTER(CONTAINS(STR(?file), "/media/uploads/"))
                        FILTER(!CONTAINS(STR(?file), "_metadata"))
                        FILTER(
                            STRENDS(LCASE(STR(?file)), ".geojson") ||
                            STRENDS(LCASE(STR(?file)), ".json")
                        )
                    }}
                    LIMIT 1
                """
                dataset_file_results = graph_manager.query_graphdb(dataset_file_query)

                if dataset_file_results:
                    file_url_for_upload_dir = dataset_file_results[0].get("file", {}).get("value")

            if file_url_for_upload_dir:
                parsed_path = urlparse(file_url_for_upload_dir).path

                if not parsed_path.startswith(settings.MEDIA_URL):
                    return Response(
                        {"error": "Invalid dataset file path."},
                        status=status.HTTP_400_BAD_REQUEST
                    )

                relative_path = parsed_path.replace(settings.MEDIA_URL, "", 1)

                existing_file_path = os.path.normpath(
                    os.path.join(settings.MEDIA_ROOT, relative_path)
                )

                media_root = os.path.abspath(settings.MEDIA_ROOT)
                existing_file_path_abs = os.path.abspath(existing_file_path)

                if not existing_file_path_abs.startswith(media_root):
                    return Response(
                        {"error": "Invalid dataset file path."},
                        status=status.HTTP_400_BAD_REQUEST
                    )

                upload_dir = os.path.dirname(existing_file_path_abs)
                file_uuid = os.path.basename(upload_dir)

            else:
                file_uuid = str(uuid.uuid4())
                upload_dir = os.path.join(settings.MEDIA_ROOT, "uploads", file_uuid)

            os.makedirs(upload_dir, exist_ok=True)

            metadata_file_path = os.path.join(
                upload_dir,
                f"{file_uuid}_metadata{metadata_file_extension}"
            )

            with open(metadata_file_path, "wb") as destination:
                for chunk in metadata_file.chunks():
                    destination.write(chunk)

            metadata_file_url = f"/media/uploads/{file_uuid}/{file_uuid}_metadata{metadata_file_extension}"

            delete_old_metadata_query = f"""
                DELETE {{
                    <{dataset_id}> ?p ?oldValue .
                }}
                WHERE {{
                    <{dataset_id}> ?p ?oldValue .
                    FILTER(STRSTARTS(STR(?p), "http://purl.org/dc/terms/"))
                }}
            """
            graph_manager.update_graphdb(delete_old_metadata_query)

            metadata["metadata_file_url"] = metadata_file_url

            cleaned_metadata = {
                key: value
                for key, value in metadata.items()
                if value and key not in ["catalog", "title", "publisher"]
            }

            triples_added = graph_manager.add_dcterms_metadata_to_dataset(
                dataset_id,
                cleaned_metadata,
                excluded_keys=["catalog", "title", "publisher"]
            )

            return Response(
                {
                    "message": "Dataset metadata replaced successfully.",
                    "dataset_id": dataset_id,
                    "metadata_file_url": metadata_file_url,
                    "triples_added": len(triples_added),
                },
                status=status.HTTP_200_OK
            )

        except Exception as e:
            print(f"[ERROR] Failed to replace dataset metadata: {e}")
            return Response(
                {"error": str(e)},
                status=status.HTTP_400_BAD_REQUEST
            )

class GeoFeatureNew(APIView):
    # =============================================================================
# 📘 GeoFeatureNew — Add a Geospatial Feature via REST API
#
# Description:
#     This API endpoint allows authenticated users to add a new geospatial
#     feature to a semantic graph. It automatically creates the catalog and
#     dataset if they do not already exist, ensures feature collections are
#     attached, and inserts a GeoSPARQL-compliant feature with optional metadata.
#
# Endpoint:
#     POST /api/geo/feature/new
#
# Required Headers:
#     Authorization: Token <your_token>
#     Content-Type: application/json
#
# JSON Payload:
#     {
#       "label": "Test Point A",
#       "lat": 49.756,
#       "lng": 6.641,
#       "catalog_name": "Test Catalog",
#       "dataset_name": "Test Dataset",
#       "metadata": {
#         "http://purl.org/dc/terms/creator": "Jean-Jacques Ponciano",
#         "http://purl.org/dc/terms/date": "2025-06-20"
#       }
#     }
#
# Example curl:
#     curl -X POST http://127.0.0.1:8000/api/geo/feature/new \
#     -H "Authorization: Token 63644bad468695c215d7d77ef8186ea6658a4cfa" \
#     -H "Content-Type: application/json" \
#     -d '{ ... }'
#
# Example Success Response:
#     {
#       "message": "Feature successfully added.",
#       "feature_uri": "https://geovast3d.com/ontologies/spalod#Test_Dataset/collection/feature/<uuid>"
#     }
#
# Tested: ✅ Yes
# Test date: 2025-06-20
# Tested by: Jean-Jacques Ponciano
# =============================================================================
    def post(self, request, *args, **kwargs):
        print("::::::: GeoFeatureNew :::::::")

        # Extract basic data from the request
        label = request.data.get('label')  # Label for the new feature
        lat = request.data.get('lat')      # Latitude (float)
        lng = request.data.get('lng')      # Longitude (float)
        catalog_name = request.data.get('catalog_name')  # Catalog grouping this dataset
        dataset_name = request.data.get('dataset_name')  # Dataset under the catalog
        user_id = request.user.id                        # Authenticated user
        metadata = request.data.get('metadata')          # Additional metadata as JSON
        
        try:
            metadata = json.loads(metadata)
        except ValueError:
            return Response({'error': 'Invalid JSON for metadata.'}, status=status.HTTP_400_BAD_REQUEST)

        print(f"Adding a new feature for User: {user_id}")

        # Validate coordinates
        try:
            lat = float(lat)
            lng = float(lng)
        except ValueError:
            return Response({'error': 'Invalid coordinates.'}, status=status.HTTP_400_BAD_REQUEST)
        # Create WKT representation of the geometry
        wkt = f"POINT({lng} {lat})"

       
        try:
            # Normalize catalog and dataset names to make valid URIs (replace spaces, dots, dashes)
            catalog_name = re.sub(r"[ .-]", "_", catalog_name)
            dataset_name = re.sub(r"[ .-]", "_", dataset_name)
            graph_manager = GraphDBManager(user_id)
            catalog_uri, dataset_uri = graph_manager.initialize_dataset_structure(catalog_name, user_id, dataset_name)
            triples_added = graph_manager.add_dcterms_metadata_to_dataset(dataset_uri,metadata)
            feature_collection_uri = graph_manager.get_or_create_feature_collection_uri(dataset_uri)

            result =  graph_manager.create_feature_with_geometry(feature_collection_uri, label, wkt, metadata)
            print("✅ Feature created:")
            print("Feature URI:", result["feature_uri"])
            print("Geometry URI:", result["geometry_uri"])


            # Respond with success and return the new feature URI
            return Response({
                'message': 'Feature successfully added.',
                'feature_uri': str(result["feature_uri"]),
                'catalog_uri': str(catalog_uri),
                'dataset_uri': str(dataset_uri),
            }, status=status.HTTP_201_CREATED)

        except Exception as e:
            # Generic fallback for unexpected errors
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
        

class GeoDatasetGeoJsonLD(APIView):

    def get(self, request, *args, **kwargs):
        print("::::::: GeoDatasetGeoJsonLD :::::::")

        GEOJSON_VOCAB = "https://purl.org/geojson/vocab#"

        GEOJSON_SPECIAL_URIS = {
            f"{GEOJSON_VOCAB}type",
            f"{GEOJSON_VOCAB}geometry",
            f"{GEOJSON_VOCAB}properties",
        }

        dataset_id = request.query_params.get("id")
        if not dataset_id:
            return Response(
                {"error": "id is required"},
                status=status.HTTP_400_BAD_REQUEST,
            )

        dataset_iri = dataset_id.strip().strip("<>")

        user = request.user
        user_id = user.id
        graph_manager = GraphDBManager(user_id)


        qs = VocabularyMapping.objects.filter(user=user, dataset_iri=dataset_iri)
        uri_map = {
            m.original_uri: (m.new_uri or m.original_uri)
            for m in qs
        }

        context = {
            "geojson": "https://purl.org/geojson/vocab#",
        }

        RDF_TYPE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"

        def split_namespace(uri: str):
            if "#" in uri:
                ns, local = uri.rsplit("#", 1)
                return ns + "#", local
            else:
                ns, local = uri.rsplit("/", 1)
                return ns + "/", local

        def pretty_prefix_for_ns(ns: str) -> str:
            
            if ns == "http://www.w3.org/1999/02/22-rdf-syntax-ns#":
                return "rdf"
            if ns == "https://schema.org/":
                return "schema"
            if ns == "https://geovast3d.com/ontologies/spalod#":
                return "spalod"
            if ns == "http://www.opengis.net/ont/geosparql#":
                return "geosparql"

            for key, value in context.items():
   
                if isinstance(value, str) and value == ns and (value.endswith("#") or value.endswith("/")):
                    return key
            try:
                parsed = urlparse(ns)
                base = (parsed.netloc or parsed.path or "vocab")

                base = base.split(".")[0]
            except Exception:
                base = "vocab"
            base = re.sub(r"[^A-Za-z0-9_]", "_", base) or "vocab"

            prefix = base
            i = 1
            while prefix in context:
                prefix = f"{base}{i}"
                i += 1
            return prefix

        def to_curie(uri: str) -> str:
            ns, local = split_namespace(uri)
            
            prefix = pretty_prefix_for_ns(ns)

            if prefix not in context:
                context[prefix] = ns

            return f"{prefix}:{local}"

        def get_term_for_uri(original_uri: str) -> str:

            target_uri = uri_map.get(original_uri, original_uri)

            base_label = label_from_uri(target_uri) or "prop"
            term = re.sub(r"[^A-Za-z0-9_]", "_", base_label) or "prop"

            curie = to_curie(target_uri)

  
            suffix = 1
            while term in context and context[term] != curie:
                term = f"{base_label}_{suffix}"
                term = re.sub(r"[^A-Za-z0-9_]", "_", term) or "prop"
                suffix += 1

            context[term] = curie
            return term
        

        def sort_context(ctx: dict) -> OrderedDict:
            prefixes = {}
            terms_by_prefix = {}
            other_terms = {}

            for key, value in ctx.items():
                if not isinstance(value, str):
                    other_terms[key] = value
                    continue

                if value.endswith("#") or value.endswith("/"):
                    prefixes[key] = value
                else:
                    if ":" in value:
                        pfx, _ = value.split(":", 1)
                        terms_by_prefix.setdefault(pfx, {})[key] = value
                    else:
                        other_terms[key] = value

            ordered = OrderedDict()

            preferred_order = ["geojson", "spalod", "schema"]
            other_prefixes = sorted(p for p in prefixes.keys() if p not in preferred_order)
            prefix_order = [p for p in preferred_order if p in prefixes] + other_prefixes

            for p in prefix_order:

                ordered[p] = prefixes[p]


                for term in sorted(terms_by_prefix.get(p, {}).keys()):
                    ordered[term] = terms_by_prefix[p][term]

            for term in sorted(other_terms.keys()):
                ordered[term] = other_terms[term]

            return ordered


        sparql_features = f"""
            SELECT ?feature ?wkt
            WHERE {{
                <{dataset_iri}> geosparql:hasFeatureCollection ?fc .
                ?fc  geosparql:hasFeature ?feature .
                ?feature geosparql:hasGeometry ?geom .
                ?geom geosparql:asWKT ?wkt .
            }}
        """

        try:
            res_feat = graph_manager.query_graphdb(sparql_features)
        except Exception as e:
            return Response({"error": str(e)}, status=status.HTTP_400_BAD_REQUEST)

        if isinstance(res_feat, list):
            feat_bindings = res_feat
        else:
            feat_bindings = res_feat.get("results", {}).get("bindings", [])

        features = []

        for row in feat_bindings:
            feature_iri = row.get("feature", {}).get("value")
            wkt_value = row.get("wkt", {}).get("value")

            if not feature_iri or not wkt_value:
                continue

            try:
                geom = load_wkt(wkt_value)
                geometry = geom_mapping(geom)
            except Exception:
                continue

            sparql_props = f"""
                SELECT ?p ?o
                WHERE {{
                    <{feature_iri}> ?p ?o .
                    FILTER (?p != geosparql:hasGeometry)
                }}
            """

            try:
                res_props = graph_manager.query_graphdb(sparql_props)
            except Exception:
                res_props = []

            if isinstance(res_props, list):
                bindings = res_props
            else:
                bindings = res_props.get("results", {}).get("bindings", [])

            props = {}

            for b in bindings:
                p_uri = b.get("p", {}).get("value")
                o_node = b.get("o", {})

                if not p_uri or not o_node:
                    continue

                if p_uri == RDF_TYPE_URI:
                    continue

                if p_uri in GEOJSON_SPECIAL_URIS:
                    continue

                term = get_term_for_uri(p_uri)

                value = o_node.get("value")

                if value is None or value == "None" or value == "":
                    continue

                if term in props:
                    if isinstance(props[term], list):
                        props[term].append(value)
                    else:
                        props[term] = [props[term], value]
                else:
                    props[term] = value

            feature_obj = {
                "type": "Feature",
                "@id": feature_iri,
                "geometry": geometry,
                "properties": props,
            }
            features.append(feature_obj)

        dataset_label = label_from_uri(dataset_iri) or "dataset"
        file_name = re.sub(r"[^A-Za-z0-9_.-]", "_", dataset_label) or "dataset"
        context = sort_context(context)
        fc = {
            "@context": context,
            "type": "FeatureCollection",
            "@id": dataset_iri,
            "features": features,
        }

        data_str = json.dumps(fc, ensure_ascii=False, indent=2)

        response = HttpResponse(
            data_str,
            content_type="application/ld+json",
        )
        response["Content-Disposition"] = f'attachment; filename="{file_name}.geojsonld"'
        return response