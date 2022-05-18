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
package info.ponciano.lab.Spalodwfs.geotime.models.geojson;

import info.ponciano.lab.Spalodwfs.geotime.models.semantic.KB;
import info.ponciano.lab.pisemantic.PiOnt;
import info.ponciano.lab.pisemantic.PiOntologyException;
import info.ponciano.lab.pitools.utility.PiRegex;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.lang.Number;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author Dr. Jean-Jacques Ponciano
 */
public class GeoJsonRDF {

    /**
     * Uplift a geoJSON file in an ontology
     *
     * @param pathGeoJson path of the file to read
     * @param ont         ontology model in with the uplift should be done
     * @throws java.io.FileNotFoundException                    If the file is not found
     * @throws org.json.simple.parser.ParseException            if the file cannot be
     *                                                          parsed.
     * @throws info.ponciano.lab.pisemantic.PiOntologyException if something is
     *                                                          wrong or not yet supported
     */
    public static void upliftGeoJSON(String pathGeoJson, PiOnt ont) throws FileNotFoundException, IOException, ParseException, PiOntologyException, Exception {

        JSONParser parser = new JSONParser();//creates an instance of a JSONParser object
        Object object = parser
                .parse(new FileReader(pathGeoJson));

        //converts Objects to JSONObject
        JSONObject jsonObject = (JSONObject) object;

        //Reads the collection
        String nameCollection = (String) jsonObject.get("name");
        FeatureCollection featureCollection = new FeatureCollection(nameCollection);

        extractFeatures(jsonObject, featureCollection);

        // uplifts
        List<Feature> allfeatures = featureCollection.getFeatures();
        String name = featureCollection.getName();
        //create an individual
        OntClass dataset = ont.createClass(DCAT_DATASET);
        // System.out.println(dataset);
        //creates the individual data
        String nameFC = ont.getNs() + name;
        //generate a new name if the name is already known
        if (ont.getIndividual(nameFC) != null) {
            nameFC = ont.getNs() + dataset.getLocalName().toLowerCase() + "_" + UUID.randomUUID().toString();
        }
        Individual data = dataset.createIndividual(nameFC);
        // System.out.println(data);
        featureUplift(allfeatures, ont, data);

    }

    public static void featureUplift(List<Feature> allfeatures, PiOnt ont, Individual data) throws Exception {
        for (Feature f : allfeatures) {
            //creates the geometry
            Geometry geometry = f.getGeometry();
            String type = geometry.getType();
            String name1 = SF + type;
            OntClass ontClassGeo = ont.getOntClass(name1);
            if (ontClassGeo == null) {
                throw new Exception(name1 + "\" does not exists but is requiered");
            }


            Individual indGeo = ontClassGeo.createIndividual(ont.getNs() + ontClassGeo.getLocalName().toLowerCase() + "_" + UUID.randomUUID().toString());
            // System.out.println(indGeo);
            var asWKT = ont.getDataProperty(GEOSPARQLAS_WKT);
            if (asWKT == null) {
                throw new PiOntologyException("the property \"http://www.opengis.net/ont/geosparql#asWKT\" does not exists but is requiered");
            }
            var value = geometry.getWKTPoint();
            indGeo.addLiteral(asWKT, value);
            // System.out.println(asWKT + " -> " + value);
            //creates a feature
            OntClass ontClassFeature = ont.getOntClass(GEOSPARQL_FEATURE);
            if (ontClassFeature == null) {
                throw new PiOntologyException("the class \"http://www.opengis.net/ont/geosparql#Feature\" does not exists but is requiered");
            }

            Individual indF = ontClassFeature.createIndividual(KB.NS + ontClassFeature.getLocalName().toLowerCase() + "_" + UUID.randomUUID().toString());

            //asigns a geometry to the feature
            var hasGeometry = ont.getObjectProperty(GEOSPARQLHAS_GEOMETRY);
            if (hasGeometry == null) {
                throw new PiOntologyException("the property \"http://www.opengis.net/ont/geosparql#hasGeometry\" does not exists but is requiered");
            }
            indF.addProperty(hasGeometry, indGeo);
            //asigns properties
            Map<String, Object> properties = f.getProperties();
            properties.forEach((k, v) -> {
                DatatypeProperty p = ont.createDatatypeProperty(k);
                indF.addLiteral(p, v);
            });
            //asigns the feature to the datasets
            ObjectProperty hasFeature = ont.createObjectProperty("hasFeature");
            if (data != null)
                data.addProperty(hasFeature, indF);
        }
    }



    public static final String SF = "http://www.opengis.net/ont/sf#";
    public static final String GEOSPARQLHAS_GEOMETRY = "http://www.opengis.net/ont/geosparql#hasGeometry";
    public static final String GEOSPARQLAS_WKT = "http://www.opengis.net/ont/geosparql#asWKT";
    public static final String GEOSPARQL_FEATURE = "http://www.opengis.net/ont/geosparql#Feature";
    public static final String DCAT_DATASET = "http://www.w3.org/ns/dcat#Dataset";

    private static void extractFeatures(JSONObject jsonObject, FeatureCollection featureCollection) throws NumberFormatException, PiOntologyException {
        String typeCollection = (String) jsonObject.get("type");
        //creates the object

        //if the type is "FeatureCollection"
        if (typeCollection.equals("FeatureCollection")) {

            JSONArray features = (JSONArray) jsonObject.get("features");//get all feature

            for (Iterator it = features.iterator(); it.hasNext(); ) {//for each feature
                //extracts information
                JSONObject feature = (JSONObject) it.next();
                String type = (String) feature.get("type");
                if (type.equals("Feature")) {
                    Geometry geo = extractGeo(feature);
                    //gets the properties
                    JSONObject properties = (JSONObject) feature.get("properties");
                    final Feature f = new Feature(geo);
                    properties.keySet().forEach(k -> {
                        Object get = properties.get(k);
                        f.addProperty((String) k, get);
                    });
                    featureCollection.add(f);

                } else {
                    throw new PiOntologyException("parsing for " + type + " is not yet supported");
                }
            }

        } else {
            throw new PiOntologyException("parsing for " + typeCollection + " is not yet supported");
        }
    }

    private static Geometry extractGeo(JSONObject feature) throws NumberFormatException {
        //gets the geometry
        JSONObject geometry = (JSONObject) feature.get("geometry");
        String geotype = (String) geometry.get("type");
        JSONArray coords = (JSONArray) geometry.get("coordinates");//get all coordinates
        double[] coordinates = new double[coords.size()];
        for (int i = 0; i < coordinates.length; i++) {
            try{
                if(coords.get(i).getClass().equals(Long.class)) {
                    Long y = (long) coords.get(i);
                    double d = y.doubleValue();
                    coordinates[i] = d;
                }else{
                    coordinates[i] = (double) coords.get(i);
                }

            }catch (Exception e){
                System.out.println(e);
            }

        }
        Geometry geo = new Geometry(geotype, coordinates);
        return geo;
    }

    /**
     * Downlift in geoJSOn all information about a dataset
     *
     * @param ont        ontology under working
     * @param datasetURI URI of the dataset individual targeted (individual of
     *                   dcat#Dataset)
     * @return the GeoJSON String containing all information about the
     * individuals
     * @throws PiOntologyException if the downlift is impossible with the
     *                             individual targeted.
     */
    public static String downlift(PiOnt ont, String datasetURI) throws PiOntologyException {
        JSONObject data = new JSONObject();
        JSONArray features = new JSONArray();
        Individual individual = ont.getIndividual(datasetURI);
        data.put("name", individual.getLocalName());

        StmtIterator properties = individual.listProperties();
        while (properties.hasNext()) {
            Statement next = properties.next();
            Property predicate = next.getPredicate();
            RDFNode object = next.getObject();

            switch (predicate.getLocalName()) {
                case "hasFeature" -> {
                    //object should be a feature

                    Resource f = object.asResource();
                    StmtIterator fproperties = f.listProperties();

                    JSONObject feature = new JSONObject();
                    feature.put("type", "Feature");
                    JSONObject propertiesJson = new JSONObject();
                    JSONObject jsonGEO = new JSONObject();
                    while (fproperties.hasNext()) {
                        Statement n = fproperties.next();
                        Property fpredicate = n.getPredicate();
                        RDFNode fobject = n.getObject();
                        if (fpredicate.getURI().equals(GeoJsonRDF.GEOSPARQLHAS_GEOMETRY)) {
                            createGeometryJson(fobject, jsonGEO);
                        } else if (!fpredicate.getLocalName().equals("type")) {

                            propertiesJson.put(fpredicate.getLocalName(), fobject.asLiteral().getValue());

                        }
                    }
                    feature.put("geometry", jsonGEO);
                    feature.put("properties", propertiesJson);
                    features.add(feature);
                }
                case "type" -> data.put("type", "FeatureCollection");//should be a FeatureCollection

                default -> throw new PiOntologyException(predicate.getLocalName() + " not yet supported");
            }
        }
        data.put("features", features);
        return data.toJSONString();
    }

    public static void createGeometryJson(RDFNode object, JSONObject jsonGEO) throws PiOntologyException, NumberFormatException {
        //fobject is a geometry
        StmtIterator geoPrpt = object.asResource().listProperties();
        String geotype = null;
        String coords = null;
        while (geoPrpt.hasNext()) {
            Statement geop = geoPrpt.next();
            if (geop.getPredicate().getURI().equals(GEOSPARQLAS_WKT)) {
                coords = geop.getObject().asLiteral().getString();
            } else if (geop.getPredicate().getLocalName().equals("type")) {
                geotype = geop.getObject().asResource().getLocalName();
            }
        }
        if (geotype == null || coords == null) {
            throw new PiOntologyException(" geotype or coords are null");
        }

        jsonGEO.put("type", geotype);
        if (geotype.equals("Point")) {
            JSONArray array = new JSONArray();
            String[] split = coords.substring(coords.indexOf("(") + 1, coords.lastIndexOf(")")).split(PiRegex.whiteCharacter);
            for (String c : split) {
                array.add(Double.parseDouble(c));
            }
            jsonGEO.put("coordinates", array);

        } else {
            throw new PiOntologyException(geotype + " not yet supported");
        }
    }
}
