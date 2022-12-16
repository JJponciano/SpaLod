/*
 * Copyright (C) 2020 claireprudhomme.
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
package info.ponciano.lab.spalodwfs.geotime.models;

import info.ponciano.lab.spalodwfs.geotime.models.semantic.KB;
import info.ponciano.lab.spalodwfs.geotime.models.semantic.OntoManagementException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author claireprudhomme
 */
public class Catalogs {
    private JSONObject jo;
    public List<Catalog> catalogues;

    public Catalogs() throws OntoManagementException {
        this.jo=new JSONObject();
        this.catalogues=new ArrayList<Catalog>();
        //create an array of distribution
        JSONArray jodists=new JSONArray();
        //catalogs description in json
        JSONObject jodist1=new JSONObject();
        jodist1.put("href", "https://Spalodwfs.herokuapp.com/api/SpalodWFS/collections?f=json");
        jodist1.put("rel", "self");
        jodist1.put("type", "application/json");
        jodist1.put("title", "this document");
        jodists.put(jodist1);
        //catalogs description in xml
        JSONObject jodist2=new JSONObject();
        jodist2.put("href", "https://Spalodwfs.herokuapp.com/api/SpalodWFS/collections?f=xml");
        jodist2.put("rel", "alternate");
        jodist2.put("type", "text/xml");
        jodist2.put("title", "this document as XML");
        jodists.put(jodist2);
        //catalogs description in html
        JSONObject jodist3=new JSONObject();
        jodist3.put("href", "https://Spalodwfs.herokuapp.com/api/SpalodWFS/collections?f=html");
        jodist3.put("rel", "alternate");
        jodist3.put("type", "text/html");
        jodist3.put("title", "this document as HTML");
        jodists.put(jodist3);
        //adding the array describing the distributions
        jo.put("links", jodists);
        
        //create an array of collections
        JSONArray collections=new JSONArray(); 
        List<String[]> cSet=initCatalogSet();
        for (int i=0; i<cSet.size(); i++){
            Catalog c=new Catalog(cSet.get(i)[0]);
            this.catalogues.add(c);
            JSONObject cjson=c.getJo();
            collections.put(cjson);
        }
        //adding the array describing the collections
        jo.put("collections", collections);
    }
    
    private List<String[]> initCatalogSet() throws OntoManagementException{
        List<String[]> info = new ArrayList<String[]>();
        //initialize the query to retrieve the title and the description of the catalog
            String query = "SELECT ?c "
                    + "WHERE{"
                    +"?c rdf:type dcat:Catalog."
                    + "}";
            System.out.println(KB.get().getSPARQL(query));
            //create the table of variables
            String[] var = {"c"};
            //query the ontology
            info = KB.get().queryAsArray(query, var, false, false);
            return info;
    }

    public JSONObject getJo() {
        return jo;
    }
    
}
