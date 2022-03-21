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
package info.ponciano.lab.Spalodwfs.geotime.models.semantic;

import info.ponciano.lab.pisemantic.PiSparql;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSet;

/**
 * Knowledge base singleton class to manage semantic access.
 *
 * @author Dr Jean-Jacques Ponciano Contact: jean-jacques@ponciano.info
 */
public class KB implements KnowledgeBaseInterface {

    public static final String STORAGE_DIR = "dynamic_storage";
    public static final String URI = "http://lab.ponciano.info/ont/spalod";
    private static KB kb = null;
    public static final String NS = "http://lab.ponciano.info/ont/spalod#";
    private static final String DEFAULT_ONTO_ISO = "src/main/resources/ontologies/iso-19115.owl";
    private static final String DEFAULT_ONTO = "src/main/resources/ontologies/spalod.owl";
    public static final String OUT_ONTO = "SpalodOutput.owl";
    private OwlManagement model;

    public static KB get() throws OntoManagementException {
        if (kb == null) {
            kb = new KB();
        }
        return kb;
    }

    private KB() throws OntoManagementException {
        File file = new File(OUT_ONTO);
        if (file.exists()) {
            try {
                this.model = new OwlManagement(OUT_ONTO);
            } catch (Exception e) {
                file.delete();
                this.model = new OwlManagement(DEFAULT_ONTO);
                this.model.ont.setNs(NS);
            }

        } else {
            this.model = new OwlManagement(DEFAULT_ONTO);
            this.model.ont.setNs(NS);
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
        try {
            KB.get();
        } catch (OntoManagementException ex) {
            Logger.getLogger(KB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public PiSparql getOntEmpty() throws OntoManagementException {
        PiSparql o = new OwlManagement(DEFAULT_ONTO).ont;
        o.setNs(NS);
        return   o;
    }
}
