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

import java.io.IOException;


import info.ponciano.lab.pitools.files.PiFile;

/**
 * @author claireprudhomme
 *
 */
class SHPdataTest {

	/**
	 * Test method for
	 * {@link info.ponciano.lab.Spalodwfs.models.SHPdata#SHPdata()}.
	 */
	//@Test
	final void testSHPdata() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.ponciano.lab.Spalodwfs.models.SHPdata#SHPdata(java.lang.String, java.lang.String)}.
	 */
	//@Test
	final void testSHPdataStringString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.ponciano.lab.Spalodwfs.models.SHPdata#getMetadata()}.
	 */
	//@Test
	final void testGetMetadata() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.ponciano.lab.Spalodwfs.models.SHPdata#setMetadata(java.lang.String)}.
	 */
	//@Test
	final void testSetMetadata() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.ponciano.lab.Spalodwfs.models.SHPdata#getTitle()}.
	 */
	//@Test
	final void testGetTitle() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.ponciano.lab.Spalodwfs.models.SHPdata#setTitle(java.lang.String)}.
	 */
	//@Test
	final void testSetTitle() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.ponciano.lab.Spalodwfs.models.SHPdata#getVersion()}.
	 */
	//@Test
	final void testGetVersion() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.ponciano.lab.Spalodwfs.models.SHPdata#setVersion(java.lang.String)}.
	 */
	//@Test
	final void testSetVersion() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.ponciano.lab.Spalodwfs.models.SHPdata#getVersionNote()}.
	 */
	//@Test
	final void testGetVersionNote() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.ponciano.lab.Spalodwfs.models.SHPdata#setVersionNote(java.lang.String)}.
	 */
	//@Test
	final void testSetVersionNote() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.ponciano.lab.Spalodwfs.models.SHPdata#getPrevAsset()}.
	 */
	//@Test
	final void testGetPrevAsset() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.ponciano.lab.Spalodwfs.models.SHPdata#setPrevAsset(java.lang.String)}.
	 */
	//@Test
	final void testSetPrevAsset() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.ponciano.lab.Spalodwfs.models.SHPdata#representationRDF(java.lang.String, java.lang.String)}.
	 */
	//@Test
	final void testRepresentationRDF() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link info.ponciano.lab.Spalodwfs.models.SHPdata#shpUpliftProcess(java.lang.String, java.lang.String)}.
	 */
	// //@Test Passed but skipped due to the dependencies downloading that is not
	// compatible to CI in GitHub/lab.
	final void testShpUpliftProcess() {
		// SHPdata.main(null);
		String URI = "http://i3mainz.de/";
		String shapeFilePAth = "src/main/resources/datatest/vg250krs.shp";
		String[] results;
		try {
			results = SHPdata.shpUpliftProcess(shapeFilePAth, URI);
			if (results.length == 2 && results[0] != null && results[1] != null) {
				assertTrue(new PiFile(results[0]).exists());
				assertTrue(new PiFile(results[1]).exists());
			} else
				fail("Something wrong in shpUpliftProcess(" + shapeFilePAth + "," + URI + ")");
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link info.ponciano.lab.Spalodwfs.models.SHPdata#getDataSet()}.
	 */
	//@Test
	final void testGetDataSet() {
		fail("Not yet implemented"); // TODO
	}

}
