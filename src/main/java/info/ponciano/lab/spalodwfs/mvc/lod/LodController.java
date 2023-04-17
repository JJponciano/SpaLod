package info.ponciano.lab.spalodwfs.mvc.lod;

import info.ponciano.lab.spalodwfs.controller.storage.StorageService;
import info.ponciano.lab.spalodwfs.mvc.controllers.last.EnrichmentController;
import info.ponciano.lab.spalodwfs.mvc.models.SparqlQuery;
import info.ponciano.lab.spalodwfs.mvc.models.StringForm;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.OntoManagementException;
import info.ponciano.lab.pisemantic.PiSparql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class LodController {

    String r = null;
    private final StorageService storageService;
    private ExtractFromLOD sem;

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
     SparqlQuery sparqlquery() {
        return new SparqlQuery();
    }


    @PostMapping("/lod")
    public ExtractFromLOD results(@ModelAttribute("squery") SparqlQuery sq, Model model) throws Exception {

        try {
            String query=  sq.getResults();
            String triplestore = "https://" + sq.getTriplestore() + ".org/sparql";
            this.sem=new ExtractFromLOD(triplestore,query);

        } catch (Exception e) {
            r = e.getMessage();
        }
        //add attributes to model
//        model.addAttribute("cl", this.sem.getHeader());
//        model.addAttribute("MDlist", this.sem.getData());
//        model.addAttribute("errorMessage", r);
//        model.addAttribute("ontPath", this.sem.getOntPath());
//        return "lod";
        return this.sem;
    }

//    @PostMapping("/lodenrich")
//    public String lodenrich( Model model) {
//        try {
//            PiSparql ont = KB.get().getOnt();
//            this.sem.enrich(ont);
//            KB.get().save();
//            model.addAttribute("message", "The Knowledge-base was enriched!");
//            return "lod";
//        } catch (Exception ex) {
//            Logger.getLogger(EnrichmentController.class.getName()).log(Level.SEVERE, null, ex);
//            model.addAttribute("message", ex.getMessage());
//            return "lod";
//        }
//    }
}
