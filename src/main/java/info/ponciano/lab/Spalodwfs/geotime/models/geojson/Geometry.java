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

import java.util.ArrayList;
import java.util.List;
import org.apache.jena.geosparql.implementation.WKTLiteralFactory;
import org.apache.jena.rdf.model.Literal;

/**
 *
 * @author Dr. Jean-Jacques Ponciano
 */
public  class Geometry {

    private final String type;
    private final List<Double> coordinates;

    public Geometry(String type, double... coordinates) {
        if (!type.equals("Point")) {
            throw new UnsupportedOperationException(type + " geometry is not supported yet.");
        }
        this.type = type;
        this.coordinates = new ArrayList<>();
        for (double coordinate : coordinates) {
            this.coordinates.add(coordinate);
        }
    }

    public String getType() {
        return type;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public Literal getWKTPoint(){
       return WKTLiteralFactory.createPoint(coordinates.get(0), coordinates.get(1));
    }
    @Override
    public String toString() {
        String value = "POINT (";
        for (int i = 0; i < coordinates.size(); i++) {
            double coordinate = coordinates.get(i);
            value += coordinate;
            if (i + 1 < coordinates.size()) {
                value += " ";
            }
        }
        value += ")";
        return value;
    }

}
