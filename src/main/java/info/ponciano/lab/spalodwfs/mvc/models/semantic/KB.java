/*
 * Copyright (C) 2020 Dr Jean-Jacques Ponciano Contact: jean-jacques@ponciano.info.
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
package info.ponciano.lab.spalodwfs.mvc.models.semantic;
    import java.net.URI;
import java.net.URISyntaxException;

import info.ponciano.lab.pisemantic.PiOnt;
import info.ponciano.lab.pisemantic.PiOntologyException;
import info.ponciano.lab.pisemantic.PiSparql;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.update.*;

/**
 * Knowledge base singleton class to manage semantic access.
 *
 * @author Dr Jean-Jacques Ponciano Contact: jean-jacques@ponciano.info
 */
public class KB implements KnowledgeBaseInterface {
    
    public static final String GRAPHDB="http://localhost";
    
    public static final String GRAPHDB_QUERY_ENDPOINT = GRAPHDB+ ":7200/repositories/Spalod";
    public static final String GRAPHDB_UPDATE_ENDPOINT = GRAPHDB+":7200/repositories/Spalod/statements";
    // public static final String SERVER="https://spalod.northeurope.cloudapp.azure.com";
    public static final String STORAGE_DIR = "dynamic_storage";
    public static final String URI = "https://spalod.northeurope.cloudapp.azure.com";
    public static final String NS = URI+"#";
    private static KB kb = null;
  
    private static final String DEFAULT_ONTO_ISO = "src/main/resources/ontologies/iso-19115.owl";
    public static final String DEFAULT_ONTO = "src/main/resources/ontologies/spalod.owl";
    private static final String SCHEAMORD_PATH = "src/main/resources/ontologies/schemaorg.owl";

    public static final String OUT_ONTO = "SpalodOutput.owl";

    public static final String PREFIX = "PREFIX schema: <http://schema.org/>\n"
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
            + "PREFIX hist: <http://wikiba.se/history/ontology#>\n"
            + "PREFIX wd: <http://www.wikidata.org/entity/>\n"
            + "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n"
            + "PREFIX wikibase: <http://wikiba.se/ontology#>\n"
            + "PREFIX dcat: <http://www.w3.org/ns/dcat#>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX bd: <http://www.bigdata.com/rdf#>\n"
            + "PREFIX wds: <http://www.wikidata.org/entity/statement/>\n"
            + "PREFIX wdv: <http://www.wikidata.org/value/>\n"
            + "PREFIX p: <http://www.wikidata.org/prop/>\n"
            + "PREFIX ps: <http://www.wikidata.org/prop/statement/>\n"
            + "PREFIX psv: <http://www.wikidata.org/prop/statement/value/>\n"
            + "PREFIX pq: <http://www.wikidata.org/prop/qualifier/>\n"
            + "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>\n"
            + "PREFIX dp: <http://dbpedia.org/resource/>\n"
            + "PREFIX dpp: <http://dbpedia.org/property/>\n"
            + "PREFIX spalod: <" + KB.NS + ">\n"
            + "PREFIX geosparql: <http://www.opengis.net/ont/geosparql#>\n"
            + "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
    private OwlManagement model;

    public static KB get() {
        if (kb == null) {
            kb = new KB();
        }
        return kb;
    }

    private KB() {
        try {
            File file = new File(OUT_ONTO);
            if (file.exists()) {
                try {
                    System.out.println("Load: " + OUT_ONTO);
                    this.model = new OwlManagement(OUT_ONTO);
                } catch (Exception e) {
                    file.delete();
                    System.out.println("Reset from : " + DEFAULT_ONTO);
                    this.model = new OwlManagement(DEFAULT_ONTO);

                }

            } else {
                System.out.println("Set from : " + DEFAULT_ONTO);
                this.model = new OwlManagement(DEFAULT_ONTO);
            }
            this.model.ont.setNs(NS);
        } catch (OntoManagementException ex) {
            ex.printStackTrace();
        }
    }


    public static OntModel getSchemaOrg() {
        try {
            return new PiOnt(SCHEAMORD_PATH).getOnt();
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public String getOutputPath() {
        return OUT_ONTO;
    }

    /**
     * Save the current ontology in an OWL file.
     *
     * @throws IOException If the file cannot be written.
     */
    public void save() throws IOException {
        this.model.saveOntology(OUT_ONTO);
    }

    @Override
    public void addPrefix(String key, String namespace) {
        this.model.addPrefix(key, namespace);
    }

    @Override
    public boolean uplift(String xmlPathfile) {
        return this.model.uplift(xmlPathfile);
    }

    @Override
    public String downlift(String metadataURI) throws OntoManagementException {
        return this.model.downlift(metadataURI);
    }

    @Override
    public boolean change(String ind, String property, String value) {
        return this.model.change(ind, property, value);
    }

    @Override
    public String getSPARQL(String query) {
        return this.model.getSPARQL(query);
    }

    @Override
    public boolean construct(String queryString) throws OntoManagementException {
        return this.model.construct(queryString);
    }

    @Override
    public ResultSet select(String queryString) {
        return this.model.select(queryString);
    }

    @Override
    public void update(String query) throws OntoManagementException {
        this.model.update(query);
    }

    @Override
    public List<String[]> queryAsArray(String query, String[] var, boolean fullURI, boolean onlyNS) {
        return this.model.queryAsArray(query, var, fullURI, onlyNS);
    }

    @Override
    public void dataImport(String mduri, String ttlpath) {
        this.model.dataImport(mduri, ttlpath);
    }

    @Override
    public PiSparql getOnt() {
        return this.model.ont;
    }

    public void add(OntModel ont) {
        this.model.ont.getOnt().add(ont);
    }

    public static void main(String[] args) {
        KB.get();
    }

    public PiSparql getOntEmpty() throws OntoManagementException {
        PiSparql o = new OwlManagement(DEFAULT_ONTO).ont;
        o.setNs(NS);
        return o;
    }

    public static ResultSet select(OntModel ont, String queryString) {
        String prefix = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
        prefix = prefix + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
        prefix = prefix + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
        prefix = prefix + "PREFIX dbr:    <http://dbpedia.org/resource/>\n";
        prefix = prefix + "PREFIX dbo:    <http://dbpedia.org/ontology/>\n";
        prefix = prefix + "PREFIX dct:    <http://purl.org/dc/terms/>\n";
        prefix = prefix + "PREFIX owl:    <http://www.w3.org/2002/07/owl#>\n";
        prefix = prefix + "PREFIX prov:   <http://www.w3.org/ns/prov#>\n";
        prefix = prefix + "PREFIX qb:     <http://purl.org/linked-data/cube#>\n";
        prefix = prefix + "PREFIX qudt:   <http://qudt.org/1.1/schema/qudt#>\n";
        prefix = prefix + "PREFIX schema: <http://schema.org/>\n";
        prefix = prefix + "PREFIX skos:   <http://www.w3.org/2004/02/skos/core#>\n";
        prefix = prefix + "PREFIX unit:   <http://qudt.org/vocab/unit#>\n";
        prefix = prefix + "PREFIX sdmx:   <http://purl.org/linked-data/sdmx#>\n";
        if (queryString != null && !queryString.isEmpty()) {
            queryString = prefix + queryString;
            Query query = QueryFactory.create(queryString);
            QueryExecution queryExecution = QueryExecutionFactory.create(query, ont);
            return queryExecution.execSelect();
        } else {
            return null;
        }
    }

    public void update(String[] statement) {

        String query = "INSERT DATA{" + statement[0] + " " + statement[1] + " " + statement[2] + "}";
        System.out.println("query = " + query);
        try {
            this.getOnt().update(query);
        } catch (PiOntologyException e) {
            e.printStackTrace();
        }

    }

public static String sparqlValue(String str) {
    // Regular expression for checking if a string represents a float
    String floatPattern = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";
    // Regular expression for checking if a string represents a date in ISO 8601 format
    String datePattern = "\\d{4}-\\d{2}-\\d{2}";
    // Regular expression for checking if a string represents a boolean
    String boolPattern = "true|false";
    // Regular expression for checking if a string represents an integer
    String intPattern = "-?\\d+";
    // Regular expression for checking if a string is a prefixed name (in the format "prefix:something")
    String prefixPattern = "\\w+:\\w+";
    
    if (str.matches(prefixPattern)||str.startsWith("spalod:")) {

        // The string is a prefixed name
        return str;
    }

    try {
        // Check if the string is a valid URI
        URI uri = new URI(str);
        if (uri.getScheme() != null && uri.getHost() != null) {
            return "<" + str + ">";
        }
    } catch (IllegalArgumentException | URISyntaxException ex) {
        // Not a URI
    }

    if (str.matches(floatPattern)) {
        // The string represents a float
        return "\"" + str + "\"^^<http://www.w3.org/2001/XMLSchema#float>";
    }

    if (str.matches(datePattern)) {
        // The string represents a date
        return "\"" + str + "\"^^<http://www.w3.org/2001/XMLSchema#date>";
    }
    
    if (str.matches(boolPattern)) {
        // The string represents a boolean
        return "\"" + str + "\"^^<http://www.w3.org/2001/XMLSchema#boolean>";
    }

    if (str.matches(intPattern)) {
        // The string represents an integer
        return "\"" + str + "\"^^<http://www.w3.org/2001/XMLSchema#integer>";
    }

    // If none of the above, we assume it's a simple literal
    return "\"" + str + "\"";
}

}
