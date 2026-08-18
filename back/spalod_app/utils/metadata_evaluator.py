from pathlib import Path
from .metadata_assessment import assess_xml_file


def evaluate_metadata_xml(xml_file_path: str) -> dict:
    result = assess_xml_file(Path(xml_file_path))

    return {
        "letters": result.letter_code,
        "stars": result.star_range,
        "details": result.letters,
    }