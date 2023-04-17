package info.ponciano.lab.spalodwfs.mvc.uplift;

import info.ponciano.lab.spalodwfs.mvc.controllers.last.GeoJsonController;
import info.ponciano.lab.spalodwfs.mvc.controllers.last.GeoJsonForm;
import info.ponciano.lab.spalodwfs.mvc.controllers.storage.FileDownloadController;
import info.ponciano.lab.spalodwfs.mvc.controllers.storage.StorageProperties;
import info.ponciano.lab.spalodwfs.mvc.controllers.storage.StorageService;
import info.ponciano.lab.spalodwfs.mvc.models.geojson.GeoJsonRDF;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.OntoManagementException;
import info.ponciano.lab.spalodwfs.mvc.sem.SemData;
import info.ponciano.lab.pisemantic.PiOnt;
import info.ponciano.lab.pisemantic.PiSparql;
import info.ponciano.lab.pitools.files.PiFile;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
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
        // store file
        storageService.store(file);
        //File reading
        String filename = file.getOriginalFilename();
        String geojsonfilepath = KB.STORAGE_DIR + "/" + filename;
        String out = filename + ".owl";
        String res = new StorageProperties().getLocation() + "/" + out;

        try {
            PiSparql result = KB.get().getOntEmpty();
            if (file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                //uplift
                GeoJsonRDF.csv2RDF(result, geojsonfilepath);
                result.write(res);
            } else if (file.getOriginalFilename().toLowerCase().endsWith(".geojson")) {
                //RedirectAttributes redirectAttributes) {

                //execute the upliftkl
                GeoJsonRDF.upliftGeoJSON(geojsonfilepath, result);

//                try {
                result.write(res);

//                } catch (org.apache.jena.shared.BadURIException e) {
//                    String query = "SELECT ?s ?p ?o WHERE {?s ?p ?o}";
//
//                    String onto_triples = result.selectAsText(query);
//                    new PiFile("log.txt").writeTextFile(onto_triples);
//                    System.out.println(onto_triples);
//                    new PiFile("tmp.txt").writeTextFile(onto_triples);
//                    model.addAttribute("message", "File cannot be converted due to malformed URI");
//                }
            } else
                model.addAttribute("message", "File extension must be geojson");


            System.out.println("Results saved: " + res);
            model.addAttribute("message", "The file was uplifted, you can now download the ontology corresponding to the data uplifted!");
            model.addAttribute("file", "/files/" + out);

            ResultSet select = result.select("SELECT ?s ?p ?o WHERE{?s ?p ?o}");
           List<String[]>data=new ArrayList<>();
            while (select.hasNext()){
                QuerySolution next = select.next();
                String s = next.get("?s").toString();
                String p= next.get("?p").toString();
                String o = next.get("?o").toString();
                data.add(new String[]{s,p,o});
            }
            model.addAttribute("data", new SemData(List.of("Subject","Predicate","Object"),data,out));
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
