package info.ponciano.lab.spalodwfs.mvc.enrichment;

import info.ponciano.lab.spalodwfs.mvc.controllers.storage.StorageService;
import info.ponciano.lab.spalodwfs.mvc.lod.ExtractFromLOD;
import info.ponciano.lab.spalodwfs.mvc.models.SparqlQuery;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;
import info.ponciano.lab.spalodwfs.mvc.sem.SemData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResEnrich {
    String r = null;
    private final StorageService storageService;
    private ExtractFromLOD sem;

    @Autowired
    public ResEnrich(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Query a triplestore and return the results in a 2D array of string.
     *
     * @param sq Sparql query where the  "query" and the "triplestore" have to be defined. If triplestore is empty, the USK will be used.
     *           To use the map, the 4 first variables have to be the category, the label (display in the popup),the  latitude, and, the longitude
     * @return String[][] corresponding to the query results with the first row as header.
     * Example of curl query:
     * curl -X POST http://localhost:8080/datalod -H 'Content-type:application/json' -d '{"query":"SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE {   VALUES ?category{ wd:Q3914}   ?item wdt:P17 wd:Q183.  ?item wdt:P31 ?category .  ?item p:P625 ?statement .   ?statement psv:P625 ?coordinate_node .  ?coordinate_node wikibase:geoLatitude ?latitude .  ?coordinate_node wikibase:geoLongitude ?longitude .FILTER(?latitude <= 86.42397134276521).FILTER(?latitude >= -63.39152174400882).FILTER(?longitude <= 219.02343750000003).FILTER(?longitude >= -202.85156250000003)}LIMIT 500", "triplestore": "https://query.wikidata.org/sparql"}'
     */
    @PostMapping("/infer")
    public Infer infer(@RequestBody Infer inf) {
        System.out.println("Inference of :" + inf);
        try {
            boolean construct = KB.get().construct("CONSTRUCT{?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://schema.org/Museum> }WHERE{ ?s <https://schema.org/category> \"http://www.wikidata.org/entity/Q33506\"}");
            KB.get().construct("CONSTRUCT{?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://schema.org/StadiumOrArena> }WHERE{ ?s <https://schema.org/category> \"http://www.wikidata.org/entity/Q1154710\"}");
            KB.get().construct("CONSTRUCT{?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://schema.org/StadiumOrArena> }WHERE{ ?s <https://schema.org/category> \"http://www.wikidata.org/entity/Q589481\"}");
            KB.get().construct("CONSTRUCT{?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://schema.org/StadiumOrArena> }WHERE{ ?s <https://schema.org/category> \"http://www.wikidata.org/entity/Q2310214\"}");
            KB.get().construct("CONSTRUCT{?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://schema.org/StadiumOrArena> }WHERE{ ?s <https://schema.org/category> \"http://www.wikidata.org/entity/Q1763828\"}");
            KB.get().construct("CONSTRUCT{?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://schema.org/School> }WHERE{ ?s <https://schema.org/category> \"http://www.wikidata.org/entity/Q3914\"}");
            KB.get().construct("CONSTRUCT{?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://schema.org/FireStation> }WHERE{ ?s <https://schema.org/category> \"http://www.wikidata.org/entity/Q1195942\"}");
            KB.get().construct("CONSTRUCT{?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://schema.org/Hospital> }WHERE{ ?s <https://schema.org/category> \"http://www.wikidata.org/entity/Q16917\"}");
            KB.get().construct("CONSTRUCT{?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://schema.org/GasStation> }WHERE{ ?s <https://schema.org/category> \"http://www.wikidata.org/entity/Q205495\"}");
            KB.get().construct("CONSTRUCT{?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://schema.org/Library> }WHERE{ ?s <https://schema.org/category> \"http://www.wikidata.org/entity/Q7075\"}");
            KB.get().save();
            if (construct)
                return new Infer("Inferred!");
            else
                return new Infer("Nothing more to infer!");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

}
