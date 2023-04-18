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
package info.ponciano.lab.spalodwfs.mvc.models;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

import info.ponciano.lab.pitools.files.PiFile;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.OntoManagement;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.OntoManagementException;

/**
 *
 * @author Claire Prudhomme
 */
public class SHPdata {

    private static final String DIR_RDF_DATA = "rdf-data/";
    private static final String DIR_R2ML_MAPPING = "r2rml-mapping/";
    public static final String DIR_SHP_DATA = "shp-data/";
    private String metadata;
    private String title;
    private String version;
    private String versionNote;
    private String prevAsset;
    private String ontoUri;

    public SHPdata() {
    }

    public SHPdata(String version, String versionNote) {
        this.version = version;
        this.versionNote = versionNote;
        this.prevAsset = null;
        this.title = "test";
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionNote() {
        return versionNote;
    }

    public void setVersionNote(String versionNote) {
        this.versionNote = versionNote;
    }

    public String getPrevAsset() {
        return prevAsset;
    }

    public void setPrevAsset(String prevAsset) {
        this.prevAsset = prevAsset;
    }

    public String getOntoUri() {
        return this.ontoUri;
    }

    /**
     * Function that creates the RDF representation of a versioned asset
     * representing shapefile data
     *
     * @param rdfdata, path to the local RDF file representing the shapefile
     * content
     * @param shpdata, path to the local shapefile
     * @return the OntModel containing the RDF representation of a versioned
     * asset representing shapefile data
     * @throws Exception
     */
    public OntModel representationRDF(String rdfdata, String shpdata) throws Exception {

        OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        String gtdcat = "http://lab.ponciano.info/ontology/2020/Spalod/dcat#";

        // if the data is an update (that means it has a previous asset),
        // some of its attributes must beinitiated according to its previous version
        if (this.prevAsset != null) {
            this.initAttUpdate();
        } else {
            // retrieve the selected metadata uri according to its title
            Metadata md = new Metadata();
            List<String[]> info = md.getMetadata();
            int i = 0;
            boolean found = false;
            String mduri = "";
            while (i < info.size() && !found) {
                if (info.get(i)[2].equals(this.metadata)) {
                    mduri = info.get(i)[0];
                    this.metadata = OntoManagement.NS + mduri;
                    System.out.println("upload case:" + this.metadata);
                    // create the metadata individual with catalog record type
                    /*
					 * OntClass c = ont.createClass("http://www.w3.org/ns/dcat#CatalogRecord");
					 * c.createIndividual(OntoManagement.NS + mduri);
					 * 
					 * // add the GDI catalog c =
					 * ont.createClass("http://www.w3.org/ns/dcat#Catalog"); c.createIndividual(
					 * "http://lab.ponciano.info/ontology/2020/Spalod/dcat#gdi_catalog");
					 * 
					 * // add the metadata as a record of the catalog ObjectProperty p =
					 * ont.createObjectProperty("http://www.w3.org/ns/dcat#record");
					 * ont.add(ont.getResource(gtdcat + "gdi_catalog"), p,
					 * ont.getResource(OntoManagement.NS + mduri)); 
                     */
                    found = true;
                }
                i++;
            }
        }

        // create a new dataset version = an Asset
        UUID dsUri = UUID.randomUUID();
        // OntClass c=ont.createClass("http://www.w3.org/ns/dcat#Dataset");
        OntClass c = ont.createClass("http://www.w3.org/ns/adms#Asset");
        c.createIndividual(gtdcat + dsUri);
        this.ontoUri = gtdcat + dsUri;

        // add the title of the dataset
        DatatypeProperty dp = ont.createDatatypeProperty("http://purl.org/dc/elements/1.1/title");
        ont.add(ont.getResource(gtdcat + dsUri), dp, this.title);

        // add the version of the dataset through owl:versionInfo
        dp = ont.createDatatypeProperty("http://www.w3.org/2002/07/owl#versionInfo");
        ont.add(ont.getResource(gtdcat + dsUri), dp, this.version);

        // add the version note of the dataset through adms:versionNotes
        dp = ont.createDatatypeProperty("http://www.w3.org/ns/adms#versionNotes");
        ont.add(ont.getResource(gtdcat + dsUri), dp, this.versionNote);

        // add the default status of the dataset, which is unverified
        String statusURI = "http://lab.ponciano.info/ontology/2020/Spalod/data-status#unverified";
        c = ont.createClass("http://www.w3.org/2004/02/skos/core#Concept");
        c.createIndividual(statusURI);
        ObjectProperty op = ont.createObjectProperty("http://www.w3.org/ns/adms#status");
        ont.add(ont.getResource(gtdcat + dsUri), op, ont.getResource(statusURI));

        // if the data is an update (that means it has a previous asset),
        // add the links adms:next and adms:prev between it and its previous asset
        if (this.prevAsset != null) {
            // add the links adms:next
            op = ont.createObjectProperty("http://www.w3.org/ns/adms#next");
            ont.add(ont.getResource(this.prevAsset), op, ont.getResource(gtdcat + dsUri));
            // add the links adms:prev
            op = ont.createObjectProperty("http://www.w3.org/ns/adms#prev");
            ont.add(ont.getResource(gtdcat + dsUri), op, ont.getResource(this.prevAsset));
        }
        // add the property adms:last to itself
        op = ont.createObjectProperty("http://www.w3.org/ns/adms#last");
        ont.add(ont.getResource(gtdcat + dsUri), op, ont.getResource(gtdcat + dsUri));

        // link dataset to catalog through dcat:dataset
        op = ont.createObjectProperty("http://www.w3.org/ns/dcat#dataset");
        ont.add(ont.getResource("http://lab.ponciano.info/ontology/2020/Spalod/dcat#gdi_catalog"), op,
                ont.getResource(gtdcat + dsUri));

        // link dataset to metadata record through foaf:primaryTopic
        System.out.println(this.metadata);
        op = ont.createObjectProperty("http://xmlns.com/foaf/0.1/primaryTopic");
        ont.add(ont.getResource(this.metadata), op, ont.getResource(gtdcat + dsUri));

        // create the RDF distribution
        UUID distRdfUri = UUID.randomUUID();
        c = ont.createClass("http://www.w3.org/ns/dcat#Distribution");
        c.createIndividual(gtdcat + distRdfUri);
        // create the SHP distribution
        UUID distShpUri = UUID.randomUUID();
        c.createIndividual(gtdcat + distShpUri);

        // associate the distributions to the newly created dataset
        op = ont.createObjectProperty("http://www.w3.org/ns/dcat#distribution");
        ont.add(ont.getResource(gtdcat + dsUri), op, ont.getResource(gtdcat + distRdfUri));
        ont.add(ont.getResource(gtdcat + dsUri), op, ont.getResource(gtdcat + distShpUri));

        // create a dataservice
        UUID dservUriRDF = UUID.randomUUID();
        c = ont.createClass("http://www.w3.org/ns/dcat#DataService");
        c.createIndividual(gtdcat + dservUriRDF);
        UUID dservUriSHP = UUID.randomUUID();
        c.createIndividual(gtdcat + dservUriSHP);

        // create the local access as a resource
        String hrefRDF = "https://local/" + DIR_RDF_DATA + rdfdata;
        ont.createResource(hrefRDF);
        String hrefSHP = "https://local/" + DIR_SHP_DATA + shpdata;
        ont.createResource(hrefSHP);

        // associate it the url to access it
        op = ont.createObjectProperty("http://www.w3.org/ns/dcat#endpointURL");
        ont.add(ont.getResource(gtdcat + dservUriRDF), op, ont.getResource(hrefRDF));
        ont.add(ont.getResource(gtdcat + dservUriSHP), op, ont.getResource(hrefSHP));

        // associate the dataservice to the distribution
        op = ont.createObjectProperty("http://www.w3.org/ns/dcat#accessService");
        ont.add(ont.getResource(gtdcat + distRdfUri), op, ont.getResource(gtdcat + dservUriRDF));
        ont.add(ont.getResource(gtdcat + distShpUri), op, ont.getResource(gtdcat + dservUriSHP));

        // add the title to the distribution
        String ttle = this.title + " (RDF data)";
        dp = ont.createDatatypeProperty("http://purl.org/dc/elements/1.1/title");
        ont.add(ont.getResource(gtdcat + distRdfUri), dp, ttle);
        ttle = this.title + " (Shapefile)";
        ont.add(ont.getResource(gtdcat + dservUriSHP), dp, ttle);
        return ont;
    }

    /**
     * Initialization of the class attributes in the case of the adding of a new
     * asset version
     *
     * @throws Exception
     */
    private void initAttUpdate() throws Exception {
        String[] val = this.prevAsset.split("/");
        this.title = val[0];
        this.prevAsset = "http://lab.ponciano.info/ontology/2020/Spalod/dcat#" + val[1];
        List<String[]> info = this.getlastDataInfo();
        if (info.size() != 0) {
            this.metadata = info.get(0)[0];
            System.out.println("update case:" + this.metadata);
            // the version is computed according to its previous version number
            this.computeVersion(info.get(0)[1]);
        } else {
            throw new Exception(
                    "Error to retrieve metadata and version of the previous data version in initAttUpdate()");
        }
    }

    /**
     * Compute and set the version number of the current data according to the
     * version number of its previous version
     *
     * @param v String containing the previous version
     */
    private void computeVersion(String v) {
        // remove the V of version
        String vnum = v.substring(1);
        // split integer part from decimal part
        String[] val = vnum.split("\\.");
        // transform the string into int
        int num = Integer.parseInt(val[0]);
        // increase the int
        num++;
        this.version = "V" + num + ".0";
    }

    /**
     * Uplift a shape file to RDF data by executing an external script
     *
     * @param pathSHP path of the shape file
     * @param URI URI use to creates RDF individuals
     * @return [0]: path of the RDF data, and in [1]: path of the mapping TTL
     * file
     * @throws IOException
     */
    public static String[] shpUpliftProcess(String pathSHP, String URI) throws IOException {
        String scriptJarPAth = "script/geotriples-dependencies.jar";
        PiFile scriptDir = new PiFile("script");
        if (!scriptDir.exists()) {
            scriptDir.mkdir();
        }
        PiFile scriptJar = new PiFile(scriptJarPAth);
        if (!scriptJar.exists()) {
            downloadDependencies(scriptJarPAth);
        }

        boolean sync = true;
        // clone the repository according to the GIT
        // JGit jgit=new JGit("geotriples",
        // "https://github.com/LinkedEOData/GeoTriples.git"); Could be interesting ot
        // use git directly

        PiFile rdfData = new PiFile(DIR_RDF_DATA);
        PiFile r2ml = new PiFile(DIR_R2ML_MAPPING);
        // test if the directories exists. If not creates it
        if (!rdfData.exists()) {
            rdfData.mkdir();
        }
        if (!r2ml.exists()) {
            r2ml.mkdir();
        }

        String pathTTL = rdfData + UUID.randomUUID().toString() + ".ttl";
        String pathMappingTTL = r2ml + UUID.randomUUID().toString() + ".ttl";
        // String dir_script = "src/main/resources/script/";

        /* Generate Mapping files: */
        // String cmdGMF = "java -cp " + dir_script +
        // "geotriples-core/geotriples-dependencies.jar
        // eu.linkedeodata.geotriples.GeoTriplesCMD generate_mapping -o " +
        // pathMappingTTL + " -b " + URI + " " + pathSHP;
        String cmdGMF = "java -cp " + scriptJarPAth + " eu.linkedeodata.geotriples.GeoTriplesCMD generate_mapping -o  "
                + pathMappingTTL + " -b " + URI + " " + pathSHP;

        System.out.println(cmdGMF);
        var p1 = Runtime.getRuntime().exec(cmdGMF);
        /* Transform file into RDF */
        if (sync) {
            while (p1.isAlive()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
        // java -cp <geotriples-core/ dependencies jar>
        // eu.linkedeodata.geotriples.GeoTriplesCMD dump_rdf -o <output file> -b <URI
        // base> (-sh <shp file>) <(produced) mapping file (.ttl)>
        String cmdRDF = "java -cp " + scriptJarPAth + " eu.linkedeodata.geotriples.GeoTriplesCMD dump_rdf -o " + pathTTL
                + " -b " + URI + " -sh " + pathSHP + " " + pathMappingTTL;
        System.out.println(cmdRDF);
        var p2 = Runtime.getRuntime().exec(cmdRDF);

        if (sync) {
            while (p2.isAlive()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        return new String[]{pathTTL, pathMappingTTL};

    }

    /**
     * Function to download a jar dependency to heavy for GitHub
     *
     * @param scriptJarPAth, path of the jar to download
     */
    private static final void downloadDependencies(String scriptJarPAth) {
        String url = "https://seafile.rlp.net/f/c2d485a9fb4a4415b271/?dl=1";
        try (BufferedInputStream inputStream = new BufferedInputStream(new URL(url).openStream());
                FileOutputStream fileOS = new FileOutputStream(scriptJarPAth)) {
            byte data[] = new byte[1024];
            int byteContent;
            while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
                fileOS.write(data, 0, byteContent);
            }
        } catch (IOException e) {
            System.err.println("SHP 2 RDF error: dependancies not downloaded\n" + e.getMessage());
        }
    }

    // Main used to test the functionality and download the dependency
    public static void main(String[] args) {
        String URI = "http://i3mainz.de/";
        String shapeFilePAth = "src/main/resources/datatest/vg250krs.shp";
        String[] results;
        try {
            results = shpUpliftProcess(shapeFilePAth, URI);

            // assert not null
            if (results.length == 2 && results[0] != null && results[1] != null) {
                System.out.println("Path RDF: " + results[0]);
                System.out.println("Path RDF: " + results[1]);
                if (!new PiFile(results[0]).exists() || !new PiFile(results[1]).exists()) {
                    System.err.println("Something wrongin shpUpliftProcess(" + shapeFilePAth + "," + URI
                            + "): the results files are not created");
                }

            } else {
                System.err.println("Something wrongin shpUpliftProcess(" + shapeFilePAth + "," + URI + ")");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to retrieve instances of last versioned asset and their
     * associated title
     *
     * @return a list containing for each last versionned asset its uri and its
     * title in a string table
     * @throws OntoManagementException in case of failure during SPARQL query
     */
    public List<String[]> getDataSet() throws OntoManagementException {
        List<String[]> info = new ArrayList<String[]>();

        // initialize the query to retrieve instances of last versionned asset and their associated title
        String query = "SELECT ?t ?d " + "WHERE{" + "?d rdf:type <http://www.w3.org/ns/adms#Asset>. "
                + "?d2 <http://www.w3.org/ns/adms#last> ?d. " + "?d <http://purl.org/dc/elements/1.1/title> ?t. " + "}";
        System.out.println(query);
        System.out.println(KB.get().getSPARQL(query));
        // create the table of variables
        String[] var = {"t", "d"};
        // query the ontology
        info = KB.get().queryAsArray(query, var, false, false);
        return info;
    }

    /**
     * Transform a list of string table into a list of string, in which the
     * string is the concatenation of the table
     *
     * @param infoset, list of string table
     * @return a list of string, in which the string is the concatenation of the
     * table
     */
    public List<String> getInfoString(List<String[]> infoset) {
        List<String> info = new ArrayList<String>();
        for (int i = 0; i < infoset.size(); i++) {
            String[] var = infoset.get(i);
            if (var.length >= 0) {
                String res = var[0];
                for (int j = 1; j < var.length; j++) {
                    System.out.println(var[j]);
                    res += "/" + var[j];
                }
                info.add(res);
            }
        }
        return info;
    }

    /**
     * Function that retrieves version numbeer and metadata associated to the
     * previous version of the current asset
     *
     * @return the list containing the metadata uri and the version of the
     * previous version in a string table
     * @throws OntoManagementException
     */
    public List<String[]> getlastDataInfo() throws OntoManagementException {
        List<String[]> info = new ArrayList<String[]>();

        // initialize the query to retrieve all instances of asset and their associated
        // organization, title, and dataset title
        String query = "SELECT ?m ?v " + "WHERE{" + "<" + this.prevAsset
                + "> <http://www.w3.org/2002/07/owl#versionInfo> ?v. " + "?m <http://xmlns.com/foaf/0.1/primaryTopic> <"
                + this.prevAsset + ">. " + "}";
        System.out.println(query);
        System.out.println(KB.get().getSPARQL(query));
        // create the table of variables
        String[] var = {"m", "v"};
        // query the ontology
        info = KB.get().queryAsArray(query, var, true, false);
        return info;
    }

    /**
     * Function that retrieves the previous versions of the current asset
     *
     * @return the list containing the uri of previous versions as a string
     * @throws OntoManagementException
     */
    public List<String[]> getpreviousVersion() throws OntoManagementException {
        List<String[]> info = new ArrayList<String[]>();

        // initialize the query to retrieve all instances of previous asset
        String query = "SELECT ?a "
                + "WHERE{ ?a rdf:type <http://www.w3.org/ns/adms#Asset>. ?a <http://www.w3.org/ns/adms#last> <"
                + this.prevAsset + ">. " + "}";
        System.out.println(query);
        System.out.println(KB.get().getSPARQL(query));
        // create the table of variables
        String[] var = {"a"};
        // query the ontology
        info = KB.get().queryAsArray(query, var, true, false);
        return info;
    }
}
