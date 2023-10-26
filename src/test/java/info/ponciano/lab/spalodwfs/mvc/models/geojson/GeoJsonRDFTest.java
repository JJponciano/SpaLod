package info.ponciano.lab.spalodwfs.mvc.models.geojson;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

import info.ponciano.lab.pisemantic.PiOntologyException;
import info.ponciano.lab.pisemantic.PiSparql;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.OntoManagementException;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.OwlManagement;

public class GeoJsonRDFTest {
    @Test
    void testDownlift() {

    }

    @Test
    void testUpliftGeoJSON() throws FileNotFoundException, IOException, ParseException, PiOntologyException, Exception {
        String geojsonfilepath="src/test/resources/researchLaboratory.json";
        geojsonfilepath="C:\\Users\\49151\\Downloads\\Radnetz_Deutschland_06-2022.geojson";
            PiSparql ont = new OwlManagement(KB.DEFAULT_ONTO).getOnt();
            // execute the uplift
            GeoJsonRDF.upliftGeoJSON(geojsonfilepath, ont);
            System.out.println("ok");
    }
}
