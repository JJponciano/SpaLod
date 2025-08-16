import os
import uuid
import json
import pyproj
import laspy
import pydeck as pdk
import rdflib
from rdflib import Graph, URIRef, Literal, Namespace, XSD
from rdflib.namespace import RDF, RDFS, OWL
import re
from shapely.geometry import Polygon
from shapely.ops import transform
from SPARQLWrapper import SPARQLWrapper, POST, URLENCODED
import numpy as np
import sys
from django.conf import settings
from django.http import JsonResponse
from shapely import wkt as shapely_wkt
from SPARQLWrapper import SPARQLWrapper, POST, JSON, SPARQLExceptions
import uuid
import time

from ..utils.env import get_env_settings


BATCH_SIZE = 5000  # Adjust batch size to balance speed and memory usage
#PROPERTIES AND CLASS URIs
hasGeometry="http://www.opengis.net/ont/geosparql#hasGeometry"
asWKT="http://www.opengis.net/ont/geosparql#asWKT"
hasFeature="https://geovast3d.com/ontologies/spalod#hasFeature"
hasFile="https://geovast3d.com/ontologies/spalod#hasFile"
hasOWL="https://geovast3d.com/ontologies/spalod#hasOWL"
dcat_dataset_uri = "http://www.w3.org/ns/dcat#Dataset"
dcat_catalog_uri = "http://www.w3.org/ns/dcat#Catalog"
gdi_uri="https://registry.gdi-de.org/"
dcterms = "http://purl.org/dc/terms/"
spalod="https://geovast3d.com/ontologies/spalod#"
NS = {
            "GEOSPARQL": Namespace(hasGeometry.rsplit('#', 1)[0] + "#"),
            'SPALOD': Namespace(hasFeature.rsplit('#', 1)[0] + "#"),
            "DCAT": Namespace(dcat_dataset_uri.rsplit('#', 1)[0] + "#"),
            "DCTERMS": Namespace(dcterms),
            "GDI": Namespace(gdi_uri)
        }
class OntologyProcessor:
    def __init__(self, file_uuid, ontology_url, file_url, metadata,user_id):
        self.sparql = SPARQLWrapper(settings.GRAPH_DB)
        self.sparql_statements = SPARQLWrapper(settings.GRAPH_DB_STATEMENTS)
        self.file_uuid = file_uuid
        self.ontology_url = ontology_url
        self.file_url = file_url
        self.metadata = metadata
        self.graph_iri = f"https://geovast3d.com/ontologies/spalod#graph_{user_id}"
        
        # Generate URIs as class attributes
        catalog_name = self.metadata.get("catalog")
        catalog_name = re.sub(r"[ .-]", "_", catalog_name)
        self.catalog_uri = URIRef(f"{NS['SPALOD']}{catalog_name}")
        self.dataset_uri = URIRef(f"{NS['SPALOD']}{self.file_uuid}")

        # Load ontology into an RDFlib graph
        ontology_path = os.path.join(os.path.dirname(__file__), '../data/spalod.owl')
        self.graph = Graph()
        self.graph.parse(ontology_path, format="application/rdf+xml")

        self.graph.bind("dcat", NS["DCAT"])
        self.graph.bind("dcterms", NS["DCTERMS"])
        self.graph.bind("geo", NS["GEOSPARQL"])
        self.graph.bind('SPALOD', NS['SPALOD'])
        self.process_catalog_and_dataset()

    def process_catalog_and_dataset(self):
        catalog_name = self.metadata.get("catalog")
        dataset_title = self.metadata.get("title")
        # Check if catalog exists in GraphDB

        if not self.catalog_exists():
            self.create_catalog(catalog_name)

        # Create dataset
        self.create_dataset(dataset_title)

    def save(self,ontpath):
        self.graph.serialize(destination=ontpath, format="application/rdf+xml")
        print(f"Ontology updated and saved to {ontpath}")

    def catalog_exists(self):
       
        query = f"""
        ASK {{
            <{self.catalog_uri}> a <{NS["DCAT"].Catalog}> .
        }}
        """
        self.sparql.setQuery(query)
        self.sparql.setReturnFormat(JSON)
        try:
            result = self.sparql.query().convert()
            return result.get("boolean", False)
        except SPARQLExceptions.SPARQLWrapperException as e:
            print(f"SPARQL Query Error: {e}")
            return False

    def create_catalog(self, catalog_name):
        catalog_data = [
            (self.catalog_uri, RDF.type, NS["DCAT"].Catalog),
            (self.catalog_uri, RDFS.label, Literal(catalog_name)),
        ]
        for triple in catalog_data:
            self.graph.add(triple)
        self.upload_to_graphdb(catalog_data)

    def create_dataset(self, title):
        spalod_url = get_env_settings("SPALOD_URL")
        dataset_data = [
            (self.dataset_uri, RDF.type, NS["DCAT"].Dataset),
            (self.dataset_uri, RDFS.label, Literal(title)),
            (self.catalog_uri, NS["DCAT"].dataset, self.dataset_uri),
            (self.dataset_uri, NS['SPALOD'].hasOWL, URIRef(f"{spalod_url}{self.ontology_url}")),  # Ensure it's a URI
            (self.dataset_uri, NS['SPALOD'].hasFile, URIRef(f"{spalod_url}{self.file_url}"))  # Ensure it's a URI
        ]

        # Add additional metadata from DCTERMS
        for key, value in self.metadata.items():
            if key not in ["catalog", "title"]:  # Exclude mandatory fields already handled
                dataset_data.append((self.dataset_uri, NS["DCTERMS"][key], Literal(value)))
        for triple in dataset_data:
            self.graph.add(triple)
        self.upload_to_graphdb(dataset_data)

    def upload_to_graphdb(self, triples):
        
        # Prepare SPARQL Update query for GraphDB
        update_query = "PREFIX dc: <http://purl.org/dc/elements/1.1/> \n INSERT DATA" 
        update_query +=f"{{ GRAPH <{self.graph_iri }> {{"
        for s, p, o in triples:
            if isinstance(o, URIRef):
                update_query += f"<{s}> <{p}> <{o}> . "
            else:
                update_query += f"<{s}> <{p}> \"{o}\" . "
        update_query += "}}"

        # Initialize SPARQLWrapper
        self.sparql_statements.setMethod(POST)
        self.sparql_statements.setQuery(update_query.encode("utf-8"))
        self.sparql_statements.setQuery(update_query)
        self.sparql_statements.setReturnFormat(JSON)

        try:
            # print("SPARQL Update Query:")
            # print(update_query)
            # print()
            response = self.sparql_statements.query()
            # print("Response Headers:", response.info())
            # print("Converted Response:", response.convert())
        except Exception as e:
            print(f"SPARQL update failed: {e}")
            print(f"Error uploading to GraphDB: {e}")
            raise

    

    
    def convert_coordinates_to_wkt(self, feature_type, coordinates):
        """Converts coordinates to a valid WKT format, dynamically handling 2D and 3D coordinates."""
        def format_coords(coord):
            """Format individual coordinates based on 2D or 3D structure."""
            if len(coord) == 3:  # Handle 3D coordinates dynamically
                return f"{coord[0]} {coord[1]} {coord[2]}"
            else:  # Handle 2D coordinates
                return f"{coord[0]} {coord[1]}"

        # Handling different geometry types based on the feature type
        if feature_type.lower() == "point":
            # WKT for Point: POINT (x y)
            formatted_coords = format_coords(coordinates)
            wkt_string = f"POINT ({formatted_coords})"

        elif feature_type.lower() == "linestring":
            # WKT for LineString: LINESTRING (x1 y1, x2 y2, ...)
            formatted_coords = ", ".join([format_coords(coord) for coord in coordinates])
            wkt_string = f"LINESTRING ({formatted_coords})"

        elif feature_type.lower() == "polygon":
            # WKT for Polygon: POLYGON ((x1 y1, x2 y2, ...))
            formatted_coords = ", ".join([format_coords(coord) for coord in coordinates])
            wkt_string = f"POLYGON (({formatted_coords}))"

        elif feature_type.lower() == "multilinestring":
            # WKT for MultiLineString: MULTILINESTRING ((x1 y1, x2 y2), (x3 y3, x4 y4), ...)
            formatted_lines = ", ".join([f"({', '.join([format_coords(coord) for coord in line])})" for line in coordinates])
            wkt_string = f"MULTILINESTRING ({formatted_lines})"

        elif feature_type.lower() == "multipolygon":
            # WKT for MultiPolygon: MULTIPOLYGON (((x1 y1, x2 y2, ...)), ((x3 y3, x4 y4, ...)), ...)
            formatted_polygons = ", ".join([f"(({', '.join([format_coords(coord) for coord in polygon])}))" for polygon in coordinates])
            wkt_string = f"MULTIPOLYGON ({formatted_polygons})"

        else:
            raise ValueError("Unsupported geometry type")

        # Check for square brackets and raise an error or print a message if found
        if "[" in wkt_string or "]" in wkt_string:
            print(f"Error: WKT string contains square brackets: {wkt_string}")

        return wkt_string

    def get_wkt_polygon(self, file_path, utm_zone=32, northern_hemisphere=True):
        """Generates a WKT polygon from a LAS file, transforming coordinates to WGS84."""
        with laspy.open(file_path) as las:
            point_data = las.read()

        x_coords = point_data.x
        y_coords = point_data.y

        # Create a convex hull
        points = np.vstack((x_coords, y_coords)).T
        hull = Polygon(points).convex_hull

        # Transform to WGS84
        utm_crs = f"epsg:326{utm_zone}" if northern_hemisphere else f"epsg:327{utm_zone}"
        project = pyproj.Transformer.from_crs(utm_crs, "epsg:4326", always_xy=True).transform
        hull_latlon = transform(project, hull)

        return hull_latlon.wkt

    def add_pointcloud(self, file_path, pointcloud_id, pointcloud_uuid):
        """Adds a point cloud to the ontology and GraphDB."""
        random_uuid = uuid.uuid4()
        feature_uri = URIRef(NS['SPALOD'][f"feature{random_uuid}"])
        geom_uri = URIRef(NS['SPALOD'][f"geom{random_uuid}"])
        pointcloud_uri = URIRef(NS['SPALOD'][pointcloud_uuid])
        # print(f"[INFO] creation of pointcloud_uri {pointcloud_uri}")

        # Add data to ontology
        wkt_string = self.get_wkt_polygon(file_path)
        geom_literal = Literal(wkt_string, datatype=NS["GEOSPARQL"].asWKT)
        # print(f"[INFO] creation of wkt polygon {wkt_string}")

        triples = [
            (pointcloud_uri, RDF.type, NS['SPALOD'].Pointcloud),
            (pointcloud_uri, NS['SPALOD'].pointcloud_id, Literal(pointcloud_id, datatype=XSD.string)),
            (pointcloud_uri, NS['SPALOD'].pointcloud_uuid, Literal(pointcloud_uuid, datatype=XSD.string)),
            (feature_uri, NS['SPALOD'].hasPointcloud, pointcloud_uri),
            (feature_uri, RDF.type, NS["GEOSPARQL"].Feature),
            (feature_uri, NS["GEOSPARQL"].hasGeometry, geom_uri),
            (geom_uri, RDF.type, NS["GEOSPARQL"].Geometry),
            (geom_uri, NS["GEOSPARQL"].asWKT, geom_literal)
        ]

        for triple in triples:
            self.graph.add(triple)

        self.upload_to_graphdb(triples)
        # print(f"[INFO] {triples}")


    def format_time(self, seconds):
        """Formats time into hours, minutes, and seconds for readability."""
        if seconds >= 3600:
            return f"{int(seconds // 3600)}h {int((seconds % 3600) // 60)}m {int(seconds % 60)}s"
        elif seconds >= 60:
            return f"{int(seconds // 60)}m {int(seconds % 60)}s"
        else:
            return f"{int(seconds)}s"


    def process_json_file(self, file_path):
        """Processes a GeoJSON file, validates structure, and uploads in batches to GraphDB."""
        print(f"PROCESSING JSON: {file_path}")

        start_time = time.time()
        processed_count = 0
        last_display_time = start_time
        batched_triples = []  # Stores triples before batch upload

        try:
            # Load JSON file
            with open(file_path, 'r') as f:
                data = json.load(f)

            # Validate JSON structure
            if not isinstance(data, dict) or "type" not in data or data["type"] != "FeatureCollection":
                raise ValueError("Invalid JSON structure: 'type' must be 'FeatureCollection'.")

            if "name" not in data:
                data["name"] = str(uuid.uuid4())  # Generate a random name if not provided

            if "features" not in data or not isinstance(data["features"], list):
                raise ValueError("Invalid JSON structure: Missing or incorrect 'features' field (must be a list).")

            # Create FeatureCollection in RDF and link it to dataset
            feature_collection_uri = URIRef(NS["GEOSPARQL"][f"FeatureCollection/{data['name']}"])
            collection_label = Literal(data["name"])

            batched_triples.extend([
                (feature_collection_uri, RDF.type, NS["GEOSPARQL"].FeatureCollection),
                (feature_collection_uri, RDFS.label, collection_label),
                (self.dataset_uri, NS["GEOSPARQL"].hasFeatureCollection, feature_collection_uri)  # Link dataset to collection
            ])

            # Process each feature
            total_features = len(data["features"])
            print(f"Detected {total_features} features in collection '{data['name']}'.")

            for i, feature in enumerate(data["features"]):
                try:
                    # Validate feature structure
                    if not isinstance(feature, dict) or "type" not in feature or feature["type"] != "Feature":
                        raise ValueError(f"Feature {i} is invalid: Missing or incorrect 'type' (must be 'Feature').")

                    if "properties" not in feature or not isinstance(feature["properties"], dict):
                        raise ValueError(f"Feature {i} is invalid: Missing or incorrect 'properties' field (must be a dictionary).")

                    if "geometry" not in feature or not isinstance(feature["geometry"], dict):
                        raise ValueError(f"Feature {i} is invalid: Missing or incorrect 'geometry' field (must be a dictionary).")

                    # Extract geometry and properties
                    geometry = feature["geometry"]
                    feature_type = geometry.get("type")
                    coordinates = geometry.get("coordinates")
                    properties = feature.get("properties", {})

                    if not feature_type or not coordinates:
                        raise ValueError(f"Feature {i} is invalid: Missing 'geometry' type or coordinates.")

                    # Generate unique URIs
                    random_uuid = uuid.uuid4()
                    feature_uri = URIRef(NS["GEOSPARQL"][f"feature{random_uuid}"])
                    geom_uri = URIRef(NS["GEOSPARQL"][f"geom{random_uuid}"])

                    # Convert geometry to WKT format
                    wkt_string = self.convert_coordinates_to_wkt(feature_type, coordinates)
                    geom_literal = Literal(wkt_string, datatype=NS["GEOSPARQL"].asWKT)

                    # Prioritize rdfs:label assignment
                    rdfs_label = None
                    if "label" in properties:
                        rdfs_label = Literal(properties["label"])
                    elif "name" in properties:
                        rdfs_label = Literal(properties["name"])
                    elif "item" in properties:
                        rdfs_label = Literal(properties["item"])

                    # Collect RDF triples
                    batched_triples.extend([
                        (feature_uri, RDF.type, NS["GEOSPARQL"].Feature),
                        (feature_collection_uri, NS["GEOSPARQL"].hasFeature, feature_uri),  # Link feature to FeatureCollection
                        (feature_uri, NS["GEOSPARQL"].hasGeometry, geom_uri),
                        (geom_uri, RDF.type, NS["GEOSPARQL"].Geometry),
                        (geom_uri, NS["GEOSPARQL"].asWKT, geom_literal)
                    ])

                    # Add rdfs:label if available
                    if rdfs_label:
                        batched_triples.append((feature_uri, RDFS.label, rdfs_label))

                    # Add other properties as triples
                    spalod_ns = NS['SPALOD']
                    batched_triples.extend([
                        (feature_uri, URIRef(spalod_ns + prop), Literal(value))
                        for prop, value in properties.items()
                    ])

                    processed_count += 1

                    # Display progress every second
                    current_time = time.time()
                    if current_time - last_display_time >= 1:
                        elapsed_time = current_time - start_time
                        estimated_total_time = (elapsed_time / max(processed_count, 1)) * total_features
                        remaining_time = estimated_total_time - elapsed_time

                        print(f"Processed {processed_count}/{total_features} features "
                            f"({(processed_count / total_features) * 100:.2f}%) "
                            f"| Estimated remaining: {self.format_time(remaining_time)}")

                        last_display_time = current_time  # Reset display timer

                    # Upload batch when limit is reached
                    if len(batched_triples) >= BATCH_SIZE:
                        self.upload_to_graphdb(batched_triples)
                        batched_triples = []  # Clear batch

                except Exception as fe:
                    print(f"Error processing feature {i}: {fe}")

            # Final batch upload for remaining triples
            if batched_triples:
                self.upload_to_graphdb(batched_triples)

            total_time = time.time() - start_time
            print(f"Processing completed in {self.format_time(total_time)}.")

        except Exception as e:
            print(f"Error during processing: {str(e)}")
            raise ValueError(f"File processing failed: {e}")

   
   
    def process(self, file):
            """Processes files based on their format."""
            if file.endswith('json'):
                try:
                    self.process_json_file(file)
                except Exception as e:
                    raise Exception(f"Ontology processing failed because: {str(e)}")

            elif file.endswith(('.ttl', '.rdf', '.owl', '.nt')):
                if file.endswith('.ttl'):
                    file_format = 'turtle'
                elif file.endswith('.rdf'):
                    file_format = 'xml'
                elif file.endswith('.owl'):
                    file_format = 'xml'
                elif file.endswith('.nt'):
                    file_format = 'nt'
                else:
                    raise ValueError(f"Unsupported file format for {file}")

                newowl = rdflib.Graph()
                newowl.parse(file, format=file_format)

                self.graph += newowl

                # Save the updated graph to GraphDB
                triples_to_save = []
                for s, p, o in newowl:
                    triples_to_save.append((s, p, o))
                self.upload_to_graphdb(triples_to_save)
                print(f"OWL file {file} loaded successfully in {file_format} format and saved to GraphDB.")
            else:
                raise ValueError(f"Unsupported file format for {file}")