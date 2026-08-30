from functools import lru_cache
from rdflib import Graph, RDF, RDFS, OWL, URIRef
from django.conf import settings
import requests
import gzip

SCHEMA_NAMESPACES = ("https://schema.org/","http://schema.org/")

# @lru_cache(maxsize=1)
# def load_graph():
#     """Load schema.org graph once and cache it."""
#     g = Graph()
#     g.parse(settings.USKB_SOURCE, format="turtle")
#     return g

@lru_cache(maxsize=1)
def load_graph():
    """Load schema.org graph once and cache it."""
    url = settings.USKB_SOURCE
    resp = requests.get(
        url,
        headers={"Accept-Encoding": "gzip, deflate"},
        timeout=30,
    )
    resp.raise_for_status()

    raw = resp.content
    if raw[:2] == b"\x1f\x8b":   # GZIP signature
        raw = gzip.decompress(raw)

    turtle_text = raw.decode("utf-8")
    g = Graph()
    g.parse(data=turtle_text, format="turtle")

    return g


def get_label(g: Graph, uri: URIRef) -> str:
    """Return English or default label; fallback to tail of URI."""
    for lbl in g.objects(uri, RDFS.label):
        s = str(lbl)
        lang = getattr(lbl, "language", None)
        if not lang or str(lang).lower() in ("", "en"):
            return s
    s = str(uri)
    return s.split("#")[-1] if "#" in s else s.rstrip("/").split("/")[-1]


@lru_cache(maxsize=1)
def label_to_uri_map() -> dict[str, str]:
    """Map label → URI (case-insensitive lookup)."""
    g = load_graph()
    kinds = [RDF.Property, OWL.ObjectProperty, OWL.DatatypeProperty, OWL.AnnotationProperty]
    mapping = {}
    seen = set()

    for t in kinds:
        for s in g.subjects(RDF.type, t):
            if not isinstance(s, URIRef) or s in seen:
                continue

            uri = str(s)
            if not uri.startswith(SCHEMA_NAMESPACES):
                continue

            seen.add(s)
            lbl = get_label(g, s)
            mapping[lbl.strip().lower()] = uri

    return mapping


def resolve_schema_uri_by_label(label: str) -> str | None:
    """Return schema.org URI if label exists, otherwise None."""
    if not label:
        return None
    return label_to_uri_map().get(label.strip().lower())


def get_property_terms(q: str = "", limit: int = 1000) -> list[dict[str, str]]:
    """Return property terms for autocomplete (cached)."""
    g = load_graph()
    kinds = [RDF.Property, OWL.ObjectProperty, OWL.DatatypeProperty, OWL.AnnotationProperty]
    ql = (q or "").strip().lower()

    seen, out = set(), []
    for t in kinds:
        for s in g.subjects(RDF.type, t):
            if not isinstance(s, URIRef) or s in seen:
                continue

            uri = str(s)
            if not uri.startswith(SCHEMA_NAMESPACES):
                continue

            seen.add(s)
            lbl = get_label(g, s)
            if ql and (ql not in lbl.lower() and ql not in uri.lower()):
                continue
            out.append({"uri": uri, "label": lbl})
            if len(out) >= limit:
                break
        if len(out) >= limit:
            break

    out.sort(key=lambda x: x["label"].lower())
    return out

