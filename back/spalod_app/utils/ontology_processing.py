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
from shapely.geometry import Polygon#
from shapely.ops import transform
from SPARQLWrapper import SPARQLWrapper, POST
import numpy as np
import sys

class OntologyProcessor:
    def __init__(self,file_uuid, ontology_url, map_url,metadata):
        self.metadata=metadata
        self.file_uuid=file_uuid
        self.ontology_url=ontology_url
        self.map_url=map_url
        self.ontology_path = os.path.join(os.path.dirname(__file__), '../data/base_ontology.owl')
        self.geo = Namespace("http://www.opengis.net/ont/geosparql#")
        self.ex =  Namespace("https://registry.gdi-de.org/id/hamburg/")
        #self.gdi = Namespace("https://registry.gdi-de.org/id/de.bund.balm.radnetz/")
        self.gdi = Namespace("https://registry.gdi-de.org/")
        self.flyvast = Namespace("https://flyvast.com/")
        self.spalod = Namespace("http://spalod/")
        self.graph = Graph()
                   # Load base ontology
        self.graph.parse(self.ontology_path, format="application/rdf+xml")


         # Prepare metadata triples
        self.catalog_uri = URIRef(self.spalod[f"catalog_{file_uuid}"])
        self.graph.add((self.catalog_uri, RDF.type, self.geo.Catalog))
        self.ensure_property(self.spalod.hasHTML, OWL.DatatypeProperty, "hasHTML")
        self.ensure_property(self.spalod.hasFeature, OWL.ObjectProperty, "hasFeature")
        self.ensure_property(self.spalod.hasOWL, OWL.DatatypeProperty, "hasOWL")
        self.graph.add((self.catalog_uri, self.spalod.hasHTML,Literal(map_url, datatype=XSD.string)))
        self.graph.add((self.catalog_uri, self.spalod.hasOWL,Literal(ontology_url, datatype=XSD.string)))
        for key, value in metadata.items():
            self.ensure_property(self.spalod[key], OWL.DatatypeProperty, key)
            self.graph.add((self.catalog_uri, self.spalod[key], Literal(value, datatype=XSD.string)))

        
      
    
    def get_wkt_polygon(self,file_path, utm_zone=32, northern_hemisphere=True):
        # Load the LAS file
        with laspy.open(file_path) as las:
            point_data = las.read()
            
        # Extract X and Y coordinates from the point cloud
        x_coords = point_data.x
        y_coords = point_data.y
        
        # Create a convex hull of the points to get the 2D boundary
        points = np.vstack((x_coords, y_coords)).T
        hull = Polygon(points).convex_hull
        
        # Define the projection transformation from UTM to WGS84 (latitude/longitude)
        utm_crs = f"epsg:326{utm_zone}" if northern_hemisphere else f"epsg:327{utm_zone}"
        project = pyproj.Transformer.from_crs(utm_crs, "epsg:4326", always_xy=True).transform
        hull_latlon = transform(project, hull)
        
        # Swap latitude and longitude coordinates
        hull_latlon_swapped = Polygon([(y, x) for x, y in hull_latlon.exterior.coords])
        
        # Return the WKT representation of the polygon in latitude/longitude
        wkt_polygon = hull_latlon_swapped.wkt
        return wkt_polygon


       
    def process(self,file):
        if  file.endswith('json'):
            try:
                # Process GeoJSON file and add data to ontology
                self.convert_geojson_to_ontology(file)
            except Exception as e:
                print(f"Error during GeoJSON processing: {str(e)}")
                raise Exception(f"Ontology processing failed: {str(e)}")
        elif file.endswith(('.ttl', '.rdf', '.owl', '.nt')):
               # Determine the format based on the file extension
            if file.endswith('.ttl'):
                file_format = 'turtle'
            elif file.endswith('.rdf'):
                file_format = 'xml'
            elif file.endswith('.owl'):
                file_format = 'xml'  # OWL files are typically in RDF/XML format
            elif file.endswith('.nt'):
                file_format = 'nt'
            else:
                raise ValueError(f"Unsupported file format for {file}")
            
            # Parse the OWL file and add it to the self.graph
            newowl = rdflib.Graph()
            newowl.parse(file, format=file_format)

            # Merge the newowl graph into the main graph
            self.graph += newowl

            print(f"OWL file {file} loaded successfully in {file_format} format.")
        else:
            raise ValueError(f"Unsupported file format for {file}")
        
    def save(self,destination,map_file_path):
         # After processing, save the updated ontology
        self.graph.serialize(destination=destination, format="turtle")
        self.generate_map(output=map_file_path, need_transform=False)

        # SAVE TO GRAPHDB
        sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod/statements")
        sparql.setMethod(POST)
        sparql.setRequestMethod("postdirectly")  # SPARQLWrapper uses this method for SPARQL update requests
        sparql.addCustomHttpHeader("Content-Type", "application/sparql-update")        
        graph_iri = self.spalod.General
        graph_specific = self.spalod[self.file_uuid]

            # Add hasHTML and hasOWL properties (as strings)
        sparql_update_query = f"""
            PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
            INSERT {{
                GRAPH <{graph_iri}> {{
                        <{self.catalog_uri}> <{self.spalod.hasHTML}> "{self.map_url}"^^xsd:string .
                        <{self.catalog_uri}> <{self.spalod.hasOWL}> "{self.ontology_url}"^^xsd:string .
                }}
            }}WHERE {{}}
        """
        print(sparql_update_query)
        sparql.setQuery(sparql_update_query)
        sparql.query()
        # Prepare metadata triples
        metadata_triples = ""
        for key, value in self.metadata.items():
            metadata_triples += f'<{self.catalog_uri}> <{self.spalod[key]}> "{value}"^^xsd:string .\n'

        metadata_query = f"""
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        INSERT DATA {{
            GRAPH <{graph_iri}> {{
                {metadata_triples}
            }}
        }}
        """
        print(metadata_query)
        sparql.setQuery(metadata_query)
        sparql.query()

        # Serialize the ontology as plain N-Triples, which can be inserted directly into the SPARQL query
        rdf_data = self.graph.serialize(format="nt")
        
        # Construct SPARQL query
        try:
            try:
                insert_query = f"""
                INSERT DATA {{
                    GRAPH <{graph_specific}> {{
                        {rdf_data}
                    }}
                }}
                """
                sparql.setQuery(insert_query)
                sparql.query()
            except Exception as e:
                print(f"Failed to add ontology to GraphDB: {str(e)}")
                # Split the serialized RDF data into individual triples
                rdf_triples = rdf_data.splitlines()

                # Remove any double periods and ensure each triple ends with a single period
                rdf_triples_cleaned = [
                    triple.strip().rstrip('.') + ' .' for triple in rdf_triples[:100]
                ]

                # Join the triples back into a string
                rdf_triples_limited = "\n".join(rdf_triples_cleaned)

                # Construct the SPARQL INSERT DATA query
                insert_query = f"""
                    INSERT DATA {{
                        GRAPH <{graph_specific}> {{
                            {rdf_triples_limited}
                        }}
                    }}
                """
                sparql.setQuery(insert_query)
                sparql.query()
        except Exception as e:
            raise Exception(f"Failed to add ontology to GraphDB: {str(e)}")

    def convert_coordinates(self, coordinates):
        """Converts coordinates from [lon, lat, (optional) z] to [lat, lon], automatically handling 2D and 3D coordinates."""
        
        # If it's a nested structure (like LineString, Polygon, etc.)
        if isinstance(coordinates[0], list):  
            return [self.convert_coordinates(coord) for coord in coordinates]
        
        # Automatically detect if there's a Z-coordinate by checking the length of the coordinate
        if len(coordinates) == 3:  # Handle 3D coordinates
            lon, lat, _ = coordinates  # Discard the Z value
        else:
            lon, lat = coordinates[:2]  # Only consider lon, lat for 2D coordinates
        
        # Return [lat, lon] regardless of 2D or 3D coordinates
        return [lat, lon]
    def add_pointcloud(self,file_path,pointcloud_id,pointcloud_uuid):
         #get the wkt
        random_uuid = uuid.uuid4()
        feature_uri = URIRef(self.ex[f"feature{random_uuid}"])
        geom_uri = URIRef(self.ex[f"geom{random_uuid}"])
        pointcloud_uri = URIRef(self.flyvast[pointcloud_uuid])

        self.ensure_property(self.geo.asWKT, OWL.DatatypeProperty, "asWKT")
        self.ensure_property(self.geo.hasGeometry, OWL.ObjectProperty, "hasGeometry")
        self.ensure_property(self.geo.hasPointcloud, OWL.ObjectProperty, "hasPointcloud")

        # Flyvast properties
        self.ensure_property(self.flyvast.pointcloud_id, OWL.DatatypeProperty, "pointcloud_id")
        self.ensure_property(self.flyvast.pointcloud_uuid, OWL.DatatypeProperty, "pointcloud_uuid")
        self.graph.add((pointcloud_uri, RDF.type, self.flyvast.Pointcloud))
        self.graph.add((pointcloud_uri, self.flyvast.pointcloud_id, pointcloud_id))
        self.graph.add((pointcloud_uri, self.flyvast.pointcloud_uuid, pointcloud_uuid))
        
        self.graph.add((feature_uri, self.geo.hasPointcloud, pointcloud_uri))
        self.graph.add((feature_uri, RDF.type, self.geo.Feature))
        self.graph.add((feature_uri, self.geo.hasGeometry, geom_uri))
        self.graph.add((self.catalog_uri, self.spalod.hasFeature, feature_uri))

        self.graph.add((geom_uri, RDF.type, self.geo.Geometry))
        wkt_string =self.get_wkt_polygon(file_path)
        print(f"WKT: {wkt_string}")
        geom_literal = Literal(wkt_string, datatype=self.geo.wktLiteral)
        self.graph.add((geom_uri, self.geo.asWKT, geom_literal))
    
    def convert_geojson_to_ontology(self, geojson_path):
        """Converts GeoJSON data to RDF and adds it to the ontology."""
        
        try:
            with open(geojson_path, 'r') as f:
                data = json.load(f)

            for feature in data.get("features", []):
                geometry = feature.get("geometry")
                if geometry:
                    feature_type = geometry.get("type")
                    # Apply the conversion to all geometries
                    coordinates = self.convert_coordinates(geometry.get("coordinates"))
                    if feature_type is not None and coordinates is not None:
                        # Create URIs for new features and geometries
                        random_uuid = uuid.uuid4()
                        feature_uri = URIRef(self.ex[f"feature{random_uuid}"])
                        geom_uri = URIRef(self.ex[f"geom{random_uuid}"])
                        route_uri = URIRef(self.ex[f"route{random_uuid}"])
                        knotena_uri = URIRef(self.ex[f"knoten_a{random_uuid}"])
                        knotenz_uri = URIRef(self.ex[f"knoten_z{random_uuid}"])
                        measure_uri = URIRef(self.ex[f"measure{random_uuid}"])

                        properties = {k: v for k, v in feature.get("properties", {}).items() if v is not None}
                        self.add_individual(feature_uri, route_uri, knotena_uri, knotenz_uri, measure_uri, geom_uri, feature_type, coordinates, properties)
                    else:
                        print("coords none")
        except Exception as e:
            print(f"Error during convert_geojson_to_ontology processing: {str(e)}")
            raise Exception(f"Ontology processing failed: {str(e)}")
        
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



    def add_individual(self, feature_uri, route_uri, knotena_uri, knotenz_uri, measure_uri, geom_uri, feature_type, coordinates, properties):
        """Adds an individual feature and its geometry to the ontology."""
        self.ensure_property(self.geo.asWKT, OWL.DatatypeProperty, "asWKT")
        self.ensure_property(self.geo.hasGeometry, OWL.ObjectProperty, "hasGeometry")

        self.graph.add((feature_uri, RDF.type, self.geo.Feature))
        self.graph.add((self.catalog_uri, self.spalod.hasFeature, feature_uri))
        self.graph.add((feature_uri, self.geo.hasGeometry, geom_uri))
        self.graph.add((route_uri, RDF.type, self.ex.Route))
        self.graph.add((knotena_uri, RDF.type, self.ex.Knoten))
        self.graph.add((knotenz_uri, RDF.type, self.ex.Knoten))
        self.graph.add((feature_uri, RDF.type, self.ex.Streckenabschnitt))
        self.graph.add((feature_uri, self.gdi.hasRoute, route_uri))
        self.graph.add((feature_uri, self.gdi.beginntBeiKnoten, knotena_uri))
        self.graph.add((feature_uri, self.gdi.endetBeiKnoten, knotenz_uri))
        self.graph.add((measure_uri, RDF.type, self.ex.MeasureType))
        self.graph.add((feature_uri, self.gdi.laenge, measure_uri))
        
        self.graph.add((geom_uri, RDF.type, self.geo.Geometry))
        wkt_string = self.convert_coordinates_to_wkt(feature_type, coordinates)
        geom_literal = Literal(wkt_string, datatype=self.geo.wktLiteral)
        self.graph.add((geom_uri, self.geo.asWKT, geom_literal))
        # Add properties
        for prop, value in properties.items():
            if isinstance(value, str):
                 value = value.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n")

            if prop == "length":
                prop_uri = URIRef(self.gdi["hasValue"])
                self.graph.add((measure_uri, prop_uri, Literal(value)))
                self.ensure_property(prop_uri, OWL.DatatypeProperty, "hasValue")
            elif prop == "routenname":
                prop_uri = URIRef(self.gdi["name"])
                self.graph.add((route_uri, prop_uri, Literal(value)))
                self.ensure_property(prop_uri, OWL.DatatypeProperty, "name")
            elif prop == "routentyp":
                prop_uri = URIRef(self.ex[prop])
                self.graph.add((route_uri, prop_uri, Literal(value)))
                self.ensure_property(prop_uri, OWL.DatatypeProperty, prop)
            elif prop == "routeninformation":
                prop_uri = URIRef(self.ex[prop])
                self.graph.add((route_uri, prop_uri, Literal(value)))
                self.ensure_property(prop_uri, OWL.DatatypeProperty, prop)
            elif prop == "richtung":
                if value=="Rückweg":#gegen geometrie richtung
                    richtung_individual = URIRef(f"https://registry.gdi-de.org/codelist/de.bund.balm.radnetz/Richtung/3")
                elif value=="Hinweg":#geometrie richtung
                    richtung_individual = URIRef(f"https://registry.gdi-de.org/codelist/de.bund.balm.radnetz/Richtung/2")
                elif value=="Hin- und Rückweg":#beide richtung
                    richtung_individual = URIRef(f"https://registry.gdi-de.org/codelist/de.bund.balm.radnetz/Richtung/1")
                else:#unbekannt
                    richtung_individual = URIRef(f"https://registry.gdi-de.org/codelist/de.bund.balm.radnetz/Richtung/9")
                self.graph.add((feature_uri, self.gdi['richtung'], richtung_individual))
                self.ensure_property(self.ex['richtung'], OWL.ObjectProperty, "richtung")
            else:
                prop_uri = URIRef(self.ex[prop])
                self.graph.add((feature_uri, prop_uri, Literal(value)))
                self.ensure_property(prop_uri, OWL.DatatypeProperty, prop)

    def ensure_property(self, property_uri, property_type, label):
        """Ensures a property exists in the ontology, adding it if necessary."""
        if (property_uri, None, None) not in self.graph:
            self.graph.add((property_uri, RDF.type, property_type))
            self.graph.add((property_uri, RDFS.label, Literal(label)))

    def generate_map(self, output='map.html', need_transform=False, max_features=100000):
        """Generates an interactive map using pydeck from the ontology data."""
        # Initialize Pydeck layer data
        line_data = []
        point_data = []
        # Iterate through features in the ontology
        feature_count = 0

        for feature in self.graph.subjects(RDF.type, self.geo.Feature):
            if feature_count >= max_features:
                break

            # Get geometry
            geom_uri = self.graph.value(subject=feature, predicate=self.geo.hasGeometry)
            wkt_string = self.graph.value(subject=geom_uri, predicate=self.geo.asWKT)
                    # Print the first 200 characters of wkt_string
            if "[" in wkt_string or "]" in wkt_string:
                    wkt_string = wkt_string.replace("[", "(").replace("]", ")")
            # Get properties
            properties = {}
            for prop, value in self.graph.predicate_objects(subject=feature):
                if isinstance(value, Literal):
                    properties[str(prop.split('#')[-1])] = str(value)
                else:   
                    try:
                        properties[str(prop.split('#')[-1])] = str(value)
                    except Exception as e:
                        print(f"Error in extracting property: {str(e)}")
                        print(str(prop.split('#')[-1]))

            pdisplay = ""
            for cle, valeur in properties.items():
                clestr = str(cle).replace("https://registry.gdi-de.org/id/de.bund.balm.radnetz/", "gdi:")
                clestr = clestr.replace("http://www.opengis.net/ont/geosparql#", "geosparql:")
                clestr = clestr.replace("https://registry.gdi-de.org/id/hamburg/", "hamburg:")
                pdisplay += clestr + " = " + valeur + "</br>"
                
            
            if self.ex in geom_uri:
                line_color = [0, 0, 255, 255]  # Blue
                line_width = 10  # Blue lines are wider
                try:
                    coords = self.parse_wkt(str(wkt_string))  # Assuming no Z-value
                    if not coords:
                        print(f"Warning: Parsed coordinates are empty for WKT: {wkt_string[:200]}")
                except Exception as e:
                    print(f":: Error during parse_wkt: {str(e)} for WKT: {wkt_string[:200]}")
            else:
                line_color = [255, 0, 0, 255]  # Red
                line_width = 20
                try:
                    coords = self.parse_wkt(str(wkt_string)) 
                    if not coords:
                        print(f"Warning: Parsed coordinates are empty for WKT: {wkt_string[:200]}")
                except Exception as e:
                    print(f"Error during parse_wkt: {str(e)} for WKT: {wkt_string[:200]}")
                    print(f"FAILED to parse WKT string for geometry URI: {geom_uri}")
                    continue  # Skip this feature if it fails






            # For LineString, add data to the line layer
            if "LINESTRING" in wkt_string.upper() or "MULTILINESTRING" in wkt_string.upper():
                try:
                    line_data.append({
                        "start": coords[0],
                        "end": coords[-1],
                        "path": coords,
                        "properties": pdisplay,
                        "get_color": line_color,  # Set the line color based on the condition
                        "width_min_pixels": line_width  # Set the line width based on the condition
                    })
                    feature_count += 1
                except Exception as e:
                    print(f"Error in line_data append: {str(e)}")
                    print(f"Feature has no defined coordinates in linestring: {feature}")
            # For other geometries like Points, add data to point layer
            else:
                print(f"Not a line: ")
                for coord in coords:
                    point_data.append({
                        "position": coord,
                        "properties": properties
                    })
                feature_count += 1

        # Debug: Line data and point data information
        print(f"Total line data: {len(line_data)}")
        print(f"Total point data: {len(point_data)}")

        # Define Pydeck layers
        line_layer = pdk.Layer(
            'PathLayer',
            line_data,
            get_path="path",
            width_scale=20,
            get_color="get_color",  # Use the get_color field to specify line colors
            get_width="width_min_pixels",  # Use the get_width field to specify line widths
            pickable=True
        )

        point_layer = pdk.Layer(
            'ScatterplotLayer',
            point_data,
            get_position="position",
            get_radius=100,
            get_color=[0, 0, 255, 255],  # Default blue color for points
            pickable=True
        )

        # Define view state
        view_state = pdk.ViewState(latitude=51.1657, longitude=10.4515, zoom=6)

        # Create Pydeck deck
        r = pdk.Deck(layers=[line_layer, point_layer], initial_view_state=view_state,
                    tooltip={"html": "{properties}", "style": {"color": "white", "backgroundColor": "#f00"}})

        # Render the deck to a standalone HTML file
        r.to_html(output)

    def parse_wkt(self, wkt_string):
        """Parses WKT (Well-Known Text) for coordinates, automatically handling both 2D and 3D coordinates."""
        if wkt_string is None:
            print("Warning: WKT string is None")
            return []

        # Helper function to clean and parse individual coordinates
        def clean_and_parse(coord_string):
            """Removes extraneous characters and splits the coordinate string into floats."""
            parts = list(map(float, coord_string.replace('(', '').replace(')', '').split()))
            if len(parts) == 3:  # 3D coordinates
                return (parts[1], parts[0], parts[2])  # Return as (lat, lon, z)
            elif len(parts) == 2:  # 2D coordinates
                return (parts[1], parts[0])  # Return as (lat, lon)
            else:
                raise ValueError(f"Unexpected number of coordinate components: {len(parts)}")

        # Handle MultiLineString
        if wkt_string.lower().startswith("multilinestring"):
            # Adjust regex to capture the coordinates inside MultiLineString
            coords_text = re.search(r'multilinestring\s*\(\((.+)\)\)', wkt_string, re.IGNORECASE)
            if coords_text is not None:
                coords = [coord.strip() for coord in coords_text.group(1).split(',')]
                coords_cleaned = [clean_and_parse(coord) for coord in coords]
                return coords_cleaned

            print(f"Warning: No coordinates found in MultiLineString WKT: {wkt_string[:200]}")
            return []

        # Handle LineString
        elif wkt_string.lower().startswith("linestring"):
            # Adjust regex to capture the coordinates inside LineString
            coords_text = re.search(r'linestring\s*\((.+)\)', wkt_string, re.IGNORECASE)
            if coords_text is not None:
                coords = [coord.strip() for coord in coords_text.group(1).split(',')]
                coords_cleaned = [clean_and_parse(coord) for coord in coords]
                return coords_cleaned

            print(f"Warning: No coordinates found in LineString WKT: {wkt_string[:200]}")
            return []

        else:
            print(f"Warning: Unexpected WKT type or could not parse WKT string: {wkt_string[:200]}")
        
        return []



    def transform_coords(self, coords):
        """Transforms coordinates from EPSG:25832 to EPSG:4326."""
        source_crs = pyproj.CRS("EPSG:25832")
        dest_crs = pyproj.CRS("EPSG:4326")
        transformer = pyproj.Transformer.from_crs(source_crs, dest_crs, always_xy=True)
        return [transformer.transform(lon, lat) for lat, lon in coords]
def main():
    if len(sys.argv) != 2:
        print("Usage: python ontology_processing.py <path_to_las_file>")
        sys.exit(1)
    processor = OntologyProcessor()
    file_path = sys.argv[1]
    polygon_wkt = processor.get_wkt_polygon(file_path)
    print(polygon_wkt)

if __name__ == "__main__":
    main()
