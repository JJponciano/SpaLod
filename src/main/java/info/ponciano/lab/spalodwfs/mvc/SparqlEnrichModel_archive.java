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

public class SparqlEnrichModel_archive {
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
    private List<String> header;
    private ArrayList<String[]> data;
    private String outputFilePath, triplestore,  query;

    public SparqlEnrichModel_archive(String triplestore, String q) throws Exception {
        this. triplestore=triplestore;
        this.query=q;
        init(triplestore, q);
        this.run();
    }

    private void init(String triplestore, String q) {
        String queryString = prefixes + q;
        //execute the query
        Query query = QueryFactory.create(queryString);

        QueryExecution qexec;
        if (triplestore == null || triplestore.isBlank())
            qexec = QueryExecutionFactory.create(query, KB.get().getOnt().getOnt());
        else
            qexec = QueryExecutionFactory.sparqlService(triplestore, query);

        //store results in ResultSet format
        ResultSet resultset = qexec.execSelect();

        //get columns name as results var
        this.header = resultset.getResultVars();
        this.data = new ArrayList<>();

        //for all the QuerySolution in the ResultSet file
        while (resultset.hasNext()) {
            QuerySolution solution = resultset.next();
            String[] properties = new String[header.size()];
            for (int i = 0; i < header.size(); i++) {
                String columnName = header.get(i);
                RDFNode node = solution.get(columnName);

                String value = null;
                //test if resource
                if (node == null) value = "NC";
                else if (node.isResource()) {
                    value = node.asResource().getURI();
                } else
                    //test if literal
                    if (node.isLiteral()) {
                        value = node.asLiteral().toString();
                    }
                //clean literal
                if (value.contains("^^http://www.w3.org/2001/XMLSchema#double")) {
                    value = value.replace("^^http://www.w3.org/2001/XMLSchema#double", "");
                }
                if (value.contains("^^http://www.w3.org/2001/XMLSchema#float")) {
                    value = value.replace("^^http://www.w3.org/2001/XMLSchema#float", "");
                }
                properties[i] = value;
            }
            data.add(properties);
        }
    }


    //TODO here the type should be set, not the "wikidataOrigin" and in a generic way based on hearder name (not index) such as "category"
    private void replaceWikidataType() {
        for (int i = 0; i < data.size(); i++) {
            String[] properties = data.get(i);
            String code = properties[0];
            switch (code) {
                case "Q3914":
                    properties[0] = "HS";
                    break;
                case "Q1195942":
                    properties[0] = "Feuerwehr";
                    break;
                case "Q16917":
                    properties[0] = "KHV";
                    break;
                case "Q33506":
                    properties[0] = "Museen";
                    break;
                case "Q205495":
                    properties[0] = "Tankstellen";
                    break;
                case "Q2140665": // or Q1477760
                    properties[0] = "LadeSt";
                    break;
                case "Q180846":
                    properties[0] = "Supermarkt";
                    break;
                case "Q7075":
                    properties[0] = "Bibliothek";
                    break;
                case "Q44782":
                    properties[0] = "Seehaefen";
                    break;
                case "Q55488":
                    properties[0] = "Bahnhof";
                    break;
                case "Q11707":
                    properties[0] = "Restaurant";
                    break;
                case "Q41253":
                    properties[0] = "Kino";
                    break;
                case "Q4989906":
                    properties[0] = "Denkmal";
                    break;
                case "Q861951":
                    properties[0] = "BPOL";
                    break;
                case "Q27686":
                    properties[0] = "Hotel";
                    break;
                case "Q483110":
                    properties[0] = "Stadium";
                    break;
                case "Q1248784":
                    properties[0] = "Flughaefen";
                    break;
                case "Q22908":
                    properties[0] = "Seniorenheime";
                    break;
                case "Q200023":
                    properties[0] = "Schwimmbad";
                    break;
                case "Q19010":
                    properties[0] = "Wetterstation";
                    break;
                case "Q483242":
                    properties[0] = "Laboratorium";
                    break;
                case "Q364005":
                    properties[0] = "Kita";
                    break;
                default:
                    properties[0] = "KmBAB";
            }
        }
    }

    public String getOutputFilePath() throws Exception {
        return outputFilePath;
    }

    public void enrich(PiSparql ont) throws Exception {
        OntClass dataset = ont.createClass(GeoJsonRDF.DCAT_DATASET);
        String name = KB.NS + UUID.randomUUID();
        Individual data = dataset.createIndividual(name);

        List<Feature> features = new ArrayList<>();

        //for each item
        for (String[] properties : this.data) {
            //create a feature
            //TODO all properties included  longitude (properties[3]) and latitude(properties[2]) have to be extracted dynamically either
            // extract longitude and latitude
            Double latitude = null;
            Double longitude = null;
            for (int i = 0; i < this.header.size(); i++) {
                String pname = this.header.get(i);
                if (pname.contains("longitude")) {
                    longitude = Double.parseDouble(properties[i]);
                } else if (pname.contains("latitude")) {
                    latitude = Double.parseDouble(properties[i]);
                }
            }
            if (longitude == null || latitude == null)
                throw new UnsupportedOperationException("Latitude and Longitude are missing in the query. You should used it as key (e.g. ?latitude, ?longitude)");
            Geometry geo = new Geometry("Point", longitude, latitude);
            Feature f = new Feature(geo);
            for (int i = 0; i < this.header.size(); i++) {
                String pname = this.header.get(i);
                if (!pname.contains("longitude") && !pname.contains("latitude")) {
                    f.addProperty(pname, properties[i]);
                    features.add(f);
                }
            }

        }
        GeoJsonRDF.featureUplift(features, ont, data);
    }

    public List<String> getHeader() {
        return header;
    }

    public void setHeader(List<String> header) {
        this.header = header;
    }

    public ArrayList<String[]> getData() {
        return data;
    }

    public void setData(ArrayList<String[]> data) {
        this.data = data;
    }

    private void run() throws Exception {
//        if(this.triplestore.contains("wikidata"))
//            this.replaceWikidataType();
//        PiSparql ont = KB.get().getOntEmpty();
        //creation of thematic map dataset
//        enrich(ont);
        String p = UUID.randomUUID().toString() + ".csv";
        String[] ts = this.header.toArray(new String[this.header.size()]);
        String[][] d = this.data.toArray(new String[this.data.size()][this.data.get(0).length]);
        this.writesCSV(FileDownloadController.DIR + p,d,"," ,ts);
        this.outputFilePath = FileDownloadController.DOWNLOAD_DATA + p;
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
