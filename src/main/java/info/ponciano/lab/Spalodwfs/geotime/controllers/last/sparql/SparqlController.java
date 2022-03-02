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
package info.ponciano.lab.Spalodwfs.geotime.controllers.last.sparql;

import info.ponciano.lab.Spalodwfs.geotime.controllers.storage.StorageService;
import info.ponciano.lab.Spalodwfs.geotime.models.semantic.KB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Dr. Jean-Jacques Ponciano
 */
@Controller
@RequestMapping(value = "/sparqlend")
public class SparqlController {

    private final StorageService storageService;

    @Autowired
    public SparqlController(StorageService storageService) {
        this.storageService = storageService;
    }
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("queryresult", "");
        return "sparql/sparqlEP";
    }

    @ModelAttribute(name = "squery")
    public SparqlQuery sarqlquery() {
        return new SparqlQuery();
    }

    @PostMapping("/query")
    public String query(@ModelAttribute("squery") SparqlQuery sq, Model model) throws Exception {
        String r;
        try {
            r = KB.get().getOnt().selectAsText(sq.getQuery());
        } catch (Exception e) {
            r = e.getMessage();
        }

        model.addAttribute("queryresult", r);
        return "sparql/sparqlEP";
    }

}
