from __future__ import annotations
from datetime import date

ISO_GEOGRAPHIC_BBOX_EPSG = 4326
PYGEOMETA_PLACEHOLDER_BBOX = [
    0.0,
    0.0,
    0.0,
    0.0,
]

def empty_contact(
    organization: str,
    url: str,
) -> dict:
    return {
        "organization": organization,
        "url": url,
        "individualname": "",
        "positionname": "",
        "phone": "",
        "fax": "",
        "address": "",
        "city": "",
        "administrativearea": "",
        "postalcode": "",
        "country": "",
        "email": "",
        "hoursofservice": "",
        "contactinstructions": "",
    }


def dcat_model_to_mcf(
    model: dict,
) -> dict:
    dates = {}

    if model["issued"]:
        dates["creation"] = model["issued"]

    if model["modified"]:
        dates["revision"] = model["modified"]

    keyword_groups = {}

    if model["keywords"]:
        keyword_groups["keywords"] = {
            "keywords": model["keywords"],
            "keywords_type": "",
            "vocabulary": {
                "name": "",
                "url": "",
            },
        }

    if model["themes"]:
        keyword_groups["themes"] = {
            "keywords": model["themes"],
            "keywords_type": "theme",
            "vocabulary": {
                "name": "DCAT themes",
                "url": (
                    "http://www.w3.org/ns/dcat#theme"
                ),
            },
        }

    spatial_extents = [{
    "bbox": (
        model["bbox"]
        if model["bbox"]
        else PYGEOMETA_PLACEHOLDER_BBOX.copy()
    ),
    "crs": ISO_GEOGRAPHIC_BBOX_EPSG,
    "description": "",
    }]

    contacts = {}

    if model["publishers"]:
        contacts["publisher"] = empty_contact(
            model["publishers"][0],
            "",
        )

    distributions = {}

    for distribution in model["distributions"]:
        distributions[distribution["key"]] = {
            "url": distribution["url"],
            "type": distribution["type"],
            "rel": "",
            "name": distribution["name"],
            "description": (
                distribution["description"]
            ),
            "function": (
                distribution["function"]
            ),
            "format": distribution["format"],
            "format_version": (
                distribution["format_version"]
            ),
        }

    metadata_identifier = (
        f"{model['primary_identifier']}-evaluation-iso"
        if model["primary_identifier"]
        else "generated-evaluation-iso"
    )

    return {
        "mcf": {
            "version": 1.0,
        },
        "metadata": {
            "identifier": metadata_identifier,
            "language": model["language"] or "und",
            "language_alternate": "",
            "charset": "utf8",
            "parentidentifier": "",
            "hierarchylevel": "dataset",
            "datestamp": date.today().isoformat(),
            "dataseturi": model["dataset_uri"],
        },
        "spatial": {
            "datatype": "vector",
            "geomtype": (
                model["geometry_type"]
                or "complex"
            ),
        },
        "identification": {
            "title": (
                model["title"]
                or model["primary_identifier"]
            ),
            "abstract": model["description"],
            "edition": "",
            "dates": dates,
            "status": "",
            "maintenancefrequency": "unknown",
            "browsegraphic": "",
            "keywords": keyword_groups,
            "accessconstraints": (
                "otherRestrictions"
            ),
            "language": model["language"] or "und",
            "charset": "utf8",
            "topiccategory": [],
            "extents": {
                "spatial": spatial_extents,
                "temporal": model["temporal"],
            },
            "url": "",
        },
        "contact": contacts,
        "distribution": distributions,
        "content_info": {},
        "dataquality": (
            {
                "scope": {
                    "level": "dataset",
                },
                "lineage": {
                    "statement": " | ".join(
                        model["provenance"]
                    ),
                },
            }
            if model["provenance"]
            else {}
        ),
    }