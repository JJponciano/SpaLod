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
package info.ponciano.lab.Spalodwfs.array_uplift;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.junit.jupiter.api.Test;

import info.ponciano.lab.pitools.files.PiFile;

class ArrayUpliftTest {

	ArrayUpliftModelImp instance;

	public ArrayUpliftTest() {
		super();
		PiFile pf = new PiFile("testdata/vg250gem.csv");
		String[][] attribute;
		try {
			attribute = pf.readCSV(";");
			this.instance = new ArrayUpliftModelImp(attribute, "testdata/ontoCSVtest", "testdata/vocabtest.owl");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			fail("Exception from ArrayUpliftModelImp constructor: " + e.getMessage());
			;
		}
	}

	@Test
	final void initpropertyNamesTest() {
		String expectedProp = "http://lab.ponciano.info/ontology/2020/Spalod/data#hasArea";
		assertEquals(expectedProp, instance.propertyNames.get("AREA,N,20,5"));

		String expectedProp2 = "http://lab.ponciano.info/ontology/2020/Spalod/data#hasPerimeter";
		assertEquals(expectedProp2, instance.propertyNames.get("PERIMETER,N,20,5"));

	}

	@Test
	final void initpropertyRangesTest() {
		String expectedRange = "http://www.w3.org/2001/XMLSchema#double";
		assertEquals(expectedRange,
				instance.propertyRanges.get("http://lab.ponciano.info/ontology/2020/Spalod/data#hasArea"));

		String expectedRange2 = "http://www.w3.org/2001/XMLSchema#string";
		assertEquals(expectedRange2,
				instance.propertyRanges.get("http://lab.ponciano.info/ontology/2020/Spalod/data#hasDescription"));
		
		String expectedRange3 = "http://lab.ponciano.info/ontology/2020/Spalod/data#Municipality";
		assertEquals(expectedRange3,
				instance.propertyRanges.get("http://lab.ponciano.info/ontology/2020/Spalod/data#hasMunicipality"));

	}

	@Test
	final void getPropertiesTest() {
		int expectedSize = instance.getProperties().size();
		assertEquals(expectedSize, 10);
	}

	@Test
	final void getObjectPropertiesTest() {
		if (instance.getObjectProperties() != null) {
			int expectedSize = instance.getObjectProperties().size();
			assertEquals(expectedSize, 1);
		} else {
			fail("Not yet implemented");
		}
	}

	@Test
	final void getDataPropertiesTest() {
		if (instance.getDataProperties() != null) {
			int expectedSize = instance.getDataProperties().size();
			assertEquals(expectedSize, 9);
		} else {
			fail("Not yet implemented");
		}
	}

	@Test
	final void geFirstRowsTest() {
		List<String[]> expectedlist = new ArrayList<String[]>();
		String[] row1 = new String[10];
		row1[0] = "AREA,N,20,5";
		row1[1] = "PERIMETER,N,20,5";
		row1[2] = "GEM_,N,11,0";
		row1[3] = "GEM_ID,N,11,0";
		row1[4] = "SHN,C,12";
		row1[5] = "RAU,C,12";
		row1[6] = "USE,N,2,0";
		row1[7] = "KEY,C,8";
		row1[8] = "GEN,C,50";
		row1[9] = "DES,C,50";
		expectedlist.add(row1);

		assertEquals(expectedlist.size(), instance.geFirstRows(1).size());
		assertEquals(expectedlist.get(0)[1], instance.geFirstRows(1).get(0)[1]);
		assertEquals(expectedlist.get(0)[5], instance.geFirstRows(1).get(0)[5]);
		assertEquals(expectedlist.get(0)[9], instance.geFirstRows(1).get(0)[9]);
	}

	@Test
	final void addPropertyTest() {
		instance.addProperty("hasWheelchairAccess", "xsd:boolean");
		String expectedRange1 = "http://www.w3.org/2001/XMLSchema#boolean";
		assertEquals(expectedRange1,
				instance.propertyRanges.get("http://lab.ponciano.info/ontology/2020/Spalod/data#hasWheelchairAccess"));
		
		instance.addProperty("hasCity", "City");
		String expectedRange2 = "http://lab.ponciano.info/ontology/2020/Spalod/data#City";
		assertEquals(expectedRange2,
				instance.propertyRanges.get("http://lab.ponciano.info/ontology/2020/Spalod/data#hasCity"));
	}

	@Test
	final void addPropertyMappingTest() {
		instance.addProperty("hasWheelchairAccess", "xsd:boolean");
		boolean b=instance.addPropertyMapping("hasWheelchairAccess", "wheelchair");
		assertTrue(b);
		
		OntProperty p= instance.vocab.getOntProperty("http://lab.ponciano.info/ontology/2020/Spalod/data#hasWheelchairAccess");
		String exp="wheelchair";
		assertEquals(exp,p.getLabel(null));

		String expectedProperty = "http://lab.ponciano.info/ontology/2020/Spalod/data#hasWheelchairAccess";
		assertEquals(expectedProperty,instance.propertyNames.get("wheelchair"));
	}
	
	@Test
	final void createOntologyTest() {
		List<String> ls=new ArrayList<String>();
		ls.add(instance.dataUri+"hasArea");
		ls.add(instance.dataUri+"hasPerimeter");
		ls.add(instance.dataUri+"hasNumber");
		ls.add(instance.dataUri+"hasId");
		ls.add(instance.dataUri+"hasShapeNode");
		ls.add(instance.dataUri+"hasSpace");
		ls.add(instance.dataUri+"hasUse");
		ls.add(instance.dataUri+"hasKey");
		ls.add(instance.dataUri+"hasMunicipality");
		ls.add(instance.dataUri+"hasDescription");
		try {
			instance.createOntology("Gemeinde", ls);
			OntClass c=instance.ontology.getOntClass(instance.dataUri+"Gemeinde");
			String exp=instance.dataUri+"Gemeinde";
			assertEquals(exp,c.getURI());
			c=instance.ontology.getOntClass(instance.dataUri+"Municipality");
			exp=instance.dataUri+"Municipality";
			assertEquals(exp,c.getURI());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
