package info.ponciano.lab.Spalodwfs;

import info.ponciano.lab.Spalodwfs.geotime.controllers.last.EnrichmentController;
import info.ponciano.lab.Spalodwfs.geotime.controllers.last.GeoJsonController;
import info.ponciano.lab.Spalodwfs.geotime.controllers.last.GeoJsonForm;
import info.ponciano.lab.Spalodwfs.geotime.controllers.storage.StorageService;
import info.ponciano.lab.Spalodwfs.geotime.models.SparqlQuery;
import info.ponciano.lab.Spalodwfs.geotime.models.StringForm;
import info.ponciano.lab.Spalodwfs.geotime.models.geojson.GeoJsonRDF;
import info.ponciano.lab.Spalodwfs.geotime.models.semantic.KB;
import info.ponciano.lab.Spalodwfs.geotime.models.semantic.OntoManagementException;
import info.ponciano.lab.pisemantic.PiOnt;
import info.ponciano.lab.pisemantic.PiSparql;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class AssessmentController {

    String r = null;
    private final StorageService storageService;
    private SparqlEnrichModel sem;

    @Autowired
    public AssessmentController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/assessment")
    public String lod(Model model) throws OntoManagementException {
        String rtn = "assessment";
        try {
            PiOnt ont = KB.get().getOnt();
            OntClass ontClass = ont.createClass(GeoJsonRDF.DCAT_DATASET);
            //get lists of Dataset URI
            List<Individual> individuals = ont.getIndividuals(ontClass);

            model.addAttribute("individuals", individuals.toArray());
            return rtn;
        } catch (Exception ex) {
            Logger.getLogger(GeoJsonController.class.getName()).log(Level.SEVERE, null, ex);
            model.addAttribute("message", ex.getMessage());
            return "error";
        }
    }

    @ModelAttribute(name = "stringform")
    public StringForm stringform() {
        return new StringForm();
    }

    @ModelAttribute(name = "squery")
    public SparqlQuery sparqlquery() {
        return new SparqlQuery();
    }
    // initialize the model attribute "dataindiv"
    @ModelAttribute(name = "dataindiv")
    public GeoJsonForm dataindiv() {
        return new GeoJsonForm();
    }
    @PostMapping("/select")
    public String results(@ModelAttribute("dataindiv") GeoJsonForm di, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("message", bindingResult.getAllErrors().toString());
            return "geojson/geoJSON";
        }
        String uri = di.getName();

//        try {
//            String query=  sq.getResults();
//            String triplestore = "https://" + sq.getTriplestore() + ".org/sparql";
//            this.sem=new SparqlEnrichModel(triplestore,query);
//
//        } catch (Exception e) {
//            r = e.getMessage();
//        }
//        //add attributes to model
//        model.addAttribute("cl", this.sem.getHeader());
//        model.addAttribute("MDlist", this.sem.getData());
//        model.addAttribute("errorMessage", r);
//        model.addAttribute("ontPath", this.sem.getOntPath());

        return "assessment";
    }

}
