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
    private final StorageService storageService;
    private SparqlEnrichModel sem;

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

        try {
            String query=  sq.getResults();
            String triplestore = "https://" + sq.getTriplestore() + ".org/sparql";
            this.sem=new SparqlEnrichModel(triplestore,query);

        } catch (Exception e) {
            r = e.getMessage();
        }
        //add attributes to model
        model.addAttribute("cl", this.sem.getHeader());
        model.addAttribute("MDlist", this.sem.getData());
        model.addAttribute("errorMessage", r);
        model.addAttribute("ontPath", this.sem.getOntPath());

        return "lod";
    }

    @PostMapping("/lodenrich")
    public String lodenrich( Model model) {
        try {

            PiSparql ont = KB.get().getOnt();
            this.sem.enrich(ont);
            KB.get().save();
            model.addAttribute("message", "The Knowledge-base was enriched!");
            return "lod";
        } catch (Exception ex) {
            Logger.getLogger(EnrichmentController.class.getName()).log(Level.SEVERE, null, ex);
            model.addAttribute("message", ex.getMessage());
            return "lod";
        }
    }
}
