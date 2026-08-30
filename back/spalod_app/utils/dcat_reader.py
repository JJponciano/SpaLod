from __future__ import annotations
from shapely import wkt as shapely_wkt
from shapely.errors import GEOSException
from shapely.ops import transform as shapely_transform
from typing import Any

from rdflib import Graph, Literal, URIRef
from rdflib.namespace import DCAT, DCTERMS, FOAF, RDF, RDFS

from pyproj import CRS, Transformer
from pyproj.exceptions import CRSError,ProjError


LOCN_GEOMETRY = URIRef("http://www.w3.org/ns/locn#geometry")
SCHEMA_START_DATE = URIRef("http://schema.org/startDate")
SCHEMA_END_DATE = URIRef("http://schema.org/endDate")
ADMS_IDENTIFIER = URIRef("http://www.w3.org/ns/adms#identifier")
GEO_WKT_LITERAL = URIRef("http://www.opengis.net/ont/geosparql#" "wktLiteral")
CRS84_URI = ("http://www.opengis.net/def/crs/" "OGC/1.3/CRS84")

def unique_values(items) -> list[str]:
    result = []
    seen = set()

    for item in items:
        value = str(item).strip() if item is not None else ""

        if value and value not in seen:
            seen.add(value)
            result.append(value)

    return result


def values(
    graph: Graph,
    subject,
    predicate,
) -> list[str]:
    return unique_values(
        graph.objects(subject, predicate)
    )


def first(
    graph: Graph,
    subject,
    predicate,
    default: str = "",
) -> str:
    found = values(
        graph,
        subject,
        predicate,
    )

    return found[0] if found else default


def resource_label(
    graph: Graph,
    resource,
) -> str:
    if isinstance(resource, Literal):
        return str(resource).strip()

    for predicate in (
        FOAF.name,
        DCTERMS.title,
        RDFS.label,
    ):
        label = first(
            graph,
            resource,
            predicate,
        )

        if label:
            return label

    return str(resource).strip()


def parse_wkt_geometry(
    wkt_value: str,
):
    wkt_text = (
        wkt_value or ""
    ).strip()

    if not wkt_text:
        return None

    if wkt_text.startswith("<"):
        closing_bracket = wkt_text.find(">")

        if closing_bracket == -1:
            raise ValueError(
                "Invalid CRS prefix in WKT geometry."
            )

        wkt_text = wkt_text[
            closing_bracket + 1:
        ].strip()

    try:
        geometry = shapely_wkt.loads(
            wkt_text
        )
    except (GEOSException, ValueError) as exc:
        raise ValueError(
            f"Invalid WKT geometry: {exc}"
        ) from exc

    if geometry.is_empty:
        return None

    return geometry

def iso_geometry_type(
    geometry,
) -> str:
    if geometry is None:
        return ""

    geometry_type = geometry.geom_type

    mapping = {
        "Point": "point",
        "MultiPoint": "point",
        "LineString": "curve",
        "LinearRing": "curve",
        "MultiLineString": "curve",
        "Polygon": "surface",
        "MultiPolygon": "surface",
        "GeometryCollection": "complex",
    }

    return mapping.get(
        geometry_type,
        "complex",
    )

def wkt_bbox(
    wkt_value: str,
) -> list[float] | None:
    geometry = parse_wkt_geometry(
        wkt_value
    )

    if geometry is None:
        return None

    min_x, min_y, max_x, max_y = (
        geometry.bounds
    )

    return [
        float(min_x),
        float(min_y),
        float(max_x),
        float(max_y),
    ]

def geographic_wkt_bbox(
    wkt_value: str,
    crs_uri: str,
) -> list[float] | None:
    geometry = parse_wkt_geometry(
        wkt_value
    )

    if geometry is None:
        return None

    if not crs_uri:
        return None

    try:
        source_crs = CRS.from_user_input(
            crs_uri
        )

        target_crs = CRS.from_user_input(
            CRS84_URI
        )

        if source_crs != target_crs:
            transformer = Transformer.from_crs(
                source_crs,
                target_crs,
                always_xy=True,
            )

            geometry = shapely_transform(
                transformer.transform,
                geometry,
            )

    except (
        CRSError,
        ProjError,
        GEOSException,
    ) as exc:
        raise ValueError(
            (
                "Could not transform the WKT "
                f"geometry to CRS84: {exc}"
            )
        ) from exc

    if geometry.is_empty:
        return None

    min_x, min_y, max_x, max_y = (
        geometry.bounds
    )

    return [
        float(min_x),
        float(min_y),
        float(max_x),
        float(max_y),
    ]

def wkt_crs_uri(
    geometry_value,
) -> str:
    if geometry_value is None:
        return ""

    raw_value = str(
        geometry_value
    ).strip()

    if not raw_value:
        return ""

    if raw_value.startswith("<"):
        closing_bracket = raw_value.find(">")

        if closing_bracket == -1:
            raise ValueError(
                "Invalid CRS prefix in WKT geometry."
            )

        return raw_value[
            1:closing_bracket
        ].strip()
    
    if (
        isinstance(geometry_value, Literal)
        and geometry_value.datatype
        == GEO_WKT_LITERAL
    ):
        return CRS84_URI
    
    return ""


def epsg_code_from_crs_uri(
    crs_uri: str,
) -> int | None:
    value = (
        crs_uri or ""
    ).strip().rstrip("/")

    if not value:
        return None

    upper_value = value.upper()

    if "/DEF/CRS/EPSG/" in upper_value:
        candidate = value.split("/")[-1]

        if candidate.isdigit():
            return int(candidate)

    if upper_value.startswith(
        "URN:OGC:DEF:CRS:EPSG:"
    ):
        candidate = value.split(":")[-1]

        if candidate.isdigit():
            return int(candidate)

    if upper_value.startswith("EPSG:"):
        candidate = value.split(":")[-1]

        if candidate.isdigit():
            return int(candidate)

    return None


def read_single_dcat_dataset(
    xml_bytes: bytes,
) -> tuple[Graph, Any]:
    graph = Graph()

    graph.parse(
        data=xml_bytes,
        format="xml",
    )

    datasets = list(
        dict.fromkeys(
            graph.subjects(
                RDF.type,
                DCAT.Dataset,
            )
        )
    )

    if not datasets:
        raise ValueError(
            "No dcat:Dataset was found."
        )

    if len(datasets) > 1:
        raise ValueError(
            "Exactly one dcat:Dataset is currently supported."
        )

    return graph, datasets[0]

def extract_dcat_model(
    graph: Graph,
    dataset,
) -> dict:
    dataset_uri = (
        str(dataset)
        if isinstance(dataset, URIRef)
        else ""
    )

    identifiers = values(
        graph,
        dataset,
        DCTERMS.identifier,
    )

    identifiers.extend(
        values(
            graph,
            dataset,
            ADMS_IDENTIFIER,
        )
    )

    if dataset_uri:
        identifiers.append(dataset_uri)

    identifiers = unique_values(identifiers)

    keywords = values(
        graph,
        dataset,
        DCAT.keyword,
    )

    themes = values(
        graph,
        dataset,
        DCAT.theme,
    )


    access_rights = [
        resource_label(graph, resource)
        for resource in graph.objects(
            dataset,
            DCTERMS.accessRights,
        )
    ]

    licenses = [
        resource_label(graph, resource)
        for resource in graph.objects(
            dataset,
            DCTERMS.license,
        )
    ]

    publishers = [
        resource_label(graph, resource)
        for resource in graph.objects(
            dataset,
            DCTERMS.publisher,
        )
    ]

    temporal_extents = []

    for period in graph.objects(
        dataset,
        DCTERMS.temporal,
    ):
        begin = first(
            graph,
            period,
            SCHEMA_START_DATE,
        )

        end = first(
            graph,
            period,
            SCHEMA_END_DATE,
        )

        if begin or end:
            temporal_extents.append({
                "begin": begin,
                "end": end,
                "resolution": "",
            })

    geometry_literal = None

    for location in graph.objects(
        dataset,
        DCTERMS.spatial,
    ):
        for geometry_value in graph.objects(
            location,
            LOCN_GEOMETRY,
        ):
            if isinstance(
                geometry_value,
                Literal,
            ):
                geometry_literal = geometry_value
                break

        if geometry_literal is not None:
            break

    geometry = (
        str(geometry_literal).strip()
        if geometry_literal is not None
        else ""
    )

    parsed_geometry = parse_wkt_geometry(geometry)   
    geometry_type = iso_geometry_type(parsed_geometry)
    crs_uri = wkt_crs_uri(geometry_literal)
    crs_code = epsg_code_from_crs_uri(crs_uri)
    distributions = []

    for distribution_index, distribution in enumerate(
        dict.fromkeys(
            graph.objects(
                dataset,
                DCAT.distribution,
            )
        ),
        start=1,
    ):
        distribution_licenses = [
            resource_label(graph, resource)
            for resource in graph.objects(
                distribution,
                DCTERMS.license,
            )
        ]

        licenses.extend(distribution_licenses)

        distribution_title = first(
            graph,
            distribution,
            DCTERMS.title,
        )

        distribution_format = first(
            graph,
            distribution,
            DCTERMS.format,
        )

        if not distribution_format:
            distribution_format = first(
                graph,
                distribution,
                DCAT.mediaType,
            )

        byte_size = first(
            graph,
            distribution,
            DCAT.byteSize,
        )

        for url_index, url in enumerate(
            values(
                graph,
                distribution,
                DCAT.downloadURL,
            ),
            start=1,
        ):
            distributions.append({
                "key": (
                    f"distribution-"
                    f"{distribution_index}-"
                    f"download-"
                    f"{url_index}"
                ),
                "url": url,
                "type": (
                    "WWW:DOWNLOAD-1.0-http--download"
                ),
                "function": "download",
                "name": distribution_title,
                "description": distribution_title,
                "format": distribution_format,
                "format_version": "",
                "byte_size": byte_size,
            })

        for url_index, url in enumerate(
            values(
                graph,
                distribution,
                DCAT.accessURL,
            ),
            start=1,
        ):
            distributions.append({
                "key": (
                    f"distribution-"
                    f"{distribution_index}-"
                    f"access-"
                    f"{url_index}"
                ),
                "url": url,
                "type": "WWW:LINK-1.0-http--link",
                "function": "information",
                "name": distribution_title,
                "description": distribution_title,
                "format": distribution_format,
                "format_version": "",
                "byte_size": byte_size,
            })

    provenance = [
        resource_label(graph, resource)
        for resource in graph.objects(
            dataset,
            DCTERMS.provenance,
        )
    ]

    primary_identifier = (
        identifiers[0]
        if identifiers
        else dataset_uri
    )

    return {
        "dataset_uri": dataset_uri,
        "primary_identifier": primary_identifier,
        "identifiers": identifiers,
        "title": first(
            graph,
            dataset,
            DCTERMS.title,
        ),
        "description": first(
            graph,
            dataset,
            DCTERMS.description,
        ),
        "issued": first(
            graph,
            dataset,
            DCTERMS.issued,
        ),
        "modified": first(
            graph,
            dataset,
            DCTERMS.modified,
        ),
        "language": first(
            graph,
            dataset,
            DCTERMS.language,
        ),
        "keywords": keywords,
        "themes": themes,
        "access_rights": unique_values(
            access_rights
        ),
        "licenses": unique_values(
            licenses
        ),
        "publishers": unique_values(
            publishers
        ),
        "temporal": temporal_extents,
        "bbox": geographic_wkt_bbox(geometry,crs_uri),
        "crs_uri": crs_uri,
        "crs_code": crs_code,
        "geometry_type": geometry_type,
        "distributions": distributions,
        "provenance": unique_values(
            provenance
        ),
    }