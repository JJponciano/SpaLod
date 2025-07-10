import os
from django.conf import settings
from rest_framework.views import APIView
from rest_framework.response import Response
from SPARQLWrapper import SPARQLWrapper, JSON
from rest_framework import status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rdflib import Graph, URIRef, Literal, Namespace, XSD
from ..utils.GraphDBManager import process_owl_file,delete_ontology_entry,GraphDBManager,NS
from  .sparql_query import SparqlQueryAPIView
from ..serializers import SparqlQuerySerializer
import json, re, uuid

from rdflib import URIRef, Literal, Namespace, RDF, Graph

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
        user_id = request.user.id
        graph_manager = GraphDBManager(user_id)
        sparql_query=f"""
            SELECT ?dataset ?label
            WHERE {{ 
                <{catalog_id}>  dcat:dataset ?dataset.
                OPTIONAL {{ ?dataset rdfs:label ?label }} 
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
        print("::::::: GeoGetFeature :::::::")
        
        id = request.query_params.get('id')
        
        sparql_query = f"""     
             SELECT ?key ?value ?label WHERE {{
                <{id}> ?key ?value . 
                OPTIONAL {{ <{id}> rdfs:label ?label  }} 

            }}
        """
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
        
    
# class GeoGetFeature_graphdb_only(APIView):
#     def get(self, request, *args, **kwargs):
#         print("::::::: GeoGetFeature :::::::")
#         id = request.query_params.get('id')
#         catalog_id = request.query_params.get('catalog_id')

#         if catalog_id:
#             # If catalog_id is provided, query within the specific graph.
#             sparql_query = f"""     
#                 select ?key ?value
#                 where {{
#                     GRAPH <{catalog_id}> {{ 
#                         <{id}> ?key ?value .
#                     }}
#                 }}
#             """
#         else:
#             # If catalog_id is not provided, query across all graphs.
#             sparql_query = f"""     
#                 select ?key ?value
#                 where {{
#                     <{id}> ?key ?value .
#                 }}
#             """

#         sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod")
#         self.spalod = Namespace("http://spalod/")
#         sparql.setQuery(sparql_query)
#         sparql.setReturnFormat(JSON)
        
#         try:
#             results = sparql.query().convert()
#             print(results)
#             return Response(results, status=status.HTTP_200_OK)
#         except Exception as e:
#             return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
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
    Uploads a file and attaches it to a feature using spalod:hasFile.

    Required fields in the request:
    - feature_id: URI or identifier of the dataset to which the file is linked
    - file: multipart/form-data file to upload

    Tested: ✅ Yes
    Test date: 2025-07-10
    Tested by: Jean-Jacques Ponciano
    """

    def post(self, request, *args, **kwargs):
        print("::::::: GeoFeatureAddFile :::::::")

        # Extract the dataset (feature) ID and uploaded file
        feature_id = request.data.get('feature_id')
        file = request.FILES.get('file')

        if not feature_id or not file:
            return Response({'error': 'feature_id and file are required.'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)

            # Generate a unique file ID and extension
            file_uuid = str(uuid.uuid4())
            file_ext = os.path.splitext(file.name)[1]
            filename = f"{file_uuid}{file_ext}"

            # Create upload path
            upload_dir = os.path.join(settings.MEDIA_ROOT, 'uploads', file_uuid)
            os.makedirs(upload_dir, exist_ok=True)

            file_path = os.path.join(upload_dir, filename)

            # Save the file
            with open(file_path, 'wb+') as dest:
                for chunk in file.chunks():
                    dest.write(chunk)

            # Construct the public URL
            file_url = f"/media/uploads/{file_uuid}/{filename}"

            # Add the file to the dataset using SPALOD
            feature_uri = URIRef(feature_id)
            graph_manager.add_file_to_dataset_or_feature(feature_uri, file_url)

            return Response({
                'message': 'File successfully uploaded and linked to dataset.',
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

        print(f"Adding a new feature for User: {user_id}")

        # Validate coordinates
        try:
            lat = float(lat)
            lng = float(lng)
        except ValueError:
            return Response({'error': 'Invalid coordinates.'}, status=status.HTTP_400_BAD_REQUEST)

        # Initialize the GraphDB manager for SPARQL communication
        try:
            graph_manager = GraphDBManager(user_id)
        except Exception as e:
            return Response({'error': f'GraphDB initialization failed: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

        try:
            # Normalize catalog and dataset names to make valid URIs (replace spaces, dots, dashes)
            catalog_name = re.sub(r"[ .-]", "_", catalog_name)
            dataset_name = re.sub(r"[ .-]", "_", dataset_name)

            # Initialize the GraphDB manager for the user
            graph_manager = GraphDBManager(user_id)
            # Build  catalog and dataset
            if not graph_manager.catalog_exists(catalog_name):
                catalog_uri=graph_manager.create_catalog(catalog_name)
            else:
                catalog_uri = URIRef(f"{NS['SPALOD']}{catalog_name}")
            if not graph_manager.dataset_exists(dataset_name):
               dataset_uri= graph_manager.create_dataset(dataset_name,catalog_uri)
            else:
                dataset_uri = URIRef(f"{NS['SPALOD']}{dataset_name}")
            # Check if the dataset triple exists (catalog dcat:dataset dataset)
            check_dataset_query = f"""
            ASK {{
                <{catalog_uri}> <{NS["DCAT"].dataset}> <{dataset_uri}> .
            }}
            """
            if not graph_manager.query_graphdb(check_dataset_query)['boolean']:
                # If not, create the catalog → dataset triple and dataset metadata
                dataset_graph = Graph()
                dataset_graph.add((catalog_uri, NS["DCAT"].dataset, dataset_uri))
                dataset_graph.add((dataset_uri, RDF.type, NS["DCAT"].Dataset))
                dataset_graph.add((dataset_uri, NS["RDFS"].label, Literal(dataset_name)))
                graph_manager.add_graph(dataset_graph)

            # Define the feature collection URI for this dataset
            feature_collection_uri = URIRef(f"{dataset_uri}/collection")

            # Check if the dataset has a linked feature collection
            check_fc_query = f"""
            ASK {{
                <{dataset_uri}> <{NS["GEOSPARQL"].hasFeatureCollection}> <{feature_collection_uri}> .
            }}
            """
            if not graph_manager.query_graphdb(check_fc_query)['boolean']:
                # Create and link the feature collection if missing
                fc_graph =  Graph() 
                fc_graph.add((dataset_uri, NS["GEOSPARQL"].hasFeatureCollection, feature_collection_uri))
                fc_graph.add((feature_collection_uri, RDF.type, NS["GEOSPARQL"].FeatureCollection))
                graph_manager.add_graph(fc_graph)

            # Generate a unique URI for the new feature
            feature_id = str(uuid.uuid4())
            feature_uri = URIRef(f"{feature_collection_uri}/feature/{feature_id}")
            geometry_uri = URIRef(f"{feature_uri}/geom")

            # Create WKT representation of the geometry
            point_wkt = f"POINT({lng} {lat})"

            # Create RDF triples for the new feature
            feature_graph = Graph()
            feature_graph.add((feature_collection_uri, NS["GEOSPARQL"].hasFeature, feature_uri))
            feature_graph.add((feature_uri, RDF.type, NS["GEOSPARQL"].Feature))
            feature_graph.add((feature_uri, NS["RDFS"].label, Literal(label)))
            feature_graph.add((feature_uri, NS["GEOSPARQL"].hasGeometry, geometry_uri))
            feature_graph.add((geometry_uri, RDF.type, NS["GEOSPARQL"].Geometry))
            feature_graph.add((geometry_uri, NS["GEOSPARQL"].asWKT, Literal(point_wkt, datatype=NS["GEOSPARQL"].wktLiteral)))

            # If metadata is passed, add it as RDF properties on the feature
            if isinstance(metadata, dict):
                for key, value in metadata.items():
                    # Assumes `key` is a valid full URI (e.g., DCTERMS.creator)
                    feature_graph.add((feature_uri, URIRef(key), Literal(value)))

            # Upload all triples to GraphDB
            graph_manager.add_graph(feature_graph)

            # Respond with success and return the new feature URI
            return Response({
                'message': 'Feature successfully added.',
                'feature_uri': str(feature_uri)
            }, status=status.HTTP_201_CREATED)

        except Exception as e:
            # Generic fallback for unexpected errors
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)