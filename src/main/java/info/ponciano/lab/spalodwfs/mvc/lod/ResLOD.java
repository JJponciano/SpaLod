package info.ponciano.lab.spalodwfs.mvc.lod;

import info.ponciano.lab.spalodwfs.controller.storage.StorageService;
import info.ponciano.lab.spalodwfs.mvc.lod.ExtractFromLOD;
import info.ponciano.lab.spalodwfs.mvc.models.SparqlQuery;
import info.ponciano.lab.spalodwfs.mvc.sem.SemData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResLOD {
    String r = null;
    private final StorageService storageService;
    private ExtractFromLOD sem;

    @Autowired
    public ResLOD(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Query a triplestore and return the results in a 2D array of string.
     *
     * @param sq Sparql query where the  "query" and the "triplestore" have to be defined. If triplestore is empty, the USK will be used.
     * To use the map, the 4 first variables have to be the category, the label (display in the popup),the  latitude, and, the longitude
     * @return String[][] corresponding to the query results with the first row as header.
     * Example of curl query:
     * curl -X POST "+KB.SERVER+":8080/datalod -H 'Content-type:application/json' -d '{"query":"SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE {   VALUES ?category{ wd:Q3914}   ?item wdt:P17 wd:Q183.  ?item wdt:P31 ?category .  ?item p:P625 ?statement .   ?statement psv:P625 ?coordinate_node .  ?coordinate_node wikibase:geoLatitude ?latitude .  ?coordinate_node wikibase:geoLongitude ?longitude .FILTER(?latitude <= 86.42397134276521).FILTER(?latitude >= -63.39152174400882).FILTER(?longitude <= 219.02343750000003).FILTER(?longitude >= -202.85156250000003)}LIMIT 500", "triplestore": "https://query.wikidata.org/sparql"}'
     */
    @PostMapping("/datalod")
    public SemData sparqlQuery(@RequestBody SparqlQuery sq) {
        try {
            String query = sq.getResults();
            String triplestore = sq.getTriplestore();
            this.sem = new ExtractFromLOD(triplestore, query);
            return new SemData(this.sem.getHeader(),this.sem.getData(),this.sem.getOutputFilePath());

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

}
