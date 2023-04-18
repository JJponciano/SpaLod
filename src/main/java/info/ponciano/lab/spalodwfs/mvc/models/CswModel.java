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

public interface CswModel {
    /**
     * allows CSW clients to retrieve service metadata from a server
     *
     * @return
     */
    public String getCapabilities(); 

    /**
     * allows a client to discover elements of the information model supported
     * by the target catalogue service. The operation allows some or all of the
     * information model to be described
     *
     * @return
     */
    public String describeRecord(); //To change body of generated methods, choose Tools | Templates.
    

    /**
     * search for records, returning record IDs
     *
     * @return
     */
    public String getRecords(); 

    /**
     * retrieves the default representation of catalogue records using their
     * identifier
     *
     *
     * @return
     */
    public String getRecordById() ; 

    /**
     * used to obtain runtime information about the range of values of a
     * metadata record element or request parameter
     *
     *
     * @return
     */
    public String getDomain(); 

    /**
     * create/update metadata by asking the server to 'pull' metadata from
     * somewhere
     *
     * @return
     */
    public String harvest(); 

    /**
     * create/edit metadata by 'pushing' the metadata to the server
     *
     * @return
     */
    public String transaction(); 
}