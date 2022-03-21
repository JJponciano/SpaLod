package info.ponciano.lab.Spalodwfs;

import info.ponciano.lab.Spalodwfs.geotime.models.SparqlQuery;
import info.ponciano.lab.Spalodwfs.geotime.models.StringForm;
import info.ponciano.lab.Spalodwfs.geotime.models.semantic.OntoManagementException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class LodController {
    @GetMapping("/lod")
    public String lod(Model model) throws OntoManagementException {
        String rtn="lod";
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

}
