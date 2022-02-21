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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dr. Jean-Jacques Ponciano
 */
 public class FeatureCollection {

    private final String name;
    private final List<Feature> features;

    public FeatureCollection(String name) {
        this.name = name;
        this.features = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public boolean add(Feature feature) {
        return this.features.add(feature);
    }
}
