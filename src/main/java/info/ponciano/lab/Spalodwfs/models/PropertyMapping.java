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
package info.ponciano.lab.Spalodwfs.models;

import java.util.ArrayList;
import java.util.List;

public class PropertyMapping {

	private String classname;
	//private String properties;
	//private List<String> listproperties;
	private List<String> properties;
	
	public PropertyMapping() {
		properties=new ArrayList<String>();
	}

	/*public List<String> getListProperties() {
		return listproperties;
	}
	public String getProperties() {
		return properties;
	}

	public void setProperties(String p) {
		this.properties=p;
		this.listproperties.add(p);
	}*/
	

	public String getClassname() {
		return classname;
	}

	public List<String> getProperties() {
		return properties;
	}

	public void setProperties(List<String> properties) {
		this.properties = properties;
	}

	/*public void setProperties(int i, String p) {
		this.properties.add(i, p); 
	}*/
	
	public void setClassname(String classname) {
		this.classname = classname;
	}
	
	
}
