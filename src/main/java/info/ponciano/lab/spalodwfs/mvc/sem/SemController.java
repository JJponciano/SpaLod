package info.ponciano.lab.spalodwfs.mvc.sem;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import info.ponciano.lab.spalodwfs.mvc.models.semantic.OntoManagementException;


@Controller
public class SemController {

    public SemController(){
    }

    @GetMapping("/sem")
    public String lod(Model model) throws OntoManagementException {
        return "sem";
    }

}
