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

import info.ponciano.lab.pisemantic.PiOnt;
import java.util.List;

import org.apache.jena.query.ResultSet;

/**
 * Interface to manage knowledge.
 *
 * @author Dr Jean-Jacques Ponciano Contact: jean-jacques@ponciano.info
 */
public interface KnowledgeBaseInterface {

    /**
     * Add prefix for every spaql query
     *
     * @param key kew use as prefix
     * @param namespace name space to be added.      <pre>
     * Example for  xsd:type \n"
     * addPrefix(xsd,http://www.w3.org/2001/XMLSchema#)
     * </pre>
     */
    public abstract void addPrefix(String key, String namespace);

    /**
     * Uplift Metadata from an XML file to the ontology
     *
     * @param xml the XML file path on the server.
     * @return true if the elevation succeeded, false otherwise.
     */
    public abstract boolean uplift(String xml);

    /**
     * Downlifts ontology metadata information as a string formatted in XML.
     *
     * @param metadataURI URI of the metadata in the ontology
     * @return the XML String in iso-119115 format.
     */
    public abstract String downlift(String metadataURI) throws OntoManagementException;

    public abstract boolean change(String ind, String property, String value);

    //**************************************************************************
    // ---------------------------- SPARQL -----------------------------------
    //**************************************************************************
    /**
     * Executes SPARQL select function and format the results in an String
     *
     * @param query select query
     * @return query's results in String format.
     */
    public abstract String getSPARQL(String query);

    /**
     * Execute the SPARQL construct query.
     *
     * @param queryString query to process.
     * @return true if the execution was successful.
     * @throws OntoManagementException
     */
    public abstract boolean construct(String queryString) throws OntoManagementException;

    /**
     * Executes SPARQL select query.
     * <p>
     * Example of use:
     * </p>
     * 
     * <pre><code>
     *   ResultSet select = this.select(query);
     * List gts = new ArrayList(); while (select.hasNext()) {
     * Resource resource = select.next().getResource(vcode); gts.add(resource);
     * }
     * </code></pre>
     *
     * @param queryString SPARQL query string.
     * @return ResultSet obtained from the query selection.
     */
    public abstract ResultSet select(String queryString);

    /**
     * Execute a update query on the data set
     *
     * @param query query to be executed
     * @throws OntoManagementException if something is wrong
     */
    public abstract void update(String query) throws OntoManagementException;
    
    /**
     * Function that executes a SPARQL query and return the result as a list of
     * String table
     *
     * @param query contains the SPARQL query to execute
     * @param var contains the different variables of the SPARQL query, whose
     * the result will be returned
     * @param fullURI: if fullURI is true, returns the full URI of the resources, 
     * else returns the local name of the resources
     * @param onlyNS: if onlyNS is true, it does not return results with external 
     * name space, else returns all results
     * @return a list of string table containing each result row for the seta of
     * variables
     */
    public List<String[]> queryAsArray(String query, String[] var, boolean fullURI, boolean onlyNS);

    /**
     * Function to import a data associated to its metadata into the knowledge 
     * base
     * 
     * @param mduri: URI of the metadata linked to the data to import
     * @param ttlpath: data content represented through triples
     */
    public void dataImport(String mduri, String ttlpath);
    
    /**
     * 
     * @return the OntModel of the knowledge base
     */
    public PiOnt getOnt();
}
