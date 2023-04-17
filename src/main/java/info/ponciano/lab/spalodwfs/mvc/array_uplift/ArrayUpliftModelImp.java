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
package info.ponciano.lab.spalodwfs.mvc.array_uplift;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.util.iterator.ExtendedIterator;

import info.ponciano.lab.pisemantic.PiSparql;

public class ArrayUpliftModelImp extends ArrayUpliftModel {

	
	public ArrayUpliftModelImp(String[][] attributesArray, String ontpath) throws FileNotFoundException {
		super(attributesArray, ontpath);
	}

	public ArrayUpliftModelImp(String[][] attributesArray, String ontpath, String vocabpath)
			throws FileNotFoundException {
		super(attributesArray, ontpath, vocabpath);
	}

	@Override
	protected void initpropertyNames() {
		//init hashmap propertyNames
		this.propertyNames=new HashMap<String,String>();
		//retrieve all properties
		List<String> prop=this.getProperties();
		//for each property 
		for(int i=0; i<prop.size();i++) {
			//retrieve its label
			OntProperty p=this.vocab.getOntProperty(prop.get(i));
			ExtendedIterator<RDFNode> labels=p.listLabels(null);
			while(labels.hasNext()) {
				RDFNode l=labels.next();
				//add the label with the property to the hashmap
				this.propertyNames.put(l.asLiteral().toString(), prop.get(i));
			}
		}
		
	}

	@Override
	protected void initpropertyRanges() {
		// init hashmap propertyRanges
		this.propertyRanges = new HashMap<String, String>();
		// retrieve all properties
		List<String> prop=this.getProperties();
		//for each property 
		for(int i=0; i<prop.size();i++) {
			// for each property retrieve its range
			OntProperty p=this.vocab.getOntProperty(prop.get(i));
			OntResource range=p.getRange();
			// add the range with the property to the hashmap
			if(range!=null) {
				this.propertyRanges.put(prop.get(i), range.getURI());
				//System.out.println(prop.get(i));
				//System.out.println(range.getURI());
			}
		}
	}

	@Override
	public List<String> getProperties() {
		List<String> properties = this.getObjectProperties();
		List<String> dp = this.getDataProperties();
		if (dp != null)
			for (int i = 0; i < dp.size(); i++) {
				properties.add(dp.get(i));
			}
		return properties;
	}

	@Override
	public List<String> getObjectProperties() {
		ExtendedIterator<ObjectProperty> l=this.vocab.getOnt().listObjectProperties();
		List<String> oprop=new ArrayList<String>();
		while(l.hasNext()) {
			ObjectProperty op=l.next();
			oprop.add(op.getURI());
		}
		return oprop;
	}

	@Override
	public List<String> getDataProperties() {
		ExtendedIterator<DatatypeProperty> l=this.vocab.getOnt().listDatatypeProperties();
		List<String> dprop=new ArrayList<String>();
		while(l.hasNext()) {
			DatatypeProperty dp=l.next();
			dprop.add(dp.getURI());
		}
		return dprop;
	}

	@Override
	public List<String[]> geFirstRows(int nbrows) {
		List<String[]> firstrows = new ArrayList<String[]>();
		for (int i = 0; i < nbrows; i++) {
			firstrows.add(List.of(attributes).get(i));
		}
		return firstrows;
	}

	@Override
	public boolean addProperty(String localname, String range) {
		boolean datatypeproperty=range.contains("xsd:");
		//case of Datatypeproperty
		
		if(datatypeproperty) {
			//adding to vocab ontology
			DatatypeProperty dp=this.vocab.createDatatypeProperty(dataUri+localname);
			Resource r=new ResourceImpl(xsdUri+range.substring(4));
			dp.addRange(r);
			//adding to propertyRanges
			this.propertyRanges.put(dp.getURI(), dp.getRange().getURI());
		}
		//case of ObjectProperties
		else {
			//adding to vocab ontology
			ObjectProperty op=this.vocab.createObjectProperty(dataUri+localname);
			OntClass c=this.vocab.createClass(dataUri+range);
			op.addRange(c);
			//adding to propertyRanges
			this.propertyRanges.put(op.getURI(), op.getRange().getURI());
		}
		return true;
	}

	@Override
	public boolean createOntology(String classname, List<String> mappedProperties) throws Exception {
		//first row processing: add the mapping for each property
		List<String[]> at=List.of(this.attributes);
		if(at.size()>0) {
			if(at.get(0).length!= mappedProperties.size()) throw new Exception("The size of mapping array does not fit with the number of properties");
			else{
				for(int i=0; i<at.get(0).length; i++)
					this.addPropertyMapping(mappedProperties.get(i),at.get(0)[i]);
			}
		}
		
		//for each other row of the attributes array
		for(int j=1; j<at.size(); j++) {
			//create an individual
			OntClass c=this.ontology.getOntClass(this.dataUri+classname);
			if(c==null) c=this.ontology.createClass(this.dataUri+classname);
			Individual ind=this.ontology.createIndividual(c);

			//add the property value to the individual for each property
			for(int k=0; k<mappedProperties.size(); k++) {
				if(mappedProperties.get(k)!=null && mappedProperties.get(k)!="") {
					//retrieve the property range
					String range=this.propertyRanges.get(mappedProperties.get(k));
					boolean isDp=range.contains(this.xsdUri);
					//DatatypeProperty case
					if(isDp) {
						DatatypeProperty dp=this.ontology.getDataProperty(mappedProperties.get(k));
						if(dp==null) dp=this.ontology.createDatatypeProperty(mappedProperties.get(k));
						ind.addLiteral(dp, at.get(j)[k]);
					}
					//Objectproperty case
					else {
						//retrieve property
						ObjectProperty op=this.ontology.getObjectProperty(mappedProperties.get(k));
						if(op==null) op=this.ontology.createObjectProperty(mappedProperties.get(k));
						//retrieve its range class
						OntClass cr=this.ontology.getOntClass(range);
						if(cr==null) cr=this.ontology.createClass(range);
						//create an individual of the range
						Individual indrange=this.ontology.createIndividual(cr);
						indrange.addLabel(at.get(j)[k], null);
						ind.addProperty(op, indrange);
					}
				}
			}	
		}
		
		//save the created ontology
		this.ontology.write(this.ontopath);
		return true;
	}

	@Override
	public PiSparql getOntology() {
		return this.ontology;
	}

	@Override
	public boolean addPropertyMapping(String localname, String label) {
		OntProperty p=this.vocab.getOntProperty(dataUri+localname);
		if(p==null)
			return false;
		else {
			p.addLabel(label,null);
			this.propertyNames.put(label, p.getURI());
			return true;
		}
	}

}
