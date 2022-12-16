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
package info.ponciano.lab.spalodwfs.geotime.models.parser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.locationtech.jts.io.WKTReader; 

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.XSD;

/**
 * Converts any GML file in its corresponding namespace to RDF, with optionally
 * making assumptions about ranges and domains.
 * 
 * @author timo.homburg
 *
 */
public class KnownSchemaParser implements ContentHandler {

	public OntModel model;

	public static final String NSGEO = "http://www.opengis.net/ont/geosparql#";
	public static final String NSSF = "http://www.opengis.net/ont/sf#";
	public static final String GML = "gml";
	public static final String WKT = "asWKT";
	public static final String ASGML = "asGML";
	public static final String WKTLiteral = "wktLiteral";
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String GMLLiteral = "gmlLiteral";

	public static final String hasGeometry = "hasGeometry";

	private static final String TRUE = "true";

	private static final String FALSE = "false";

	private static final String POINT = "Point";

	private static final String HTTP = "http://";

	private static Set<String> featureMembers = new TreeSet<String>(
			Arrays.asList(new String[] { "featureMember", "member", "cityObjectMember" }));

	private static final String XLINKHREF = "xlink:href";

	private static final String CODESPACE = "codeSpace";

	private Map<String, OntResource> knownMappings;

	private Individual currentIndividual;

	private Individual lastlinkedIndividual;

	private String currentType;

	private Map<String, String> currentRestrictions;

	private List<String> openedTags, openedTags2;

	private Boolean featureMember, inClass, envelope;

	private StringBuilder multipleChildrenBuffer;

	private StringBuilder gmlStrBuilder;

	private String codeSpace = "", provider = "", license = "", origin = "";

	private Stack<String> stack, stack2;

	private Stack<Map<String, String>> restrictionStack;

	private Integer outertagCounter;

	private Integer splitterCountThreshold = 0;

	private StringBuilder attbuilder;

	SimpleDateFormat parserSDF1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	SimpleDateFormat parserSDF2 = new SimpleDateFormat("yyyy-MM-dd");

	private WKTReader wktreader;

	//private FileWriter writerWOModel;

	private Boolean range = false, domain = false, stringAttributeBool = false;

	private StringBuilder literalBuffer;

	private boolean alreadyHandled;

	private OntClass codelist;

	private Attributes attributes;

	private String uuid = UUID.randomUUID().toString(), stringAttribute = "";

	GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("CEST"));

	private Date startTime;

	public KnownSchemaParser(OntModel model, Boolean range, Boolean domain, String outpath, String provider,
			String license, String origin) throws IOException {
		this.model = model;
		this.codelist = model.createClass("http://semgis.de/geodata#Codelist");
		this.outertagCounter = 0;
		this.setEnvelope(false);
		this.knownMappings = new TreeMap<String, OntResource>();
		this.openedTags = new LinkedList<String>();
		this.openedTags2 = new LinkedList<String>();
		this.featureMember = false;
		this.inClass = false;
		this.multipleChildrenBuffer = new StringBuilder();
		this.gmlStrBuilder = new StringBuilder();
		this.attbuilder = new StringBuilder();
		this.currentRestrictions = new TreeMap<String, String>();
		this.stack = new Stack<String>();
		this.wktreader = new WKTReader();
		this.stack2 = new Stack<String>();
		this.range = range;
		this.domain = domain;
		this.license = license;
		this.provider = provider;
		this.origin = origin;
		this.restrictionStack = new Stack<Map<String, String>>();
		//this.writerWOModel = new FileWriter(new File("outriskwoModel.rdf"));
		startTime = new Date(System.currentTimeMillis());
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		throw new UnsupportedOperationException("not yet implemented");
	}

	private void importMetaData(Individual ind, String indname, String publisher) {
		if (!this.license.isEmpty())
			ind.addProperty(model.createDatatypeProperty("http://purl.org/dc/terms/distribution"), this.license);
		if (!origin.isEmpty()) {
			OntClass dist = model.createClass("http://purl.org/dc/terms/Distribution");
			Individual distind = dist.createIndividual(ind.getURI() + "_distribution");
			ind.addProperty(model.createObjectProperty("http://purl.org/dc/terms/distribution"), distind);
			distind.addProperty(model.createObjectProperty("http://purl.org/dc/terms/downloadURL"), this.origin);
			ind.addProperty(model.createDatatypeProperty("http://purl.org/dc/terms/license"), this.license);
		}
		ind.addRDFType(model.createClass("http://purl.org/dc/terms/Dataset"));
		OntClass entity = model.createClass("http://www.w3.org/ns/prov#Entity");
		OntClass agent = model.createClass("http://www.w3.org/ns/prov#Agent");
		OntClass activity = model.createClass("http://www.w3.org/ns/prov#Activity");
		Individual importactivity = activity.createIndividual(ind.getURI() + "_GMLImporter");
		ObjectProperty wasAttributedTo = model.createObjectProperty("http://www.w3.org/ns/prov#wasAttributedTo");
		ObjectProperty wasAssociatedWith = model.createObjectProperty("http://www.w3.org/ns/prov#wasAssociatedWith");
		ObjectProperty wasGeneratedBy = model.createObjectProperty("http://www.w3.org/ns/prov#wasGeneratedBy");
		ObjectProperty used = model.createObjectProperty("http://www.w3.org/ns/prov#used");
		DatatypeProperty startedAtTime = model.createDatatypeProperty("http://www.w3.org/ns/prov#startedAtTime");
		DatatypeProperty endedAtTime = model.createDatatypeProperty("http://www.w3.org/ns/prov#endedAtTime");
		if (this.provider.isEmpty()) {
			this.provider = "prov";
		}
		Individual agentind = agent
				.createIndividual("http://semgis.de/geodata#" + this.provider.toLowerCase().replace(" ", "_"));
		ind.addProperty(model.createDatatypeProperty("http://purl.org/dc/terms/publisher"), agentind);
		agentind.setLabel(this.provider, "en");
		ind.addProperty(wasAttributedTo, agentind);
		ind.addProperty(wasGeneratedBy, importactivity);
		importactivity.addProperty(wasAssociatedWith, agentind);
		importactivity.addProperty(used, ind);
		String convertedDate;
		try {
			gc.setTime(startTime);
			convertedDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc).toXMLFormat();
			importactivity.addProperty(startedAtTime, convertedDate);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		try {
			gc.setTime(new Date(System.currentTimeMillis()));
			convertedDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc).toXMLFormat();
			importactivity.addProperty(endedAtTime, convertedDate);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		ind.addRDFType(entity);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		this.attributes = attributes;
		System.out.println(localName);
		alreadyHandled = false;
		if (featureMember) {
			String uriString = uri + "#" + localName;
			uriString = uriString.replace("##", "#");
			if (!knownMappings.containsKey(uriString) && openedTags.size() % 2 != 1) {
				knownMappings.put(uriString, model.createClass(uriString));
			} else if (!knownMappings.containsKey(uriString)) {
				knownMappings.put(uriString, model.createOntProperty(uriString));
			}
			// System.out.println("Add: "+uriString);
			this.openedTags.add(uriString);
			this.openedTags2.add(qName);
			// System.out.println("OpenedTags: "+openedTags);
			String value;
			if (attributes.getValue("gml:id") == null) {
				value = UUID.randomUUID().toString();
			} else {
				value = attributes.getValue("gml:id");
			}
			if (attributes.getValue(CODESPACE) != null)
				this.codeSpace = attributes.getValue(CODESPACE);
			String indid = (uri + "#" + value).replace("##", "#");
			if (knownMappings.get(uriString) != null && knownMappings.get(uriString).isClass()
					&& (!inClass || openedTags.size() > 2)) {
				this.inClass = true;
				if (openedTags.size() % 2 != 0) {
					this.currentIndividual = model.createIndividual(indid, model.createOntResource(indid));
					this.importMetaData(this.currentIndividual, indid, "GDI-DE");
					OntClass cls = model.createClass(uriString);
					this.currentIndividual.setRDFType(cls);
					this.currentType = uriString;
					if (uriString.contains("Envelop")) {
						this.setEnvelope(true);
						this.multipleChildrenBuffer.delete(0, this.multipleChildrenBuffer.length());
						this.attbuilder = this.attbuilder.delete(0, this.attbuilder.length());
						attbuilder.append("<");
						attbuilder.append(qName);
						attbuilder.append(" xmlns:gml=\"").append(uri).append("\"");
						for (int i = 0; i < attributes.getLength(); i++) {
							attbuilder.append(" ").append(attributes.getLocalName(i)).append("=\"")
									.append(attributes.getValue(i) + "\"");
						}
						attbuilder.append(">");
						this.multipleChildrenBuffer.append(attbuilder.toString());
					}

					// if(stack.isEmpty())
					stack.push(this.currentIndividual.toString());
					stack2.push(qName);
					if (attributes.getLength() > 0)
						this.currentIndividual.addLabel(value, "en");
					if (attributes.getLength() > 1) {
						for (int i = 0; i < attributes.getLength(); i++) {
							Literal liter = this.determineLiteralType(attributes.getValue(i));
							this.currentIndividual.addProperty(
									model.createDatatypeProperty(uri + "#" + attributes.getQName(i)), liter);
							/*try {
								writerWOModel.write("<" + this.currentIndividual.getURI() + "> <" + uri + "#"
										+ attributes.getQName(i) + "> \"" + liter.getValue() + "\"^^"
										+ liter.getDatatype());
							} catch (IOException e) {
								e.printStackTrace();
							}*/
							if (domain)
								model.createDatatypeProperty(uri + "#" + attributes.getQName(i))
										.addDomain(this.currentIndividual.getRDFType());
							if (range)
								model.createDatatypeProperty(uri + "#" + attributes.getQName(i))
										.addRange(model.getResource(liter.getDatatypeURI()));
						}
					}
					this.restrictionStack.push(this.currentRestrictions);
				}

			} else {
				// System.out.println("Property: "+qName);
			}

			if (attributes.getValue(XLINKHREF) != null) {
				String linkString;
				if (!attributes.getValue(XLINKHREF).startsWith(HTTP)) {
					linkString = uri + attributes.getValue(XLINKHREF);
				} else {
					linkString = attributes.getValue(XLINKHREF);
				}
				Individual propInd;
				if (this.model.getIndividual(linkString) == null) {
					propInd = model.createIndividual(linkString, this.model.createOntResource(linkString));
					this.currentIndividual
							.addProperty(model.createObjectProperty(openedTags.get(openedTags.size() - 1)), propInd);
					this.lastlinkedIndividual = this.currentIndividual;
					if (domain)
						model.getObjectProperty(openedTags.get(openedTags.size() - 1))
								.addDomain(this.currentIndividual.getRDFType());
				} else {
					propInd = model.getIndividual(linkString);
					this.currentIndividual
							.addProperty(model.createObjectProperty(openedTags.get(openedTags.size() - 1)), propInd);
					this.lastlinkedIndividual = this.currentIndividual;
					if (domain)
						model.getObjectProperty(openedTags.get(openedTags.size() - 1))
								.addDomain(this.currentIndividual.getRDFType());
					if (range)
						model.getObjectProperty(openedTags.get(openedTags.size() - 1)).addRange(propInd.getRDFType());
					model.createAllValuesFromRestriction(this.currentIndividual.getRDFType().getURI(),
							model.getObjectProperty(openedTags.get(openedTags.size() - 1)), propInd.getRDFType());
				}
			}
		}
		if (featureMembers.contains(localName)) {
			this.featureMember = true;
			outertagCounter++;
			splitterCountThreshold++;
			if (splitterCountThreshold == 1000) {
				splitterCountThreshold = 0;
			}
		}

		literalBuffer = new StringBuilder();

	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// if(outertagCounter>beginProcessing){
		String literal = new String(ch, start, length);
		literalBuffer.append(literal.trim());
		if (featureMember && openedTags.size() > 1 && !literal.trim().isEmpty()) {
			if (openedTags.get(openedTags.size() - 1).contains("value")) {
				this.setStringAttributeBool(false);
				System.out.println("StringAttribute Adding: " + this.stringAttribute + " - " + literal);
				currentIndividual.addProperty(model.createDatatypeProperty(this.stringAttribute),
						this.determineLiteralType(literal));
				this.stringAttribute = "";
				this.alreadyHandled = true;
			} else if (knownMappings.get(openedTags.get(openedTags.size() - 1)) != null
					&& knownMappings.get(openedTags.get(openedTags.size() - 1)).isObjectProperty()
					&& this.currentRestrictions.containsKey(openedTags.get(openedTags.size() - 1))
					&& !this.currentRestrictions.get(openedTags.get(openedTags.size() - 1))
							.contains("http://www.w3.org/2001/XMLSchema#")
					&& StringUtils.isNumeric(literal)) {
				this.currentIndividual.addProperty(model.getObjectProperty(openedTags.get(openedTags.size() - 1)),
						model.getIndividual(
								this.currentRestrictions.get(openedTags.get(openedTags.size() - 1)) + "_" + literal));
				alreadyHandled = true;
			} else if (openedTags.get(openedTags.size() - 1).contains("Corner")
					&& stack2.lastElement().contains("Envelope")) {
				multipleChildrenBuffer.append("<").append(openedTags2.get(openedTags2.size() - 1)).append(">")
						.append(literal).append("</").append(openedTags2.get(openedTags2.size() - 1)).append(">");
				alreadyHandled = true;
			} else if (openedTags.get(openedTags.size() - 1).contains("posList")
					|| openedTags.get(openedTags.size() - 1).contains("pos")) {
				String wktlit = this.currentIndividual.getRDFType().getLocalName() + "(" + literal + ")";
				this.gmlStrBuilder.delete(0, gmlStrBuilder.length());
				gmlStrBuilder.append("<").append(stack2.lastElement()).append(" xmlns:gml=\"")
						.append(stack.lastElement().substring(0, stack.lastElement().lastIndexOf('#'))).append("\"><")
						.append(openedTags2.get(openedTags2.size() - 1)).append(">").append(literal).append("</")
						.append(openedTags2.get(openedTags2.size() - 1)).append("></").append(stack2.lastElement())
						.append(">");
				String gmlStr = gmlStrBuilder.toString();
				// System.out.println("gmlStr: "+gmlStr);
				if (this.lastlinkedIndividual != null) {
					this.lastlinkedIndividual.addProperty(this.model.createObjectProperty(NSGEO + hasGeometry),
							this.currentIndividual);
					this.lastlinkedIndividual = null;
				}
				this.currentIndividual.addProperty(this.model.createDatatypeProperty(NSGEO + ASGML),
						this.model.createTypedLiteral(gmlStr, NSGEO + GMLLiteral));
				if (!wktlit.contains(POINT))
					wktlit = formatWKTString(wktlit, ' ', 2);
				try {
					if (this.lastlinkedIndividual != null) {
						this.lastlinkedIndividual.addProperty(this.model.createObjectProperty(NSGEO + hasGeometry),
								this.currentIndividual);
						this.lastlinkedIndividual = null;
					}
					wktreader.read(wktlit);
					this.currentIndividual.addProperty(this.model.createDatatypeProperty(NSGEO + WKT),
							this.model.createTypedLiteral(wktlit, NSGEO + WKTLiteral));
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(wktlit);
				}
				alreadyHandled = true;
			}

		}
		/*
		 * if(!this.stringAttribute.isEmpty()) { this.stringAttributeBool=true; }else {
		 * this.stringAttributeBool=false; }
		 */
	}

	public static String formatWKTString(String str, char c, int n) {
		// System.out.println(str);
		if (StringUtils.countMatches(str, " ") <= 1)
			return str;
		StringBuilder builder = new StringBuilder();
		String[] splitted=str.split(c+"");
		for(int i=0;i<splitted.length;i++) {
			if(i%2==0) {
				builder.append(splitted[i]+" ");
			}else {
				builder.append(splitted[i]+",");
			}
		}
		builder.delete(builder.length() - 1, builder.length());
		String result=builder.toString().toUpperCase();
		if(result.contains("LINESTRINGSEGMENT")) {
			result=result.replace("LINESTRINGSEGMENT","LINESTRING");
		}
		if(result.contains("ARCSTRING")) {
			result=result.replace("ARCSTRING","LINESTRING");
		}
		if(result.contains("LINEARRING")) {
			result=result.replace("LINEARRING","POLYGON(")+")";
		}
		return result;
	}

//	private Map<String, String> restrictedTypes(String classType) {
//		Map<String, String> restrictedTypes = new TreeMap<String, String>();
//		Queue<String> superClasses = new LinkedList<String>();
//		for (Iterator<OntClass> supers = this.model.getOntClass(classType).listSuperClasses(true); supers.hasNext();) {
//			OntClass superClass = supers.next();
//
//			if (superClass.isRestriction() && superClass.asRestriction().isAllValuesFromRestriction()) {
//				OntProperty prop = superClass.asRestriction().getOnProperty();
//				restrictedTypes.put(prop.getURI(),
//						((OntClass) superClass.asRestriction().asAllValuesFromRestriction().getAllValuesFrom())
//								.getURI());
//			} else if (superClass.isClass() && superClass.getURI() != null) {
//				// System.out.println("New SuperClass: "+superClass.toString());
//				superClasses.add(superClass.toString());
//			}
//		}
//		if (!superClasses.isEmpty()) {
//			while (!superClasses.isEmpty()) {
//				String cls = superClasses.poll();
//				for (Iterator<OntClass> supers = this.model.getOntClass(cls).listSuperClasses(true); supers
//						.hasNext();) {
//					OntClass superClass = supers.next();
//					if (superClass.isRestriction() && superClass.asRestriction().isAllValuesFromRestriction()) {
//						OntProperty prop = superClass.asRestriction().getOnProperty();
//						restrictedTypes.put(prop.getURI(),
//								((OntClass) superClass.asRestriction().asAllValuesFromRestriction().getAllValuesFrom())
//										.getURI());
//					} else if (superClass.isClass() && superClass.getURI() != null) {
//						// System.out.println("New SuperClass: "+superClass.toString());
//						superClasses.add(superClass.getURI());
//					}
//				}
//			}
//		}
//		return restrictedTypes;
//	}

	private Literal determineLiteralType(String literal) {
		try {
			if (StringUtils.isNumeric(literal) && !literal.contains(".")) {
				return this.model.createTypedLiteral(Integer.valueOf(literal), XSD.xint.getURI());
			} else if (StringUtils.isNumeric(literal) && literal.contains(".")) {
				Double d = Double.valueOf(literal);
				return this.model.createTypedLiteral(d, XSD.xdouble.getURI());
			}
		} catch (Exception e) {

		}
		// System.out.println("DETERMINE LITERAL TYPE - "+literal);
		try {
			Date date = parserSDF1.parse(literal);
			// System.out.println("DETERMINE LITERAL TYPE DATE? "+date);
			if (date != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				String dateStr = sdf.format(date);
				return this.model.createTypedLiteral(dateStr, XSD.dateTime.getURI());
			}
		} catch (Exception e) {

		}
		try {
			Date date = parserSDF2.parse(literal);
			// System.out.println("DETERMINE LITERAL TYPE DATE? "+date);
			if (date != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				String dateStr = sdf.format(date);
				return this.model.createTypedLiteral(dateStr, XSD.date.getURI());
			}
		} catch (Exception e) {

		}
		if (TRUE.equals(literal) || FALSE.equals(literal)) {
			return this.model.createTypedLiteral(Boolean.valueOf(literal), XSD.xboolean.getURI());
		} else {
			return this.model.createTypedLiteral(literal, XSD.xstring.getURI());
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String comb = uri + "#" + localName;
		comb = comb.replace("##", "#");
		String literal = literalBuffer.toString();
		if (!alreadyHandled && !openedTags.isEmpty()) {
			if (!literal.isEmpty()) {
				if (knownMappings.get(openedTags.get(openedTags.size() - 1)) != null
						&& knownMappings.get(openedTags.get(openedTags.size() - 1)).isAnnotationProperty()) {
					this.currentIndividual.addProperty(
							model.createAnnotationProperty(openedTags.get(openedTags.size() - 1)),
							this.model.createOntResource(literal));
					alreadyHandled = true;
				} else if (knownMappings.get(openedTags.get(openedTags.size() - 1)) != null
						&& knownMappings.get(openedTags.get(openedTags.size() - 1)).isDatatypeProperty()) {
					if (!codeSpace.isEmpty()) {
						this.currentIndividual.addProperty(
								model.createObjectProperty(openedTags.get(openedTags.size() - 1)),
								codelist.createIndividual(codeSpace + literal));
					} else {
						Literal liter = this.determineLiteralType(literal);
						this.currentIndividual.addProperty(
								model.createDatatypeProperty(openedTags.get(openedTags.size() - 1)), liter);
						if (domain)
							model.getDatatypeProperty(openedTags.get(openedTags.size() - 1))
									.addDomain(this.currentIndividual.getRDFType());
						if (range)
							model.getDatatypeProperty(openedTags.get(openedTags.size() - 1))
									.addRange(model.getResource(liter.getDatatypeURI()));
						model.createAllValuesFromRestriction(this.currentIndividual.getRDFType().getURI(),
								model.getDatatypeProperty(openedTags.get(openedTags.size() - 1)),
								model.getResource(liter.getDatatypeURI()));
					}
					alreadyHandled = true;
				} else if (knownMappings.get(openedTags.get(openedTags.size() - 1)) != null
						&& knownMappings.get(openedTags.get(openedTags.size() - 1)).isProperty()) {
					if (!codeSpace.isEmpty()) {
						this.currentIndividual.addProperty(
								model.createObjectProperty(openedTags.get(openedTags.size() - 1)),
								codelist.createIndividual(codeSpace + literal));
					} else {
						this.currentIndividual.addProperty(
								model.createDatatypeProperty(openedTags.get(openedTags.size() - 1)),
								this.determineLiteralType(literal));
					}
					alreadyHandled = true;
				}
			}
		}
		if (openedTags.size() % 2 == 0 && !alreadyHandled && !stack.isEmpty() && !openedTags.isEmpty()
				&& literal.isEmpty() && attributes.getLength() > 1) {
			OntClass cls = model.createClass(uuid);
			Individual ind = cls.createIndividual(UUID.randomUUID().toString());
			System.out.println((openedTags.get(openedTags.size() - 1)));
			System.out.println(openedTags.get(openedTags.size() - 1) + " Literal is empty " + attributes.getLength());
			int i = 0;
			while (i < attributes.getLength()) {
				System.out.println(openedTags.get(openedTags.size() - 1) + " - " + attributes.getLocalName(i) + " - "
						+ attributes.getValue(i));
				ind.addProperty(model.createDatatypeProperty(attributes.getLocalName(i)),
						determineLiteralType(attributes.getValue(i)));
				i++;
			}
			this.currentIndividual.addProperty(model.createObjectProperty(openedTags.get(openedTags.size() - 1)), ind);
			this.lastlinkedIndividual = this.currentIndividual;
		}
		// System.out.println("Remove: "+uri+localName+ this.openedTags.contains(comb)+"
		// - "+this.openedTags.size());
		this.openedTags.remove(comb);
		// System.out.println("After removal: "+this.openedTags.size());
		this.openedTags2.remove(qName);

		// if(outertagCounter>beginProcessing){
		// System.out.println("OpenedTags: "+openedTags);
		if (openedTags.size() % 2 == 0 && !stack.isEmpty())

		{
			if (localName.contains("Envelop")) {
				this.setEnvelope(false);
				if (this.lastlinkedIndividual != null)
					this.lastlinkedIndividual.addProperty(this.model.createObjectProperty(NSGEO + hasGeometry),
							this.currentIndividual);
				this.multipleChildrenBuffer.append("</").append(qName).append(">");
				this.currentIndividual.addProperty(this.model.createDatatypeProperty(NSGEO + ASGML),
						this.model.createTypedLiteral(multipleChildrenBuffer.toString(), GMLLiteral));
				if (multipleChildrenBuffer.toString().contains(":pos")) {
					String lit = multipleChildrenBuffer.substring(multipleChildrenBuffer.indexOf(":pos"),
							multipleChildrenBuffer.indexOf(":pos", multipleChildrenBuffer.indexOf(":pos")));
					lit = lit.substring(lit.indexOf('>') + 1, lit.indexOf('<'));
					String wktlit = this.currentIndividual.getRDFType().getLocalName() + "(" + lit + ")";
					if (!wktlit.contains(POINT))
						wktlit = formatWKTString(wktlit, ' ', 2);
					try {
						if (this.lastlinkedIndividual != null) {
							this.lastlinkedIndividual.addProperty(this.model.createObjectProperty(NSGEO + hasGeometry),
									this.currentIndividual);
							this.lastlinkedIndividual = null;
						}
						 wktreader.read(wktlit);
						this.currentIndividual.addProperty(this.model.createDatatypeProperty(NSGEO + WKT),
								this.model.createTypedLiteral(wktlit, NSGEO + WKTLiteral));
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println(wktlit);
					}
				}else if(multipleChildrenBuffer.toString().contains(":upperCorner") && multipleChildrenBuffer.toString().contains(":lowerCorner")) {
					System.out.println(multipleChildrenBuffer.toString());
					String lowlit = multipleChildrenBuffer.substring(multipleChildrenBuffer.indexOf(":lowerCorner"),
							multipleChildrenBuffer.indexOf(":lowerCorner", multipleChildrenBuffer.indexOf(":lowerCorner")+1));
					String uplit = multipleChildrenBuffer.substring(multipleChildrenBuffer.indexOf(":upperCorner"),
							multipleChildrenBuffer.indexOf(":upperCorner", multipleChildrenBuffer.indexOf(":upperCorner")+1));
					System.out.println("Lowlit: "+lowlit);
					System.out.println("Uplit: "+uplit);
					lowlit = lowlit.substring(lowlit.indexOf('>') + 1, lowlit.indexOf('<'));
					uplit = uplit.substring(uplit.indexOf('>') + 1, uplit.indexOf('<'));
					String combinedlit=lowlit+" "+uplit;
					combinedlit="ENVELOPE("+combinedlit.trim().replace(" ",",")+")";
					this.currentIndividual.addProperty(this.model.createDatatypeProperty(NSGEO + WKT),
							this.model.createTypedLiteral(combinedlit, NSGEO + WKTLiteral));
				}
			}
			String lastElement = stack.pop();
			stack2.pop();
			restrictionStack.pop();
			if (!stack.isEmpty()) {
				this.currentIndividual = this.model.getIndividual(stack.lastElement());
				this.importMetaData(this.currentIndividual, this.currentIndividual.getLocalName(), "GDI-DE");
				// System.out.println("endElement addProperty: "+lastElement);
				this.currentIndividual.addProperty(
						this.model.createObjectProperty(this.openedTags.get(this.openedTags.size() - 1)),
						this.model.getIndividual(lastElement));
				if (domain)
					model.getObjectProperty(this.openedTags.get(this.openedTags.size() - 1))
							.addDomain(this.currentIndividual.getRDFType());
			}
			if (!restrictionStack.isEmpty())
				this.currentRestrictions = restrictionStack.lastElement();
			alreadyHandled = true;
		}

		if (featureMembers.contains(localName)) {
			this.featureMember = false;
		}
		if (localName.contains("stringAttribute")) {
			setStringAttributeBool(false);
		}
		if ((uri + "#" + localName).equals(currentType)) {
			this.inClass = false;
		}
		this.codeSpace = "";
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {

	}

	public static void restructureDomains(OntModel model) {
		ExtendedIterator<ObjectProperty> test = model.listObjectProperties();
		List<ObjectProperty> objprops = new LinkedList<ObjectProperty>();
		while (test.hasNext()) {
			objprops.add(test.next());
		}
		test.close();
		for (ObjectProperty prop : objprops) {
			ExtendedIterator<? extends OntResource> domains = prop.listDomain();
			List<RDFNode> elements = new LinkedList<RDFNode>();
			List<RDFNode> elements2 = new LinkedList<RDFNode>();
			Boolean first = true, second = true;
			if (domains.hasNext())
				while (domains.hasNext()) {
					if (second && !first) {
						second = false;
					}
					if (first) {
						first = false;
					}
					OntResource curdom = domains.next();
					if (!curdom.getURI().contains("http://www.w3.org/2000/01/rdf-schema#")
							&& !curdom.getURI().contains(OWL.NS))
						elements.add(curdom);
				}
			if (!second)
				prop.setDomain(
						model.createUnionClass(null, model.createList(elements.toArray(new RDFNode[elements.size()]))));
			domains.close();
			first = true;
			second = true;
			ExtendedIterator<? extends OntResource> ranges = prop.listRange();
			while (ranges.hasNext()) {
				if (second && !first) {
					second = false;
				}
				if (first) {
					first = false;
				}
				OntResource curran = ranges.next();
				if (!curran.getURI().contains("http://www.w3.org/2000/01/rdf-schema#")
						&& !curran.getURI().contains(OWL.NS))
					elements2.add(curran);
			}
			if (!second)
				prop.setRange(model.createUnionClass(null,
						model.createList(elements2.toArray(new RDFNode[elements2.size()]))));
			ranges.close();
		}
		List<DatatypeProperty> dataprops = new LinkedList<DatatypeProperty>();
		ExtendedIterator<DatatypeProperty> test2 = model.listDatatypeProperties();
		while (test2.hasNext()) {
			dataprops.add(test2.next());
		}
		test2.close();

		for (DatatypeProperty prop2 : dataprops) {
			// DatatypeProperty prop2 = test2.next();
			ExtendedIterator<? extends OntResource> domains2 = prop2.listDomain();
			List<RDFNode> doms = new LinkedList<RDFNode>();
			List<RDFNode> rgs = new LinkedList<RDFNode>();
			Boolean first = true, second = true;
			while (domains2.hasNext()) {
				if (second && !first) {
					second = false;
				}
				if (first) {
					first = false;
				}
				OntResource curdom = domains2.next();
				if (curdom.getURI() != null && !curdom.getURI().contains("http://www.w3.org/2000/01/rdf-schema#")
						&& !curdom.getURI().contains(OWL.NS))
					doms.add(curdom);
				// domains.add(domains2.next());
			}
			if (!second)
				prop2.setDomain(model.createUnionClass(null, model.createList(doms.toArray(new RDFNode[doms.size()]))));
			domains2.close();
			first = true;
			second = true;
			ExtendedIterator<? extends OntResource> ranges2 = prop2.listRange();
			while (ranges2.hasNext()) {
				if (second && !first) {
					second = false;
				}
				if (first) {
					first = false;
				}
				OntResource curran = ranges2.next();
				if (!curran.getURI().contains("http://www.w3.org/2000/01/rdf-schema#")
						&& !curran.getURI().contains(OWL.NS))
					rgs.add(curran);
			}
			if (!second)
				prop2.setRange(model.createUnionClass(null, model.createList(rgs.toArray(new RDFNode[rgs.size()]))));
			ranges2.close();
		}
		test2.close();
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void startDocument() throws SAXException {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void endDocument() throws SAXException {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		throw new UnsupportedOperationException("not yet implemented");
	}

	public Boolean getEnvelope() {
		return envelope;
	}

	public void setEnvelope(Boolean envelope) {
		this.envelope = envelope;
	}

	public Boolean getStringAttributeBool() {
		return stringAttributeBool;
	}

	public void setStringAttributeBool(Boolean stringAttributeBool) {
		this.stringAttributeBool = stringAttributeBool;
	}

}

