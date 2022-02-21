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
package info.ponciano.lab.Spalodwfs.models.geojson;

import info.ponciano.lab.pisemantic.PiOnt;
import info.ponciano.lab.pisemantic.PiOntologyException;
import info.ponciano.lab.pitools.files.PiFile;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.ontology.Individual;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Dr. Jean-Jacques Ponciano
 */
public class GeoJsonRDFTest {

    private String geoExample = "{\n"
            + "	\"type\" : \"FeatureCollection\",\n"
            + "	\"name\" : \"HS\",\n"
            + "	\"features\" : [\n"
            + "		{\n"
            + "			\"type\" : \"Feature\",\n"
            + "			\"geometry\" : {\n"
            + "				\"type\" : \"Point\",\n"
            + "				\"coordinates\" : [ 6.0779364934, 50.7776408005 ]\n"
            + "			},\n"
            + "			\"properties\" : {\n"
            + "				\"HS_Nr\" : \"1\",\n"
            + "				\"Name\" : \"Rheinisch-Westfälische Technische Hochschule Aachen\",\n"
            + "				\"Kurzname\" : \"Aachen TH\",\n"
            + "				\"Strasse\" : \"Templergraben\",\n"
            + "				\"Hn\" : \"55\",\n"
            + "				\"PLZ\" : \"52062\",\n"
            + "				\"Ort\" : \"Aachen\",\n"
            + "				\"Telefon\" : \"0241/80-1\",\n"
            + "				\"Telefax\" : \"0241/80-92312\",\n"
            + "				\"Homepage\" : \"www.rwth-aachen.de\",\n"
            + "				\"HS_Typ\" : \"Universitäten\",\n"
            + "				\"Traegersch\" : \"öffentlich-rechtlich\",\n"
            + "				\"Anzahl_Stu\" : 45945,\n"
            + "				\"Gruendungs\" : 1870,\n"
            + "				\"Promotion\" : \"Ja\",\n"
            + "				\"Habilitati\" : \"Ja\",\n"
            + "				\"PLZ_Postfa\" : \"52056\",\n"
            + "				\"Ort_Postfa\" : \"Aachen\",\n"
            + "				\"Mitglied_H\" : 1,\n"
            + "				\"Quelle\" : \"HRK\",\n"
            + "				\"RS\" : \"053340002002\",\n"
            + "				\"Bundesland\" : \"Nordrhein-Westfalen\",\n"
            + "				\"Regierungs\" : \"Köln\",\n"
            + "				\"Kreis\" : \"Städteregion Aachen\",\n"
            + "				\"Verwaltung\" : \"Aachen\",\n"
            + "				\"Gemeinde\" : \"Aachen\"\n"
            + "			}\n"
            + "		},{\n"
            + "			\"type\" : \"Feature\",\n"
            + "			\"geometry\" : {\n"
            + "				\"type\" : \"Point\",\n"
            + "				\"coordinates\" : [ 7.6435043046, 51.8989428278 ]\n"
            + "			},\n"
            + "			\"properties\" : {\n"
            + "				\"HS_Nr\" : \"X910\",\n"
            + "				\"Name\" : \"Deutsche Hochschule der Polizei, Münster (U)\",\n"
            + "				\"Kurzname\" : \"Münster U\",\n"
            + "				\"Strasse\" : \"Zum Roten Berge\",\n"
            + "				\"Hn\" : \"18-24\",\n"
            + "				\"PLZ\" : \"48165\",\n"
            + "				\"Ort\" : \"Münster\",\n"
            + "				\"HS_Typ\" : \"Universitäten\",\n"
            + "				\"Traegersch\" : \"öffentlich-rechtlich\",\n"
            + "				\"Anzahl_Stu\" : 421,\n"
            + "				\"Gruendungs\" : 0,\n"
            + "				\"Mitglied_H\" : 0,\n"
            + "				\"Quelle\" : \"StaBA\",\n"
            + "				\"RS\" : \"055150000000\",\n"
            + "				\"Bundesland\" : \"Nordrhein-Westfalen\",\n"
            + "				\"Regierungs\" : \"Münster\",\n"
            + "				\"Kreis\" : \"Münster\",\n"
            + "				\"Verwaltung\" : \"Münster\",\n"
            + "				\"Gemeinde\" : \"Münster\"\n"
            + "			}\n"
            + "		}\n"
            + "	]\n"
            + "}";

    public GeoJsonRDFTest() {
    }

    /**
     * Test of upliftGeoJSON method, of class GeoJsonRDF.
     */
    @Test
    public void testUpliftGeoJSON() {
        try {
            System.out.println("upliftGeoJSON");
            String pathGeoJson = "geotest.json";
            new PiFile(pathGeoJson).writeTextFile(geoExample);
            test(pathGeoJson);
        } catch (IOException | ParseException | PiOntologyException ex) {
            Logger.getLogger(GeoJsonRDFTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    public void test(String pathGeoJson) throws PiOntologyException, FileNotFoundException, IOException, ParseException {
        try {
            // PiOnt ont = new PiOnt("src/main/resources/ontologies/geosparql.owl");
            PiOnt ont = new PiOnt("src/main/resources/ontologies/spalod.owl");
            normalTest(pathGeoJson, ont);
        } catch (Exception ex) {
            Logger.getLogger(GeoJsonRDFTest.class.getName()).log(Level.SEVERE, null, ex);
             fail(ex.getMessage());
        }
    }

    public void normalTest(String pathGeoJson, PiOnt ont) throws Exception {
        GeoJsonRDF.upliftGeoJSON(pathGeoJson, ont);
        List<Individual> individuals = ont.getIndividuals(ont.getOntClass(GeoJsonRDF.DCAT_DATASET));
        assertFalse(individuals.isEmpty());
        assertTrue(individuals.size()>0);
        
        StmtIterator properties = individuals.get(0).listProperties();
        while (properties.hasNext()) {
            Statement next = properties.next();
            Property predicate = next.getPredicate();
            RDFNode object = next.getObject();
            if (predicate.getLocalName().equals("hasFeature")) {
                //object should be a feature
                assertTrue(object.isResource());
                Resource f = object.asResource();
                StmtIterator fproperties = f.listProperties();
                
                while (fproperties.hasNext()) {
                    Statement n = fproperties.next();
                    Property fpredicate = n.getPredicate();
                    RDFNode fobject = n.getObject();
                    if (fpredicate.getURI().equals(GeoJsonRDF.GEOSPARQLHAS_GEOMETRY)) {
                        //fobject is a geometry
                    } else if (fpredicate.getLocalName().equals("type")) {
                        //fobject is a Feature class
                        assertEquals(fobject.asResource().getURI(), GeoJsonRDF.GEOSPARQL_FEATURE);
                    } else {
                        System.out.println(fpredicate.getLocalName() + " ----> " + fobject);
                        Literal lit = fobject.asLiteral();
                        String datatypeURI = lit.getDatatypeURI();
                        String v;
                        if (datatypeURI.contains("string")) {
                            v = "\"" + fobject.asLiteral().getValue().toString() + "\"";
                        } else {
                            v = fobject.asLiteral().getValue().toString();
                        }
                        String name = "\"" + fpredicate.getLocalName() + "\" : " + v;
                        //should be a property
                        System.out.println(name);
                        assertTrue(geoExample.contains(name));
                    }
                }
            } else if (predicate.getLocalName().equals("type")) {
                assertEquals(object.asResource().getURI(), GeoJsonRDF.DCAT_DATASET);
            } else {
                fail(predicate + " unknown");
            }
        }
        ont.write("test.owl");
    }

    /**
     * Test of downlift method, of class GeoJsonRDF.
     */
    @Test
    public void testDownlift() {
        try {
            System.out.println("downlift");
            String pathGeoJson = "geotest.json";
            new PiFile(pathGeoJson).writeTextFile(geoExample);
            
            JSONParser parser = new JSONParser();//creates an instance  of  JSONParser object
            Object object = parser .parse(new FileReader(pathGeoJson));
            //convert Object to JSONObject
            JSONObject expected = (JSONObject) object;
            
            PiOnt ont = new PiOnt("src/main/resources/ontologies/geosparql.owl");
            GeoJsonRDF.upliftGeoJSON(pathGeoJson, ont);
            String geotest2json = "geotest2.json";
            String downlift = GeoJsonRDF.downlift( ont,  ont.getIndividuals(ont.getOntClass(GeoJsonRDF.DCAT_DATASET)).get(0).getURI());
            
              new PiFile(geotest2json).writeTextFile(downlift);
            test(geotest2json);
           
            normalTest(geotest2json, ont);
            normalTest(pathGeoJson, ont);
            
       } catch (Exception ex) {
            Logger.getLogger(GeoJsonRDFTest.class.getName()).log(Level.SEVERE, null, ex);
             fail(ex.getMessage());
        } 
    }

}
