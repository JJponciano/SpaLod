package info.ponciano.lab.spalodwfs.sem;

import info.ponciano.lab.spalodwfs.geotime.models.semantic.OntoManagementException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class SemController {

    public SemController(){
    }

    @GetMapping("/sem")
    public String lod(Model model) throws OntoManagementException {
        return "sem";
    }

}
