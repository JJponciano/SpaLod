/*
 * Copyright (C) 2020 claireprudhomme.
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author claireprudhomme
 */
public class SemanticWFSRequest {

    private String url;
    public SemanticWFSRequest() {
        url="https://ld.gdi-de.org/semanticwfs/";
    }
    
    private String getConformance(String type) throws IOException, InterruptedException{
        String url2=url+"conformance?f=";
        String res= QuerySemanticWFS(url2+type);
       
        return res;
    }
    public String getJSONConformance()throws IOException, InterruptedException{
        return getConformance("json");
    }
    public String getXMLConformance() throws IOException, InterruptedException{
        return getConformance("gml");
    }
    public String getHTMLConformance() throws IOException, InterruptedException{
        return getConformance("html");
    }
    
    
    private String getCollections(String type) throws IOException, InterruptedException{
        String res="";
        String url2=url+"collections?f=";
        try {
           HttpRequest request = HttpRequest.newBuilder(new URI(url2+type)). GET().build();
           HttpResponse<String> response;
           response = HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());
           res=response.body();
           //System.out.println(res);
        } catch (URISyntaxException ex) {
            Logger.getLogger(SemanticWFSRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }
    
    public String getJSONCollections()throws IOException, InterruptedException{
        return getCollections("json");
    }
    
    public String getQueryables(String collectionid, String type){
        String url2=url+"collections/{"+ collectionid+"}/queryables?f=";
        String res= QuerySemanticWFS(url2+type);
       
        return res;
    }
     private String QuerySemanticWFS(String url){
        String res="";
        try {
           HttpRequest request = HttpRequest.newBuilder(new URI(url)). GET().build();
           HttpResponse<String> response;
           response = HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());
           res=response.body();
           //System.out.println(res);
        } catch (URISyntaxException | IOException | InterruptedException ex) {
            Logger.getLogger(SemanticWFSRequest.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return res;
     }
 
     
     ///collections/{collectionid}/items
     public String getCollectionItems(String collectionid, String format, Integer limit, Integer offset, 
             String bbox, String style, String crs, String bboxcrs, String filter, String filterlang, String datetime){
        String url2=url+"collections/{"+ collectionid+"}/items";
        int nbparam=0;
        if(format!=null)
        {
            if(nbparam==0)
                url2=url2+"?f="+format;
            else
                url2=url2+"&f="+format;
            nbparam++;
        }
        if(limit!=null)
        {
            if(nbparam==0)
                url2=url2+"?limit="+limit;
            else
                url2=url2+"&limit="+limit;
            nbparam++;
        }
        if(offset!=null)
        {
            if(nbparam==0)
                url2=url2+"?offset="+offset;
            else
                url2=url2+"&offset="+offset;
            nbparam++;
        }
        if(bbox!=null)
        {
            if(nbparam==0)
                url2=url2+"?bbox="+bbox;
            else
                url2=url2+"&bbox"+bbox;
            nbparam++;
        }
        if(style!=null)
        {
            if(nbparam==0)
                url2=url2+"?mapstyle="+style;
            else
                url2=url2+"&mapstyle="+style;
            nbparam++;
        }
        if(crs!=null)
        {
            if(nbparam==0)
                url2=url2+"?crs="+crs;
            else
                url2=url2+"&crs="+crs;
            nbparam++;
        }
        if(bboxcrs!=null)
        {
            if(nbparam==0)
                url2=url2+"?bbox-crs="+bboxcrs;
            else
                url2=url2+"&bbox-crs="+bboxcrs;
            nbparam++;
        }
        if(filter!=null)
        {
            if(nbparam==0)
                url2=url2+"?filter="+filter;
            else
                url2=url2+"&filter="+filter;
            nbparam++;
        }
        if(filterlang!=null)
        {
            if(nbparam==0)
                url2=url2+"?filter-lang="+filterlang;
            else
                url2=url2+"&filter-lang="+filterlang;
            nbparam++;
        }
        if(datetime!=null)
        {
            if(nbparam==0)
                url2=url2+"?datetime="+datetime;
            else
                url2=url2+"&datetime="+datetime;
            nbparam++;
        }
        System.out.println(url2);
        String res= QuerySemanticWFS(url2);
        return res;
     }
}
