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
package info.ponciano.lab.spalodwfs.mvc;

import info.ponciano.lab.spalodwfs.mvc.controllers.last.sparql.SparqlQuery;
import info.ponciano.lab.spalodwfs.mvc.controllers.storage.StorageService;
import info.ponciano.lab.spalodwfs.mvc.models.StringForm;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;

import java.util.ArrayList;
import java.util.List;

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

          //TODO DOES NOT WORK, use FEATURE and WKT with point for geometry  cn = getResults(resultset, rl);
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

            if (object.isLiteral()){
                String point_toprocess = object.asLiteral().getString();
                String a = point_toprocess.split("\\(" ) [1];
                a = a.split("\\)" ) [0];
                String[] coord = a.split(" ");
                String x = coord[1];
                String y = coord[0];
                ls[2] = x;
                ls[3] = y;
                ls[1] = solu.get("?l").asLiteral().getString().replace("@de","");
                String code = "";
                try{ code =solu.get("?c").asLiteral().getString();}catch(Exception e){}


                switch(code){
                    case "Q3914":
                        ls[0]="HS";
                        break;
                    case "Q1195942":
                        ls[0]="Feuerwehr";
                        break;
                    case "Q16917":
                        ls[0]="KHV";
                        break;
                    case "Q33506":
                        ls[0]="Museen";
                        break;
                    case "Q205495":
                        ls[0]="Tankstellen";
                        break;
                    case "Q2140665": // or Q1477760
                        ls[0]="LadeSt";
                        break;
                    case "Q180846":
                        ls[0]="Supermarkt";
                        break;
                    case "Q7075":
                        ls[0]="Bibliothek";
                        break;
                    case "Q44782":
                        ls[0]="Seehaefen";
                        break;
                    case "Q55488":
                        ls[0]="Bahnhof";
                        break;
                    case "Q11707":
                        ls[0]="Restaurant";
                        break;
                    case "Q41253":
                        ls[0]="Kino";
                        break;
                    case "Q4989906":
                        ls[0]="Denkmal";
                        break;
                    case "Q861951":
                        ls[0]="BPOL";
                        break;
                    case "Q27686":
                        ls[0]="Hotel";
                        break;
                    case "Q483110":
                        ls[0]="Stadium";
                        break;
                    case "Q1248784":
                        ls[0]="Flughaefen";
                        break;
                    case "Q22908":
                        ls[0]="Seniorenheime";
                        break;
                    case "Q200023":
                        ls[0]="Schwimmbad";
                        break;
                    case "Q19010":
                        ls[0]="Wetterstation";
                        break;
                    case "Q483242":
                        ls[0]="Laboratorium";
                        break;
                    case "Q364005":
                        ls[0]="Kita";
                        break;
                    default :
                        ls[0]="KmBAB";
                }
                rl.add(ls);
            }
        }
        return cn;
    }

}