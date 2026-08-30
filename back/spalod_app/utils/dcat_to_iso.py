from __future__ import annotations

from lxml import etree
from pygeometa.schemas.iso19139 import (
    ISO19139OutputSchema,
)

from .dcat_reader import (
    extract_dcat_model,
    read_single_dcat_dataset,
)
from .dcat_to_mcf import dcat_model_to_mcf


GMD = "http://www.isotc211.org/2005/gmd"
GCO = "http://www.isotc211.org/2005/gco"
GMX = "http://www.isotc211.org/2005/gmx"
XLINK = "http://www.w3.org/1999/xlink"

NS = {
    "gmd": GMD,
    "gco": GCO,
    "gmx": GMX,
    "xlink": XLINK,
}


def qname(
    namespace: str,
    name: str,
) -> str:
    return f"{{{namespace}}}{name}"


def add_identifiers(
    root,
    identifiers: list[str],
) -> None:
    citation = root.find(
        (
            ".//gmd:identificationInfo"
            "//gmd:citation/gmd:CI_Citation"
        ),
        namespaces=NS,
    )

    if citation is None:
        return

    for identifier in identifiers:
        identifier_wrapper = etree.SubElement(
            citation,
            qname(GMD, "identifier"),
        )

        md_identifier = etree.SubElement(
            identifier_wrapper,
            qname(GMD, "MD_Identifier"),
        )

        code_wrapper = etree.SubElement(
            md_identifier,
            qname(GMD, "code"),
        )

        etree.SubElement(
            code_wrapper,
            qname(GCO, "CharacterString"),
        ).text = identifier


def add_legal_constraints(
    root,
    access_rights: list[str],
    licenses: list[str],
) -> None:
    legal = root.find(
        (
            ".//gmd:identificationInfo"
            "//gmd:MD_LegalConstraints"
        ),
        namespaces=NS,
    )

    if legal is None:
        return

    for access_right in access_rights:
        wrapper = etree.SubElement(
            legal,
            qname(GMD, "otherConstraints"),
        )

        etree.SubElement(
            wrapper,
            qname(GCO, "CharacterString"),
        ).text = access_right

    for license_value in licenses:
        wrapper = etree.SubElement(
            legal,
            qname(GMD, "otherConstraints"),
        )

        if license_value.startswith(
            ("http://", "https://")
        ):
            anchor = etree.SubElement(
                wrapper,
                qname(GMX, "Anchor"),
                {
                    qname(
                        XLINK,
                        "href",
                    ): license_value,
                },
            )

            anchor.text = license_value

        else:
            etree.SubElement(
                wrapper,
                qname(GCO, "CharacterString"),
            ).text = license_value


def normalize_reference_system(
    root,
    model: dict,
) -> None:
    reference_systems = root.xpath(
        "./gmd:referenceSystemInfo",
        namespaces=NS,
    )

    crs_uri = model.get("crs_uri", "")

    if not crs_uri:
        for reference_system in reference_systems:
            reference_system.getparent().remove(
                reference_system
            )

        return

    for reference_system in reference_systems:
        code_element = reference_system.find(
            (
                ".//gmd:RS_Identifier/"
                "gmd:code/gco:CharacterString"
            ),
            namespaces=NS,
        )

        if code_element is not None:
            code_element.text = crs_uri

        # pygeometa always inserts EPSG authority details.
        # They are not correct for every CRS, such as CRS84.
        for element in reference_system.xpath(
            (
                ".//gmd:RS_Identifier/gmd:authority"
                " | "
                ".//gmd:RS_Identifier/gmd:version"
            ),
            namespaces=NS,
        ):
            element.getparent().remove(element)

def remove_placeholder_elements(
    root,
    model: dict,
) -> None:
    if not model["language"]:
        for element in root.xpath(
            "./gmd:language",
            namespaces=NS,
        ):
            element.getparent().remove(element)

        for element in root.xpath(
            (
                ".//gmd:identificationInfo"
                "//gmd:MD_DataIdentification/gmd:language"
            ),
            namespaces=NS,
        ):
            element.getparent().remove(element)
            
    if not model["bbox"]:
        # Remove elements that specifically describe
        # the spatial representation.
        spatial_xpaths = (
            "./gmd:spatialRepresentationInfo",
            "./gmd:referenceSystemInfo",
            (
                ".//gmd:identificationInfo"
                "//gmd:spatialRepresentationType"
            ),
        )

        for xpath in spatial_xpaths:
            for element in root.xpath(
                xpath,
                namespaces=NS,
            ):
                element.getparent().remove(
                    element
                )

        for geographic_element in root.xpath(
            (
                ".//gmd:identificationInfo"
                "//gmd:EX_Extent/"
                "gmd:geographicElement"
            ),
            namespaces=NS,
        ):
            geographic_element.getparent().remove(
                geographic_element
            )

        for extent_wrapper in root.xpath(
            (
                ".//gmd:identificationInfo"
                "//gmd:extent"
            ),
            namespaces=NS,
        ):
            ex_extent = extent_wrapper.find(
                "gmd:EX_Extent",
                namespaces=NS,
            )

            if (
                ex_extent is None
                or len(ex_extent) == 0
            ):
                extent_wrapper.getparent().remove(
                    extent_wrapper
                )

    if not model["distributions"]:
        for element in root.xpath(
            "./gmd:distributionInfo",
            namespaces=NS,
        ):
            element.getparent().remove(
                element
            )

    if (
        not model["access_rights"]
        and not model["licenses"]
    ):
        for element in root.xpath(
            (
                ".//gmd:identificationInfo"
                "//gmd:resourceConstraints"
            ),
            namespaces=NS,
        ):
            element.getparent().remove(
                element
            )

    for element in root.xpath(
        (
            ".//gmd:identificationInfo"
            "//gmd:status"
        ),
        namespaces=NS,
    ):
        element.getparent().remove(
            element
        )

    for xpath in (
        (
            ".//gmd:identificationInfo"
            "//gmd:resourceMaintenance"
        ),
        "./gmd:metadataMaintenance",
    ):
        for element in root.xpath(
            xpath,
            namespaces=NS,
        ):
            element.getparent().remove(
                element
            )

    for type_wrapper in root.xpath(
        (
            ".//gmd:MD_Keywords/gmd:type"
            "[gmd:MD_KeywordTypeCode"
            "/@codeListValue='']"
        ),
        namespaces=NS,
    ):
        type_wrapper.getparent().remove(
            type_wrapper
        )


def remove_duplicate_formats(root) -> None:
    seen = set()

    for format_wrapper in root.xpath(
        (
            "./gmd:distributionInfo"
            "//gmd:distributionFormat"
        ),
        namespaces=NS,
    ):
        format_name = "".join(
            format_wrapper.itertext()
        ).strip()

        if not format_name:
            continue

        if format_name in seen:
            format_wrapper.getparent().remove(
                format_wrapper
            )
        else:
            seen.add(format_name)

def normalize_format_names(root) -> None:
    for name_element in root.xpath(
        (
            "./gmd:distributionInfo"
            "//gmd:MD_Format/gmd:name"
        ),
        namespaces=NS,
    ):
        if len(name_element):
            continue

        value = (
            name_element.text or ""
        ).strip()

        if not value:
            continue

        name_element.text = None

        etree.SubElement(
            name_element,
            qname(
                GCO,
                "CharacterString",
            ),
        ).text = value


def convert_dcat_to_iso(
    xml_bytes: bytes,
) -> bytes:
    graph, dataset = read_single_dcat_dataset(
        xml_bytes
    )

    model = extract_dcat_model(
        graph,
        dataset,
    )

    mcf = dcat_model_to_mcf(model)

    iso_bytes = ISO19139OutputSchema().write(
        mcf
    )

    root = etree.fromstring(iso_bytes)
    normalize_reference_system(root,model)

    remove_placeholder_elements(
        root,
        model,
    )

    remove_duplicate_formats(root)
    normalize_format_names(root)

    add_identifiers(
        root,
        model["identifiers"],
    )

    add_legal_constraints(
        root,
        model["access_rights"],
        model["licenses"],
    )

    return etree.tostring(
        root,
        encoding="UTF-8",
        xml_declaration=True,
        pretty_print=True,
    )