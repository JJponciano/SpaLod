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
package info.ponciano.lab.Spalodwfs;

import info.ponciano.lab.Spalodwfs.geotime.controllers.last.sparql.SparqlQuery;
import info.ponciano.lab.Spalodwfs.geotime.controllers.storage.StorageService;
import info.ponciano.lab.Spalodwfs.geotime.models.StringForm;
import info.ponciano.lab.Spalodwfs.geotime.models.semantic.KB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Dr. Jean-Jacques Ponciano
 */
@Controller
public class EndPointController {

    String r = null;
    List<String> columnNames = new ArrayList<String>();
    List<String[]> resultList = new ArrayList<String[]>();
    private final StorageService storageService;

    @Autowired
    public EndPointController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/sparqlend")
    public String home(Model model) {
        model.addAttribute("queryresult", "");
        return "endpoint";
    }

    @ModelAttribute(name = "squery")
    public SparqlQuery sparqlquery() {
        return new SparqlQuery();
    }

    @ModelAttribute(name = "stringform")
    public StringForm stringform() {
        return new StringForm();
    }

    @PostMapping("/sparqlend_query")
    public String query(@ModelAttribute("squery") SparqlQuery sq, Model model) throws Exception {

        //prefixes for SPARQL query
        String prefixes = "PREFIX schema: <http://schema.org/>"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                + "PREFIX hist: <http://wikiba.se/history/ontology#>"
                + "PREFIX wd: <http://www.wikidata.org/entity/>"
                + "PREFIX wdt: <http://www.wikidata.org/prop/direct/>"
                + "PREFIX wikibase: <http://wikiba.se/ontology#>"
                + "PREFIX dct: <http://purl.org/dc/terms/>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "PREFIX bd: <http://www.bigdata.com/rdf#>"
                + "PREFIX wds: <http://www.wikidata.org/entity/statement/>\r\n"
                + "PREFIX wdv: <http://www.wikidata.org/value/>"
                + "PREFIX p: <http://www.wikidata.org/prop/>\r\n"
                + "PREFIX ps: <http://www.wikidata.org/prop/statement/>\r\n"
                + "PREFIX psv: <http://www.wikidata.org/prop/statement/value/>"
                + "PREFIX gs: <http://www.opengis.net/ont/geosparql#>"
                + "PREFIX pq: <http://www.wikidata.org/prop/qualifier/>";
        try {
            String queryString = prefixes + sq.getQuery();
            r = KB.get().getOnt().selectAsText(queryString);
            List<String> cn = new ArrayList<String>();
            List<String[]> rl = new ArrayList<String[]>();
            //store results in ResultSet format
            ResultSet resultset = KB.get().getOnt().select(queryString);
            //gives the column names of the query
            cn = getResults(resultset, rl);
            this.columnNames = cn;
            this.resultList = rl;

        } catch (Exception e) {
            r = e.getMessage();
        }

        //add Attribute to model
        model.addAttribute("queryresult", r);
        model.addAttribute("cl", columnNames);
        model.addAttribute("MDlist", resultList);
        model.addAttribute("errorMessage", r);

        return "endpoint";
    }

    public static List<String> getResults(ResultSet resultset, List<String[]> rl) {
        List<String> cn;

        cn = resultset.getResultVars();
        List<Integer> numberOfColumns = new ArrayList<Integer>();
        for (int i = 0; i < cn.size(); i++) {
            numberOfColumns.add(i);
        }
        //for all the QuerySolution in the ResultSet file
        while(resultset.hasNext()){
            QuerySolution solu = resultset.next();
            String[] ls = new String[4];
            RDFNode object = solu.get("?o");

            String[] schoolkeywords = {"schul","gymnasium","kolleg","collegium","school","lyzeum","akademie","academy","école","berufsbildungszentrum","bsz","studien","bildungs","seminar","skolen","universität"};
            String[] restaurantkeywords = {"restaurant"};

            if (object.isLiteral()){
                String point_toprocess = object.asLiteral().getString();
                String a = point_toprocess.split("\\(" ) [1];
                a = a.split("\\)" ) [0];
                String[] coord = a.split(" ");
                String x = coord[1];
                String y = coord[0];
                ls[0] = x;
                ls[1] = y;
                ls[2] = solu.get("?l").asLiteral().getString();
                ls[3]="default";



                for(String s :schoolkeywords ) {
                    if (ls[2].toLowerCase().contains(s)) {
                        ls[3] = "school";
                        break;
                    }
                }
                if(ls[3]=="default") {
                    for (String s : restaurantkeywords) {
                        if (ls[2].toLowerCase().contains(s)) {
                            ls[3] = "restaurant";
                            break;
                        }
                    }
                }

                rl.add(ls);
            }
        }
        return cn;
    }

}