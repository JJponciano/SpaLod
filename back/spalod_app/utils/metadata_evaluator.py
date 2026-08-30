from __future__ import annotations

import json
from hashlib import sha256
from json import JSONDecodeError
from pathlib import Path
from uuid import uuid4

from .dcat_to_iso import convert_dcat_to_iso
from .metadata_assessment import assess_xml_file
from .metadata_parser import detect_format

DCAT_TO_ISO_CACHE_VERSION = "1"


def evaluation_response(result) -> dict:
    return {
        "letters": result.letter_code,
        "stars": result.star_range,
        "details": result.letters,
    }


def source_sha256(
    xml_bytes: bytes,
) -> str:
    return sha256(
        xml_bytes
    ).hexdigest()


def dataset_base_name(
    metadata_path: Path,
) -> str:
    """
    Convert a metadata filename into the dataset base name.

    Example:
    <uuid>.data.xml -> <uuid>
    """

    filename = metadata_path.name

    known_suffixes = (
        ".data.xml",
        "_metadata.xml",
        ".metadata.xml",
        ".xml",
    )

    for suffix in known_suffixes:
        if filename.endswith(suffix):
            return filename[
                :-len(suffix)
            ]

    return metadata_path.stem


def cached_iso_path(
    metadata_path: Path | str,
) -> Path:
    metadata_path = Path(
        metadata_path
    )

    base_name = dataset_base_name(
        metadata_path
    )

    return metadata_path.with_name(
        f"{base_name}.iso19139"
    )


def cache_state_path(
    iso_path: Path,
) -> Path:
    # The leading dot keeps this technical file hidden
    # in Finder under normal settings.
    return iso_path.with_name(
        f".{iso_path.name}.state.json"
    )


def expected_cache_state(
    xml_bytes: bytes,
) -> dict:
    return {
        "source_sha256": source_sha256(
            xml_bytes
        ),
        "converter_version": (
            DCAT_TO_ISO_CACHE_VERSION
        ),
    }


def read_cache_state(
    state_path: Path,
) -> dict | None:
    if not state_path.exists():
        return None

    try:
        content = state_path.read_text(
            encoding="utf-8"
        )

        state = json.loads(
            content
        )
    except (
        OSError,
        JSONDecodeError,
    ):
        return None

    if not isinstance(state, dict):
        return None

    return state


def atomic_write_bytes(
    destination: Path,
    content: bytes,
) -> None:
    temporary_path = destination.with_name(
        (
            f".{destination.name}."
            f"{uuid4().hex}.tmp"
        )
    )

    try:
        temporary_path.write_bytes(
            content
        )

        temporary_path.replace(
            destination
        )
    finally:
        temporary_path.unlink(
            missing_ok=True
        )


def atomic_write_json(
    destination: Path,
    payload: dict,
) -> None:
    content = json.dumps(
        payload,
        ensure_ascii=False,
        indent=2,
        sort_keys=True,
    ).encode("utf-8")

    atomic_write_bytes(
        destination,
        content,
    )


def get_or_create_cached_iso(
    metadata_path: Path,
    xml_bytes: bytes,
) -> Path:
    iso_path = cached_iso_path(
        metadata_path
    )

    state_path = cache_state_path(
        iso_path
    )

    expected_state = expected_cache_state(
        xml_bytes
    )

    current_state = read_cache_state(
        state_path
    )

    if (
        iso_path.exists()
        and current_state == expected_state
    ):
        return iso_path

    iso_bytes = convert_dcat_to_iso(
        xml_bytes
    )

    # Write the ISO first. The state file is written
    # only after successful conversion.
    atomic_write_bytes(
        iso_path,
        iso_bytes,
    )

    atomic_write_json(
        state_path,
        expected_state,
    )

    return iso_path


def evaluate_metadata_xml(
    xml_file_path: str,
) -> dict:
    metadata_path = Path(
        xml_file_path
    )

    xml_bytes = metadata_path.read_bytes()

    metadata_format = detect_format(
        xml_bytes
    )

    if metadata_format == "iso":
        result = assess_xml_file(
            metadata_path
        )

        return evaluation_response(
            result
        )

    if metadata_format == "dcat":
        iso_path = get_or_create_cached_iso(
            metadata_path,
            xml_bytes,
        )

        result = assess_xml_file(
            iso_path
        )

        return evaluation_response(
            result
        )

    raise ValueError(
        (
            "Unsupported metadata format. "
            "Expected ISO 19139 or "
            "DCAT RDF/XML."
        )
    )