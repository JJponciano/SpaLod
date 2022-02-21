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
package info.ponciano.lab.Spalodwfs.models;

import info.ponciano.lab.Spalodwfs.models.semantic.KB;
import info.ponciano.lab.Spalodwfs.models.semantic.OntoManagementException;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author claireprudhomme
 */
public class Metadata {
    
    //private String[] delivryMd;
    //private Object c;
    
    private String delivryMd;
    private String delivryDS;
    
    public List<String[]> getMetadata() throws OntoManagementException{
        List<String[]> info = new ArrayList<String[]>();

            //initialize the query to retrieve all instances of metadata and their associated organization, title, and dataset title
            String query = "SELECT DISTINCT ?m ?o ?t ?dt "
                    + "WHERE{"
                    + "?m rdf:type iso115:MD_Metadata. "
                    + "?m <http://xmlns.com/foaf/0.1/primaryTopic> ?d. "
                    + "?d <http://purl.org/dc/elements/1.1/title> ?dt. "
                    + "?m iso115:contact ?co. "
                    + "?co iso115:organisationName ?o. "
                    + "?m iso115:identificationInfo ?i. "
                    + "?i iso115:citation ?ci. "
                    + "?ci iso115:title ?t. "
                    + "}";
            //create the table of variables
            String[] var = {"m", "o", "t", "dt"};
            //query the ontology
            info = KB.get().queryAsArray(query, var, false, true);
        return info;
    }


    

    
    public String getDelivryMd() {
    return delivryMd;
    }
    public void setDelivryMd(String delivryMd) {
    this.delivryMd = delivryMd;
    }
    
    /*public String[] getDelivryMd() {
        return delivryMd;
    }
    public void setDelivryMd(String[] delivryMd) {
        this.delivryMd = delivryMd;
    }*/

    public String getDelivryDS() {
        return delivryDS;
    }

    public void setDelivryDS(String delivryDS) {
        this.delivryDS = delivryDS;
    }


    
}
