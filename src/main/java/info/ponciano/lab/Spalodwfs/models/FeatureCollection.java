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

import java.util.List;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

import info.ponciano.lab.Spalodwfs.models.semantic.KB;
import info.ponciano.lab.Spalodwfs.models.semantic.OntoManagementException;
import info.ponciano.lab.pisemantic.PiSparql;
import info.ponciano.lab.pitools.files.PiFile;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class FeatureCollection {

	public String assetUri;
	public String geopath;
	public String rdfpath;
	public List<String[]> features;
	public HashMap<Integer, RDFNode> individus;
	public HashMap<Integer, RDFNode> properties;

	public FeatureCollection(String uri) throws OntoManagementException {
		this.assetUri = uri;
		initPaths();
		initIndividus();
		this.features = new ArrayList<String[]>();
		System.out.println(this.individus.size());
		if(this.individus.size()>0)
		{	
			initProperties();
			initFeatures();
		}
	}

	/**
	 * Function that initiates the List of features with properties as abscisse and
	 * indivviduals as ordinate from the ontmodel created from the RDFpath.
	 */
	private void initFeatures() {
		//OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		//ont.read(this.rdfpath);
		if (!new PiFile(this.rdfpath).exists())
			throw new InternalError("File not exists: " + this.rdfpath);
		PiSparql ont;
		try {
			ont = new PiSparql(this.rdfpath);
			
		// init first row with property names as headtable
		String[] table = new String[this.properties.size() + 1];
		table[0] = "Individuals/Properties";
		System.out.println("properties");
		for (int j = 1; j <= this.properties.size(); j++) {
			table[j] = this.properties.get(j).asResource().getLocalName();
			System.out.println(table[j]);
		}
		this.features.add(table);
		// add all other rows
		System.out.println("size list: "+features.size());
		System.out.println("features");
		for (int i = 1; i <= this.individus.size(); i++) {
			table = new String[this.properties.size() + 1];
			table[0] = this.individus.get(i).asResource().getURI();
			System.out.println(table[0]);
			// adding of property values of the current individuals in the string table
			for (int k = 1; k <= this.properties.size(); k++) {
				/*NodeIterator o = ont.listObjectsOfProperty(this.individus.get(i).asResource(), this.properties.get(j));
				while (o.hasNext()) {
					RDFNode node = o.next();
					if (this.properties.get(j).isDatatypeProperty())
						table[j] = node.asLiteral().toString();
					else
						table[j] = node.asResource().getLocalName();*/
				//retrieve property values
				ResultSet select = ont.select(""
						+ "SELECT ?o "
						+ "WHERE{<"+this.individus.get(i).asResource().getURI()+"> <"+this.properties.get(k).asResource().getURI()+"> ?o. "
						+ "}");
				System.out.println("SELECT ?o "
						+ "WHERE{<"+this.individus.get(i).asResource().getURI()+"> <"+this.properties.get(k).asResource().getURI()+"> ?o. "
						+ "}");
				
				String s="";
				//retrieve the values as a string
				while (select.hasNext()) {
					RDFNode rdfNode = select.next().get("?o");
					if (rdfNode.isLiteral())
						s+= rdfNode.asLiteral().toString()+" ";
					else
						s+= rdfNode.asResource().getURI()+" ";
				}
				//add the property values into the table
				table[k]=s;
				System.out.println("S: "+s);
				}
			// adding of the table into the list = adding of a row
			this.features.add(table);
			System.out.println("size list: "+features.size());
			}

	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
	}

	/**
	 * Function that initiates the HashMap of properties, by retrieving properties
	 * of an ontmodel from the RDFpath and assigning them a number
	 */
	private void initProperties() {
		/*OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		ont.read(this.rdfpath);
		ExtendedIterator<OntProperty> p = ont.listAllOntProperties();
		int j = 1;
		this.properties = new HashMap<Integer, OntProperty>();
		while (p.hasNext()) {
			this.properties.put(j, p.next());
			j++;
		}*/
		if (!new PiFile(this.rdfpath).exists())
			throw new InternalError("File not exists: " + this.rdfpath);
		PiSparql ont;
		try {
			ont = new PiSparql(this.rdfpath);
			ResultSet select = ont.select("SELECT ?p "
					+ "WHERE{ <"+this.individus.get(5).asResource()+"> ?p ?c. "
					+ " }");
			int i = 1;
			this.properties = new HashMap<Integer, RDFNode>();
			while (select.hasNext()) {
				RDFNode rdfNode = select.next().get("?p");
				this.individus.put(i, rdfNode);
				i++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
			
	}

	/**
	 * Function that initiates the HashMap of individuals, by retrieving individuals
	 * of an ontmodel from the RDFpath and assigning them a number
	 */
	private void initIndividus() {
		if (!new PiFile(this.rdfpath).exists())
			throw new InternalError("File not exists: " + this.rdfpath);
		PiSparql ont;
		try {
			ont = new PiSparql(this.rdfpath);
			ResultSet select = ont.select(""
					+ "SELECT ?i "
					+ "WHERE{ ?i rdf:type ?c. "
					+ "FILTER NOT EXISTS{?i rdf:type <http://www.opengis.net/ont/geosparql#Geometry> } . "
					+ "}");
			System.out.println();
//		OntModel ont= ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
//        ont.read(this.rdfpath);
//			Iterator<OntClass> classes = ont.getOnt().listObjects();
//			OntClass dataclass = null;
//			while (dataclass == null && classes.hasNext()) {
//				OntClass c = classes.next();
//				if (c.getLocalName() != "Thing" && c.getLocalName() != "Geometry")
//					dataclass = c;
//			}
//			Iterator<Individual> individuals = ont.getIndividuals(dataclass).iterator();
			int i = 1;
			this.individus = new HashMap<Integer, RDFNode>();
			while (select.hasNext()) {
				RDFNode rdfNode = select.next().get("?i");
				this.individus.put(i, rdfNode);
				i++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Function that initiates geopath and rdfpath from the assetURI through a
	 * SPARQL query
	 * 
	 * @throws OntoManagementException
	 */
	private void initPaths() throws OntoManagementException {
		List<String[]> info = new ArrayList<String[]>();

		// initialize the query to retrieve all instances of previous asset
		String query = "SELECT ?ep " + "WHERE{ <http://lab.ponciano.info/ontology/2020/Spalod/dcat#" + this.assetUri
				+ "> <http://www.w3.org/ns/dcat#distribution> ?d. "
				+ "?d <http://www.w3.org/ns/dcat#accessService> ?s. "
				+ "?s <http://www.w3.org/ns/dcat#endpointURL> ?ep." + "}";
		System.out.println(query);
		System.out.println(KB.get().getSPARQL(query));
		// create the table of variables
		String[] var = { "ep" };
		// query the ontology
		info = KB.get().queryAsArray(query, var, false, false);
		for (int i = 0; i < info.size(); i++) {
			for (int j = 0; j < info.get(i).length; j++) {
				String s = info.get(i)[j];
				String ext = s.substring(s.length() - 3);
				if (ext.equals("shp"))
					this.geopath = "shp-data/" + s;
				if (ext.equals("ttl"))
					this.rdfpath = "rdf-data/" + s;
			}
		}

	}

}
