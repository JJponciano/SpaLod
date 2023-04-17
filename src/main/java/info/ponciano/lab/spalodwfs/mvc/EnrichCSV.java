package info.ponciano.lab.spalodwfs.mvc;

import info.ponciano.lab.pisemantic.PiSparql;
import info.ponciano.lab.pitools.files.PiFile;
import info.ponciano.lab.spalodwfs.mvc.controllers.storage.FileDownloadController;
import info.ponciano.lab.spalodwfs.mvc.models.geojson.Feature;
import info.ponciano.lab.spalodwfs.mvc.models.geojson.GeoJsonRDF;
import info.ponciano.lab.spalodwfs.mvc.models.geojson.Geometry;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EnrichCSV {
    //prefixes for SPARQL query
    String prefixes = "PREFIX schema: <http://schema.org/>"
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
            + "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
            + "PREFIX hist: <http://wikiba.se/history/ontology#>"
            + "PREFIX wd: <http://www.wikidata.org/entity/>"
            + "PREFIX wdt: <http://www.wikidata.org/prop/direct/>"
            + "PREFIX wikibase: <http://wikiba.se/ontology#>"
            + "PREFIX dct: <http://purl.org/dc/terms/>"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
            + "PREFIX bd: <http://www.bigdata.com/rdf#>"
            + "PREFIX wds: <http://www.wikidata.org/entity/statement/>\r\n"
            + "PREFIX wdv: <http://www.wikidata.org/value/>"
            + "PREFIX p: <http://www.wikidata.org/prop/>\r\n"
            + "PREFIX ps: <http://www.wikidata.org/prop/statement/>\r\n"
            + "PREFIX psv: <http://www.wikidata.org/prop/statement/value/>"
            + "PREFIX pq: <http://www.wikidata.org/prop/qualifier/>"
            + "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"
            + "PREFIX dp: <http://dbpedia.org/resource/>"
            + "PREFIX dpp: <http://dbpedia.org/property/>"
            + "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>";

    private String outputFilePath, triplestore, query;
    private String[][] data;

    public EnrichCSV(String[][] data) throws Exception {
        this.data = data;
    }

    public String getOutputFilePath() throws Exception {
        return outputFilePath;
    }



    private void run() throws Exception {
//        if(this.triplestore.contains("wikidata"))
//            this.replaceWikidataType();

    }

    public static void writesCSV(final String path, final String[][] array, final String separator,
                                 final String[] header) {
        String write = "";

        for (int i = 0; i < header.length; i++) {
            write += header[i];
            if (i + 1 < header.length) {
                write += separator;
            }
        }
        write += "\n";
        for (String[] array1 : array) {
            for (int j = 0; j < array1.length; j++) {
                write += array1[j];
                if (j + 1 < array1.length) {
                    write += separator;
                }
            }
            write += "\n";
        }
        new PiFile(path).writeTextFile(write);
    }
}
