package info.ponciano.lab.Spalodwfs.enrichment;

import info.ponciano.lab.Spalodwfs.geotime.models.semantic.KB;
import info.ponciano.lab.Spalodwfs.geotime.models.semantic.OntoManagementException;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UnknownDataManagerTest {

    @Test
    void run() throws OntoManagementException {
        String path_ont_source = "src/test/resources/source.owl";
        String path_ont_target = "src/test/resources/test.owl";
        OntModel source = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        source.read(path_ont_source);
        OntModel target = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        target.read(path_ont_target);

        UnknownDataManager udm = new UnknownDataManager(source, target);
        udm.run();
        List<String[]> data_known = udm.getData_known();
        MatchingDataCreationDto data_unknown = udm.getData_unknown();
        List<MatchingDataModel> data = data_unknown.getData();
        List<String[]> remainingData = udm.getRemainingData();
        List<RDFNode> noUri = udm.getNoUri();

        System.out.println("data_known: " + data_known.size());
        System.out.println("data_unknown = " + data_unknown);
        assertEquals(3, data.size());

        for (MatchingDataModel datum : data) {
            if (datum.getInput().equals("http://lab.ponciano.info/ont/spalod#containedInPlace"))
                assertEquals("https://schema.org/containedInPlace", datum.getValue());
            if (datum.getInput().equals("http://lab.ponciano.info/ont/spalod#Place")) {
                assertEquals("https://schema.org/Place", datum.getValue());
            }
        }


        System.out.println("remainingData: " + remainingData.size());
        for (String[] strings : remainingData) {
            System.out.println(strings[0]+" "+strings[1]+" "+strings[2]);
        }

        // try to enrich the ontology
        for (String[] strings : data_known) {
            KB.get().update(strings);
        }


    }
}