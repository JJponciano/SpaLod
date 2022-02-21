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

import static org.junit.jupiter.api.Assertions.*;


import info.ponciano.lab.Spalodwfs.models.semantic.OntoManagementException;

/**
 * @author claireprudhomme
 *
 */
class FeatureCollectionTest {
/**
 * Text of the functuion initPaths
 * @throws OntoManagementException 
 */
	//@Test 
	final void testInitPaths() throws OntoManagementException {
		System.out.println("testInitPaths");
		FeatureCollection instance= new FeatureCollection("2c04f566-ec08-4c6d-8beb-becc89a0418c");

		String expectedShp = "shp-data/vg250krs_2000.shp";
		assertEquals(expectedShp, instance.geopath);
		
		String expectedRDF = "rdf-data/776cc9ba-a5c2-46e3-abbd-427696bd4b36.ttl";
		assertEquals(expectedRDF, instance.rdfpath);
	}

}
