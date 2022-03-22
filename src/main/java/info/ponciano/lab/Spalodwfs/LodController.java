package info.ponciano.lab.Spalodwfs;

import info.ponciano.lab.Spalodwfs.geotime.controllers.last.EnrichmentController;
import info.ponciano.lab.Spalodwfs.geotime.controllers.storage.FileDownloadController;
import info.ponciano.lab.Spalodwfs.geotime.controllers.storage.StorageService;
import info.ponciano.lab.Spalodwfs.geotime.models.SparqlQuery;
import info.ponciano.lab.Spalodwfs.geotime.models.StringForm;
import info.ponciano.lab.Spalodwfs.geotime.models.geojson.Feature;
import info.ponciano.lab.Spalodwfs.geotime.models.geojson.GeoJsonRDF;
import info.ponciano.lab.Spalodwfs.geotime.models.geojson.Geometry;
import info.ponciano.lab.Spalodwfs.geotime.models.semantic.KB;
import info.ponciano.lab.Spalodwfs.geotime.models.semantic.OntoManagementException;
import info.ponciano.lab.pisemantic.PiSparql;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class LodController {

    String r = null;
    List<String> columnNames = new ArrayList<String>();
    List<String[]> resultList = new ArrayList<String[]>();
    private final StorageService storageService;

    @Autowired
    public LodController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/lod")
    public String lod(Model model) throws OntoManagementException {
        String rtn = "lod";
        return rtn;
    }

    @ModelAttribute(name = "stringform")
    public StringForm stringform() {
        return new StringForm();
    }

    @ModelAttribute(name = "squery")
    public SparqlQuery sparqlquery() {
        return new SparqlQuery();
    }

    @PostMapping("/lod")
    public String results(@ModelAttribute("squery") SparqlQuery sq, Model model) throws Exception {

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
                + "PREFIX pq: <http://www.wikidata.org/prop/qualifier/>";

        try {

            String queryString = prefixes + sq.getResults();
            Query query = QueryFactory.create(queryString);
            System.out.println(queryString);
            QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", query);
            List<String> cn = new ArrayList<String>();
            List<String[]> rl = new ArrayList<String[]>();
            //store results in ResultSet format
            ResultSet resultset = qexec.execSelect();
            //gives the column names of the query
            cn = getResults(resultset, rl);
            this.columnNames = cn;
            this.resultList = rl;
        } catch (Exception e) {
            r = e.getMessage();
        }
        String p = getOntPath();
        //add attributes to model
        model.addAttribute("cl", columnNames);
        model.addAttribute("MDlist", resultList);
        model.addAttribute("errorMessage", r);
        model.addAttribute("ontPath", p);

        return "lod";
    }

    public static List<String> getResults(ResultSet resultset, List<String[]> rl) {
        List<String> cn;

        cn = resultset.getResultVars();
        System.out.println("Column Names : " + cn);
        List<Integer> numberOfColumns = new ArrayList<Integer>();
        for (int i = 0; i < cn.size(); i++) {
            numberOfColumns.add(i);
        }
        System.out.println(numberOfColumns);
        //for all the QuerySolution in the ResultSet file
        while (resultset.hasNext()) {
            QuerySolution solu = resultset.next();
            String[] ls = new String[cn.size()];
            for (int i = 0; i < cn.size(); i++) {
                String columnName = cn.get(i);
                RDFNode node = solu.get(columnName);
                String a = null;

                //test if resource
                if (node.isResource()) {
                    a = node.asResource().getLocalName();
                }
                //test if literal
                if (node.isLiteral()) {
                    a = node.asLiteral().toString();
                }
                if (a.contains("^^http://www.w3.org/2001/XMLSchema#double")) {
                    a = a.replace("^^http://www.w3.org/2001/XMLSchema#double", "");
                }
                ls[i] = a;
            }
            Arrays.deepToString(ls);
            rl.add(ls);
        }
        return cn;
    }

    @PostMapping("/lodenrich")
    public String lodenrich( Model model) {
        try {
            PiSparql ont = KB.get().getOnt();
            //creation of thematic map dataset
            OntClass dataset = ont.createClass(GeoJsonRDF.DCAT_DATASET);
            OntClass mt = ont.createClass(KB.NS + "Dataset");

            String name = KB.NS + UUID.randomUUID().toString();

            Individual data = dataset.createIndividual(name);
            data.addProperty(RDF.type, mt);

            enrich(ont,data);
            KB.get().save();
            model.addAttribute("message", "The Knowledge-base was enriched!");
            return "lod";
        } catch (Exception ex) {
            Logger.getLogger(EnrichmentController.class.getName()).log(Level.SEVERE, null, ex);
            model.addAttribute("message", ex.getMessage());
            return "lod";
        }
    }

    private void enrich(PiSparql ont,Individual data) throws Exception {

        List<Feature> features = new ArrayList<>();
        //for each item
        for (String[] r : resultList) {
            //create a feature
            Geometry geo = new Geometry("Point", Double.parseDouble(r[3]), Double.parseDouble(r[2]));
            Feature f = new Feature(geo);
            f.addProperty("hasWikidataOrigin", r[0]);
            f.addProperty("hasLabel", r[1]);
            features.add(f);
        }
        GeoJsonRDF.featureUplift(features, ont, data);
    }



    private String getOntPath() throws Exception {
        PiSparql ont = KB.get().getOntEmpty();
        //creation of thematic map dataset
        enrich(ont,null);

        String p = UUID.randomUUID().toString() + ".owl";
        ont.write(FileDownloadController.DIR+p);
        return (FileDownloadController.DOWNLOAD_DATA +p);
    }
}
