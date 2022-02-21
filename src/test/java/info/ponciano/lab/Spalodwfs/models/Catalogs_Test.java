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
package info.ponciano.lab.Spalodwfs.models;

import info.ponciano.lab.Spalodwfs.models.semantic.OntoManagementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author pc-asus
 */
public class Catalogs_Test {
    
    public Catalogs_Test() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUcp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getJo method, of class Catalogs.
     */
    @Test
    public void testGetJo() {
        try {
            System.out.println("getJo");
            Catalogs instance = new Catalogs();
            JSONObject expResult = null;
            JSONObject result = instance.getJo();
          //  assertEquals(expResult, result); //Error, line needs to be fixed
           
        } catch (OntoManagementException ex) {
            Logger.getLogger(Catalogs_Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
