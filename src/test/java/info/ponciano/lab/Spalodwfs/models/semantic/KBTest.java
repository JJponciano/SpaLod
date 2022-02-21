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
package info.ponciano.lab.Spalodwfs.models.semantic;

import info.ponciano.lab.pisemantic.PiOnt;
import info.ponciano.lab.pitools.files.PiFile;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Dr. Jean-Jacques Ponciano
 */
public class KBTest {

    public KBTest() {
    }

    /**
     * Test of get method, of class KB.
     */
    @Test
    public void testGet() throws Exception {
        System.out.println("get");
        KB result = KB.get();

    }

    /**
     * Test of getOutputPath method, of class KB.
     */
    @Test
    public void testGetOutputPath() {
        try {
            System.out.println("getOutputPath");
            KB instance = KB.get();
            String expResult =  "src/main/resources/ontologies/spalod.owl";
            String result = instance.getOutputPath();
            assertEquals(expResult, result);
        } catch (Exception ex) {
            Logger.getLogger(KBTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of save method, of class KB.
     */
    @Test
    public void testSave() {
        try {
            System.out.println("save");
            KB instance = KB.get();
            instance.save();
            assertTrue(new PiFile(instance.getOutputPath()).exists());
        } catch (Exception ex) {
            Logger.getLogger(KBTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of addPrefix method, of class KB.
     */
    @Test
    public void testAddPrefix() {
        try {
            System.out.println("addPrefix");
            String key = "";
            String namespace = "";
            KB instance = KB.get();
            instance.addPrefix(key, namespace);
        } catch (Exception ex) {
            Logger.getLogger(KBTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of uplift method, of class KB.
     */
    @Test
    public void testUplift() {
        try {
            System.out.println("uplift");
            String xmlPathfile = "";
            KB instance = KB.get();
            boolean expResult = false;
            boolean result = instance.uplift(xmlPathfile);
            assertEquals(expResult, result);
        } catch (Exception ex) {
            Logger.getLogger(KBTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of downlift method, of class KB.
     */
    @Test
    public void testDownlift() {
        try {
            System.out.println("downlift");
            String metadataURI = "";
            KB instance = KB.get();
            String expResult = "";
            String result = instance.downlift(metadataURI);
            assertEquals(expResult, result);
        } catch (Exception ex) {
            Logger.getLogger(KBTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of change method, of class KB.
     */
    @Test
    public void testChange() {
        try {
            System.out.println("change");
            String ind = "";
            String property = "";
            String value = "";
            KB instance = KB.get();
            boolean expResult = false;
            boolean result = instance.change(ind, property, value);
            assertEquals(expResult, result);
        } catch (Exception ex) {
            Logger.getLogger(KBTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of getSPARQL method, of class KB.
     */
    @Test
    public void testGetSPARQL() {
        try {
            System.out.println("getSPARQL");
            String query = "";
            KB instance = KB.get();
            String expResult = "";
            String result = instance.getSPARQL(query);
            assertEquals(expResult, result);
        } catch (Exception ex) {
            Logger.getLogger(KBTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of construct method, of class KB.
     */
    @Test
    public void testConstruct() {
        try {
            System.out.println("construct");
            String queryString = "";
            KB instance = KB.get();
            boolean expResult = false;
            boolean result = instance.construct(queryString);
            assertEquals(expResult, result);
        } catch (Exception ex) {
            Logger.getLogger(KBTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of select method, of class KB.
     */
    @Test
    public void testSelect() {
        try {
            System.out.println("select");
            String queryString = "";
            KB instance = KB.get();
            ResultSet expResult = null;
            ResultSet result = instance.select(queryString);
            assertEquals(expResult, result);
        } catch (Exception ex) {
            Logger.getLogger(KBTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of update method, of class KB.
     */
    @Test
    public void testUpdate() {
        try {
            System.out.println("update");
            String query = "";
            KB instance = KB.get();
            instance.update(query);
        } catch (Exception ex) {
            Logger.getLogger(KBTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of queryAsArray method, of class KB.
     */
    @Test
    public void testQueryAsArray() {
        try {
            System.out.println("queryAsArray");
            String query = "";
            String[] var = null;
            boolean fullURI = false;
            boolean onlyNS = false;
            KB instance = KB.get();
            List expResult = null;
            List result = instance.queryAsArray(query, var, fullURI, onlyNS);
            assertEquals(expResult, result);
        } catch (Exception ex) {
            Logger.getLogger(KBTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of dataImport method, of class KB.
     */
    @Test
    public void testDataImport() {
        try {
            System.out.println("dataImport");
            String mduri = "";
            String ttlpath = "";
            KB instance = KB.get();
            instance.dataImport(mduri, ttlpath);
        } catch (Exception ex) {
            Logger.getLogger(KBTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of getOnt method, of class KB.
     */
    @Test
    public void testGetOnt() {
        try {
            System.out.println("getOnt");
            KB instance = KB.get();
            PiOnt expResult = null;
            PiOnt result = instance.getOnt();
            assertEquals(expResult, result);
        } catch (Exception ex) {
            Logger.getLogger(KBTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of add method, of class KB.
     */
    @Test
    public void testAdd() {
        try {
            System.out.println("add");
            OntModel ont = null;
            KB instance = KB.get();
            instance.add(ont);
        } catch (Exception ex) {
            Logger.getLogger(KBTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

}
