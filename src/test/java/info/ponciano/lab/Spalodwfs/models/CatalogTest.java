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

import info.ponciano.lab.Spalodwfs.models.semantic.OntoManagementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
/**
 *
 * @author claireprudhomme
 */
public class CatalogTest {
    
    JSONObject expResult;
    public CatalogTest() {
        try {
            //initialize the expected result
            expResult = new JSONObject();
            expResult.put("recordid", "gdi_catalog");
            expResult.put("type", "catalogue");
            expResult.put("title", "GDI Catalog");
            expResult.put("description", "A sample catalogue of geospatial data stored in a triplestore and provided by GDI.DE.");
            JSONArray jar=new JSONArray();
            JSONObject link = new JSONObject();
            link.put("href", "https://Spalodwfs.herokuapp.com/api/SpalodWFS/collections/gdi_catalog?f=html");
            link.put("rel", "collection");
            link.put("title", "Root URL for this record collection in HTML");
            jar.put(link);
            link = new JSONObject();
            link.put("href", "https://Spalodwfs.herokuapp.com/api/SpalodWFS/collections/gdi_catalog?f=json");
            link.put("rel", "collection");
            link.put("title", "Root URL for this record collection in JSON");
            jar.put(link);
            link = new JSONObject();
            link.put("href", "https://Spalodwfs.herokuapp.com/api/SpalodWFS/collections/gdi_catalog?f=xml");
            link.put("rel", "collection");
            link.put("title", "Root URL for this record collection in XML");
            jar.put(link);
            expResult.put("links",jar);
        } catch (JSONException ex) {
            Logger.getLogger(CatalogTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getJo method, of class Catalog.
     */
  //  @Test
    public void testGetJo() {
        try {
            System.out.println("getJo");
            
            //initialize result
            Catalog instance = new Catalog("gdi_catalog");       
            JSONObject result = instance.getJo();
            System.out.println(result);
            //assertEquals(expResult, result);
            System.out.println(expResult.get("recordid"));
            assertEquals(expResult.get("recordid"), result.get("recordid"));
            assertEquals(expResult.get("type"), result.get("type"));
            assertEquals(expResult.get("title"), result.get("title"));
            assertEquals(expResult.get("description"), result.get("description"));
            JSONArray exparray=expResult.getJSONArray("links");
            JSONArray resarray=result.getJSONArray("links");
            assertEquals(exparray.length(), resarray.length());
        } catch (OntoManagementException ex) {
            Logger.getLogger(CatalogTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CatalogTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of setJo method, of class Catalog.
     */
  //  @Test
    public void testSetJo() {
        try {
            CatalogTest ct=new CatalogTest();
            System.out.println(ct);
            System.out.println("setJo");
            JSONObject jo = null;
            Catalog instance = new Catalog();
            instance.setJo(expResult);
            jo=instance.getJo();
            
            Catalog instance2 = new Catalog("gdi_catalog");
            JSONObject result = instance2.getJo();
            assertEquals(jo.get("recordid"), result.get("recordid"));
            assertEquals(jo.get("type"), result.get("type"));
            assertEquals(jo.get("title"), result.get("title"));
            assertEquals(jo.get("description"), result.get("description"));
            JSONArray exparray=jo.getJSONArray("links");
            JSONArray resarray=result.getJSONArray("links");
            assertEquals(exparray.length(), resarray.length());
        } catch (OntoManagementException ex) {
            Logger.getLogger(CatalogTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CatalogTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
