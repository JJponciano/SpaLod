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


public class Schema {

	//Attributes
	private String type;
	private String dist;
	private String distTitle;
	private String distFormat;
	private String username;
	private String psw;
	
	//Getters and setters
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDist() {
		return dist;
	}
	public void setDist(String dist) {
		this.dist = dist;
		String[] w = dist.split("/");
		this.distTitle=w[0];
		this.distFormat=w[1];
	}
	public String getDistTitle() {
		return distTitle;
	}
	public String getDistFormat() {
		return distFormat;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPsw() {
		return psw;
	}
	public void setPsw(String psw) {
		this.psw = psw;
	}
	
	public void display() {
		System.out.println("type: "+this.type);
		System.out.println("dist: "+this.dist);
		System.out.println("title: "+this.distTitle);
		System.out.println("format: "+this.distFormat);
		System.out.println("username: "+this.username);
	}
	
}
