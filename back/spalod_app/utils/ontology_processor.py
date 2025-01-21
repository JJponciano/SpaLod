import os
import uuid
import json
import pyproj
import pydeck as pdk
import rdflib
from rdflib import Graph, URIRef, Literal, Namespace, XSD
from rdflib.namespace import RDF, RDFS, OWL
import re
import laspy
from shapely.geometry import Polygon
from shapely.ops import transform
from SPARQLWrapper import SPARQLWrapper, POST, URLENCODED
import numpy as np
import sys
from django.conf import settings
from django.http import JsonResponse
from shapely import wkt as shapely_wkt
from SPARQLWrapper import SPARQLWrapper, POST, JSON, SPARQLExceptions
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
class OntologyProcessor:
    def __init__(self, file_uuid, ontology_url, file_url, metadata):
        self.sparql = SPARQLWrapper(settings.GRAPH_DB)
        self.sparql_statements = SPARQLWrapper(settings.GRAPH_DB_STATEMENTS)
        self.file_uuid = file_uuid
        self.ontology_url = ontology_url
        self.file_url = file_url
        self.metadata = metadata
         # Define namespaces
        self.NS = {
            "GEOSPARQL": Namespace(hasGeometry.rsplit('#', 1)[0] + "#"),
            'SPALOD': Namespace(hasFeature.rsplit('#', 1)[0] + "#"),
            "DCAT": Namespace(dcat_dataset_uri.rsplit('#', 1)[0] + "#"),
            "DCTERMS": Namespace(dcterms),
            "GDI": Namespace(gdi_uri)
        }
        # Generate URIs as class attributes
        catalog_name = self.metadata.get("catalog")
        catalog_name = re.sub(r"[ .-]", "_", catalog_name)
        self.catalog_uri = URIRef(f"{self.NS['SPALOD']}{catalog_name}")
        self.dataset_uri = URIRef(f"{self.NS['SPALOD']}{self.file_uuid}")

        # Load ontology into an RDFlib graph
        ontology_path = os.path.join(os.path.dirname(__file__), '../data/spalod.owl')
        self.graph = Graph()
        self.graph.parse(ontology_path, format="application/rdf+xml")

       

        self.graph.bind("dcat", self.NS["DCAT"])
        self.graph.bind("dcterms", self.NS["DCTERMS"])
        self.graph.bind("geo", self.NS["GEOSPARQL"])
        self.graph.bind('SPALOD', self.NS['SPALOD'])
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
            <{self.catalog_uri}> a <{self.NS["DCAT"].Catalog}> .
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
            (self.catalog_uri, RDF.type, self.NS["DCAT"].Catalog),
            (self.catalog_uri, RDFS.label, Literal(catalog_name)),
        ]
        for triple in catalog_data:
            self.graph.add(triple)
        self.upload_to_graphdb(catalog_data)

    def create_dataset(self, title):
        dataset_data = [
            (self.dataset_uri, RDF.type, self.NS["DCAT"].Dataset),
            (self.dataset_uri, RDFS.label, Literal(title)),
            (self.catalog_uri, self.NS["DCAT"].dataset, self.dataset_uri),
            (self.dataset_uri, self.NS['SPALOD'].hasOWL, URIRef(f"https://spalod.geovast3d.com{self.ontology_url}")),  # Ensure it's a URI
            (self.dataset_uri, self.NS['SPALOD'].hasFile, URIRef(f"https://spalod.geovast3d.com{self.file_url}"))  # Ensure it's a URI
        ]

        # Add additional metadata from DCTERMS
        for key, value in self.metadata.items():
            if key not in ["catalog", "title"]:  # Exclude mandatory fields already handled
                dataset_data.append((self.dataset_uri, self.NS["DCTERMS"][key], Literal(value)))
        for triple in dataset_data:
            self.graph.add(triple)
        self.upload_to_graphdb(dataset_data)

    def upload_to_graphdb(self, triples):
        graph_iri = self.NS['SPALOD'].Global
        # Prepare SPARQL Update query for GraphDB
        update_query = "PREFIX dc: <http://purl.org/dc/elements/1.1/> \n INSERT DATA" 
        update_query +=f"{{ GRAPH <{graph_iri}> {{"
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

    def add_individual(self, feature_uri, geom_uri, wkt, properties):
        """Adds an individual feature and its geometry to the ontology and GraphDB."""
        # Add feature and geometry to ontology
        self.graph.add((feature_uri, RDF.type, self.NS["GEOSPARQL"].Feature))
        self.graph.add((self.catalog_uri, self.NS['SPALOD'].hasFeature, feature_uri))
        self.graph.add((feature_uri, self.NS["GEOSPARQL"].hasGeometry, geom_uri))
        self.graph.add((geom_uri, RDF.type, self.NS["GEOSPARQL"].Geometry))
        geom_literal = Literal(wkt, datatype=self.NS["GEOSPARQL"].asWKT)
        self.graph.add((geom_uri, self.NS["GEOSPARQL"].asWKT, geom_literal))

        # Add properties to ontology
        for prop, value in properties.items():
            prop_uri = URIRef(f"{self.NS['SPALOD']}{prop}")
            self.graph.add((feature_uri, prop_uri, Literal(value)))

        # Prepare data for GraphDB
        triples = [
            (feature_uri, RDF.type, self.NS["GEOSPARQL"].Feature),
            (self.catalog_uri, self.NS['SPALOD'].hasFeature, feature_uri),
            (feature_uri, self.NS["GEOSPARQL"].hasGeometry, geom_uri),
            (geom_uri, RDF.type, self.NS["GEOSPARQL"].Geometry),
            (geom_uri, self.NS["GEOSPARQL"].asWKT, geom_literal)
        ]

        for prop, value in properties.items():
            prop_uri = URIRef(f"{self.NS['SPALOD']}{prop}")
            triples.append((feature_uri, prop_uri, Literal(value)))
        self.upload_to_graphdb(triples)

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
        feature_uri = URIRef(self.NS['SPALOD'][f"feature{random_uuid}"])
        geom_uri = URIRef(self.NS['SPALOD'][f"geom{random_uuid}"])
        pointcloud_uri = URIRef(self.NS['SPALOD'][pointcloud_uuid])

        # Add data to ontology
        wkt_string = self.get_wkt_polygon(file_path)
        geom_literal = Literal(wkt_string, datatype=self.NS["GEOSPARQL"].asWKT)

        triples = [
            (pointcloud_uri, RDF.type, self.NS['SPALOD'].Pointcloud),
            (pointcloud_uri, self.NS['SPALOD'].pointcloud_id, Literal(pointcloud_id, datatype=XSD.string)),
            (pointcloud_uri, self.NS['SPALOD'].pointcloud_uuid, Literal(pointcloud_uuid, datatype=XSD.string)),
            (feature_uri, self.NS['SPALOD'].hasPointcloud, pointcloud_uri),
            (feature_uri, RDF.type, self.NS["GEOSPARQL"].Feature),
            (feature_uri, self.NS["GEOSPARQL"].hasGeometry, geom_uri),
            (geom_uri, RDF.type, self.NS["GEOSPARQL"].Geometry),
            (geom_uri, self.NS["GEOSPARQL"].asWKT, geom_literal)
        ]

        for triple in triples:
            self.graph.add(triple)

        self.upload_to_graphdb(triples)

    def process_json_file(self, file_path):
        print("PROCESSING OF THE JSON ",file_path)
        """Processes GeoJSON or JSON files to add features and geometries."""
        try:
            with open(file_path, 'r') as f:
                data = json.load(f)
            if "features" in data:  # GeoJSON
                for feature in data.get("features", []):
                    geometry = feature.get("geometry")
                    if geometry:
                        feature_type = geometry.get("type")
                        coordinates = geometry.get("coordinates")
                        if feature_type and coordinates:
                            random_uuid = uuid.uuid4()
                            feature_uri = URIRef(self.NS["GDI"][f"feature{random_uuid}"])
                            geom_uri = URIRef(self.NS["GDI"][f"geom{random_uuid}"])
                            properties = feature.get("properties", {})
                            wkt_string = self.convert_coordinates_to_wkt(feature_type, coordinates)
                            self.add_individual(feature_uri, geom_uri, wkt_string, properties)

            elif "head" in data and "results" in data:  # SPARQL JSON

                        #TODO il faut aussi sauvegarder dans GRAPHDB


                for binding in data.get("results", {}).get("bindings", []):
                    random_uuid = uuid.uuid4()
                    feature_uri = URIRef(self.NS["GDI"][f"feature{random_uuid}"])
                    
                    # Initialize triples list for GraphDB
                    triples = [
                        (feature_uri, RDF.type, self.NS["GEOSPARQL"].Feature),
                        (self.catalog_uri, self.NS['SPALOD'].hasFeature, feature_uri)
                    ]

                    # Add the same triples to the local ontology
                    self.graph.add((feature_uri, RDF.type, self.NS["GEOSPARQL"].Feature))
                    self.graph.add((self.catalog_uri, self.NS['SPALOD'].hasFeature, feature_uri))

                    geom_uri = None
                    wkt = None
                    for k, v in binding.items():
                        datatype = v.get("datatype")
                        value = v.get("value")

                        if datatype and "wktLiteral" in datatype:
                            geom_uri = URIRef(self.NS["GDI"][f"geom{random_uuid}"])
                            wkt = value
                            geom_literal = Literal(wkt, datatype=self.NS["GEOSPARQL"].asWKT)

                            # Add geometry-related triples to GraphDB list
                            triples.extend([
                                (feature_uri, self.NS["GEOSPARQL"].hasGeometry, geom_uri),
                                (geom_uri, RDF.type, self.NS["GEOSPARQL"].Geometry),
                                (geom_uri, self.NS["GEOSPARQL"].asWKT, geom_literal)
                            ])

                            # Add geometry-related triples to the local ontology
                            self.graph.add((feature_uri, self.NS["GEOSPARQL"].hasGeometry, geom_uri))
                            self.graph.add((geom_uri, RDF.type, self.NS["GEOSPARQL"].Geometry))
                            self.graph.add((geom_uri, self.NS["GEOSPARQL"].asWKT, geom_literal))
                        else:
                            prop_uri = URIRef(self.NS['SPALOD'][k])
                            literal_value = Literal(value)

                            # Add property-related triples to GraphDB list
                            triples.append((feature_uri, prop_uri, literal_value))

                            # Add property-related triples to the local ontology
                            self.graph.add((feature_uri, prop_uri, literal_value))

                    # Upload the collected triples to GraphDB
                    self.upload_to_graphdb(triples)

            
        except Exception as e:
            return f"Error during processing: {str(e)}"

    def process(self, file):
            """Processes files based on their format."""
            if file.endswith('json'):
                try:
                    self.process_json_file(file)
                except Exception as e:
                    print(f"Error during JSON processing: {str(e)}")
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