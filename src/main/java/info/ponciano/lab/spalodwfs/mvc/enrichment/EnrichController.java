package info.ponciano.lab.spalodwfs.mvc.enrichment;

import info.ponciano.lab.spalodwfs.controller.storage.StorageService;
import info.ponciano.lab.spalodwfs.mvc.controllers.last.GeoJsonController;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.OntoManagementException;
import info.ponciano.lab.pisemantic.PiOntologyException;
import info.ponciano.lab.pisemantic.PiSparql;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class EnrichController {

    private final StorageService storageService;
    private UnknownDataManager udm;
    private List<String[]> data;
    private Progress progress=new Progress(0,"no current processing");
    @Autowired
    public EnrichController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/enrichment")
    public String home(Model model){
        return "enrichment";
    }
    @GetMapping(value = "/progress")
    public @ResponseBody
    Progress getProgress() {
        return this.progress;
    }
    @PostMapping("/matching_validation")
    public String matching(@ModelAttribute MatchingDataCreationDto form, Model model) {
        String message = "Knowledge base enriched !";

        try {
            List<MatchingDataModel> data = form.getData();
            for (MatchingDataModel datum : data) {
                // for each value, replace in the data the input by the value
                String value = "<" + datum.getValue() + ">";
                String input = "<" + datum.getInput() + ">";
                if (value != null && !value.isBlank()&&value.contains("http")&&value.contains("/")) {
                    for (int i = 0; i < this.data.size(); i++) {
                        String[] strings = this.data.get(i);
                        for (int j = 0; j < strings.length; j++) {
                            if (strings[j].equals(input))
                                strings[j] = value;
                        }
                    }
                }
            }
            this.enrich();
        } catch (OntoManagementException | IOException e) {
            message = "Wrong URI given:\n" + e.getMessage();
        }

        model.addAttribute("message", message);
        return "/enrichment";
    }

    @PostMapping("/enrichment")
    public String enrichment(@RequestParam("file") MultipartFile file, Model model) {
        this.progress=new Progress(0,"initialisation");
        System.out.println("------------------------/enrichment");

        try {
            this.progress.setValue(10);
            this.progress.setMessage("Ontology reading...");
            System.out.print("Ontology reading...");
            String ext = file.getOriginalFilename().toLowerCase();
            if (ext.endsWith(".owl")||ext.endsWith(".ttl")||ext.endsWith(".ont")||ext.endsWith(".rdf")) {
                // store file
                storageService.store(file);
                this.data = new ArrayList<>();
                //File reading
                String filename = file.getOriginalFilename();
                String file_path = KB.STORAGE_DIR + "/" + filename;
                //Read the ontology
                OntModel target = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
                target.read(file_path);
                OntModel kb = KB.get().getOnt().getOnt();

                System.out.println("DONE!");
                this.progress.setValue(30);
                this.progress.setMessage("Data matching...");
                System.out.print("Analysing data...");
                //Extract data and split them in unknown, known and
                this.udm = new UnknownDataManager(kb, target);
                udm.run();
                //get data unknown to send to the user
                MatchingDataCreationDto data_unknown = udm.getData_unknown();
                List<String[]> remainingData = udm.getRemainingData();
                if (!remainingData.isEmpty()) {
                    model.addAttribute("form", data_unknown);
                }
                System.out.println("DONE!");
                this.progress.setValue(90);
                this.progress.setMessage("Ontology saving...");
                List<String[]> data_known = udm.getData_known();
                this.data.addAll(data_known);
                this.data.addAll(remainingData);
                //LOG
                String message = "";
                if (remainingData.isEmpty()) message = "Information  integrated !" + System.lineSeparator();
                List<RDFNode> noUri = udm.getNoUri();
                for (RDFNode ind : noUri) {
                    message += "\n " + ind.toString() + " has not an URI";
                }

                model.addAttribute("message", message);
                KB.get().save();
                this.progress.setValue(100);
                this.progress.setMessage("Work done!");
            }else{
                model.addAttribute("message", "File format not allowed. Please use the following: rdf, ttl, owl, or ont.");
            }
            return "enrichment";
        } catch (Exception ex) {
            Logger.getLogger(GeoJsonController.class.getName()).log(Level.SEVERE, null, ex);
            model.addAttribute("message", ex.getMessage());
            return "error";
        }
    }

    private void enrich() throws OntoManagementException, IOException {

        if (!this.data.isEmpty()) {
            // enrich the ontology with the data know
            for (String[] strings : this.data) {
                KB.get().update(strings);
            }
            KB.get().save();
        }
    }

    private void update(PiSparql ont, Resource s, Resource p, @NotNull RDFNode o) throws PiOntologyException {
        if (o.isResource()) {
            String query1 = "INSERT DATA{<" + s.getURI() + "> <" + p.getURI() + "><" + o.asResource().getURI() + ">}";
            ont.update(query1);
        } else {
            String query = "INSERT DATA{<" + s.getURI() + "> <" + p.getURI() + ">" + o.asLiteral() + "}";
            ont.update(query);
        }
    }

    // initialize the model attribute "dataindiv"
    @ModelAttribute(name = "vocMatching")
    public MatchingForm vocMatching() {
        return new MatchingForm();
    }
}
