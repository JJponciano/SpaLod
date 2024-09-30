<template>
  <div class="installation-guide">
    <h1>SpaLOD</h1>
    <p>If you want to use Spalod, please cite the following paper:</p>
    <blockquote>
      Ponciano, C.; Schaffert, M.; Würriehausen, F. and Ponciano, J. (2022). Publish and Enrich Geospatial Data as
      Linked Open Data. In Proceedings of the 18th International Conference on Web Information Systems and Technologies
      - WEBIST; ISBN 978-989-758-613-2; ISSN 2184-3252, SciTePress, pages 314-319. DOI: 10.5220/0011550600003318
    </blockquote>

    <h3>BibTeX</h3>
    <pre>
      @conference{webist22,
        author={Claire Ponciano. and Markus Schaffert. and Falk Würriehausen. and Jean{-}Jacques Ponciano.},
        title={Publish and Enrich Geospatial Data as Linked Open Data},
        booktitle={Proceedings of the 18th International Conference on Web Information Systems and Technologies - WEBIST},
        year={2022},
        pages={314-319},
        publisher={SciTePress},
        organization={INSTICC},
        doi={10.5220/0011550600003318},
        isbn={978-989-758-613-2},
        issn={2184-3252},
      }
    </pre>
    <h2>Introduction</h2>
    <p>
      SpaLOD addresses the increasingly complex challenge brought about by the rapid growth of geospatial data, which
      expands by at least 20% every year. This has resulted in an enormous increase in data heterogeneity, creating
      complexities in structure and vocabulary variations. The vocabularies in use depend heavily on the application
      domain and the language in which the data is described, making integration and unification a daunting task.
    </p>
    <p>
      In light of these challenges and harnessing the potential of Semantic Web technologies, numerous approaches have
      emerged to group these data into knowledge graphs. These knowledge graphs enable efficient data linking, ease
      sharing, and enhance maintenance. However, they also bring forth the daunting task of data homogenization due to
      the non-unified data structures and vocabulary variations.
    </p>
    <p>
      To overcome this problem of homogenization, we present SpaLOD, a comprehensive framework designed to efficiently
      group heterogeneous spatial data into a single knowledge base. The knowledge base is rooted in an ontology
      connected to Schema.org and DCAT-AP, providing a data structure compatible with GeoSPARQL. One of the unique
      strengths of SpaLOD is its ability to integrate geospatial data independently of their original language. This is
      made possible by translating them using advanced Neural Machine Translation.
    </p>
    <p>
      SpaLOD sets a new benchmark in the field of geospatial data, enabling a universal sharing platform and fostering
      collaboration between different states and organizations globally. Through SpaLOD, we envision a future where
      geospatial data can be universally used, integrated, and shared, regardless of their original structure and
      language.
    </p>
    <img src="../assets/bkg_i3mainz.png" alt="System Functionality">

    <h2>Related Work and Problem Statements</h2>

    <p>Storing geospatial data for widespread use and sharing is a challenging research problem, primarily because of
      the rapid growth of data, at least by 20% every year (Lee and Kang, 2015). In various studies, authors present an
      ontology-enabled framework to find geographic services aiming to solve geospatial problems. However, their
      ontologies do not provide a common vocabulary to describe geographic features and attributes, limiting their
      utility.</p>

    <p>While these research projects do not group heterogeneous spatial data in a knowledge base at an advanced level
      and using a common vocabulary, projects such as (Jung et al., 2013; Budak Arpinar et al., 2006; Dsouza et al.,
      2021) demonstrate that intelligent data provisioning using Semantic Web methods appears to be an effective
      solution for simplified linking of official and unofficial data for geodata users.</p>

    <p>Moreover, projects using ontology-based frameworks such as (Dsouza et al., 2021; Sun et al., 2019; Karalis et
      al., 2019) highlight the growing interest in structuring geospatial data in the form of knowledge graphs. Such
      graphs are stored in triplestores (such as Apache Marmotta 1, Apache Jena 2, Eclipse RDF4J 3, Strabon 4, Oracle
      Spatial and Graph 5, GraphDB 6, Stardog 7, and Virtuoso Universal Server 8) allowing fast read and write access by
      SPARQL.</p>

    <p>In the domain of geospatial, the GeoSPARQL (Battle and Kolas, 2011) approach has greatly improved access to
      geospatial data from a triplestore, allowing to retrieve and update geospatial data with simplified queries.
      However, GeoSPARQL requires that the stored data be structured according to a particular form in order to be
      usable, but most of the triplestores from the LOD have not added the structure proposed by GeoSPARQL. This
      constraint limits the use of GeoSPARQL for the LOD.</p>

    <p>The other main issue is the heterogeneity of the geospatial data. Usually these data are stored in specific
      formats (such as GeoJSON (Butler et al., 2016), Shapefile 9, OpenStreetMap (Ramm et al., 2014)) having their own
      structure and being in different languages. This variation in vocabulary poses a major challenge for the
      integration of data from different sources.</p>

    <p>We can deduce from this study that (i) the use of knowledge base (in the form of ontology and triplestore) is the
      most flexible and promising way to store large amounts of geospatial data, (ii) that there is currently no known
      platform to efficiently group heterogeneous spatial data in a knowledge base with a common vocabulary for all
      integrated datasets, (iii) that the major challenge for the integration of geospatial data is the diversity of
      structures and vocabularies used to organize and express this information.</p>

    <h2>Solution Proposed</h2>
    <p>In order to meet the challenge of storing vast amount
      of geospatial data for universal sharing, we outline
      a framework for integrating heterogeneous geospatial
      data from different sources and languages in a structured and consistent manner. Figure 1 illustrates the
      goal of this framework. As a position paper, we focus
      on how to implement this framework using existing
      research, rather than explaining all the mechanisms
      that serialized it, which is part of our future work.
      In section 3.1, we highlight solution for the integration of heterogeneous geospatial data into a Universal
      Spatial Knowledge Base (USKB). In Section
      3.2 we highlight possibility to create common vocabulary, on the basis of which universal queries can be
      performed on data in RDF format. This common vocabulary aims to bring together vocabularies used in
      data from different sources such as Linked Open Data
      and geospatial formats.
    </p>
    <img src="../assets/figure1.jpg" alt="System Functionality">

    <h3>Automatic integration of
      heterogeneous data into a Universal
      Spatial Knowledge Base</h3>
    <p>Among the various approaches developed to unify
      geodata structure and vocabularies, the Ordnance Survey’s Linked Data Platform proposes an approach
      using OWL (Antoniou and Harmelen, 2004) to link
      GeoSPARQL concepts to equivalent concepts in its
      ontology. GeoSPARQL has thus become a standard for representing and querying linked geospatial
      data for the Open Geospatial Consortium (OGC) (van
      Rees, 2013) Semantic Web.
      Inspired by this approach, we highlight the relevance of creating an ontology combining the concepts
      of GeoSPARQL with the work (Quarati et al., 2021).
      Moreover, the work (Stadler et al., 2012) allows for
      describing metadata quality concepts, and link them
      with the LinkedGeoData.
      Such an ontology aims to bring together geoinformation from different sources by converting
      common file formats in the geodomain and their
      application schemas, such as GeoJSON, Shapefile,
      OpenStreetMap, RDF (Pan, 2009). The work (Patroumpas et al., 2014) proposed such conversion without considering
      other ontologies. Thus, they are limited to the vocabulary used in the data file for creating
      RDF concepts. It is therefore necessary to adapt the
      RDF vocabulary and structure to those of the ontology for which we intend to integrate them.
      The RDF structure adaptation can be performed
      with the help of an alignment of the ontology structure as proposed in (Li et al., 2008) using automatic
      integration approaches developed in the works (Prudhomme et al., 2020; Prudhomme et al., 2017).
      However, such approaches required different
      parametrisation according to the data source quality. This quality mainly depends of the source type.
      Therefore an evaluation of data quality is needed to
      guide the integration process.
      As an exemple, such evaluation can be based on
      the ”5-star Open Data” 10 principle as follows:
      1. 1 star: Data available on the Web (in any format)
      under an open license,
      2. 2 stars: Data available as structured data (spreadsheet) in a proprietary format,
      3. 3 stars: Data available in a non-proprietary open
      format (such as CSV),
      4. 4 stars: Data available linked to URIs that to denote things,
      5. 5 stars: Data available and linked to other data to
      provide context.
      Automatic integration of data into an ontology
      without human supervision can lead to an inconsistent structure due to the accumulation of errors. Thus,
      to ensure the consistency of the added data, each concept is described using OWL2 (Consortium et al.,
      2012) constraints. These constraints allow to verify
      that the added data is consistent with the structure
      imposed by the ontology. This consistency check is
      performed using reasoner (such as HermiT (Glimm
      et al., 2014), Owlgres (Stocker and Smith, 2008), Pellet (Sirin et al., 2007), DeLorea (Bobillo et al., 2012)).
    </p>
    <h3>Universal Vocabulary</h3>
    <p>The data vocabulary provided varies depending on
      the source of the Linked Open Data (e.g. Wikidata
      (van Veen, 2019), DBpedia 11) and the data format
      (e.g. GML, KML, GeoJSON). The establishment of
      a common vocabulary allows for gathering geospatial
      knowledge in one place and thus, facilitating the retrieval of geographic data. Access to the data knowledge can
      then be done via a web interface.
      To be universal, this language can be based on the
      English lexicon provided by WordNet (Miller, 1995).
      Related works such as (Frontini et al., 2016; Bond
      and Bond, 2019) have succeed in linking GeoNames
      Ontology and WordNet. Other approaches such as
      Schema.org (Guha et al., 2016) and KBpedia 12 are
      fully exploiting the power of linked open data to provide a varied and structured vocabulary in the English
      language. Recently DCAT- AP (Kirstein et al.,
      2019) and GeoDCAT-AP13 are planning to link their
      knowledge to Schema.org 14, making Schema.org a
      10https://5stardata.info/en/, accessed on 2022-07-07
      11DBpedia: https://www.dbpedia.org, accessed on 2022-
      07-07
      12https://kbpedia.org, accessed on 2022-07-07
      13https://inspire.ec.europa.eu/good-practice/geodcat-ap,
      accessed on 2022-07-07
      14https://www.w3.org/2015/spatial/wiki/ISO 19115 -
      DCAT - Schema.org mapping, accessed on 2022-07-07
      promising base for the geospatial domain. Moreover,
      Schema.org currently support complex geometries
      and WKT literals, which would make the adoption of
      GeoSPARQL straightforward.
      Since the vocabulary comes from various languages, we propose to use a Neural Machine Translation approach to
      automatically translate each term
      into English while being able to automatically detect
      the original language. The works (Koehn, 2020) and
      (Stahlberg, 2020) provide a review of different Neural
      Machine Translation approaches. Recently, the work
      of (Zhao et al., 2021) proposes to improve these approaches by combining them with a knowledge graph,
      thus improving the accuracy of the translation.
      The translated vocabulary is then aligned with the
      knowledge base vocabulary using an ontology alignment approach such as (Zhang et al., 2014). Following this
      alignment, the aligned terms between the vocabulary used in the RDF files and the vocabulary of
      the knowledge base is submitted to the user for approval. The user can then validate and complete the
      vocabulary match in order to allow the integration of
      the data to enrich the knowledge base. The alignment
      of the terms is then stored to allow for better automation in the future.</p>
    <p>Let us take as an example a spatial data containing the geometries and attributes of schools in
      the Bundesland Rheinland-Pfalz in Germany in order to illustrate the proposed solution. In this
      dataset, a school has a geometry represented by a
      point and the following list of attributes in German: Offnungszeiten ¨ , FaxNummer, and Name. The
      names of these attributes will be translated into English as: opening hours, fax number and name
      respectively. An individual of type dcat:Dataset
      will be created to represent the data. Then for
      each school contained in the data, an individual
      will be created. This individual will be of type
      geo:Feature (from GeoSPARQL) and schema:School
      (from Schema.org). Then, an equivalence to the attributes will be matched with Schema.org in order to
      add the information related to this individual. In our
      example, the values of the attributes Offnungszeiten ¨ ,
      FaxNummer, and Name will be added respectively with the properties schema:openingHours,
      schema:faxNumber, schema:name thanks to the English translation done before. Finally, the individual will be
      linked to its geometry with the property geo:hasGeometry. Its geometry will be of type
      geo:Point and will have a value linked by the property geo:asWKT.
    </p>
    <h2>DISCUSSION</h2>
    <p>We propose conceptual guidelines for the development of a framework to unify
      the integration of geospatial data within a Universal
      Spatial Knowledge Base (USKB) and to easily link
      geospatial data to Linked Open Data. This framework is composed of an ontology, whose concepts
      are mainly based on standards (such as GeoSPARQL)
      and well-known ontologies (as GeoNames). The various data sets contained in files (such as GeoJSON,
      Shapefile, OpenStreetMap, INSPIRE) are integrated
      into this ontology using structured mapping. The consistency of the data integration is ensured by a reasoning
      applied on the constraints defined for each
      concept, which ensures a continuous consistency of
      the data. The data vocabulary is translated into English from its original language by a Neural Machine
      Translation approach. The translated vocabulary is
      mapped to the ontology vocabulary. The resulting
      mapping is submitted to the user for verification, who
      can modify or approve the proposed mapping. The
      decisions taken are then saved as a mapping table to
      allow continuous learning of the geospatial vocabulary and thus improve the mapping. The future work
      consists in enriching the basic concepts of the ontology and the mapping of the vocabulary by supervised
      learning in order to make the integration process as
      automated as possible. Furthermore, the quality of
      the data integration must be thoroughly evaluated by
      experts. The exploitation of expert knowledge modelled in an ontology can be considered in order to automate this
      evaluation, e.g. by generating correspondence concepts to semantically map the different data
      systems used to the respective INSPIRE systems.
    </p>
    <h2>ACKNOWLEDGEMENTS</h2>
    <p>This research is funded by the Federal Agency for
      Cartography and Geodesy in Germany</p>

    <div class="references">
      <h2>REFERENCES</h2>
      <ul>
        <li>Abbas, S. and Ojo, A. (2013). Towards a linked geospatial data infrastructure. In International Conference
          on Electronic Government and the Information Systems Perspective, pages 196–210. Springer.</li>
        <li>
          Antoniou, G. and Harmelen, F. v. (2004). Web ontology
          language: Owl. In Handbook on ontologies, pages
          67–92. Springer.</li>
        <li>
          Battle, R. and Kolas, D. (2011). Geosparql: enabling
          a geospatial semantic web. Semantic Web Journal,
          3(4):355–370.</li>
        <li>
          Bobillo, F., Delgado, M., and Gomez-Romero, J. (2012). ´
          Delorean: A reasoner for fuzzy owl 2. Expert Systems
          with Applications, 39(1):258–272.</li>
        <li>
          Bond, F. and Bond, A. (2019). Geonames wordnet (geown):
          extracting wordnets from geonames. In Proceedings
          of the 10th Global Wordnet Conference, pages 387–
          393.</li>
        <li>
          Budak Arpinar, I., Sheth, A., Ramakrishnan, C.,
          Lynn Usery, E., Azami, M., and Kwan, M.-P. (2006).
          Geospatial ontology development and semantic analytics. Transactions in GIS, 10(4):551–575.</li>
        <li>
          Butler, H., Daly, M., Doyle, A., Gillies, S., Hagen, S.,
          Schaub, T., et al. (2016). The geojson format. Internet
          Engineering Task Force (IETF).
          Consortium, W. W. W. et al. (2012). Owl 2 web ontology
          language document overview.</li>
        <li>
          Dsouza, A., Tempelmeier, N., Yu, R., Gottschalk, S., and
          Demidova, E. (2021). Worldkg: A world-scale geographic knowledge graph. In Proceedings of the
          30th ACM International Conference on Information &
          Knowledge Management, pages 4475–4484.</li>
        <li>
          Frontini, F., Del Gratta, R., and Monachini, M. (2016).
          Geodomainwordnet: Linking the geonames ontology
          to wordnet. In Vetulani, Z., Uszkoreit, H., and Kubis, M., editors, Human Language Technology. Challenges for
          Computer Science and Linguistics, pages
          229–242, Cham. Springer International Publishing.</li>
        <li>
          Gannon, B. M., Thompson, M. P., Deming, K. Z., Bayham,
          J., Wei, Y., and O’Connor, C. D. (2020). A geospatial framework to assess fireline effectiveness for large
          wildfires in the western usa. Fire, 3(3):43.</li>
        <li>
          Glimm, B., Horrocks, I., Motik, B., Stoilos, G., and Wang,
          Z. (2014). Hermit: an owl 2 reasoner. Journal of
          Automated Reasoning, 53(3):245–269.</li>
        <li>
          Guan, W. W., Bol, P. K., Lewis, B. G., Bertrand,
          M., Berman, M. L., and Blossom, J. C. (2012).
          Worldmap–a geospatial framework for collaborative
          research. Annals of GIS, 18(2):121–134.</li>
        <li>
          Guha, R. V., Brickley, D., and Macbeth, S. (2016).
          Schema.org: Evolution of structured data on the web.
          Commun. ACM, 59(2):44–51.</li>
        <li>
          Jung, C.-T., Sun, C.-H., and Yuan, M. (2013). An ontologyenabled framework for a geospatial problem-solving
          environment. Computers, Environment and Urban
          Systems, 38:45–57.</li>
        <li>
          Karalis, N., Mandilaras, G., and Koubarakis, M. (2019).
          Extending the yago2 knowledge graph with precise
          geospatial knowledge. In International Semantic Web
          Conference, pages 181–197. Springer.</li>
        <li>
          Karam, R. and Melchiori, M. (2013). A crowdsourcingbased framework for improving geo-spatial open data.
          In 2013 IEEE International Conference on Systems,
          Man, and Cybernetics, pages 468–473. IEEE.</li>
        <li>
          Kirstein, F., Dittwald, B., Dutkowski, S., Glikman, Y.,
          Schimmler, S., and Hauswirth, M. (2019). Linked data
          in the european data portal: A comprehensive platform for applying dcat-ap. In Lindgren, I., Janssen,
          M., Lee, H., Polini, A., Rodr´ıguez Bol´ıvar, M. P.,
          Scholl, H. J., and Tambouris, E., editors, Electronic
          Government, pages 192–204, Cham. Springer International Publishing.</li>
        <li>
          Koehn, P. (2020). Neural machine translation. Cambridge
          University Press.
          Larkou, G., Metochi, J., Chatzimilioudis, G., and
          Zeinalipour-Yazti, D. (2013). Cloda: A crowdsourced
          linked open data architecture. In 2013 IEEE 14th International Conference on Mobile Data Management,
          volume 2, pages 104–109. IEEE.</li>
        <li>
          Lee, J.-G. and Kang, M. (2015). Geospatial big data: Challenges and opportunities. Big Data Research, 2(2):74–
          81. Visions on Big Data.</li>
        <li>
          Li, J., Tang, J., Li, Y., and Luo, Q. (2008). Rimom: A dynamic multistrategy ontology alignment framework.
          IEEE Transactions on Knowledge and data Engineering, 21(8):1218–1232.</li>
        <li>
          Miller, G. A. (1995). Wordnet: A lexical database for english. In Communications of the ACM, volume 38,
          pages 39–41, Cham.</li>
        <li>
          Pan, J. Z. (2009). Resource description framework. In
          Handbook on ontologies, pages 71–90. Springer.</li>
        <li>
          Patroumpas, K., Alexakis, M., Giannopoulos, G., and
          Athanasiou, S. (2014). Triplegeo: an etl tool for transforming geospatial data into rdf triples. In Edbt/Icdt
          Workshops, pages 275–278. Citeseer.</li>
        <li>
          Prudhomme, C., Homburg, T., Ponciano, J.-J., Boochs, F.,
          Cruz, C., and Roxin, A.-M. (2020). Interpretation and
          automatic integration of geospatial data into the semantic web. Computing, 102(2):365–391.</li>
        <li>
          Prudhomme, C., Homburg, T., Ponciano, J.-J., Boochs, F.,
          Roxin, A., and Cruz, C. (2017). Automatic integration of spatial data into the semantic web. In WebIST,
          pages 107–115.</li>
        <li>
          Quarati, A., De Martino, M., and Rosim, S. (2021). Geospatial open data usage and metadata quality. ISPRS
          International Journal of Geo-Information, 10(1):30.</li>
        <li>
          Ramm, F., Names, I., Files, S., Catalogue, F., Features, P.,
          Features, N., and Cars, C. (2014). Openstreetmap data
          in layered gis format. Version 0.6, 7.</li>
        <li>
          Simon, R. and Frohlich, P. (2007). A mobile application ¨
          framework for the geospatial web. In Proceedings of
          the 16th international conference on World Wide Web,
          pages 381–390.</li>
        <li>
          Sirin, E., Parsia, B., Grau, B. C., Kalyanpur, A., and Katz,
          Y. (2007). Pellet: A practical owl-dl reasoner. Journal
          of Web Semantics, 5(2):51–53.</li>
        <li>
          Stadler, C., Lehmann, J., Hoffner, K., and Auer, S. (2012). ¨
          Linkedgeodata: A core for a web of spatial open data.
          Semantic Web, 3(4):333–354.</li>
        <li>
          Stahlberg, F. (2020). Neural machine translation: A review.
          Journal of Artificial Intelligence Research, 69:343–
          418.</li>
        <li>
          Stocker, M. and Smith, M. (2008). Owlgres: A scalable owl
          reasoner. In OWLED, volume 432.</li>
        <li>
          Sun, K., Zhu, Y., Pan, P., Hou, Z., Wang, D., Li, W., and
          Song, J. (2019). Geospatial data ontology: the semantic foundation of geospatial data integration and
          sharing. Big Earth Data, 3(3):269–296.</li>
        <li>
          van Rees, E. (2013). Open geospatial consortium (ogc).
          Geoinformatics, 16(8):28.</li>
        <li>
          van Veen, T. (2019). Wikidata. Information technology and
          libraries, 38(2):72–81.</li>
        <li>
          Zhang, Y., Wang, X., Lai, S., He, S., Liu, K., Zhao, J.,
          and Lv, X. (2014). Ontology matching with word
          embeddings. In Sun, M., Liu, Y., and Zhao, J., editors, Chinese Computational Linguistics and Natural
          Language Processing Based on Naturally Annotated
          Big Data, pages 34–45, Cham. Springer International
          Publishing.</li>
        <li>
          Zhao, Y., Zhang, J., Zhou, Y., and Zong, C. (2021). Knowledge graphs enhanced neural machine translation. In
          Proceedings of the Twenty-Ninth International Conference on International Joint Conferences on Artificial
          Intelligence, pages 4039–4045.</li>
      </ul>
    </div>


    <h1>Installation Guide</h1>
    <h2>Docker</h2>
    <p>Clone the repository and go inside:</p>
    <pre><code>
    git clone https://github.com/JJponciano/SpaLod.git
    cd SpaLod
    </code></pre>
    <p>Build the docker image and run the container:</p>
    <pre><code>
    docker build -t spalod .
    docker run -p 8080:8080 -d -i --name spalod-container spalod
    </code></pre>
    <p>Run the server:</p>
    <pre><code>
    docker exec spalod-container /home/spalod/spalod.sh
    </code></pre>

    <h2>Manual Installation</h2>
    <p>Clone the repository and go inside:</p>
    <pre><code>
    git clone https://github.com/JJponciano/SpaLod.git
    cd SpaLod
    </code></pre>
    <p>Install dependencies:</p>
    <pre><code>
    mvn install:install-file \
    -Dfile=/home/spalod/libs/pisemantic-1.0-SNAPSHOT.jar \
    -DpomFile=/home/spalod/pom.xml \
    -DgroupId=info.ponciano.lab \
    -DartifactId=pisemantic \
    -Dversion=1.0 \
    -Dpackaging=jar

    mvn install:install-file \
    -Dfile=/home/spalod/libs/pitools-1.0-SNAPSHOT.jar \
    -DpomFile=/home/spalod/pom.xml \
    -DgroupId=info.ponciano.lab \
    -DartifactId=pitools \
    -Dversion=1.0-SNAPSHOT\
    -Dpackaging=jar
    </code></pre>
    <p>Build the binaries:</p>
    <pre><code>
    mvn package
    </code></pre>
    <p>Run the server:</p>
    <pre><code>
    java -jar target/spalod-0.0.1-SNAPSHOT.jar
    </code></pre>

    <h1>User Guide</h1>

    <img src="../assets/system-functionality.png" alt="System Functionality">

    <p>As depicted in the figure above, the first step for integrating and enriching the knowledge base is to retrieve
      an RDF representation of the data to be integrated, a process also known as uplift. When integrating data from
      Linked Open Data, a query is made to retrieve the desired data as an RDF triple.</p>

    <p>The retrieved RDF files can then be integrated via the enrichment web page provided. The RDF files are parsed to
      give the user a match between the vocabulary used in the RDF files and the vocabulary of the knowledge base. The
      user can validate and complete the vocabulary match to enable the integration of the RDF files to enrich the
      knowledge base.</p>

    <p>Integration of data is achieved using Sparql queries to update the Knowledge Base. Once the integration is
      complete, the data can be evaluated either automatically or manually. In the case of automatic evaluation, the
      data is analyzed to define and assign a ranking. This ranking is added via a Sparql query. With manual evaluation,
      the user can retrieve their chosen data via Sparql queries and define a ranking in the provided interface.</p>

    <p>The ranking defined by the user is then entered into the knowledge base via a Sparql query. Finally, the user can
      access the content of the knowledge base via a Sparql endpoint, available in a web interface.</p>


  </div>
</template>

<script>
export default {
  name: 'Introduction'
}
</script>

<style scoped>
.references h2 {
  font-size: 1.5em;
  margin-bottom: 0.5em;
}

.references ul {
  list-style: disc inside;
  line-height: 1.6;
}

p {
  text-align: justify;
}

.installation-guide {
  max-width: 800px;
  margin: 0 auto;
  margin-top: 100px;

  font-size: 18px;
  line-height: 1.6;
}

img {
  max-width: 100%;
  height: auto;
  display: block;
  margin-bottom: 1em;
}

.installation-guide h1,
.installation-guide h2 {
  margin-bottom: 1em;
}

.installation-guide pre {
  background-color: #f8f8f8;
  border: 1px solid #ddd;
  padding: 1em;
  overflow: auto;
}

.introduction {
  max-width: 800px;
  margin: 0 auto;
  margin-top: 100px;

  font-size: 18px;
  line-height: 1.6;
}

.introduction h1 {
  font-size: 2em;
  margin-bottom: 1em;
}
</style>
