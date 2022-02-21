/*
 * Copyright (C) 2020 Dr Jean-Jacques Ponciano Contact: jean-jacques@ponciano.info.
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
package info.ponciano.lab.Spalodwfs.models.semantic;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.ontology.Individual;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.rdf.model.impl.StatementImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Node;

@SpringBootTest

/**
 *
 * @author Dr Jean-Jacques Ponciano Contact: jean-jacques@ponciano.info
 */
public class OwlManagementTest {

    private String xmlPathfile = "src/main/resources/metadata/metadata.xml";
    private String metadataSavedowl = "metadataSaved.owl";

    ;

    public OwlManagementTest() {
    }

    /**
     * Test of uplift method, of class OwlManagement.
     */
    @Test
    public void testUplift() {
        try {
            System.out.println("uplift");
            OwlManagement instance = new OwlManagement();
            boolean expResult = true;
            boolean result = instance.uplift(xmlPathfile);
            assertEquals(expResult, result);
            List<Individual> listIndividuals = instance.listsMetadataIndividuals().toList();
            assertEquals(1, listIndividuals.size());

            instance.saveOntology(metadataSavedowl);
        } catch (IOException | OntoManagementException ex) {
            Logger.getLogger(OwlManagementTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    /**
     * Test of change method, of class OwlManagement.
     */
    @Test
    public void testChange() {
        System.out.println("change");
        String ind = "individu";
        String p = "property";
        String v = "value";
        try {
            OwlManagement instance = new OwlManagement();
            Statement s=new StatementImpl(new ResourceImpl(OntoManagement.NS+ind), new PropertyImpl(OntoManagement.NS+p), new ResourceImpl("test"));
            instance.getOnt().getOnt().add(s);
            System.out.println("contain the test statement: "+instance.getOnt().getOnt().contains(s));
            boolean expResult = true;
            boolean result = instance.change(OntoManagement.NS+ind,OntoManagement.NS+p,v);
            assertEquals(expResult, result);
            //test that the previous statement has been removed
            boolean previousstate=instance.getOnt().getOnt().contains(s);
            assertEquals(false, previousstate);
            //test that the new statement has been added
            //Statement s2=new StatementImpl(new ResourceImpl(OntoManagement.NS+ind), new PropertyImpl(OntoManagement.NS+p), new ResourceImpl("\""+v+"\""));
            //boolean addedstate=instance.getOnt().contains(s2);
            boolean addedstate=instance.getOnt().getOnt().contains(new ResourceImpl(OntoManagement.NS+ind), new PropertyImpl(OntoManagement.NS+p));
            assertEquals(true, addedstate);
//        // TODO review the generated test code and remove the default call to fail.
//        System.out.println("The test case is a prototype.");
        } catch (OntoManagementException ex) {
            Logger.getLogger(OwlManagementTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getSPARQL method, of class OwlManagement.
     */
    @Test
    public void testGetSPARQL() {
        System.out.println("getSPARQL");
//        String[] param = null;
//        OwlManagement instance = new OwlManagement();
//        String expResult = "";
//        String result = instance.getSPARQL(param);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        System.out.println("The test case is a prototype.");
    }

    /**
     * Test of saveOntology method, of class OwlManagement.
     */
    @Test
    public void testSaveOntology() throws Exception {
        System.out.println("saveOntology");
    }

    /**
     * Test of getNodeName method, of class OwlManagement.
     */
    //@Test
    public void testGetNodeName() {
        System.out.println("getNodeName");
        Node elemNode = null;
        String expResult = "";
        String result = OwlManagement.getNodeName(elemNode);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of downlift method, of class OwlManagement.
     */
    @Test
    public void testDownlift() {
        try {
            System.out.println("downlift");
            OwlManagement instance = new OwlManagement();

            assertTrue(instance.uplift(xmlPathfile));
            instance.saveOntology(metadataSavedowl);

          
            //Lists the Metadata individuals
            List<Individual> listIndividuals = instance.listsMetadataIndividuals().toList();
            assertEquals(1, listIndividuals.size());
            String result = instance.downlift(listIndividuals.get(0).getURI());
            assertTrue(!result.isEmpty());
            assertTrue(result.contains("Shapefiles"));
            assertTrue(result.contains("Produktinformationen"));
            System.out.println(result);
//            System.out.println(instance.getSPARQL("SELECT ?s ?p ?o WHERE {?s ?p ?o}"));
        } catch (OntoManagementException | IOException ex) {
            Logger.getLogger(OwlManagementTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }
  String expResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<csw:GetRecordByIdResponse xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\">\n"
                    + "  <gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gmx=\"http://www.isotc211.org/2005/gmx\" xmlns:gts=\"http://www.isotc211.org/2005/gts\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:geonet=\"http://www.fao.org/geonetwork\" xsi:schemaLocation=\"http://www.isotc211.org/2005/gmd http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd\">\n"
                    + "    <gmd:fileIdentifier>\n"
                    + "      <gco:CharacterString>91BED37B-3333-4C97-9A64-0311D25A3171</gco:CharacterString>\n"
                    + "    </gmd:fileIdentifier>\n"
                    + "    <gmd:language>\n"
                    + "      <gmd:LanguageCode codeList=\"http://www.loc.gov/standards/iso639-2/\" codeListValue=\"ger\">ger</gmd:LanguageCode>\n"
                    + "    </gmd:language>\n"
                    + "    <gmd:characterSet>\n"
                    + "      <gmd:MD_CharacterSetCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_CharacterSetCode\" codeListValue=\"utf8\" />\n"
                    + "    </gmd:characterSet>\n"
                    + "    <gmd:hierarchyLevel>\n"
                    + "      <gmd:MD_ScopeCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_ScopeCode\" codeListValue=\"dataset\">dataset</gmd:MD_ScopeCode>\n"
                    + "    </gmd:hierarchyLevel>\n"
                    + "    <gmd:contact>\n"
                    + "      <gmd:CI_ResponsibleParty uuid=\"A6CB2FB4-92D4-4195-9C99-68B56796BE09\">\n"
                    + "        <gmd:organisationName>\n"
                    + "          <gco:CharacterString>Bundesamt für Kartographie und Geodäsie (BKG)</gco:CharacterString>\n"
                    + "        </gmd:organisationName>\n"
                    + "        <gmd:contactInfo>\n"
                    + "          <gmd:CI_Contact>\n"
                    + "            <gmd:address>\n"
                    + "              <gmd:CI_Address>\n"
                    + "                <gmd:electronicMailAddress>\n"
                    + "                  <gco:CharacterString>dlz@bkg.bund.de</gco:CharacterString>\n"
                    + "                </gmd:electronicMailAddress>\n"
                    + "              </gmd:CI_Address>\n"
                    + "            </gmd:address>\n"
                    + "          </gmd:CI_Contact>\n"
                    + "        </gmd:contactInfo>\n"
                    + "        <gmd:role>\n"
                    + "          <gmd:CI_RoleCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_RoleCode\" codeListValue=\"pointOfContact\" />\n"
                    + "        </gmd:role>\n"
                    + "      </gmd:CI_ResponsibleParty>\n"
                    + "    </gmd:contact>\n"
                    + "    <gmd:dateStamp>\n"
                    + "      <gco:Date>2020-09-24</gco:Date>\n"
                    + "    </gmd:dateStamp>\n"
                    + "    <gmd:metadataStandardName>\n"
                    + "      <gco:CharacterString>ISO19115</gco:CharacterString>\n"
                    + "    </gmd:metadataStandardName>\n"
                    + "    <gmd:metadataStandardVersion>\n"
                    + "      <gco:CharacterString>2003/Cor.1:2006</gco:CharacterString>\n"
                    + "    </gmd:metadataStandardVersion>\n"
                    + "    <gmd:referenceSystemInfo>\n"
                    + "      <gmd:MD_ReferenceSystem>\n"
                    + "        <gmd:referenceSystemIdentifier>\n"
                    + "          <gmd:RS_Identifier>\n"
                    + "            <gmd:code>\n"
                    + "              <gmx:Anchor xlink:href=\"http://www.opengis.net/def/crs/EPSG/0/25832\">EPSG 25832: ETRS89 / UTM Zone 32N</gmx:Anchor>\n"
                    + "            </gmd:code>\n"
                    + "          </gmd:RS_Identifier>\n"
                    + "        </gmd:referenceSystemIdentifier>\n"
                    + "      </gmd:MD_ReferenceSystem>\n"
                    + "    </gmd:referenceSystemInfo>\n"
                    + "    <gmd:identificationInfo>\n"
                    + "      <gmd:MD_DataIdentification uuid=\"https://registry.gdi-de.org/id/de.bund.bkg.csw/3ae7272a-1133-4e35-b1b0-ec6b2f9b16d3\">\n"
                    + "        <gmd:citation>\n"
                    + "          <gmd:CI_Citation>\n"
                    + "            <gmd:title>\n"
                    + "              <gco:CharacterString>Verwaltungsgebiete Historisch - Jubiläumsausgabe 30 Jahre Deutsche Einheit</gco:CharacterString>\n"
                    + "            </gmd:title>\n"
                    + "            <gmd:alternateTitle>\n"
                    + "              <gco:CharacterString>Verwaltungsgrenzen</gco:CharacterString>\n"
                    + "            </gmd:alternateTitle>\n"
                    + "            <gmd:alternateTitle>\n"
                    + "              <gco:CharacterString>VG-Hist</gco:CharacterString>\n"
                    + "            </gmd:alternateTitle>\n"
                    + "            <gmd:date>\n"
                    + "              <gmd:CI_Date>\n"
                    + "                <gmd:date>\n"
                    + "                  <gco:DateTime>2020-08-27T00:00:00.000+02:00</gco:DateTime>\n"
                    + "                </gmd:date>\n"
                    + "                <gmd:dateType>\n"
                    + "                  <gmd:CI_DateTypeCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode\" codeListValue=\"creation\" />\n"
                    + "                </gmd:dateType>\n"
                    + "              </gmd:CI_Date>\n"
                    + "            </gmd:date>\n"
                    + "            <gmd:identifier>\n"
                    + "              <gmd:MD_Identifier>\n"
                    + "                <gmd:code>\n"
                    + "                  <gco:CharacterString>https://registry.gdi-de.org/id/de.bund.bkg.csw/3ae7272a-1133-4e35-b1b0-ec6b2f9b16d3</gco:CharacterString>\n"
                    + "                </gmd:code>\n"
                    + "              </gmd:MD_Identifier>\n"
                    + "            </gmd:identifier>\n"
                    + "          </gmd:CI_Citation>\n"
                    + "        </gmd:citation>\n"
                    + "        <gmd:abstract>\n"
                    + "          <gco:CharacterString>Anlässlich des 30-jährigen Jubiläums der Deutschen Einheit veröffentlicht das BKG diesen Datensatz historischer Verwaltungsgebiete. Das Produkt umfasst die administrativen Verwaltungseinheiten der Bundesrepublik Deutschland und der Deutschen Demokratischen Republik in einer einheitlichen Struktur mit Verwaltungsgrenzen, Schlüsselzahlen (Ost - TGS und West - AGS), Namen, Bezeichnungen, Einwohnerzahlen sowie Flächenangaben.\n"
                    + "\n"
                    + "Im Datensatz sind die Stände 03.10.1990 sowie 31.12.1989, 01.10.1990, 31.12.1990 und 01.01.1991 vorhanden.</gco:CharacterString>\n"
                    + "        </gmd:abstract>\n"
                    + "        <gmd:purpose>\n"
                    + "          <gco:CharacterString>Dieser Datensatz dient ausschließlich zur verständlichen Übersicht über die Verwaltungsgrenzen und -strukturen im historischen Kontext und stellt keine Bewertung der damaligen Zeit oder Umstände dar. Durch die weit zurückliegende Zeit lassen sich gewisse Unstimmigkeiten nicht immer vermeiden.</gco:CharacterString>\n"
                    + "        </gmd:purpose>\n"
                    + "        <gmd:status>\n"
                    + "          <gmd:MD_ProgressCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_ProgressCode\" codeListValue=\"historicalArchive\" />\n"
                    + "        </gmd:status>\n"
                    + "        <gmd:pointOfContact>\n"
                    + "          <gmd:CI_ResponsibleParty uuid=\"2ADF3D16-1AD0-383A-AEF0-8B1FC50D7E93\">\n"
                    + "            <gmd:organisationName>\n"
                    + "              <gco:CharacterString>Bundesamt für Kartographie und Geodäsie (BKG)</gco:CharacterString>\n"
                    + "            </gmd:organisationName>\n"
                    + "            <gmd:positionName>\n"
                    + "              <gco:CharacterString>Dienstleistungszentrum des Bundes für Geoinformation und Geodäsie (DLZ)</gco:CharacterString>\n"
                    + "            </gmd:positionName>\n"
                    + "            <gmd:contactInfo>\n"
                    + "              <gmd:CI_Contact>\n"
                    + "                <gmd:phone>\n"
                    + "                  <gmd:CI_Telephone>\n"
                    + "                    <gmd:voice>\n"
                    + "                      <gco:CharacterString>+49 (0)341 5634-333</gco:CharacterString>\n"
                    + "                    </gmd:voice>\n"
                    + "                    <gmd:facsimile>\n"
                    + "                      <gco:CharacterString>+49 (0)341 5634-415</gco:CharacterString>\n"
                    + "                    </gmd:facsimile>\n"
                    + "                  </gmd:CI_Telephone>\n"
                    + "                </gmd:phone>\n"
                    + "                <gmd:address>\n"
                    + "                  <gmd:CI_Address>\n"
                    + "                    <gmd:deliveryPoint>\n"
                    + "                      <gco:CharacterString>Karl-Rothe-Str. 10-14</gco:CharacterString>\n"
                    + "                    </gmd:deliveryPoint>\n"
                    + "                    <gmd:city>\n"
                    + "                      <gco:CharacterString>Leipzig</gco:CharacterString>\n"
                    + "                    </gmd:city>\n"
                    + "                    <gmd:administrativeArea>\n"
                    + "                      <gco:CharacterString>Bundesrepublik Deutschland</gco:CharacterString>\n"
                    + "                    </gmd:administrativeArea>\n"
                    + "                    <gmd:postalCode>\n"
                    + "                      <gco:CharacterString>04105</gco:CharacterString>\n"
                    + "                    </gmd:postalCode>\n"
                    + "                    <gmd:country>\n"
                    + "                      <gco:CharacterString>DEU</gco:CharacterString>\n"
                    + "                    </gmd:country>\n"
                    + "                    <gmd:electronicMailAddress>\n"
                    + "                      <gco:CharacterString>dlz@bkg.bund.de</gco:CharacterString>\n"
                    + "                    </gmd:electronicMailAddress>\n"
                    + "                  </gmd:CI_Address>\n"
                    + "                </gmd:address>\n"
                    + "                <gmd:onlineResource>\n"
                    + "                  <gmd:CI_OnlineResource>\n"
                    + "                    <gmd:linkage>\n"
                    + "                      <gmd:URL>www.bkg.bund.de</gmd:URL>\n"
                    + "                    </gmd:linkage>\n"
                    + "                  </gmd:CI_OnlineResource>\n"
                    + "                </gmd:onlineResource>\n"
                    + "              </gmd:CI_Contact>\n"
                    + "            </gmd:contactInfo>\n"
                    + "            <gmd:role>\n"
                    + "              <gmd:CI_RoleCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_RoleCode\" codeListValue=\"distributor\" />\n"
                    + "            </gmd:role>\n"
                    + "          </gmd:CI_ResponsibleParty>\n"
                    + "        </gmd:pointOfContact>\n"
                    + "        <gmd:pointOfContact>\n"
                    + "          <gmd:CI_ResponsibleParty uuid=\"2ADF3D16-1AD0-383A-AEF0-8B1FC50D7E93\">\n"
                    + "            <gmd:organisationName>\n"
                    + "              <gco:CharacterString>Bundesamt für Kartographie und Geodäsie (BKG)</gco:CharacterString>\n"
                    + "            </gmd:organisationName>\n"
                    + "            <gmd:positionName>\n"
                    + "              <gco:CharacterString>Dienstleistungszentrum des Bundes für Geoinformation und Geodäsie (DLZ)</gco:CharacterString>\n"
                    + "            </gmd:positionName>\n"
                    + "            <gmd:contactInfo>\n"
                    + "              <gmd:CI_Contact>\n"
                    + "                <gmd:phone>\n"
                    + "                  <gmd:CI_Telephone>\n"
                    + "                    <gmd:voice>\n"
                    + "                      <gco:CharacterString>+49 (0)341 5634-333</gco:CharacterString>\n"
                    + "                    </gmd:voice>\n"
                    + "                    <gmd:facsimile>\n"
                    + "                      <gco:CharacterString>+49 (0)341 5634-415</gco:CharacterString>\n"
                    + "                    </gmd:facsimile>\n"
                    + "                  </gmd:CI_Telephone>\n"
                    + "                </gmd:phone>\n"
                    + "                <gmd:address>\n"
                    + "                  <gmd:CI_Address>\n"
                    + "                    <gmd:deliveryPoint>\n"
                    + "                      <gco:CharacterString>Karl-Rothe-Str. 10-14</gco:CharacterString>\n"
                    + "                    </gmd:deliveryPoint>\n"
                    + "                    <gmd:city>\n"
                    + "                      <gco:CharacterString>Leipzig</gco:CharacterString>\n"
                    + "                    </gmd:city>\n"
                    + "                    <gmd:administrativeArea>\n"
                    + "                      <gco:CharacterString>Bundesrepublik Deutschland</gco:CharacterString>\n"
                    + "                    </gmd:administrativeArea>\n"
                    + "                    <gmd:postalCode>\n"
                    + "                      <gco:CharacterString>04105</gco:CharacterString>\n"
                    + "                    </gmd:postalCode>\n"
                    + "                    <gmd:country>\n"
                    + "                      <gco:CharacterString>DEU</gco:CharacterString>\n"
                    + "                    </gmd:country>\n"
                    + "                    <gmd:electronicMailAddress>\n"
                    + "                      <gco:CharacterString>dlz@bkg.bund.de</gco:CharacterString>\n"
                    + "                    </gmd:electronicMailAddress>\n"
                    + "                  </gmd:CI_Address>\n"
                    + "                </gmd:address>\n"
                    + "                <gmd:onlineResource>\n"
                    + "                  <gmd:CI_OnlineResource>\n"
                    + "                    <gmd:linkage>\n"
                    + "                      <gmd:URL>www.bkg.bund.de</gmd:URL>\n"
                    + "                    </gmd:linkage>\n"
                    + "                  </gmd:CI_OnlineResource>\n"
                    + "                </gmd:onlineResource>\n"
                    + "              </gmd:CI_Contact>\n"
                    + "            </gmd:contactInfo>\n"
                    + "            <gmd:role>\n"
                    + "              <gmd:CI_RoleCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_RoleCode\" codeListValue=\"pointOfContact\" />\n"
                    + "            </gmd:role>\n"
                    + "          </gmd:CI_ResponsibleParty>\n"
                    + "        </gmd:pointOfContact>\n"
                    + "        <gmd:resourceMaintenance>\n"
                    + "          <gmd:MD_MaintenanceInformation>\n"
                    + "            <gmd:maintenanceAndUpdateFrequency>\n"
                    + "              <gmd:MD_MaintenanceFrequencyCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_MaintenanceFrequencyCode\" codeListValue=\"notPlanned\" />\n"
                    + "            </gmd:maintenanceAndUpdateFrequency>\n"
                    + "            <gmd:updateScope>\n"
                    + "              <gmd:MD_ScopeCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_ScopeCode\" codeListValue=\"dataset\" />\n"
                    + "            </gmd:updateScope>\n"
                    + "          </gmd:MD_MaintenanceInformation>\n"
                    + "        </gmd:resourceMaintenance>\n"
                    + "        <gmd:graphicOverview>\n"
                    + "          <gmd:MD_BrowseGraphic>\n"
                    + "            <gmd:fileName>\n"
                    + "              <gco:CharacterString>https://mis.bkg.bund.de/preview/VG_Hist_Meta.png</gco:CharacterString>\n"
                    + "            </gmd:fileName>\n"
                    + "          </gmd:MD_BrowseGraphic>\n"
                    + "        </gmd:graphicOverview>\n"
                    + "        <gmd:descriptiveKeywords>\n"
                    + "          <gmd:MD_Keywords>\n"
                    + "            <gmd:keyword>\n"
                    + "              <gco:CharacterString>Verwaltungseinheiten</gco:CharacterString>\n"
                    + "            </gmd:keyword>\n"
                    + "            <gmd:type>\n"
                    + "              <gmd:MD_KeywordTypeCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_KeywordTypeCode\" codeListValue=\"theme\" />\n"
                    + "            </gmd:type>\n"
                    + "            <gmd:thesaurusName>\n"
                    + "              <gmd:CI_Citation>\n"
                    + "                <gmd:title>\n"
                    + "                  <gco:CharacterString>GEMET - INSPIRE themes, version 1.0</gco:CharacterString>\n"
                    + "                </gmd:title>\n"
                    + "                <gmd:date>\n"
                    + "                  <gmd:CI_Date>\n"
                    + "                    <gmd:date>\n"
                    + "                      <gco:Date>2008-06-01</gco:Date>\n"
                    + "                    </gmd:date>\n"
                    + "                    <gmd:dateType>\n"
                    + "                      <gmd:CI_DateTypeCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode\" codeListValue=\"publication\">publication</gmd:CI_DateTypeCode>\n"
                    + "                    </gmd:dateType>\n"
                    + "                  </gmd:CI_Date>\n"
                    + "                </gmd:date>\n"
                    + "              </gmd:CI_Citation>\n"
                    + "            </gmd:thesaurusName>\n"
                    + "          </gmd:MD_Keywords>\n"
                    + "        </gmd:descriptiveKeywords>\n"
                    + "        <gmd:descriptiveKeywords>\n"
                    + "          <gmd:MD_Keywords>\n"
                    + "            <gmd:keyword>\n"
                    + "              <gco:CharacterString>Gemeinde</gco:CharacterString>\n"
                    + "            </gmd:keyword>\n"
                    + "            <gmd:keyword>\n"
                    + "              <gco:CharacterString>Einwohnerzahl</gco:CharacterString>\n"
                    + "            </gmd:keyword>\n"
                    + "            <gmd:keyword>\n"
                    + "              <gco:CharacterString>Verwaltungsgrenze</gco:CharacterString>\n"
                    + "            </gmd:keyword>\n"
                    + "            <gmd:keyword>\n"
                    + "              <gco:CharacterString>Verwaltungseinheit</gco:CharacterString>\n"
                    + "            </gmd:keyword>\n"
                    + "            <gmd:keyword>\n"
                    + "              <gco:CharacterString>Verwaltungsgebiete</gco:CharacterString>\n"
                    + "            </gmd:keyword>\n"
                    + "            <gmd:keyword>\n"
                    + "              <gco:CharacterString>Gebiet</gco:CharacterString>\n"
                    + "            </gmd:keyword>\n"
                    + "            <gmd:keyword>\n"
                    + "              <gco:CharacterString>Staat</gco:CharacterString>\n"
                    + "            </gmd:keyword>\n"
                    + "            <gmd:keyword>\n"
                    + "              <gco:CharacterString>Region</gco:CharacterString>\n"
                    + "            </gmd:keyword>\n"
                    + "            <gmd:keyword>\n"
                    + "              <gco:CharacterString>Landkreis</gco:CharacterString>\n"
                    + "            </gmd:keyword>\n"
                    + "            <gmd:keyword>\n"
                    + "              <gco:CharacterString>Deutsche</gco:CharacterString>\n"
                    + "            </gmd:keyword>\n"
                    + "            <gmd:keyword>\n"
                    + "              <gco:CharacterString>Einheit</gco:CharacterString>\n"
                    + "            </gmd:keyword>\n"
                    + "          </gmd:MD_Keywords>\n"
                    + "        </gmd:descriptiveKeywords>\n"
                    + "        <gmd:descriptiveKeywords>\n"
                    + "          <gmd:MD_Keywords>\n"
                    + "            <gmd:keyword>\n"
                    + "              <gco:CharacterString>opendata</gco:CharacterString>\n"
                    + "            </gmd:keyword>\n"
                    + "          </gmd:MD_Keywords>\n"
                    + "        </gmd:descriptiveKeywords>\n"
                    + "        <gmd:descriptiveKeywords>\n"
                    + "          <gmd:MD_Keywords>\n"
                    + "            <gmd:keyword>\n"
                    + "              <gco:CharacterString>AdVMIS</gco:CharacterString>\n"
                    + "            </gmd:keyword>\n"
                    + "          </gmd:MD_Keywords>\n"
                    + "        </gmd:descriptiveKeywords>\n"
                    + "        <gmd:resourceConstraints>\n"
                    + "          <gmd:MD_LegalConstraints>\n"
                    + "            <gmd:useConstraints>\n"
                    + "              <gmd:MD_RestrictionCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode\" codeListValue=\"otherRestrictions\">otherRestrictions</gmd:MD_RestrictionCode>\n"
                    + "            </gmd:useConstraints>\n"
                    + "            <gmd:otherConstraints>\n"
                    + "              <gco:CharacterString>Die Daten sind urheberrechtlich geschützt. Der Datensatz wird entgeltfrei mit der Datenlizenz Deutschland Namensnennung 2.0 (https://www.govdata.de/dl-de/by-2-0) zur Verfügung gestellt. Der Quellenvermerk ist zu beachten.</gco:CharacterString>\n"
                    + "            </gmd:otherConstraints>\n"
                    + "            <gmd:otherConstraints>\n"
                    + "              <gco:CharacterString>Quellenvermerk: Der Lizenznehmer ist verpflichtet, bei jeder öffentlichen Wiedergabe, Verbreitung oder Präsentation der Daten sowie bei jeder Veröffentlichung oder externer Nutzung einer Bearbeitung oder Umgestaltung einen deutlich sichtbaren Quellenvermerk anzubringen, der wie folgt auszugestalten ist.\n"
                    + "\n"
                    + "© GeoBasis-DE / BKG (Jahr des Datenbezugs)</gco:CharacterString>\n"
                    + "            </gmd:otherConstraints>\n"
                    + "            <gmd:otherConstraints>\n"
                    + "              <gco:CharacterString>{\"id\":\"dl-by-de/2.0\",\"name\":\"Datenlizenz Deutschland Namensnennung 2.0\",\"url\":\"https://www.govdata.de/dl-de/by-2-0\",\"quelle\":\"Der Lizenznehmer ist verpflichtet, bei jeder öffentlichen Wiedergabe, Verbreitung oder Präsentation der Daten sowie bei jeder Veröffentlichung oder externer Nutzung einer Bearbeitung oder Umgestaltung einen deutlich sichtbaren Quellenvermerk anzubringen, der wie folgt auszugestalten ist.\\n\\n© GeoBasis-DE / BKG (Jahr des Datenbezugs)\"}</gco:CharacterString>\n"
                    + "            </gmd:otherConstraints>\n"
                    + "          </gmd:MD_LegalConstraints>\n"
                    + "        </gmd:resourceConstraints>\n"
                    + "        <gmd:spatialRepresentationType>\n"
                    + "          <gmd:MD_SpatialRepresentationTypeCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_SpatialRepresentationTypeCode\" codeListValue=\"vector\" />\n"
                    + "        </gmd:spatialRepresentationType>\n"
                    + "        <gmd:spatialResolution>\n"
                    + "          <gmd:MD_Resolution>\n"
                    + "            <gmd:equivalentScale>\n"
                    + "              <gmd:MD_RepresentativeFraction>\n"
                    + "                <gmd:denominator>\n"
                    + "                  <gco:Integer>1000000</gco:Integer>\n"
                    + "                </gmd:denominator>\n"
                    + "              </gmd:MD_RepresentativeFraction>\n"
                    + "            </gmd:equivalentScale>\n"
                    + "          </gmd:MD_Resolution>\n"
                    + "        </gmd:spatialResolution>\n"
                    + "        <gmd:language>\n"
                    + "          <gmd:LanguageCode codeList=\"http://www.loc.gov/standards/iso639-2/\" codeListValue=\"ger\" />\n"
                    + "        </gmd:language>\n"
                    + "        <gmd:characterSet>\n"
                    + "          <gmd:MD_CharacterSetCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_CharacterSetCode\" codeListValue=\"utf8\" />\n"
                    + "        </gmd:characterSet>\n"
                    + "        <gmd:topicCategory>\n"
                    + "          <gmd:MD_TopicCategoryCode>boundaries</gmd:MD_TopicCategoryCode>\n"
                    + "        </gmd:topicCategory>\n"
                    + "        <gmd:extent>\n"
                    + "          <gmd:EX_Extent>\n"
                    + "            <gmd:geographicElement>\n"
                    + "              <gmd:EX_GeographicDescription>\n"
                    + "                <gmd:extentTypeCode>\n"
                    + "                  <gco:Boolean>true</gco:Boolean>\n"
                    + "                </gmd:extentTypeCode>\n"
                    + "                <gmd:geographicIdentifier>\n"
                    + "                  <gmd:MD_Identifier>\n"
                    + "                    <gmd:code>\n"
                    + "                      <gco:CharacterString>Raumbezug des Datensatzes</gco:CharacterString>\n"
                    + "                    </gmd:code>\n"
                    + "                  </gmd:MD_Identifier>\n"
                    + "                </gmd:geographicIdentifier>\n"
                    + "              </gmd:EX_GeographicDescription>\n"
                    + "            </gmd:geographicElement>\n"
                    + "            <gmd:geographicElement>\n"
                    + "              <gmd:EX_GeographicBoundingBox>\n"
                    + "                <gmd:extentTypeCode>\n"
                    + "                  <gco:Boolean>true</gco:Boolean>\n"
                    + "                </gmd:extentTypeCode>\n"
                    + "                <gmd:westBoundLongitude>\n"
                    + "                  <gco:Decimal>5.8667</gco:Decimal>\n"
                    + "                </gmd:westBoundLongitude>\n"
                    + "                <gmd:eastBoundLongitude>\n"
                    + "                  <gco:Decimal>15.0419</gco:Decimal>\n"
                    + "                </gmd:eastBoundLongitude>\n"
                    + "                <gmd:southBoundLatitude>\n"
                    + "                  <gco:Decimal>47.2703</gco:Decimal>\n"
                    + "                </gmd:southBoundLatitude>\n"
                    + "                <gmd:northBoundLatitude>\n"
                    + "                  <gco:Decimal>55.0585</gco:Decimal>\n"
                    + "                </gmd:northBoundLatitude>\n"
                    + "              </gmd:EX_GeographicBoundingBox>\n"
                    + "            </gmd:geographicElement>\n"
                    + "          </gmd:EX_Extent>\n"
                    + "        </gmd:extent>\n"
                    + "      </gmd:MD_DataIdentification>\n"
                    + "    </gmd:identificationInfo>\n"
                    + "    <gmd:distributionInfo>\n"
                    + "      <gmd:MD_Distribution>\n"
                    + "        <gmd:distributionFormat>\n"
                    + "          <gmd:MD_Format>\n"
                    + "            <gmd:name>\n"
                    + "              <gco:CharacterString>Shapefiles</gco:CharacterString>\n"
                    + "            </gmd:name>\n"
                    + "            <gmd:version>\n"
                    + "              <gco:CharacterString>1.0</gco:CharacterString>\n"
                    + "            </gmd:version>\n"
                    + "          </gmd:MD_Format>\n"
                    + "        </gmd:distributionFormat>\n"
                    + "        <gmd:distributionFormat>\n"
                    + "          <gmd:MD_Format>\n"
                    + "            <gmd:name>\n"
                    + "              <gco:CharacterString>DBasse</gco:CharacterString>\n"
                    + "            </gmd:name>\n"
                    + "            <gmd:version>\n"
                    + "              <gco:CharacterString>III</gco:CharacterString>\n"
                    + "            </gmd:version>\n"
                    + "          </gmd:MD_Format>\n"
                    + "        </gmd:distributionFormat>\n"
                    + "        <gmd:distributionFormat>\n"
                    + "          <gmd:MD_Format>\n"
                    + "            <gmd:name>\n"
                    + "              <gco:CharacterString>Excel</gco:CharacterString>\n"
                    + "            </gmd:name>\n"
                    + "            <gmd:version>\n"
                    + "              <gco:CharacterString>97-2003</gco:CharacterString>\n"
                    + "            </gmd:version>\n"
                    + "          </gmd:MD_Format>\n"
                    + "        </gmd:distributionFormat>\n"
                    + "        <gmd:transferOptions>\n"
                    + "          <gmd:MD_DigitalTransferOptions>\n"
                    + "            <gmd:onLine>\n"
                    + "              <gmd:CI_OnlineResource>\n"
                    + "                <gmd:linkage>\n"
                    + "                  <gmd:URL>https://gdz.bkg.bund.de/index.php/default/digitale-geodaten/verwaltungsgebiete/verwaltungsgebiete-historisch-vg-hist.html</gmd:URL>\n"
                    + "                </gmd:linkage>\n"
                    + "                <gmd:name>\n"
                    + "                  <gco:CharacterString>Produktinformationen</gco:CharacterString>\n"
                    + "                </gmd:name>\n"
                    + "                <gmd:function>\n"
                    + "                  <gmd:CI_OnLineFunctionCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_OnLineFunctionCode\" codeListValue=\"information\">information</gmd:CI_OnLineFunctionCode>\n"
                    + "                </gmd:function>\n"
                    + "              </gmd:CI_OnlineResource>\n"
                    + "            </gmd:onLine>\n"
                    + "          </gmd:MD_DigitalTransferOptions>\n"
                    + "        </gmd:transferOptions>\n"
                    + "      </gmd:MD_Distribution>\n"
                    + "    </gmd:distributionInfo>\n"
                    + "    <gmd:dataQualityInfo>\n"
                    + "      <gmd:DQ_DataQuality>\n"
                    + "        <gmd:scope>\n"
                    + "          <gmd:DQ_Scope>\n"
                    + "            <gmd:level>\n"
                    + "              <gmd:MD_ScopeCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_ScopeCode\" codeListValue=\"dataset\" />\n"
                    + "            </gmd:level>\n"
                    + "          </gmd:DQ_Scope>\n"
                    + "        </gmd:scope>\n"
                    + "        <gmd:report>\n"
                    + "          <gmd:DQ_DomainConsistency>\n"
                    + "            <gmd:result>\n"
                    + "              <gmd:DQ_ConformanceResult>\n"
                    + "                <gmd:specification>\n"
                    + "                  <gmd:CI_Citation>\n"
                    + "                    <gmd:title>\n"
                    + "                      <gco:CharacterString>Verwaltungsgebiete Historisch - VG-Hist</gco:CharacterString>\n"
                    + "                    </gmd:title>\n"
                    + "                    <gmd:date>\n"
                    + "                      <gmd:CI_Date>\n"
                    + "                        <gmd:date>\n"
                    + "                          <gco:Date>2020-08-27</gco:Date>\n"
                    + "                        </gmd:date>\n"
                    + "                        <gmd:dateType>\n"
                    + "                          <gmd:CI_DateTypeCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode\" codeListValue=\"publication\">publication</gmd:CI_DateTypeCode>\n"
                    + "                        </gmd:dateType>\n"
                    + "                      </gmd:CI_Date>\n"
                    + "                    </gmd:date>\n"
                    + "                  </gmd:CI_Citation>\n"
                    + "                </gmd:specification>\n"
                    + "                <gmd:explanation>\n"
                    + "                  <gco:CharacterString>see the referenced specification</gco:CharacterString>\n"
                    + "                </gmd:explanation>\n"
                    + "                <gmd:pass>\n"
                    + "                  <gco:Boolean>true</gco:Boolean>\n"
                    + "                </gmd:pass>\n"
                    + "              </gmd:DQ_ConformanceResult>\n"
                    + "            </gmd:result>\n"
                    + "          </gmd:DQ_DomainConsistency>\n"
                    + "        </gmd:report>\n"
                    + "        <gmd:lineage>\n"
                    + "          <gmd:LI_Lineage>\n"
                    + "            <gmd:statement>\n"
                    + "              <gco:CharacterString>Zur Erstherstellung wurden analoge und digitale Ausgangsdaten des Bundesamtes für Kartographie und Geodäsie sowie der Landesvermessungseinrichtungen als Grundlage verwendet. Der Erfassungsmaßstab variiert von 1 : 1 000 000 bis 1 : 2 000 000. Eine manuelle Digitalisierung fand bei fehlender Gebiete durch das BKG und zur Erhaltung der Topologie statt. Letztlich wurden die Attribute mit den Informationen der statistischen Ämter verknüpft.</gco:CharacterString>\n"
                    + "            </gmd:statement>\n"
                    + "          </gmd:LI_Lineage>\n"
                    + "        </gmd:lineage>\n"
                    + "      </gmd:DQ_DataQuality>\n"
                    + "    </gmd:dataQualityInfo>\n"
                    + "  </gmd:MD_Metadata>\n"
                    + "</csw:GetRecordByIdResponse>\n"
                    + "\n"
                    + "";
}
