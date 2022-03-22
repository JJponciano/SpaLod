package info.ponciano.lab.Spalodwfs;

import info.ponciano.lab.Spalodwfs.geotime.controllers.last.GeoJsonController;
import info.ponciano.lab.Spalodwfs.geotime.controllers.storage.StorageService;
import info.ponciano.lab.Spalodwfs.geotime.models.semantic.KB;
import info.ponciano.lab.Spalodwfs.geotime.models.semantic.OntoManagementException;
import info.ponciano.lab.pisemantic.PiSparql;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class EnrichController {

    private final StorageService storageService;

    @Autowired
    public EnrichController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/enrichment")
    public String lod(Model model) throws OntoManagementException {
        String rtn = "enrichment";
        return rtn;
    }

    @PostMapping("/enrichment")
    public String uplift(@RequestParam("file") MultipartFile file, Model model) {
        try {
            //RedirectAttributes redirectAttributes) {
            // store file
            storageService.store(file);

            //File reading
            String filename = file.getOriginalFilename();
            String file_path = KB.STORAGE_DIR + "/" + filename;
            //Read the ontology
            PiSparql ont = new PiSparql(file_path);

            //init list of unknown properties and classes
           MatchingDataCreationDto unknown = new MatchingDataCreationDto();
            List<String[]> remainingData = new ArrayList<>();

            //extract subject predicate and object for each individuals
            ResultSet select = ont.select("Select ?s ?p ?o WHERE{?s ?p ?o}");
            OntModel kb = KB.get().getOnt().getOnt();

            while (select.hasNext()) {
                QuerySolution next = select.next();
                Resource s = next.getResource("?s");
                Resource p = next.getResource("?p");
                RDFNode o = next.get("?o");
                //test if the property is known in the ontology
                boolean knowP = kb.getOntProperty(p.getURI()) == null;
                if (knowP) {
                    unknown.add(new MatchingDataModel(p.getURI(),""));
                }
                 if( o.isResource()&&o.asResource().getURI()==null){
                     System.err.printf("------------- URI NULL FOR: "+o);
                 }else {
                     boolean knowC = o.isResource() && kb.getOntClass(o.asResource().getURI()) == null;
                     if (knowC) {
                         unknown.add(new MatchingDataModel(o.asResource().getURI(),""));
                     }
                     //if the property and the class(if it is ) are known, add it to the ontology.
                     if (knowP && (!o.isResource() || knowC)) {
//                    if (o.isResource()) {
//                        String query1 = "INSERT DATA{<" + s.getURI() + "> <" + p.getURI() + "><" + o.asResource().getURI() + ">}";
//                        ont.update(query1);
//                    } else {
//                        String query = "INSERT DATA{<" + s.getURI() + "> <" + p.getURI() + ">" + o.asLiteral() + "}";
//                        ont.update(query);
//                    }
                     } else {
                         if (o.isResource())
                             remainingData.add(new String[]{s.getURI(), p.getURI(), o.asResource().getURI()});
                         else
                             remainingData.add(new String[]{s.getURI(), p.getURI(), o.asLiteral().toString()});
                     }
                 }
            }
            KB.get().save();

            if (unknown.getData().isEmpty())
                model.addAttribute("message", "Knowledge base enriched !");
            else {
                model.addAttribute("form", unknown);
            }
            return "enrichment";
        } catch (Exception ex) {
            Logger.getLogger(GeoJsonController.class.getName()).log(Level.SEVERE, null, ex);
            model.addAttribute("message", ex.getMessage());
            return "error";
        }
    }
    // initialize the model attribute "dataindiv"
    @ModelAttribute(name = "vocMatching")
    public MatchingForm vocMatching() {
        return new MatchingForm();
    }
}
