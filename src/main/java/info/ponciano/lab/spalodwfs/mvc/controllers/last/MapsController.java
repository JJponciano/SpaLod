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

import info.ponciano.lab.spalodwfs.mvc.controllers.storage.StorageService;
import info.ponciano.lab.spalodwfs.mvc.models.geojson.GeoJsonRDF;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;
import info.ponciano.lab.pisemantic.PiOnt;
import info.ponciano.lab.pitools.utility.PiRegex;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.query.ResultSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Dr. Jean-Jacques Ponciano
 */
@Controller
@RequestMapping("/maps")
public class MapsController {

    private final StorageService storageService;

    @Autowired
    public MapsController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String maps(Model model) {

        try {
            PiOnt ont = KB.get().getOnt();
            OntClass ontClass = ont.createClass(GeoJsonRDF.DCAT_DATASET);
            //get lists of Dataset URI
            List<Individual> individuals = ont.getIndividuals(ontClass);

            model.addAttribute("individuals", individuals.toArray());
            return "sparql/maps";
        } catch (Exception ex) {
            Logger.getLogger(MapsController.class.getName()).log(Level.SEVERE, null, ex);
            model.addAttribute("message", ex.getMessage());
            return "error";
        }

    }

    @PostMapping("/select")
    public String downlift(@ModelAttribute("dataindiv") GeoJsonForm di, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("message", bindingResult.getAllErrors().toString());
            return "sparql/maps/select";
        }
        String uriMap = di.getName();
        try {
            String query = "SELECT  ?item ?itemLabel ?value Where{"
                    + "<" + uriMap + "> spalod:hasFeature ?item . "
                    + "?item spalod:hasLabel  ?itemLabel ."
                    + " ?item geosparql:hasGeometry ?g."
                    + " ?g geosparql:asWKT  ?value"
                    + "}";
            ResultSet select = KB.get().getOnt().select(query);
            boolean hasNext = select.hasNext();
            //if the results has no next its mean they is no labels
            if (!hasNext) {
                query = "SELECT  ?item ?itemLabel ?value Where{"
                        + "<" + uriMap + "> spalod:hasFeature ?item . "
                        + "?item spalod:Name  ?itemLabel ."
                        + " ?item geosparql:hasGeometry ?g."
                        + " ?g geosparql:asWKT  ?value"
                        + "}";
                select = KB.get().getOnt().select(query);
            }

            List<String> cn = new ArrayList<String>();
            List<String[]> rl = new ArrayList<String[]>();
            cn = EnrichmentController.getResults(select, rl);
            cn.remove(cn.get(cn.size() - 1));
            cn.add("latitude");
            cn.add("longitude");

            List<String[]> results = new ArrayList<String[]>();
            for (String[] strings : rl) {
                String[] val = new String[strings.length + 1];
                val[0] = strings[0];
                val[1] = strings[1];
                int b = strings[2].indexOf("(") + 1;
                int e = strings[2].lastIndexOf(")");
                String[] s = strings[2].substring(b, e).split(PiRegex.whiteCharacter);
                val[2] = s[1];
                val[3] = s[0];
                results.add(val);
            }
            //add attributes to model
            model.addAttribute("cl", cn);
            model.addAttribute("MDlist", results);
            return "sparql/maps";
        } catch (Exception ex) {
            Logger.getLogger(MapsController.class.getName()).log(Level.SEVERE, null, ex);
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
