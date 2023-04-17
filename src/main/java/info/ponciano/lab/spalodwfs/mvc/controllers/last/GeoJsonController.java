/*
 * Copyright (C) 2021 Dr. Jean-Jacques Ponciano.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package info.ponciano.lab.spalodwfs.mvc.controllers.last;

import info.ponciano.lab.spalodwfs.mvc.controllers.storage.StorageProperties;
import info.ponciano.lab.spalodwfs.mvc.controllers.storage.StorageService;
import info.ponciano.lab.spalodwfs.mvc.models.geojson.GeoJsonRDF;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;
import info.ponciano.lab.pisemantic.PiOnt;
import info.ponciano.lab.pitools.files.PiFile;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Dr. Jean-Jacques Ponciano
 */
@Controller
@RequestMapping("/geojson")
public class GeoJsonController {

    private final StorageService storageService;

    @Autowired
    public GeoJsonController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/uplift")
    public String upliftView(Model model) {
        return "geojson/geoJSON";
    }

    @GetMapping("/downlift")
    public String downliftView(Model model) {

        try {
            PiOnt ont = KB.get().getOnt();
            OntClass ontClass = ont.createClass(GeoJsonRDF.DCAT_DATASET);
            //get lists of Dataset URI
            List<Individual> individuals = ont.getIndividuals(ontClass);

            model.addAttribute("individuals", individuals.toArray());
            return "geojson/geoJSONdownlift";
        } catch (Exception ex) {
            Logger.getLogger(GeoJsonController.class.getName()).log(Level.SEVERE, null, ex);
            model.addAttribute("message", ex.getMessage());
            return "error";
        }

    }

    @PostMapping("/uplift")
    public String uplift(@RequestParam("file") MultipartFile file, Model model) {
        try {
            //RedirectAttributes redirectAttributes) {
            // store file
            storageService.store(file);

            //File reading
            String filename = file.getOriginalFilename();
            String geojsonfilepath = KB.STORAGE_DIR + "/" + filename;

            //execute the uplift
            GeoJsonRDF.upliftGeoJSON(geojsonfilepath, KB.get().getOnt());
            KB.get().save();
            String out = "Spalod.owl";
            String res = new StorageProperties().getLocation() + "/" + out;
            System.out.println(res);
            new PiFile(KB.OUT_ONTO).copy(res);
            model.addAttribute("message", "File uplifted, you can now download the complet ontology");
            model.addAttribute("message", "Uplifting of " + out + " successfully completed !");
            model.addAttribute("file", "/files/" + out);
            return "success";
        } catch (Exception ex) {
            Logger.getLogger(GeoJsonController.class.getName()).log(Level.SEVERE, null, ex);
            model.addAttribute("message", ex.getMessage());
            return "error";
        }
    }

    @PostMapping("/downlift")
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
            return "success";
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
