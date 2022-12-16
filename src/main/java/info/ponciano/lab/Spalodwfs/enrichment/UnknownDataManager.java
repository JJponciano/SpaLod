package info.ponciano.lab.spalodwfs.enrichment;

import info.ponciano.lab.pitools.files.PiFile;
import info.ponciano.lab.spalodwfs.geotime.models.semantic.KB;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class UnknownDataManager implements Runnable {
    private final List<String[]> remainingData;
    private final List<String[]> data_known;
    private final MatchingDataCreationDto data_unknown;
    private final List<RDFNode> noUri;
    private final OntModel schemaOrg;
    private final OntModel source, target;
    private final List<String> knowFixed;
    private final List<String> allClasses;
    private final Map<String, String> map;

    /**
     * Creates a new instance of UnknownDataManager.
     *
     * @param source OntModel that have to be enriched with the target
     * @param target OntModel that will be used for the enrichment
     */
    UnknownDataManager(OntModel source, OntModel target) {
        this.source = source;
        this.target = target;
        this.data_unknown = new MatchingDataCreationDto();
        this.remainingData = new ArrayList<>();
        this.data_known = new ArrayList<>();
        this.noUri = new ArrayList<>();
        this.schemaOrg = KB.getSchemaOrg();
        this.knowFixed = new ArrayList<>();
        this.knowFixed.add("http://www.w3.org/2002/07/owl#NamedIndividual");
        this.allClasses = new ArrayList<>();
        ExtendedIterator<OntClass> ontClassExtendedIterator = target.listNamedClasses();
        while (ontClassExtendedIterator.hasNext()) {
            String uri = ontClassExtendedIterator.next().getURI();
            this.allClasses.add(uri);
        }
        this.map = new HashMap<>();
        try {
            String[][] map = new PiFile("src/main/resources/ontologies/mapping_schemaorg.csv").readCSV(";");
            for (String[] row : map) {
                this.map.put(row[0].toLowerCase(), row[1]);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the algorithm that set up data_known,data_unknown, noUri and remainingData.
     */
    public void run() {
        //extract subject predicate and object for each individual
        ResultSet select = KB.select(target, "Select ?s ?p ?o WHERE{?s ?p ?o}");
        while (select.hasNext()) {
            QuerySolution next = select.next();
            Resource s = next.getResource("?s");
            Resource p = next.getResource("?p");
            RDFNode o = next.get("?o");

            //check that the triples is well-formed
            //test if the object is known in the ontology
            boolean dataOk = true;

            boolean isNotW3cBased = !s.toString().contains("w3.org") && !p.toString().contains("w3.org") && !o.toString().contains("w3.org");
            //test if s, p, and o (for resource only) has no URI
            if (s.getURI() == null) {
                if (isNotW3cBased)
                    this.noUri.add(s);
                dataOk = false;
            }
            if (p.getURI() == null) {
                if (isNotW3cBased)
                    this.noUri.add(p);
                dataOk = false;
            }
            if (o.isResource() && o.asResource().getURI() == null) {
                if (isNotW3cBased)
                    this.noUri.add(o);
                dataOk = false;
            }
            // if all as URI
            if (dataOk) {
                //test if the property is known in the ontology
                boolean knownP = source.containsResource(p);
                if (!knownP) {
                    ExtendedIterator<OntProperty> lps = this.schemaOrg.listAllOntProperties();
                    String proposal = predictFromSchemaOrg(p.getLocalName(), lps);
                    addUnknown(p.getURI(), proposal);
                }
                //test if the object is not a resource or is not a class or is known in the fixed memory or in the ontology
                String s1 = o.toString();
                String uri="";
                if(o.isResource())
                    uri= o.asResource().getURI();
                else
                if(s1.startsWith("http://") ||s1.startsWith("https://")){
                    uri=s1;
                }
                boolean knownClass = uri.isBlank();
                knownClass= knownClass||!allClasses.contains(uri) ;
                knownClass=knownClass||this.knowFixed.contains(uri);
                knownClass=knownClass|| source.containsResource(o);
                knownClass=knownClass|| !uri.contains("www.wikidata.org");
                if (!knownClass) {
                    ExtendedIterator<OntClass> lps = this.schemaOrg.listNamedClasses();
                    int ind=uri.lastIndexOf('/')+1;
                    int ind2 = uri.lastIndexOf('#')+1;
                    if(ind2>ind)ind=ind2;
                    String proposal = predictFromSchemaOrg(uri.substring(ind,uri.length()), lps);
                    addUnknown(uri, proposal);
                }
                boolean knownS = !allClasses.contains(s.getURI()) || this.knowFixed.contains(s.getURI()) || source.containsResource(s);
                if (!knownS) {
                    ExtendedIterator<OntClass> lps = this.schemaOrg.listNamedClasses();
                    String proposal = predictFromSchemaOrg(s.getLocalName(), lps);
                    addUnknown(s.getURI(), proposal);
                }

                // Convert the object in String
                String o_string;
                o_string = convert(o);

                // if the property is known and the object is not a resource or is known
                if (knownP && (!o.isResource() || knownClass) && knownS) {     //Add to the data known
                    this.data_known.add(new String[]{"<" + s.getURI() + ">", "<" + p.getURI() + ">", o_string});
                } else {
                    this.remainingData.add(new String[]{"<" + s.getURI() + ">", "<" + p.getURI() + ">", o_string});
                }
            }
        }
    }

    /**
     * Convert the Object node in string usable in SPARQL.
     *
     * @param o object to be converted
     * @return The URI of the literal xsd syntax of the object
     */
    private String convert(RDFNode o) {
        String o_string;
        if (o.isResource()) {
            o_string = "<" + o.asResource().getURI() + ">";
        } else {
            String litURI = o.asLiteral().getDatatype().getURI();
            String lexicalForm = o.asLiteral().getLexicalForm();
            if (lexicalForm.contains("\n")) {
                lexicalForm = lexicalForm.replaceAll("\\n", " ");
            }
            if (lexicalForm.contains("\"")) {
                lexicalForm = lexicalForm.replaceAll("\"", "'");
            }
            o_string = "\"" + lexicalForm + "\"^^xsd:" + litURI.substring(litURI.lastIndexOf('#') + 1, litURI.length());
        }
        return o_string;
    }

    public List<String[]> getRemainingData() {
        return remainingData;
    }

    public List<String[]> getData_known() {
        return data_known;
    }

    public MatchingDataCreationDto getData_unknown() {
        return data_unknown;
    }

    public List<RDFNode> getNoUri() {
        return noUri;
    }


    /**
     * Predict the value of the resources according to a local name matching with Schema.or.
     *
     * @param p          resource to set the value
     * @param listsDataP list of resources contained in schema.org ontology according to the type (property or class) of p
     * @return the predicted value or an empty string.
     */
    private String predictFromSchemaOrg(String p, ExtendedIterator listsDataP) {
        String proposal = "";

        String key = p.toLowerCase();
        if (map.containsKey(key)) {
            proposal = map.get(key);
        } else {
            while (proposal.isBlank() && listsDataP.hasNext()) {
                Resource nextP = (Resource) listsDataP.next();
                if (nextP.getURI() != null && nextP.getURI().contains(p)) {
                    proposal = nextP.getURI();
                }
            }
        }
        return proposal;
    }

    /**
     * Add an unknown resource and proposal
     *
     * @param p        resource to add
     * @param proposal proposition of resources uri
     */
    private void addUnknown(String p, String proposal) {
        if(proposal.isBlank())proposal=p;
        MatchingDataModel mdm = new MatchingDataModel(p, proposal);

        //test if the data is not already known
        if (!this.data_unknown.contains(mdm)) {
            System.out.println("Propose " + proposal + " for " + p);
            this.data_unknown.add(mdm);
        }
    }
}
