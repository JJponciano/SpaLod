#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
import sys
import xml.etree.ElementTree as ET
from dataclasses import asdict, dataclass, field
from pathlib import Path
from typing import Any

NS = {
    "gco": "http://www.isotc211.org/2005/gco",
    "gmd": "http://www.isotc211.org/2005/gmd",
    "gml": "http://www.opengis.net/gml/3.2",
    "gmx": "http://www.isotc211.org/2005/gmx",
    "srv": "http://www.isotc211.org/2005/srv",
}
XLINK_HREF = "{http://www.w3.org/1999/xlink}href"
XSI_SCHEMA_LOCATION = "{http://www.w3.org/2001/XMLSchema-instance}schemaLocation"

OPEN_FORMAT_HINTS = {
    "ascii",
    "csv",
    "geojson",
    "gml",
    "gpkg",
    "geopackage",
    "json",
    "json-ld",
    "rdf",
    "ttl",
    "xml",
    "xyz",
}
OPEN_LICENSE_PATTERNS = (
    "dl-de/by-2.0",
    "dl-de/by-2-0",
    "dl-de/zero",
    "cc-by",
    "cc0",
    "creativecommons.org",
    "datenlizenz deutschland",
    "odc-by",
    "odbl",
)
CONTEXT_IGNORE_PATTERNS = (
    "opengis.net/def/crs",
    "metadata-codelist/LimitationsOnPublicAccess",
    "govdata.de/dl-de",
    "creativecommons.org",
)
GENERIC_QUALITY_EXPLANATIONS = {
    "",
    "no explanation available",
    "see the referenced specification",
}
PROVENANCE_METHOD_HINTS = (
    "airborne laser",
    "laserscanning",
    "lidar",
    "survey",
    "acquisition",
    "processing",
    "orthophoto",
    "transform",
)
WEAK_FEATURE_ID_KEYS = {
    "gid",
    "id",
    "objektid",
    "fid",
}
URL_RE = re.compile(r"https?://[^\s)>\"]+")
YEAR_RE = re.compile(r"\b(19|20)\d{2}\b")


@dataclass
class OnlineResource:
    url: str
    protocol: str = ""
    function: str = ""
    name: str = ""
    description: str = ""


@dataclass
class OperationMetadata:
    name: str
    dcps: list[str] = field(default_factory=list)
    connect_points: list[str] = field(default_factory=list)


@dataclass
class QualityReport:
    report_type: str
    specification: str
    explanation: str
    passed: bool | None


@dataclass
class JsonEvidence:
    path: Path
    title: str = ""
    top_level_id: str = ""
    links: list[str] = field(default_factory=list)
    feature_count: int = 0
    feature_ids_present: bool = False
    strong_feature_identifier: bool = False
    id_like_properties: list[str] = field(default_factory=list)
    schema_present: bool = False


@dataclass
class RecordEvidence:
    source: Path
    title: str
    scope: str
    schema_location_present: bool
    distribution_formats: list[str]
    service_types: list[str]
    online_resources: list[OnlineResource]
    operations: list[OperationMetadata]
    legal_texts: list[str]
    legal_urls: list[str]
    contextual_urls: list[str]
    identifiers: list[str]
    reference_systems: list[str]
    quality_reports: list[QualityReport]
    lineage_statements: list[str]
    process_steps: int
    companion_json: JsonEvidence | None = None


@dataclass
class Decision:
    passed: bool
    reason: str


@dataclass
class AssessmentResult:
    source: str
    dataset: str
    scope: str
    classic_star: int
    extended_star: int
    star_range: str
    letter_code: str
    letters: dict[str, dict[str, Any]]

    def to_dict(self) -> dict[str, Any]:
        return asdict(self)


MANUSCRIPT_EXPECTED = {
    "3dd8b86e-e5d6-a9d6-4c1a-6fd19bb0bb64.xml": {"range": "3-6", "letter": "SAL"},
    "14418d25-fcd7-4a3f-99a9-e3059a2772af.xml": {"range": "5-7", "letter": "SPCLQ"},
    "4A9DCE2B-DCCA-4939-BA01-54364D11C46D.xml": {"range": "3-6", "letter": "SALI"},
    "BB6538CA-404E-46ED-B00C-C548495A1BF1.xml": {"range": "5-6", "letter": "SACI"},
}


def local_name(tag: str) -> str:
    return tag.rsplit("}", 1)[-1] if "}" in tag else tag


def squash(text: str) -> str:
    return " ".join(text.split())


def text_of(element: ET.Element | None) -> str:
    if element is None:
        return ""
    return squash(" ".join(chunk for chunk in element.itertext() if chunk))


def first_descendant_text(node: ET.Element, wanted: str) -> str:
    for child in node.iter():
        if local_name(child.tag) == wanted:
            text = text_of(child)
            if text:
                return text
    return ""


def descendant_texts(node: ET.Element, wanted: str) -> list[str]:
    texts: list[str] = []
    for child in node.iter():
        if local_name(child.tag) == wanted:
            text = text_of(child)
            if text:
                texts.append(text)
    return texts


def extract_urls(text: str) -> list[str]:
    return [url.rstrip(".,);") for url in URL_RE.findall(text)]


def normalize_url(value: str) -> str:
    return value.replace("\\/", "/").strip()


def looks_like_persistent_identifier(value: str) -> bool:
    value = value.strip()
    return value.startswith(("http://", "https://", "urn:"))


def parse_online_resources(root: ET.Element) -> list[OnlineResource]:
    resources: list[OnlineResource] = []
    for node in root.findall(".//gmd:CI_OnlineResource", NS):
        url = text_of(node.find("./gmd:linkage/gmd:URL", NS))
        protocol = text_of(node.find("./gmd:protocol", NS))
        name = text_of(node.find("./gmd:name", NS))
        description = text_of(node.find("./gmd:description", NS))
        function = ""
        function_node = node.find("./gmd:function/*", NS)
        if function_node is not None:
            function = function_node.attrib.get("codeListValue", "") or text_of(function_node)
        if url or protocol or name or description or function:
            resources.append(
                OnlineResource(
                    url=normalize_url(url),
                    protocol=squash(protocol),
                    function=squash(function),
                    name=squash(name),
                    description=squash(description),
                )
            )
    return resources


def parse_operations(root: ET.Element) -> list[OperationMetadata]:
    operations: list[OperationMetadata] = []
    for node in root.findall(".//srv:SV_OperationMetadata", NS):
        name = text_of(node.find("./srv:operationName", NS))
        dcps: list[str] = []
        for dcp in node.findall(".//srv:DCPList", NS):
            value = dcp.attrib.get("codeListValue", "") or text_of(dcp)
            if value:
                dcps.append(value)
        connect_points = [
            normalize_url(text_of(url))
            for url in node.findall(".//srv:connectPoint//gmd:URL", NS)
            if text_of(url)
        ]
        if name or dcps or connect_points:
            operations.append(OperationMetadata(name=squash(name), dcps=dcps, connect_points=connect_points))
    return operations


def parse_quality_reports(root: ET.Element) -> list[QualityReport]:
    reports: list[QualityReport] = []
    for node in root.findall(".//gmd:dataQualityInfo//gmd:report/*", NS):
        report_type = local_name(node.tag)
        explanation = first_descendant_text(node, "explanation")
        specification = first_descendant_text(node, "title")
        passed_value = first_descendant_text(node, "pass").lower()
        passed: bool | None = None
        if passed_value in {"true", "false"}:
            passed = passed_value == "true"
        reports.append(
            QualityReport(
                report_type=report_type,
                specification=squash(specification),
                explanation=squash(explanation),
                passed=passed,
            )
        )
    return reports


def parse_json_evidence(path: Path) -> JsonEvidence | None:
    if not path.exists():
        return None
    payload = json.loads(path.read_text(encoding="utf-8"))
    features = payload.get("features", []) if isinstance(payload, dict) else []
    links = [normalize_url(link.get("href", "")) for link in payload.get("links", []) if isinstance(link, dict)]
    feature_ids_present = any(isinstance(feature, dict) and feature.get("id") not in (None, "") for feature in features)
    id_like_properties: list[str] = []
    strong_feature_identifier = feature_ids_present
    for feature in features[:10]:
        if not isinstance(feature, dict):
            continue
        properties = feature.get("properties", {})
        if not isinstance(properties, dict):
            continue
        for key, value in properties.items():
            lowered = key.lower()
            if "schema" in lowered:
                continue
            if lowered in WEAK_FEATURE_ID_KEYS:
                id_like_properties.append(key)
                continue
            if lowered.endswith("id") or "identifier" in lowered or "uri" in lowered:
                id_like_properties.append(key)
                if isinstance(value, str) and (looks_like_persistent_identifier(value) or not value.isdigit()):
                    strong_feature_identifier = True
    schema_present = bool(payload.get("$schema"))
    for feature in features[:10]:
        if isinstance(feature, dict) and feature.get("$schema"):
            schema_present = True
    return JsonEvidence(
        path=path,
        title=squash(str(payload.get("title", ""))),
        top_level_id=squash(str(payload.get("id", ""))),
        links=links,
        feature_count=len(features),
        feature_ids_present=feature_ids_present,
        strong_feature_identifier=strong_feature_identifier,
        id_like_properties=sorted(set(id_like_properties)),
        schema_present=schema_present,
    )


def infer_dataset_label(title: str) -> str:
    lowered = title.lower()
    if "glascontainer" in lowered or "trier" in lowered:
        return "Municipal POI (Trier)"
    if "digitales geländemodell" in lowered:
        return "Thuringia DEM"
    if "verwaltungsgebiete" in lowered and "wfs" in lowered:
        return "Administrative Units (VG250)"
    if "administrative units" in lowered:
        return "INSPIRE DLM"
    return title


def collect_record_evidence(xml_path: Path) -> RecordEvidence:
    root = ET.parse(xml_path).getroot()
    scope = "service" if root.findall(".//srv:SV_ServiceIdentification", NS) else "dataset"
    title = text_of(root.find(".//gmd:identificationInfo//gmd:citation//gmd:title", NS))
    distribution_formats: list[str] = []
    for fmt in root.findall(".//gmd:distributionFormat//gmd:MD_Format", NS):
        for part in (text_of(fmt.find("./gmd:name", NS)), text_of(fmt.find("./gmd:version", NS))):
            if part:
                distribution_formats.append(part)
    service_types: list[str] = []
    for node in root.findall(".//srv:serviceType", NS):
        value = first_descendant_text(node, "LocalName")
        if value:
            service_types.append(value)
    for node in root.findall(".//srv:serviceTypeVersion", NS):
        value = text_of(node)
        if value:
            service_types.append(value)
    legal_texts: list[str] = []
    legal_urls: list[str] = []
    for node in root.findall(".//gmd:resourceConstraints//gmd:otherConstraints", NS):
        text = text_of(node)
        if text:
            legal_texts.append(text)
            legal_urls.extend(extract_urls(text))
        for child in node.iter():
            href = child.attrib.get(XLINK_HREF)
            if href:
                legal_urls.append(normalize_url(href))
    contextual_urls: list[str] = []
    for node in root.iter():
        if local_name(node.tag) != "Anchor":
            continue
        href = node.attrib.get(XLINK_HREF, "")
        href = normalize_url(href)
        if href:
            contextual_urls.append(href)
    identifiers: list[str] = []
    for node in root.findall(".//gmd:identificationInfo//gmd:identifier", NS):
        code = first_descendant_text(node, "code")
        if code:
            identifiers.append(code)
    for node in root.findall(".//srv:identifier", NS):
        value = text_of(node)
        if value:
            identifiers.append(value)
    for node in root.findall(".//srv:operatesOn", NS):
        for attr in (node.attrib.get(XLINK_HREF, ""), node.attrib.get("uuidref", "")):
            if attr:
                identifiers.append(normalize_url(attr))
    for node in root.findall(".//*[@uuid]", NS):
        value = node.attrib.get("uuid", "")
        if value:
            identifiers.append(normalize_url(value))
    data_set_uri = root.find(".//gmd:dataSetURI", NS)
    if data_set_uri is not None:
        value = text_of(data_set_uri)
        if value:
            identifiers.append(value)
    reference_systems: list[str] = []
    for node in root.findall(".//gmd:referenceSystemIdentifier", NS):
        code = first_descendant_text(node, "code")
        if code:
            reference_systems.append(code)
    lineage_statements = descendant_texts(root, "statement")
    process_steps = sum(1 for node in root.iter() if local_name(node.tag) == "LI_ProcessStep")
    geojson_path = xml_path.with_name(f"{xml_path.stem.replace('_metadata', '')}.geojson")
    if geojson_path.exists():
        companion_json = parse_json_evidence(geojson_path)
    else:
        companion_json = parse_json_evidence(xml_path.with_name(f"data_{xml_path.stem}.json"))
    return RecordEvidence(
        source=xml_path,
        title=squash(title),
        scope=scope,
        schema_location_present=bool(root.attrib.get(XSI_SCHEMA_LOCATION)),
        distribution_formats=sorted(set(squash(value) for value in distribution_formats if value)),
        service_types=sorted(set(squash(value) for value in service_types if value)),
        online_resources=parse_online_resources(root),
        operations=parse_operations(root),
        legal_texts=legal_texts,
        legal_urls=sorted(set(normalize_url(url) for url in legal_urls if url)),
        contextual_urls=sorted(set(contextual_urls)),
        identifiers=sorted(set(squash(value) for value in identifiers if value)),
        reference_systems=sorted(set(squash(value) for value in reference_systems if value)),
        quality_reports=parse_quality_reports(root),
        lineage_statements=[squash(value) for value in lineage_statements if value],
        process_steps=process_steps,
        companion_json=companion_json,
    )


def format_hints(record: RecordEvidence) -> list[str]:
    hints = list(record.distribution_formats) + list(record.service_types)
    if record.companion_json:
        hints.append("GeoJSON companion resource")
    return [hint.lower() for hint in hints]


def assess_structure(record: RecordEvidence) -> Decision:
    hints = format_hints(record)
    open_formats = [hint for hint in hints if any(token in hint for token in OPEN_FORMAT_HINTS)]
    has_schema = False
    schema_reason = ""
    operation_names = {operation.name.lower() for operation in record.operations}
    if "describefeaturetype" in operation_names:
        has_schema = True
        schema_reason = "DescribeFeatureType is exposed"
    elif "getapidescription" in operation_names:
        has_schema = True
        schema_reason = "OGC API description is exposed"
    elif record.companion_json and record.companion_json.schema_present:
        has_schema = True
        schema_reason = f"{record.companion_json.path.name} declares a schema"
    else:
        for report in record.quality_reports:
            if "data specification" in report.specification.lower():
                has_schema = True
                schema_reason = f"quality metadata references {report.specification}"
                break
    if not has_schema and record.schema_location_present and open_formats:
        has_schema = True
        schema_reason = "the ISO record publishes a schema location"
    if open_formats and has_schema:
        format_reason = open_formats[0]
        return Decision(True, f"non-proprietary format detected from '{format_reason}' and {schema_reason}")
    if not open_formats:
        return Decision(False, "no non-proprietary distribution format could be inferred")
    return Decision(False, f"non-proprietary format detected but no schema evidence was found")


def assess_provenance(record: RecordEvidence) -> Decision:
    if not record.lineage_statements:
        return Decision(False, "no lineage statement is present")
    if record.process_steps:
        return Decision(True, f"{record.process_steps} explicit processing step(s) are present")
    for statement in record.lineage_statements:
        lowered = statement.lower()
        has_year = bool(YEAR_RE.search(statement))
        has_method = any(token in lowered for token in PROVENANCE_METHOD_HINTS)
        if has_year and has_method:
            return Decision(True, f"lineage statement captures method and time: '{statement}'")
    return Decision(False, "lineage exists but does not define a machine-actionable process history")


def assess_access(record: RecordEvidence) -> Decision:
    positive_hits: list[str] = []
    for resource in record.online_resources:
        lowered_url = resource.url.lower()
        lowered_protocol = resource.protocol.lower()
        lowered_function = resource.function.lower()
        if "ogc:api:features" in lowered_protocol or "/collections/" in lowered_url:
            positive_hits.append(resource.url or resource.protocol)
        elif "service=wfs" in lowered_url or "request=getfeature" in lowered_url:
            positive_hits.append(resource.url)
        elif "atom" in lowered_protocol or "atom" in lowered_url:
            positive_hits.append(resource.url or resource.protocol)
        elif lowered_function == "download" and lowered_url.endswith(
            (".zip", ".csv", ".gml", ".json", ".geojson", ".gpkg", ".tif", ".tiff", ".asc", ".xyz")
        ):
            positive_hits.append(resource.url)
    for operation in record.operations:
        lowered = operation.name.lower()
        if lowered in {"getfeature", "items"}:
            positive_hits.extend(operation.connect_points)
    if record.companion_json and record.companion_json.links:
        positive_hits.append(record.companion_json.links[0])
    if positive_hits:
        return Decision(True, f"machine-actionable retrieval is exposed through {positive_hits[0]}")
    return Decision(False, "only informational links or non-download endpoints were found")


def assess_connections(record: RecordEvidence) -> Decision:
    if record.scope != "dataset":
        return Decision(False, "the record is service-scoped and does not expose dataset-level contextual links")
    contextual = [
        url
        for url in record.contextual_urls
        if url.startswith(("http://", "https://"))
        and not any(pattern in url for pattern in CONTEXT_IGNORE_PATTERNS)
    ]
    if contextual:
        return Decision(True, f"external contextual links are present, e.g. {contextual[0]}")
    return Decision(False, "no resolvable dataset-level contextual links were found")


def assess_license(record: RecordEvidence) -> Decision:
    corpus = " ".join(record.legal_texts + record.legal_urls).lower()
    open_hit = next((pattern for pattern in OPEN_LICENSE_PATTERNS if pattern in corpus), "")
    if open_hit:
        return Decision(True, f"an explicit open-use license is present ({open_hit})")
    if "copyright" in corpus or "urheberrechtlich" in corpus:
        return Decision(False, "rights are described, but only through restrictive/copyright-oriented legal text")
    return Decision(False, "no explicit reusable license URL or open-license marker was found")


def assess_identifiers(record: RecordEvidence) -> Decision:
    has_dataset_identifier = any(looks_like_persistent_identifier(identifier) for identifier in record.identifiers)
    if not has_dataset_identifier:
        return Decision(False, "no persistent dataset identifier was found")
    if not record.reference_systems:
        return Decision(False, "no coordinate reference system is declared")
    if record.companion_json:
        if record.companion_json.feature_ids_present or record.companion_json.strong_feature_identifier:
            return Decision(
                True,
                f"{record.companion_json.path.name} exposes feature identifiers and CRS metadata is present",
            )
        return Decision(
            False,
            f"{record.companion_json.path.name} lacks stable feature identifiers (only {', '.join(record.companion_json.id_like_properties) or 'weak ids'})",
        )
    lowered_formats = " ".join(format_hints(record))
    wfs_like = "wfs" in lowered_formats or any(operation.name.lower() == "getfeature" for operation in record.operations)
    gml_like = "gml" in lowered_formats
    if wfs_like or gml_like:
        return Decision(True, "persistent dataset identifiers and CRS are present, and the resource is published through GML/WFS")
    return Decision(False, "dataset identifiers exist, but there is no evidence of stable feature-level identifiers")


def assess_quality(record: RecordEvidence) -> Decision:
    if not record.quality_reports:
        return Decision(False, "no quality reports are present")
    for report in record.quality_reports:
        explanation = report.explanation.lower()
        specification = report.specification.lower()
        if report.report_type != "DQ_DomainConsistency":
            return Decision(True, f"{report.report_type} provides a concrete quality measure")
        if explanation not in GENERIC_QUALITY_EXPLANATIONS:
            return Decision(True, f"quality report contains a specific explanation: '{report.explanation}'")
        if not any(token in specification for token in ("regulation", "verordnung", "guideline", "specification", "opengis")):
            return Decision(True, f"quality report references a dataset-specific specification: '{report.specification}'")
    return Decision(False, "quality metadata is limited to generic conformance checks")


def assess_letters(record: RecordEvidence) -> dict[str, Decision]:
    return {
        "S": assess_structure(record),
        "P": assess_provenance(record),
        "A": assess_access(record),
        "C": assess_connections(record),
        "L": assess_license(record),
        "I": assess_identifiers(record),
        "Q": assess_quality(record),
    }


def classic_star(record: RecordEvidence, letters: dict[str, Decision]) -> int:
    score = 0
    if record.online_resources or (record.companion_json and record.companion_json.links):
        score = 1
    if assess_structure(record).passed:
        score = 2
    # The manuscript's STAR comparison is intentionally operational rather than
    # strictly faithful to Berners-Lee's original license-first wording.
    # In the published case studies, a non-proprietary format is enough to reach
    # the "3-star" baseline even when LETTER-L fails.
    if score >= 2:
        score = 3
    if score >= 3 and letters["I"].passed and record.scope == "dataset":
        score = 4
    if score >= 3 and letters["C"].passed and record.scope == "dataset":
        score = 5
    return score


def extended_star(classic: int, letters: dict[str, Decision]) -> int:
    score = classic
    if classic >= 3 and letters["S"].passed:
        score = max(score, 6)
    if score >= 6 and letters["Q"].passed:
        score = max(score, 7)
    return score


def letter_code(letters: dict[str, Decision]) -> str:
    order = "SPACLIQ"
    return "".join(letter for letter in order if letters[letter].passed)


def assess_xml_file(xml_path: Path) -> AssessmentResult:
    record = collect_record_evidence(xml_path)
    letters = assess_letters(record)
    classic = classic_star(record, letters)
    extended = extended_star(classic, letters)
    return AssessmentResult(
        source=str(xml_path.resolve()),
        dataset=infer_dataset_label(record.title),
        scope=record.scope,
        classic_star=classic,
        extended_star=extended,
        star_range=f"{classic}-{extended}",
        letter_code=letter_code(letters),
        letters={name: {"passed": decision.passed, "reason": decision.reason} for name, decision in letters.items()},
    )


def discover_xml_files(paths: list[Path]) -> list[Path]:
    xml_files: list[Path] = []
    for path in paths:
        if path.is_dir():
            xml_files.extend(sorted(candidate for candidate in path.iterdir() if candidate.suffix.lower() == ".xml"))
        elif path.suffix.lower() == ".xml":
            xml_files.append(path)
    return sorted(dict.fromkeys(xml_files))


def render_table(results: list[AssessmentResult]) -> str:
    rows = [
        ["Dataset", "Scope", "Stars", "LETTER"],
        *[[result.dataset, result.scope, result.star_range, result.letter_code] for result in results],
    ]
    widths = [max(len(str(row[index])) for row in rows) for index in range(len(rows[0]))]
    rendered: list[str] = []
    for index, row in enumerate(rows):
        rendered.append("  ".join(str(cell).ljust(widths[col]) for col, cell in enumerate(row)))
        if index == 0:
            rendered.append("  ".join("-" * widths[col] for col in range(len(widths))))
    return "\n".join(rendered)


def assert_manuscript_results(results: list[AssessmentResult]) -> int:
    by_name = {Path(result.source).name: result for result in results}
    errors: list[str] = []
    for name, expected in MANUSCRIPT_EXPECTED.items():
        result = by_name.get(name)
        if result is None:
            errors.append(f"missing expected record {name}")
            continue
        if result.star_range != expected["range"]:
            errors.append(f"{name}: expected star range {expected['range']}, got {result.star_range}")
        if result.letter_code != expected["letter"]:
            errors.append(f"{name}: expected LETTER {expected['letter']}, got {result.letter_code}")
    if errors:
        for error in errors:
            print(error, file=sys.stderr)
        return 1
    return 0


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Assess ISO 19139 metadata records with the manuscript's LETTER and STAR rubrics."
    )
    parser.add_argument(
        "paths",
        nargs="*",
        default=["."],
        help="XML files or directories to scan. Companion data_<stem>.json files are loaded automatically.",
    )
    parser.add_argument("--json", action="store_true", help="Emit machine-readable JSON instead of a table.")
    parser.add_argument("--verbose", action="store_true", help="Include detailed letter rationales in table mode.")
    parser.add_argument(
        "--assert-manuscript",
        action="store_true",
        help="Exit non-zero if the bundled case studies do not reproduce the manuscript results.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    xml_files = discover_xml_files([Path(path) for path in args.paths])
    if not xml_files:
        print("No XML files found.", file=sys.stderr)
        return 1
    results = [assess_xml_file(path) for path in xml_files]
    if args.json:
        print(json.dumps([result.to_dict() for result in results], indent=2))
    else:
        print(render_table(results))
        if args.verbose:
            for result in results:
                print()
                print(f"{result.dataset} [{Path(result.source).name}]")
                for letter, decision in result.letters.items():
                    status = "PASS" if decision["passed"] else "FAIL"
                    print(f"  {letter}: {status} - {decision['reason']}")
    if args.assert_manuscript:
        return assert_manuscript_results(results)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
