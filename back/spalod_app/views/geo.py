import os
from urllib.parse import quote_plus
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

from ..utils.env import get_env_settings
from ..utils.GraphDBManager import GraphDBManager,NS
import re, uuid, json

from rdflib import URIRef

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

            # Generate unique file ID and path
            file_uuid = str(uuid.uuid4())
            file_ext = os.path.splitext(file.name)[1].lower()
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
            catalog_uri, dataset_uri = graph_manager.initialize_dataset_structure(catalog_name,dataset_name)
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