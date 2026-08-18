from lxml import etree
from rdflib import Graph, URIRef
from rdflib.namespace import DCAT, DCTERMS, FOAF, RDF


SKIP_AUTOFILL_FIELDS = {"catalog", "title", "publisher", "type"}

ISO_NS_BYTES = (b"http://www.isotc211.org/2005/gmd", b"http://www.isotc211.org/2005/gco", b"http://www.isotc211.org/2005/srv")
DCAT_INDICATORS = (b"http://www.w3.org/ns/dcat#", b"<dcat:catalog", b"<dcat:dataset")


MAINTAINER_ROLES = {"custodian", "owner"}
CREATOR_ROLES = {"author", "originator", "principalinvestigator"}
CONTRIBUTOR_ROLES = {"processor", "editor", "coauthor", "mediator"}
ORIGINATOR_ROLES = {"originator"}



def detect_format(xml_bytes):
    lowered = xml_bytes.lower()
    if sum(ns in lowered for ns in ISO_NS_BYTES) >= 2:
        return "iso"
    if any(ind in lowered for ind in DCAT_INDICATORS):
        return "dcat"
    return "unknown"


def _join_unique(items, sep=", "):
    seen = set()
    out = []
    for item in items:
        s = (str(item).strip() if item else "")
        if s and s not in seen:
            seen.add(s)
            out.append(s)
    return sep.join(out)



ISO_NS = {
    "gmd": "http://www.isotc211.org/2005/gmd",
    "gco": "http://www.isotc211.org/2005/gco",
    "gmx": "http://www.isotc211.org/2005/gmx",
    "srv": "http://www.isotc211.org/2005/srv",
    "xlink": "http://www.w3.org/1999/xlink",
}


def _xp(node, xpath):
    return node.xpath(xpath, namespaces=ISO_NS)


def _xp_first(node, xpath):
    matches = _xp(node, xpath)
    return matches[0] if matches else None


def _node_text(node):
    if node is None:
        return ""
    if isinstance(node, str):
        return node.strip()
    return ("".join(node.itertext()) or "").strip()


def _format_party_from_xml(party):
    """Pull CharacterString + URL values from a CI_ResponsibleParty element."""
    values = _xp(party, ".//gco:CharacterString | .//gmd:URL")
    return _join_unique(_node_text(v) for v in values)


def parse_iso(xml_bytes):
    tree = etree.fromstring(xml_bytes)
    result = {}

    # description
    abstract_node = _xp_first(tree, "//gmd:abstract//gco:CharacterString")
    if abstract_node is not None:
        val = _node_text(abstract_node)
        if val:
            result["description"] = val

    # distribution 
    distribution_urls = []
    for url_node in _xp(tree, "//gmd:distributionInfo//gmd:CI_OnlineResource//gmd:URL"):
        u = _node_text(url_node)
        if u and u.lower().startswith(("http://", "https://")):
            if u and u.lower().startswith(("http://", "https://")):
                distribution_urls.append(u)

    if distribution_urls:
        result["distribution"] = distribution_urls

    # keywords + theme split by type
    keywords = []
    themes = []
    for group in _xp(tree, "//gmd:descriptiveKeywords/gmd:MD_Keywords"):
        type_codes = _xp(group, ".//gmd:MD_KeywordTypeCode/@codeListValue")
        is_theme = any((t or "").lower() == "theme" for t in type_codes)
        for kw in _xp(group, ".//gmd:keyword"):
            anchors = _xp(kw, ".//gmx:Anchor")
            char_strings = _xp(kw, ".//gco:CharacterString")
            if is_theme:
                for a in anchors:
                    val = _node_text(a)
                    if val:
                        themes.append(val)
                if not anchors:
                    for cs in char_strings:
                        val = _node_text(cs)
                        if val:
                            themes.append(val)
            else:
                for cs in char_strings:
                    val = _node_text(cs)
                    if val:
                        keywords.append(val)
    if keywords:
        result["keywords"] = _join_unique(keywords)
    if themes:
        result["theme"] = _join_unique(themes)

    # contactPoint (all CI_ResponsibleParty inside pointOfContact)
    contacts = []
    for party_wrapper in _xp(tree, "//gmd:pointOfContact"):
        formatted = _format_party_from_xml(party_wrapper)
        if formatted:
            contacts.append(formatted)
    if contacts:
        result["contactPoint"] = " | ".join(contacts)

    # spatial (bbox as raw "west, south, east, north")
    bboxes = []
    for bbox_node in _xp(tree, "//gmd:EX_GeographicBoundingBox"):
        w = _node_text(_xp_first(bbox_node, ".//gmd:westBoundLongitude//gco:Decimal"))
        e = _node_text(_xp_first(bbox_node, ".//gmd:eastBoundLongitude//gco:Decimal"))
        s = _node_text(_xp_first(bbox_node, ".//gmd:southBoundLatitude//gco:Decimal"))
        n = _node_text(_xp_first(bbox_node, ".//gmd:northBoundLatitude//gco:Decimal"))
        if all([w, e, s, n]):
            bboxes.append(f"{w}, {s}, {e}, {n}")
    if bboxes:
        result["spatial"] = " | ".join(bboxes)

    # issued / modified (only from identification's citation, not thesaurus)
    for ci_date in _xp(tree, "//gmd:identificationInfo//gmd:citation//gmd:CI_Date"):
        type_codes = _xp(ci_date, "./gmd:dateType/gmd:CI_DateTypeCode/@codeListValue")
        if not type_codes:
            continue
        dtype = type_codes[0].lower()
        date_wrapper = _xp_first(ci_date, "./gmd:date")
        date_value = _node_text(date_wrapper)
        if not date_value:
            continue
        if dtype == "creation" and "issued" not in result:
            result["issued"] = date_value
        elif dtype == "revision" and "modified" not in result:
            result["modified"] = date_value

    # language (resource language first, then metadata language)
    lang_node = (
        _xp_first(tree, "//gmd:identificationInfo//gmd:language//gmd:LanguageCode")
        or _xp_first(tree, "//gmd:language//gmd:LanguageCode")
    )
    if lang_node is not None:
        text_val = _node_text(lang_node)
        if text_val:
            result["language"] = text_val
        else:
            code = lang_node.get("codeListValue")
            if code:
                result["language"] = code

    # accessRights (otherConstraints inside blocks with accessConstraints)
    rights = []
    for legal in _xp(tree, "//gmd:MD_LegalConstraints"):
        if not _xp(legal, "./gmd:accessConstraints"):
            continue
        for oc in _xp(legal, ".//gmd:otherConstraints"):
            anchors = _xp(oc, ".//gmx:Anchor/@xlink:href")
            if anchors:
                rights.append(anchors[0])
                continue
            cs = _xp_first(oc, ".//gco:CharacterString")
            if cs is not None:
                val = _node_text(cs)
                if val:
                    rights.append(val)
    if rights:
        result["accessRights"] = _join_unique(rights)

    # accrualPeriodicity
    freq = _xp(tree, "//gmd:MD_MaintenanceFrequencyCode/@codeListValue")
    if freq:
        result["accrualPeriodicity"] = freq[0]

    # provenance
    statement = _xp_first(tree, "//gmd:LI_Lineage//gmd:statement//gco:CharacterString")
    if statement is not None:
        val = _node_text(statement)
        if val:
            result["provenance"] = val

    # conformsTo (only pass=true)
    specs = []
    for cr in _xp(tree, "//gmd:DQ_ConformanceResult"):
        pass_value = _xp_first(cr, ".//gmd:pass//gco:Boolean")
        if pass_value is None or _node_text(pass_value).lower() != "true":
            continue
        for title in _xp(cr, ".//gmd:specification//gmd:title//gco:CharacterString"):
            val = _node_text(title)
            if val:
                specs.append(val)
    if specs:
        result["conformsTo"] = _join_unique(specs, sep=" | ")
        
    # maintainer (CI_ResponsibleParty with role custodian/owner inside identificationInfo)
    maintainers = []
    for party in _xp(tree, "//gmd:identificationInfo//gmd:CI_ResponsibleParty"):
        roles = _xp(party, ".//gmd:CI_RoleCode/@codeListValue")
        if not any((r or "").lower() in MAINTAINER_ROLES for r in roles):
            continue
        formatted = _format_party_from_xml(party)
        if formatted:
            maintainers.append(formatted)
    if maintainers:
        result["maintainer"] = " | ".join(maintainers)

    # creator / contributor / originator (filtered by role)
    for field_name, role_set in [
        ("creator", CREATOR_ROLES),
        ("contributor", CONTRIBUTOR_ROLES),
        ("originator", ORIGINATOR_ROLES),
    ]:
        parties = []
        for party in _xp(tree, "//gmd:identificationInfo//gmd:CI_ResponsibleParty"):
            roles = _xp(party, ".//gmd:CI_RoleCode/@codeListValue")
            if not any((r or "").lower() in role_set for r in roles):
                continue
            formatted = _format_party_from_xml(party)
            if formatted:
                parties.append(formatted)
        if parties:
            result[field_name] = " | ".join(parties)

    # versionInfo (gmd:edition)
    edition = _xp_first(tree, "//gmd:identificationInfo//gmd:citation//gmd:edition//gco:CharacterString")
    if edition is not None:
        val = _node_text(edition)
        if val:
            result["versionInfo"] = val

    # page (CI_OnlineResource URLs with function=information, excluding distributionInfo)
    pages = []
    for online in _xp(tree, "//gmd:CI_OnlineResource"):
        # skip ones inside distributionInfo (those are distribution, not landing pages)
        if _xp(online, "ancestor::gmd:distributionInfo"):
            continue
        func_codes = _xp(online, ".//gmd:CI_OnLineFunctionCode/@codeListValue")
        if not any((f or "").lower() == "information" for f in func_codes):
            continue
        for url_node in _xp(online, ".//gmd:URL"):
            u = _node_text(url_node)
            if u and u not in pages:
                pages.append(u)
    if pages:
        result["page"] = ", ".join(pages)

    # spatialResolutionInMeters (gmd:distance, only when value has uom=meter or no uom)
    distances = []
    for dist_node in _xp(tree, "//gmd:MD_Resolution//gmd:distance//gco:Distance"):
        uom = (dist_node.get("uom") or "").lower()
        if uom and "meter" not in uom and "metre" not in uom and uom != "m":
            continue
        val = _node_text(dist_node)
        if val and val not in distances:
            distances.append(val)
    if distances:
        result["spatialResolutionInMeters"] = ", ".join(distances)

    for key in SKIP_AUTOFILL_FIELDS:
        result.pop(key, None)
    return result



DCATDE_POLITICAL_URI = URIRef("http://dcat-ap.de/def/dcatde/politicalGeocodingURI")
DCATDE_POLITICAL_LEVEL_URI = URIRef("http://dcat-ap.de/def/dcatde/politicalGeocodingLevelURI")
DCATDE_CONTRIBUTOR_ID = URIRef("http://dcat-ap.de/def/dcatde/contributorID")
DCATDE_MAINTAINER = URIRef("http://dcat-ap.de/def/dcatde/maintainer")
DCATDE_ORIGINATOR = URIRef("http://dcat-ap.de/def/dcatde/originator")
DCATDE_LEGAL_BASIS = URIRef("http://dcat-ap.de/def/dcatde/legalBasis")
DCATDE_GEOCODING_DESCRIPTION = URIRef("http://dcat-ap.de/def/dcatde/geocodingDescription")
DCATDE_QUALITY_PROCESS_URI = URIRef("http://dcat-ap.de/def/dcatde/qualityProcessURI")

DCATAP_AVAILABILITY = URIRef("http://data.europa.eu/r5r/availability")
ADMS_VERSION_NOTES = URIRef("http://www.w3.org/ns/adms#versionNotes")
ADMS_SAMPLE = URIRef("http://www.w3.org/ns/adms#sample")
PROV_WAS_GENERATED_BY = URIRef("http://www.w3.org/ns/prov#wasGeneratedBy")
OWL_VERSION_INFO = URIRef("http://www.w3.org/2002/07/owl#versionInfo")

LOCN_GEOMETRY = URIRef("http://www.w3.org/ns/locn#geometry")
VCARD_FN = URIRef("http://www.w3.org/2006/vcard/ns#fn")
VCARD_HAS_EMAIL = URIRef("http://www.w3.org/2006/vcard/ns#hasEmail")
VCARD_HAS_URL = URIRef("http://www.w3.org/2006/vcard/ns#hasURL")
FOAF_PAGE = URIRef("http://xmlns.com/foaf/0.1/page")


def _s(value):
    return str(value).strip() if value is not None else ""


def _objects(graph, subject, predicate):
    return [_s(o) for o in graph.objects(subject, predicate) if _s(o)]


def _find_root_subject(graph):
    datasets = list(graph.subjects(RDF.type, DCAT.Dataset))
    if len(datasets) == 1:
        return datasets[0], False
    catalogs = list(graph.subjects(RDF.type, DCAT.Catalog))
    if catalogs:
        return catalogs[0], True
    if datasets:
        return datasets[0], False
    return None, False


def _format_vcard(graph, contact):
    parts = []
    for prop in (VCARD_FN, FOAF.name):
        parts.extend(_objects(graph, contact, prop))
    for prop in (VCARD_HAS_EMAIL, VCARD_HAS_URL):
        for obj in graph.objects(contact, prop):
            parts.append(_s(obj).removeprefix("mailto:"))
    return _join_unique(parts)


def parse_dcat(xml_bytes):
    graph = Graph()
    graph.parse(data=xml_bytes, format="xml")

    subject, is_catalog = _find_root_subject(graph)
    if subject is None:
        return {}

    result = {}

    desc = _objects(graph, subject, DCTERMS.description)
    if desc:
        result["description"] = desc[0]

    kws = _objects(graph, subject, DCAT.keyword)
    if kws:
        result["keywords"] = _join_unique(kws)

    themes = _objects(graph, subject, DCAT.theme)
    if themes:
        result["theme"] = _join_unique(themes)

    if not is_catalog:
        urls = []
        for dist in graph.objects(subject, DCAT.distribution):
            for prop in (DCAT.downloadURL, DCAT.accessURL):
                urls.extend(_objects(graph, dist, prop))
        if urls:
            result["distribution"] = _join_unique(urls)

    contacts = []
    for c in graph.objects(subject, DCAT.contactPoint):
        f = _format_vcard(graph, c)
        if f:
            contacts.append(f)
    if contacts:
        result["contactPoint"] = " | ".join(contacts)

    spatials = []
    for sp in graph.objects(subject, DCTERMS.spatial):
        geoms = _objects(graph, sp, LOCN_GEOMETRY)
        if geoms:
            spatials.extend(geoms)
        else:
            text = _s(sp)
            if text and not text.startswith("http"):
                spatials.append(text)
    if spatials:
        result["spatial"] = " | ".join(spatials)

    for temp in graph.objects(subject, DCTERMS.temporal):
        starts = _objects(graph, temp, DCAT.startDate)
        ends = _objects(graph, temp, DCAT.endDate)
        if starts and ends:
            result["temporal"] = f"{starts[0]}/{ends[0]}"
        elif starts:
            result["temporal"] = starts[0]
        elif ends:
            result["temporal"] = ends[0]
        if "temporal" in result:
            break

    issued = _objects(graph, subject, DCTERMS.issued)
    if issued:
        result["issued"] = issued[0]
    modified = _objects(graph, subject, DCTERMS.modified)
    if modified:
        result["modified"] = modified[0]

    langs = _objects(graph, subject, DCTERMS.language)
    if langs:
        result["language"] = _join_unique(langs)

    pages = _objects(graph, subject, DCAT.landingPage)
    if pages:
        result["landingPage"] = _join_unique(pages)

    rights = _objects(graph, subject, DCTERMS.accessRights)
    if rights:
        result["accessRights"] = _join_unique(rights)

    freqs = _objects(graph, subject, DCTERMS.accrualPeriodicity)
    if freqs:
        result["accrualPeriodicity"] = freqs[0]

    conforms = _objects(graph, subject, DCTERMS.conformsTo)
    if conforms:
        result["conformsTo"] = " | ".join(conforms)

    maintainers = []
    for m in graph.objects(subject, DCATDE_MAINTAINER):
        f = _format_vcard(graph, m) or _join_unique(_objects(graph, m, FOAF.name))
        if f:
            maintainers.append(f)
    if maintainers:
        result["maintainer"] = " | ".join(maintainers)

    pol = _objects(graph, subject, DCATDE_POLITICAL_URI)
    if pol:
        result["politicalGeocodingURI"] = _join_unique(pol)

    contrib = _objects(graph, subject, DCATDE_CONTRIBUTOR_ID)
    if contrib:
        result["contributorID"] = _join_unique(contrib)

    # creator / contributor (dct:* properties; values can be literal or blank node)
    for field_name, predicate in [
        ("creator", DCTERMS.creator),
        ("contributor", DCTERMS.contributor),
    ]:
        items = []
        for obj in graph.objects(subject, predicate):
            formatted = _format_vcard(graph, obj) or _join_unique(_objects(graph, obj, FOAF.name)) or _s(obj)
            if formatted:
                items.append(formatted)
        if items:
            result[field_name] = " | ".join(items)

    # originator (DCAT-AP.de extension)
    originators = []
    for obj in graph.objects(subject, DCATDE_ORIGINATOR):
        formatted = _format_vcard(graph, obj) or _join_unique(_objects(graph, obj, FOAF.name)) or _s(obj)
        if formatted:
            originators.append(formatted)
    if originators:
        result["originator"] = " | ".join(originators)

    # versionInfo (try multiple predicates: owl:versionInfo, dcat:version, dct:hasVersion)
    for predicate in (OWL_VERSION_INFO, DCAT.version, DCTERMS.hasVersion):
        versions = _objects(graph, subject, predicate)
        if versions:
            result["versionInfo"] = versions[0]
            break

    # versionNotes
    notes = _objects(graph, subject, ADMS_VERSION_NOTES)
    if notes:
        result["versionNotes"] = notes[0]

    # page (foaf:page links)
    pages = _objects(graph, subject, FOAF_PAGE)
    if pages:
        result["page"] = _join_unique(pages)

    # availability (DCAT-AP extension)
    avail = _objects(graph, subject, DCATAP_AVAILABILITY)
    if avail:
        result["availability"] = avail[0]

    # legalBasis (DCAT-AP.de)
    legal = _objects(graph, subject, DCATDE_LEGAL_BASIS)
    if legal:
        result["legalBasis"] = _join_unique(legal)

    # geocodingDescription (DCAT-AP.de)
    geo_desc = _objects(graph, subject, DCATDE_GEOCODING_DESCRIPTION)
    if geo_desc:
        result["geocodingDescription"] = _join_unique(geo_desc)

    # qualityProcessURI (DCAT-AP.de)
    qpu = _objects(graph, subject, DCATDE_QUALITY_PROCESS_URI)
    if qpu:
        result["qualityProcessURI"] = _join_unique(qpu)

    # wasGeneratedBy (PROV-O)
    wgb = _objects(graph, subject, PROV_WAS_GENERATED_BY)
    if wgb:
        result["wasGeneratedBy"] = _join_unique(wgb)

    # politicalGeocodingLevelURI (DCAT-AP.de)
    pol_level = _objects(graph, subject, DCATDE_POLITICAL_LEVEL_URI)
    if pol_level:
        result["politicalGeocodingLevelURI"] = _join_unique(pol_level)

    # simple dct:* relations (multi-valued, URIs or literals)
    for field_name, predicate in [
        ("relation", DCTERMS.relation),
        ("source", DCTERMS.source),
        ("isReferencedBy", DCTERMS.isReferencedBy),
        ("isVersionOf", DCTERMS.isVersionOf),
        ("references", DCTERMS.references),
    ]:
        values = _objects(graph, subject, predicate)
        if values:
            result[field_name] = _join_unique(values)

    # sample (adms:sample)
    samples = _objects(graph, subject, ADMS_SAMPLE)
    if samples:
        result["sample"] = _join_unique(samples)

    # spatialResolutionInMeters (dcat:spatialResolutionInMeters)
    sri = _objects(graph, subject, DCAT.spatialResolutionInMeters)
    if sri:
        result["spatialResolutionInMeters"] = sri[0]

    # temporalResolution (dcat:temporalResolution)
    tr = _objects(graph, subject, DCAT.temporalResolution)
    if tr:
        result["temporalResolution"] = tr[0]

    # granularity (dcat:granularity)
    gran = _objects(graph, subject, DCAT.granularity)
    if gran:
        result["granularity"] = gran[0]

    # qualifiedAttribution / qualifiedRelation (DCAT v2 — just grab URIs/text)
    for field_name, predicate in [
        ("qualifiedAttribution", DCAT.qualifiedAttribution),
        ("qualifiedRelation", DCAT.qualifiedRelation),
    ]:
        items = []
        for obj in graph.objects(subject, predicate):
            items.append(_s(obj))
        if items:
            result[field_name] = _join_unique(items)

    for key in SKIP_AUTOFILL_FIELDS:
        result.pop(key, None)
    return result


def parse_metadata_xml(xml_bytes):
    if not xml_bytes:
        return {}
    fmt = detect_format(xml_bytes)
    if fmt == "iso":
        return parse_iso(xml_bytes)
    if fmt == "dcat":
        return parse_dcat(xml_bytes)
    return {}