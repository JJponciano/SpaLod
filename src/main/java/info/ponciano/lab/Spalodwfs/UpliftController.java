package info.ponciano.lab.Spalodwfs;

import info.ponciano.lab.Spalodwfs.geotime.controllers.last.EnrichmentController;
import info.ponciano.lab.Spalodwfs.geotime.controllers.last.GeoJsonController;
import info.ponciano.lab.Spalodwfs.geotime.controllers.last.GeoJsonForm;
import info.ponciano.lab.Spalodwfs.geotime.controllers.storage.FileDownloadController;
import info.ponciano.lab.Spalodwfs.geotime.controllers.storage.StorageProperties;
import info.ponciano.lab.Spalodwfs.geotime.controllers.storage.StorageService;
import info.ponciano.lab.Spalodwfs.geotime.models.SparqlQuery;
import info.ponciano.lab.Spalodwfs.geotime.models.StringForm;
import info.ponciano.lab.Spalodwfs.geotime.models.geojson.Feature;
import info.ponciano.lab.Spalodwfs.geotime.models.geojson.GeoJsonRDF;
import info.ponciano.lab.Spalodwfs.geotime.models.geojson.Geometry;
import info.ponciano.lab.Spalodwfs.geotime.models.semantic.KB;
import info.ponciano.lab.Spalodwfs.geotime.models.semantic.OntoManagementException;
import info.ponciano.lab.pisemantic.PiOnt;
import info.ponciano.lab.pisemantic.PiSparql;
import info.ponciano.lab.pitools.files.PiFile;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class UpliftController {

    String r = null;
    List<String> columnNames = new ArrayList<String>();
    List<String[]> resultList = new ArrayList<String[]>();
    private final StorageService storageService;

    @Autowired
    public UpliftController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/uplift")
    public String lod(Model model) throws OntoManagementException {
        String rtn = "uplift";
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

    @PostMapping("/geojson_uplift")
    public String uplift(@RequestParam("file") MultipartFile file, Model model) {
        System.out.println("-------------------------/geojson_uplift" + " :");

            try {
                if (file.getOriginalFilename().toLowerCase().endsWith(".geojson")){
                //RedirectAttributes redirectAttributes) {
                // store file
                storageService.store(file);

                //File reading
                String filename = file.getOriginalFilename();
                String geojsonfilepath = KB.STORAGE_DIR + "/" + filename;

                //execute the upliftkl
                PiSparql result = new PiSparql();
                GeoJsonRDF.upliftGeoJSON(geojsonfilepath, result);
                String out = filename + ".owl";
                String res = new StorageProperties().getLocation() + "/" + out;
                try {
                    result.write(res);
                    System.out.println("Results saved: " + res);
                    model.addAttribute("message", "The file was uplifted, you can now download the complete ontology");
                    model.addAttribute("file", "/files/" + out);
                } catch (org.apache.jena.shared.BadURIException e) {
                    String query = "SELECT ?s ?p ?o WHERE {?s ?p ?o}";

                    String onto_triples = result.selectAsText(query);
                    new PiFile("log.txt").writeTextFile(onto_triples);
                    System.out.println(onto_triples);
                    new PiFile("tmp.txt").writeTextFile(onto_triples);
                    model.addAttribute("message", "File cannot be converted due to malformed URI");
                }
            } else {
            model.addAttribute("message", "File extension must be geojson");
        }
                return "uplift";
            } catch (Exception ex) {
                Logger.getLogger(GeoJsonController.class.getName()).log(Level.SEVERE, null, ex);
                model.addAttribute("message", ex.getMessage());
                return "error";
            }

    }

    @PostMapping("/geojson_downlift")
    public String downlift(@ModelAttribute("dataindiv") GeoJsonForm di, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("message", bindingResult.getAllErrors().toString());
            return "geojson/geoJSON";
        }
        String uri = di.getName();
        try {
            //downlift
            String downlift = GeoJsonRDF.downlift(KB.get().getOnt(), uri);
            //save the file
            String out = uri.substring(uri.lastIndexOf('#') + 1, uri.length()) + ".geojson";
            String res = new StorageProperties().getLocation() + "/" + out;
            System.out.println(res);
            new PiFile(res).writeTextFile(downlift);

            model.addAttribute("message", "Downlifting of " + out + " successfully completed !");
            model.addAttribute("file", "/files/" + out);
            return "uplift";
        } catch (Exception ex) {
            Logger.getLogger(GeoJsonController.class.getName()).log(Level.SEVERE, null, ex);
            model.addAttribute("message", ex.getMessage());
            return "error";
        }

    }

    // initialize the model attribute "dataindiv"
    @ModelAttribute(name = "dataindiv")
    public GeoJsonForm dataindiv() {
        return new GeoJsonForm();
    }
}
