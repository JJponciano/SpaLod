package info.ponciano.lab.Spalodwfs.enrichment;

import info.ponciano.lab.Spalodwfs.geotime.models.semantic.KB;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.ArrayList;
import java.util.List;

class UnknownDataManager implements Runnable {
    private List<String[]> remainingData;
    private List<String[]> data_known;
    private MatchingDataCreationDto data_unknown;
    private List<RDFNode> noUri;
    private OntModel schemaOrg;
    private OntModel source,  target;

    UnknownDataManager(OntModel source, OntModel target) {
        this.source=source;
        this.target=target;
        this.data_unknown = new MatchingDataCreationDto();
        this.remainingData = new ArrayList<>();
        this.data_known = new ArrayList<>();
        this.noUri = new ArrayList<>();
        this.schemaOrg = KB.getSchemaOrg();
    }

    public void run() {
        //extract subject predicate and object for each individual
        ResultSet select = KB.select(source, "Select ?s ?p ?o WHERE{?s ?p ?o}");
        while (select.hasNext()) {
            QuerySolution next = select.next();
            Resource s = next.getResource("?s");
            Resource p = next.getResource("?p");
            RDFNode o = next.get("?o");

            //check that the triples is well-formed
            //test if the object is known in the ontology
            boolean dataOk = true;
            if (s.getURI() == null) {
                this.noUri.add(s);
                dataOk = false;
            }
            if (p.getURI() == null) {
                this.noUri.add(p);
                dataOk = false;
            }
            if (o.isResource() && o.asResource().getURI() == null) {
                this.noUri.add(o);
                dataOk = false;
            }
            if (dataOk) {
                //test if the property is known in the ontology
                boolean unknownP = target.containsResource(p);
                if (unknownP) {
                    ExtendedIterator<OntProperty> lps = this.schemaOrg.listOntProperties();
                    String proposal = predictFromSchemaOrg(p, lps);
                    addUnknown(p, proposal);
                }
                //test if the subject is known in the ontology
                boolean unknownS = target.containsResource(s);
                if (unknownS) {
                    ExtendedIterator<OntClass> lps = this.schemaOrg.listClasses();
                    String proposal = predictFromSchemaOrg(p, lps);
                    addUnknown(s, proposal);
                }
                //test if the object is known in the ontology
                boolean unknownClass = o.isResource() && target.containsResource(o);
                if (unknownClass) {
                    ExtendedIterator<OntClass> lps = this.schemaOrg.listClasses();
                    String proposal = predictFromSchemaOrg(p, lps);
                    addUnknown(o.asResource(), proposal);
                }

                // Convert the object in String
                String o_string;
                if (o.isResource()) {
                    o_string = o.asResource().getURI();
                } else {
                    o_string = o.asLiteral().toString();
                }

                // if the property is known and the object is not a resource or is known
                if (!unknownS && !unknownP && (!o.isResource() || !unknownClass)) {     //Add to the data known
                    this.data_known.add(new String[]{s.getURI(), p.getURI(), o_string});
                } else {
                    this.remainingData.add(new String[]{s.getURI(), p.getURI(), o_string});
                }
            }
        }
    }

    public List<String[]> getRemainingData() {
        return remainingData;
    }

    public void setRemainingData(List<String[]> remainingData) {
        this.remainingData = remainingData;
    }

    public List<String[]> getData_known() {
        return data_known;
    }

    public void setData_known(List<String[]> data_known) {
        this.data_known = data_known;
    }

    public MatchingDataCreationDto getData_unknown() {
        return data_unknown;
    }

    public void setData_unknown(MatchingDataCreationDto data_unknown) {
        this.data_unknown = data_unknown;
    }

    public List<RDFNode> getNoUri() {
        return noUri;
    }

    public void setNoUri(List<RDFNode> noUri) {
        this.noUri = noUri;
    }

    public OntModel getSchemaOrg() {
        return schemaOrg;
    }

    public void setSchemaOrg(OntModel schemaOrg) {
        this.schemaOrg = schemaOrg;
    }

    private String predictFromSchemaOrg(Resource p, ExtendedIterator listsDataP) {
        String proposal = "";
        while (proposal.isBlank() && listsDataP.hasNext()) {
            Resource nextP = (Resource) listsDataP.next();
            if (nextP.getURI().contains(p.getLocalName())) {
                proposal = nextP.getURI();
            }
        }
        return proposal;
    }

    private void addUnknown(Resource p, String proposal) {
        MatchingDataModel mdm = new MatchingDataModel(p.getURI(), proposal);

        //test if the data is not already known
        if (this.data_known.contains(mdm)) {
            this.data_unknown.add(mdm);
        }
    }
}
