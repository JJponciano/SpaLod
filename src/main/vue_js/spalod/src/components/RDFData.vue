<template>
    <div class="rdf-data" :class="{ dark: isDarkMode }" v-if="rdfView">
        <div class="header">
            <div class="title">
                <h2 :class="{ selected: rdfView }" @click="rdfView = true">RDF Data</h2>
                <h2> | </h2>
                <h2 :class="{ selected: !rdfView }" @click="rdfView = false">Query Result</h2>
            </div>
            <div v-if="selectedTriplets.length > 0">
                <button @click="removeSelected">Remove Selected</button>
                <button @click="addSelected" class="add-selected">Add Selected</button>
            </div>
            <div v-if="rdfData && rdfData.length > 0" class="download">
                <button @click="downloadGeoJSON">Download GeoJSON</button>
                <button class="download-button" @click="downloadOwl">Download Owl</button>
            </div>
        </div>
        <button @click="refreshMap" class="refresh" v-if="rdfData && rdfData.length > 0">Refresh Map</button>
        <div class="metadata" :class="{ active: showMetadata }" v-if="rdfData && rdfData.length > 0">
            <p @click="showMetadata = !showMetadata">Show Metadata</p>
            <div class="metadata-container" v-if="showMetadata">
                    <div class="metadata-Catalog">
                        <p>Catalog: *</p>
                        <button @click="addNewCatalog">+</button>
                        <select v-model="selectedOption" v-bind:disabled="isCatalogDisabled">
                            <option value="" disabled selected hidden>Choose a Catalog</option>
                            <option v-for="(option) in options">
                                {{ option.name }}
                            </option>
                        </select>
                    </div>
                <div v-for="(queryable, index) in queryables " :key="index" class="metadata-element">
                    <h3 v-if="queryable.required">{{ queryable.q }}: *</h3>
                    <h3 v-else>{{ queryable.q }}:</h3>
                    <div class="metadata-input">
                        <input type="text" v-model="metadata[queryable.q]" class="metadata-textbox" :placeholder="queryable.d" @focus="$event.target.select()" @input="queryable.v = false" spellcheck="false">
                        <button :id="queryable.q" class="validate" @click="validateMetadata(queryable.q)" :class="{ added: queryable.v }" :disabled="queryable.q !== 'identifier' && !queryables[0].v" v-show="queryable.q === 'identifier' || queryables[0].v">{{ queryable.v ? 'Validated' : 'Validate' }}</button>
                    </div>
                </div>
            </div>
        </div>
        <div class="select-all" v-if="rdfData && rdfData.length > 0">
            <input type="checkbox" v-model="areAllSelected" @change="selectAll(areAllSelected)"/>
            <h3>Select all</h3>
        </div>
        <p v-for="(triplet, index) in rdfData" :key="triplet.id">
            <input type="checkbox" v-model="selectedTriplets" :value="triplet" />
            <input type="text" v-model="triplet.subject" class="subject" />
            <input type="text" v-model="triplet.predicate" class="predicate"
                :class="{ unknown: unkownPredicates.includes(triplet.predicate) }"
                @input="filterResults(triplet.predicate, index)" @focus="activeInput = index" :title="unkownPredicates.includes(triplet.predicate)
                        ? 'Unknown predicate: Please add it manually by specifying the type'
                        : null" />
        <ul v-if="activeInput === index" class="autocomplete-results">
            <button @click="() => activeInput = null">Close</button>
            <div class="custom-predicate">
                <h2>Custom predicate</h2>
                <input type="radio" id="datatype-property" value="DatatypeProperty" v-model="picked">
                <label for="datatype-property">DatatypeProperty</label>
                <input type="radio" id="object-property" value="ObjectProperty" v-model="picked">
                <label for="object-property">ObjectProperty</label>
                <br>
            </div>
            <div v-if="showResults">
                <h2 v-if="filteredResults.length > 0">Predicate Options</h2>
                <li v-for="(result, i) in filteredResults" :key="i" @click="selectResult(result, index)">
                    {{ decodeURIComponent(result.split('#')[1]).replace(/ /g,"").replace(/-/g,"") }}
                </li>
            </div>
        </ul>
        <input type="text" v-model="triplet.object" class="object" />
        <button class="delete-button" @click="deleteTriplet(index)">Delete</button>
        <button :id="'btn' + index" class="add-button" @click="addTriplet(triplet, index)">Add</button>
        <br v-if="rdfData[index + 1] && rdfData[index + 1].subject !== rdfData[index].subject">
        <br v-if="rdfData[index + 1] && rdfData[index + 1].subject !== rdfData[index].subject">
        </p>
    </div>
    <div class="rdf-data" :class="{ dark: isDarkMode }" v-else>
        <div class="header">
            <div class="title">
                <h2 :class="{ selected: rdfView }" @click="rdfView = true">RDF Data</h2>
                <h2> | </h2>
                <h2 :class="{ selected: !rdfView }" @click="rdfView = false">Query Result</h2>
            </div>
        </div>
        <table>
          <thead>
            <tr>
              <th v-for="key in keys">{{ key }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(result, index) in queryResult" :key="index">
              <td v-for="key in keys" @click="uriClick(result[key], key)" :class="{ clickable: key === 'collections' || key === 'dataset' || key === 'conformance' || key === 'URL' }">
                  <template v-if="key === 'JSON'">
                    <button @click="downloadJson(result.JSON, result.Feature)">DOWNLOAD JSON</button>
                  </template>
                  <template v-else-if="key === 'HTML'">
                    <button @click="uriClick(result[key], key)" class="view-html-button">VIEW HTML</button>
                  </template>
                  <template v-else>
                    {{ result[key] }}
                  </template>
              </td>
            </tr>
          </tbody>
        </table>
    </div>
</template>

<script>
import $ from 'jquery';
import {reactive, onBeforeMount } from 'vue';
$.ajaxSetup({
    xhrFields: {
        withCredentials: true
    }
});




export default {
    // setup(){
    //     console.log(localStorage.getItem("username"))
    //     onBeforeMount(async ()=> {
    //     await $.ajax({
    //         url: 'https://localhost:8081/getGitUser',
    //         method: 'GET',
    //         xhrFields: {
    //             withCredentials: true
    //         },
            // success: (response) => {
            //     localStorage.setItem("username",response);
            // },
            // error: (error) => {
            //     console.error(error);
            // }
            // })
    // });
    // },
    props: {
        file: File,
        receivedData: {
            type: Object,
            default: null,
        },
        username:String
    },
    watch: {
        file(newFile) {
            this.processContent(newFile);
        },
        receivedData(newCatalog){
            this.options.push(newCatalog);
        },
        username(newUsername){
            this.metadata["publisher"]=newUsername;
        }
    },
    data() {
        return {
            name: "",
            isDarkMode: false,
            rdfData: [],
            predicateOptions: [],
            filteredResults: [],
            selectedTriplets: [],
            unkownPredicates: [],
            queryResult: [],
            metadata: [],
            selectedOption :'',
            options: [],
            isCatalogDisabled: false,
            queryables: [
                {q: 'identifier', required: true, d: 'The unique identifier of the dataset', v: false, p: 'http://purl.org/dc/terms/identifier', literal: true},
                {q: 'title', required: true, d: 'The name given to the resource', v: false, p: 'http://purl.org/dc/terms/title', literal: true},
                {q: 'description', required: true, d: 'Description of the resource', v: false, p: 'http://purl.org/dc/terms/description', literal: true},
                {q: 'distribution', required: true, d: 'The url of the distribution', v: false, p: 'http://www.w3.org/ns/dcat#distribution', literal: false},
                {q: 'publisher', required: true, d: 'Entity making the resource available', v: false, p: 'http://purl.org/dc/terms/publisher', literal: false},
                {q: 'keywords', required: false, d: "Tags separated by ','", v: false, p: 'http://www.w3.org/ns/dcat#keyword', literal: true},
                {q: 'theme', required: false, d: "Categories separated by ','", v: false, p: 'http://www.w3.org/ns/dcat#theme', literal: false},
                {q: 'type', required: false, d: 'The nature or genre of the resource', v: false, p: 'http://purl.org/dc/terms/type', literal: false},
                {q: 'contactPoint', required: false, d: 'An entity to contact', v: false, p: 'http://www.w3.org/ns/dcat#contactPoint', literal: false},
                {q: 'spatial', required: false, d: 'Spatial area or designed place', v: false, p: 'http://www.w3.org/ns/dcat#spatial', literal: false},
                {q: 'temporal', required: false, d: 'Time interval', v: false, p: 'http://purl.org/dc/terms/temporal', literal: false},
                {q: 'issued', required: false, d: 'The date the dataset was created', v: false, p: 'http://purl.org/dc/terms/issued', literal: true},
                {q: 'modified', required: false, d: 'The date the dataset was updated', v: false, p: 'http://purl.org/dc/terms/modified', literal: true},
                {q: 'language', required: false, d: 'Language of the resource', v: false, p: 'http://purl.org/dc/terms/language', literal: false},
                {q: 'landingPage', required: false, d: 'Links to other resources', v: false, p: 'http://www.w3.org/ns/dcat#landingPage', literal: true},
                {q: 'politicalGeocodingLevelURI', required: false, d: 'Geopolitical coverage of the dataset', v: false, p: 'http://dcat-ap.de/def/dcatde/politicalGeocodingLevelURI', literal: true},
                {q: 'politicalGeocodingURI', required: false, d: 'URI of the administrative area', v: false, p: 'http://dcat-ap.de/def/dcatde/politicalGeocodingURI', literal: true},
                {q: 'availability', required: false, d: 'Planned availability of the dataset', v: false, p: 'http://data.europa.eu/r5r/availability', literal: false},
                {q: 'contributorID', required: false, d: 'The identifier of the contributor', v: false, p: 'http://dcat-ap.de/def/dcatde/contributorID', literal: false},
                {q: 'geocodingDescription', required: false, d: 'Description of the geocoding', v: false, p: 'http://dcat-ap.de/def/dcatde/geocodingDescription', literal: true},
                {q: 'versionInfo', required: false, d: 'Version number of the dataset', v: false, p: 'http://www.w3.org/2002/07/owl#versionInfo', literal: true},
                {q: 'versionNotes', required: false, d: 'Notes on the version of the dataset', v: false, p: 'http://www.w3.org/ns/adms#versionNotes', literal: true},
                {q: 'legalBasis', required: false, d: 'Legal basis for the dataset', v: false, p: 'http://dcat-ap.de/def/dcatde/legalBasis', literal: true},
                {q: 'relation', required: false, d: 'Related resource', v: false, p: 'http://purl.org/dc/terms/relation', literal: false},
                {q: 'page', required: false, d: 'Page reference', v: false, p: 'http://xmlns.com/foaf/0.1/page', literal: false},
                {q: 'conformsTo', required: false, d: 'Standard the dataset conforms to', v: false, p: 'http://purl.org/dc/terms/conformsTo', literal: false},
                {q: 'accessRights', required: false, d: 'Who can access the dataset', v: false, p: 'http://purl.org/dc/terms/accessRights', literal: false},
                {q: 'provenance', required: false, d: 'Development history of the dataset', v: false, p: 'http://purl.org/dc/terms/provenance', literal: false},
                {q: 'accrualPeriodicity', required: false, d: 'Frequency of dataset updates', v: false, p: 'http://purl.org/dc/terms/accrualPeriodicity', literal: false},
                {q: 'qualityProcessURI', required: false, d: 'URI of the quality process', v: false, p: 'http://dcat-ap.de/def/dcatde/qualityProcessURI', literal: true},
                {q: 'wasGeneratedBy', required: false, d: 'Process that generated the dataset', v: false, p: 'http://www.w3.org/ns/prov#wasGeneratedBy', literal: false},
                {q: 'spatialResolutionInMeters', required: false, d: 'Spatial resolution of the dataset', v: false, p: 'http://www.w3.org/ns/dcat#spatialResolutionInMeters', literal: true},
                {q: 'temporalResolution', required: false, d: 'Temporal resolution of the dataset', v: false, p: 'http://www.w3.org/ns/dcat#temporalResolution', literal: true},
                {q: 'granularity', required: false, d: 'Granularity of the dataset', v: false, p: 'http://www.w3.org/ns/dcat#granularity', literal: false},
                {q: 'qualifiedAttribution', required: false, d: 'Attribution of the dataset', v: false, p: 'http://www.w3.org/ns/prov#qualifiedAttribution', literal: false},
                {q: 'qualifiedRelation', required: false, d: 'Relation of the dataset', v: false, p: 'http://www.w3.org/ns/dcat#qualifiedRelation', literal: false},
                {q: 'isReferencedBy', required: false, d: 'What references the dataset', v: false, p: 'http://purl.org/dc/terms/isReferencedBy', literal: false},
                {q: 'references', required: false, d: 'What is referenced by the dataset', v: false, p: 'http://purl.org/dc/terms/references', literal: false},
                {q: 'source', required: false, d: 'Source of the dataset', v: false, p: 'http://purl.org/dc/terms/source', literal: false},
                {q: 'isVersionOf', required: false, d: 'Resource derivated by the dataset', v: false, p: 'http://purl.org/dc/terms/isVersionOf', literal: false},
                {q: 'sample', required: false, d: 'Sample of the dataset', v: false, p: 'http://www.w3.org/ns/adms#sample', literal: false},
                {q: 'creator', required: false, d: 'Creator of the dataset', v: false, p: 'http://purl.org/dc/terms/creator', literal: false},
                {q: 'contributor', required: false, d: 'Contributor to the dataset', v: false, p: 'http://purl.org/dc/terms/contributor', literal: false},
                {q: 'originator', required: false, d: 'People who own the copyright', v: false, p: 'http://dcat-ap.de/def/dcatde/originator', literal: false},
                {q: 'maintainer', required: false, d: 'People who maintain the dataset', v: false, p: 'http://dcat-ap.de/def/dcatde/maintainer', literal: false},
            ],
            showResults: false,
            activeInput: null,
            picked: "DatatypeProperty",
            rdfView: true,
            showMetadata: false,
            uid: null,
            inputCatalog: '',
            placeholdersC: 'New Catalog',
        };
    },
    mounted() {
        this.detectDarkMode();
        window.matchMedia('(prefers-color-scheme: dark)').addListener(event => {
            this.isDarkMode = event.matches;
        });
        this.loadPredicates();
        this.metadata['identifier'] = this.uuidv4();

        const fetchData = async () => {
            if(localStorage.getItem("githubLog")==null)
            {
               try {
                    const response = await $.ajax({
                        url: 'https://localhost:8081/getGitUser',
                        method: 'GET',
                        xhrFields: {
                            withCredentials: true
                        }
                    });

                    this.uid = response
                    this.metadata["publisher"] = this.getUsername(); 

                } catch (error) {
                    console.error(error);
                } 
            }
            else
            {
                this.uid=localStorage.getItem("uuid")
                this.metadata["publisher"] = localStorage.getItem("username")
            }

            var today = new Date();
                    var dd = String(today.getDate()).padStart(2, '0');
                    var mm = String(today.getMonth() + 1).padStart(2, '0');
                    var yyyy = today.getFullYear();
                    this.metadata['created'] = dd + '/' + mm + '/' + yyyy;
                    this.metadata['updated'] = dd + '/' + mm + '/' + yyyy;

            const data = {
                query: 'SELECT ?catalog ?title ?description ?publisher ?dataset where {?catalog <http://www.w3.org/ns/dcat#dataset> ?dataset . ?catalog <http://purl.org/dc/terms/title> ?title . ?collection <http://purl.org/dc/terms/description> ?description . ?collection <http://purl.org/dc/terms/publisher> ?publisher . ?collection <http://www.w3.org/ns/dcat#dataset> ?dataset .}',
                triplestore: "http://localhost:7200/repositories/Spalod", // TODO: graph DB
            };
            $.ajax({
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                'type': 'POST',
                'url': 'https://localhost:8081/api/sparql-select',
                'data': JSON.stringify(data),
                'dataType': 'json',
                success: (data) => {
                    if (data.results.bindings.length > 0) {
                        for (var i = 0; i < data.results.bindings.length; i++) {
                            var catalog = {
                                name: data.results.bindings[i].title.value,
                                desc: data.results.bindings[i].description.value,
                                id: data.results.bindings[i].catalog.value.split('#')[1],
                                publisher: data.results.bindings[i].publisher.value
                            }
                            this.options.push(catalog);
                        }
                    }
                }
            });
        };

        fetchData();

        // Check if the predicates are known in the ontology
        const checkPredicates = {
            query: 'SELECT ?type WHERE { <' + this.queryables.find(queryable => queryable.q === "identifier").p + '> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type }',
            triplestore: 'http://localhost:7200/repositories/Spalod', 
        };
        $.ajax({
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            'type': 'POST',
            'url': 'https://localhost:8081/api/sparql-select',
            'data': JSON.stringify(checkPredicates),
            'dataType': 'json',
            success: (result) => {
                if (result.results.bindings.length === 0) {
                    var data = {
                        subject: "http://www.w3.org/ns/dcat#dataset",
                        predicate: "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                        object: "http://www.w3.org/2002/07/owl#ObjectProperty"
                    };
                    this.updateTripleData(data, 'add', () => {
                        console.log("Predicate added");
                    });

                    var data = {
                        subject: "http://lab.ponciano.info/ont/spalod#hasItem",
                        predicate: "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                        object: "http://www.w3.org/2002/07/owl#ObjectProperty"
                    };
                    this.updateTripleData(data, 'add', () => {
                        console.log("Predicate added");
                    });

                    var data = {
                        subject: "http://xlmns.com/foaf/0.1/name",
                        predicate: "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                        object: "http://www.w3.org/2002/07/owl#DatatypeProperty"
                    };
                    this.updateTripleData(data, 'add', () => {
                        console.log("Predicate added");
                    });

                    this.queryables.forEach(queryable => {
                        var object = queryable.literal ? "http://www.w3.org/2002/07/owl#DatatypeProperty" : "http://www.w3.org/2002/07/owl#ObjectProperty";
                        data = {
                            subject: queryable.p,
                            predicate: "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                            object: object,
                        };
                        this.updateTripleData(data, 'add', () => {
                            console.log("Predicate added");
                        });
                    });
                }
            }
        });
        

        // Implementing OGC API - Records
        const url = new URL(window.location.href);
        var queryString = url.pathname;
        if (queryString === '/' || queryString.includes('collections') || queryString.includes('conformance')) {
            this.rdfView = false;
        }
        if (queryString.includes('/items/')) {
            queryString = queryString.split('/items/')[1]
            if (queryString && queryString.length > 0) {
                this.queryables.forEach((queryable) => {
                    const data = {
                        query: 'SELECT ?' + queryable.q + ' WHERE { <http://lab.ponciano.info/ont/spalod#' + queryString + '> <' + queryable.p + '> ?' + queryable.q + ' }',
                        triplestore: "http://localhost:7200/repositories/Spalod", 
                    };
                    $.ajax({
                        headers: {
                            'Accept': 'application/json',
                            'Content-Type': 'application/json'
                        },
                        'type': 'POST',
                        'url': 'https://localhost:8081/api/sparql-select',
                        'data': JSON.stringify(data),
                        'dataType': 'json',
                        success: (data) => {
                            if (data.results.bindings.length > 0) {
                                var result = data.results.bindings[0][queryable.q].value;
                                result = result.split('/')[result.split('/').length - 1];
                                result = result.split('#')[result.split('#').length - 1];
                                result = result.replace(/_/g, ' ');
                                this.metadata[queryable.q] = result;
                            }
                        },
                        error: (error) => {
                            console.log(error)
                        }
                    });
                });
                const data = {
                    query: 'SELECT ?title WHERE { ?catalog <http://www.w3.org/ns/dcat#dataset> <http://lab.ponciano.info/ont/spalod#' + queryString + '> . ?catalog <http://purl.org/dc/terms/title> ?title . }',
                    triplestore: '', // TODO: graph DB
                };
                $.ajax({
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    'type': 'POST',
                    'url': 'https://localhost:8081/api/sparql-select',
                    'data': JSON.stringify(data),
                    'dataType': 'json',
                    success: (data) => {
                        if (data.results.bindings.length > 0) {
                            var result = data.results.bindings[0].title.value;
                            this.selectedOption = result;
                        }
                    }
                });
            }
        }
    },
    computed: {
        keys() {
            if (this.queryResult.length > 0)
                return Object.keys(this.queryResult[0])
        }
    },
    methods: {
        getUsername()
        {
            const data = {
                query:'SELECT ?s ?p ?o WHERE {?s ?p ?o. FILTER (?s=<http://lab.ponciano.info/ont/spalod#'+this.uid+'>)}',
                triplestore: "http://localhost:7200/repositories/Spalod"
            };
            $.ajax({
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                'type': 'POST',
                'url': 'https://localhost:8081/api/sparql-select',
                'data': JSON.stringify(data),
                'dataType': 'json',
                success: (response) => {
                    const parts = response.results.bindings[0].o.value.split('/');
                    const lastPart = parts[parts.length - 1];
                    return lastPart;
                },
                error:(error) => {
                    console.log(error)
                }
            });

        },
        uuidv4() {
            return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
                (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
            );
        },
        areAllPredicatesKnown() {
            this.unkownPredicates = [];
            this.rdfData.forEach((triplet) => {
                const predicate = "http://lab.ponciano.info/ont/spalod#" + triplet.predicate;
                if (!this.predicateOptions.includes(predicate)) {
                    if (!this.unkownPredicates.includes(triplet.predicate)) {
                        this.unkownPredicates.push(triplet.predicate);
                    }
                }
            });
            return this.unkownPredicates.length === 0;
        },
        loadPredicates() {
            const data = {
                query: 'SELECT ?property ?propertyType WHERE{{?property a owl:ObjectProperty . BIND("Object Property" AS ?propertyType)} UNION {?property a owl:DatatypeProperty . BIND("Data Property" AS ?propertyType)}} ORDER BY ?property',
                triplestore: "http://localhost:7200/repositories/Spalod" //intégrer graph DB ici
            };
            $.ajax({
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                'type': 'POST',
                'url': 'https://localhost:8081/api/sparql-select',
                'data': JSON.stringify(data),
                'dataType': 'json',
                success: (data) => {
                    this.predicateOptions = data.results.bindings.map(binding => binding.property.value);
                    console.log(this.predicateOptions)
                }
            });
            this.areAllPredicatesKnown();
        },
        detectDarkMode() {
            this.isDarkMode = window.matchMedia('(prefers-color-scheme: dark)').matches;
        },
        processContent(file) {
            this.rdfData = [];
            const fileReader = new FileReader();
            fileReader.readAsText(file);
            fileReader.onload = () => {
                const geoJson = JSON.parse(fileReader.result);
                this.name = geoJson.name;
                geoJson.features.forEach(feature => {
                    const properties = feature.properties;
                    const subject = properties['itemID'] ?? 'http://lab.ponciano.info/ont/spalod#' + this.uuidv4();
                    for (const key in properties) {
                        if (key === 'Koordinate' || key === 'itemID') continue;
                        const predicate = encodeURIComponent(key.replace(/ /g,"").replace(/-/g,""));
                        const object = properties[key];
                        this.rdfData.push({
                            subject,
                            predicate,
                            object,
                        });
                    }

                    var coordinates = feature.properties.Koordinate ?? feature.geometry.coordinates;
                    if (coordinates.length > 0) {
                        if (coordinates.includes('(')) {
                            coordinates = coordinates.split('(')[1];
                            coordinates = coordinates.split(')')[0];
                            coordinates = coordinates.split(' ').map(coord => parseFloat(coord));
                        }
                        const predicate = 'coordinates';
                        const object = coordinates[0] + ', ' + coordinates[1];
                        this.rdfData.push({
                            subject,
                            predicate,
                            object,
                        });
                    } else if (feature.properties['X Koordina'] && feature.properties['Y Koordina']) {
                        const predicate = 'coordinates';
                        const object = feature.properties['X Koordina'] + ', ' + feature.properties['Y Koordina'];
                        this.rdfData.push({
                            subject,
                            predicate,
                            object,
                        });
                    }
                });
                this.processQueryResult(geoJson);
                this.areAllPredicatesKnown();
            };
        },
        refreshMap(){
            var geojones=this.getGeoJSON(); //le fromage
            const blob = new Blob([JSON.stringify(geojones)], {type: "application/json"});
            const updatefile = new File([blob], "geodata.json", {type: "application/json"});
            this.$emit('update', updatefile);
        },
        addNewCatalog(){
            this.$emit('popupCShow');
        },
        processQueryResult(geoJson) {
            this.queryResult = [];
            geoJson.features.forEach(feature => {
                if (feature.geometry.coordinates.length === 0) {
                    this.queryResult.push(feature.properties);
                } else {
                    var newJson = feature.properties;
                    newJson.coordinates = feature.geometry.coordinates;
                    this.queryResult.push(newJson);
                }
            })
        },
        deleteTriplet(index) {
            if (confirm("Are you sure you want to delete this triplet?")) {
                this.rdfData.splice(index, 1);
            }
        },
        addTriplet(triplet, index) {
            if (!this.validateForm()) {
                alert('Please validate the metadata before adding new triplets');
                return;
            }

            const predicate = "http://lab.ponciano.info/ont/spalod#" + triplet.predicate;

            // Add the new predicate
            this.loadPredicates();
            if (!this.predicateOptions.includes(predicate)) {
                if (predicate === "http://lab.ponciano.info/ont/spalod#coordinates") {
                    var tripleData = {
                        subject: "http://lab.ponciano.info/ont/spalod#longitude",
                        predicate: "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                        object: "http://www.w3.org/2002/07/owl#DatatypeProperty",
                    };
                    this.updateTripleData(tripleData, 'add', () => {
                        self.predicateOptions.push("http://lab.ponciano.info/ont/spalod#longitude");
                    });
                    tripleData = {
                        subject: "http://lab.ponciano.info/ont/spalod#latitude",
                        predicate: "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                        object: "http://www.w3.org/2002/07/owl#DatatypeProperty",
                    };
                    this.updateTripleData(tripleData, 'add', () => {
                        self.predicateOptions.push("http://lab.ponciano.info/ont/spalod#latitude");
                    });
                }
                tripleData = {
                    subject: predicate,
                    predicate: "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                    object: "http://www.w3.org/2002/07/owl#" + this.picked,
                };
                var self = this;
                this.updateTripleData(tripleData, 'add', () => {
                    self.predicateOptions.push(predicate);
                    self.areAllPredicatesKnown();
                    // Delete the old triplet
                    const data = {
                        query: 'SELECT ?o WHERE{?s <' + predicate + '> ?o . FILTER(?s = <' + String(triplet.subject).replace(/ /g, '_') + '>)}',
                        triplestore: "http://localhost:7200/repositories/Spalod"
                    };
                    $.ajax({
                        headers: {
                            'Accept': 'application/json',
                            'Content-Type': 'application/json'
                        },
                        'type': 'POST',
                        'url': 'https://localhost:8081/api/sparql-select',
                        'data': JSON.stringify(data),
                        'dataType': 'json',
                        success: (data) => {
                            if (data.results.bindings.length > 0) {
                                const object = data.results.bindings[0].o.value
                                const tripleData = {
                                    subject: triplet.subject.replace(/ /g, '_'),
                                    predicate: predicate,
                                    object: object,
                                };
                                this.updateTripleData(tripleData, 'remove', () => {
                                    console.log(JSON.stringify(tripleData) + ' removed');
                                });
                            }
                        },
                        error: function (error) {
                            console.log(error);
                        }
                    });

                    // Add the new triplet
                    if (triplet.predicate === 'coordinates') {
                        var [longitude, latitude] = triplet.object.split(',');
                        tripleData = {
                            subject: triplet.subject.replace(/ /g, '_'),
                            predicate: "http://lab.ponciano.info/ont/spalod#longitude",
                            object: parseFloat(longitude),
                        };
                        this.updateTripleData(tripleData, 'add', () => {
                            console.log(JSON.stringify(tripleData) + ' added');
                        });
                        tripleData = {
                            subject: triplet.subject.replace(/ /g, '_'),
                            predicate: "http://lab.ponciano.info/ont/spalod#latitude",
                            object: parseFloat(latitude),
                        };
                        this.updateTripleData(tripleData, 'add', () => {
                            console.log(JSON.stringify(tripleData) + ' added');
                        });
                        tripleData = {
                            subject: triplet.subject.replace(/ /g, '_'),
                            predicate: predicate,
                            object: triplet.object.replace(/ /g, '_')
                        };
                    } else {
                        tripleData = {
                            subject: triplet.subject.replace(/ /g, '_'),
                            predicate: predicate,
                            object: encodeURIComponent(triplet.object).replace(/%20/g, "_")
                        };
                    }
                    this.updateTripleData(tripleData, 'add', () => {
                        $('#btn' + index).text('Added').addClass('added');
                        console.log(JSON.stringify(tripleData) + ' added');
                    });

                    tripleData = {
                        subject: 'http://lab.ponciano.info/ont/spalod#' + this.metadata.identifier,
                        predicate: 'http://lab.ponciano.info/ont/spalod#hasItem',
                        object: triplet.subject.replace(/ /g, '_')
                    };
                    this.updateTripleData(tripleData, 'add', () => {
                        console.log(JSON.stringify(tripleData) + ' linked to the dataset');
                    });
                });
            } else {
                // Delete the old triplet
                const data = {
                    query: 'SELECT ?o WHERE{?s <' + predicate + '> ?o . FILTER(?s = <' + String(triplet.subject).replace(/ /g, '_') + '>)}',
                    triplestore: "http://localhost:7200/repositories/Spalod"
                };
                $.ajax({
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    'type': 'POST',
                    'url': 'https://localhost:8081/api/sparql-select',
                    'data': JSON.stringify(data),
                    'dataType': 'json',
                    success: (data) => {
                        if (data.results.bindings.length > 0) {
                            const object = data.results.bindings[0].o.value
                            const tripleData = {
                                subject: triplet.subject.replace(/ /g, '_'),
                                predicate: predicate,
                                object: object,
                            };
                            this.updateTripleData(tripleData, 'remove', () => {
                                console.log(JSON.stringify(tripleData) + ' removed');
                            });
                        }
                    },
                    error: function (error) {
                        console.log(error);
                    }
                });

                // Add the new triplet
                if (triplet.predicate === 'coordinates') {
                        var [longitude, latitude] = triplet.object.split(',');
                        tripleData = {
                            subject: triplet.subject.replace(/ /g, '_'),
                            predicate: "http://lab.ponciano.info/ont/spalod#longitude",
                            object: parseFloat(longitude),
                        };
                        this.updateTripleData(tripleData, 'add', () => {
                            console.log(JSON.stringify(tripleData) + ' added');
                        });
                        tripleData = {
                            subject: triplet.subject.replace(/ /g, '_'),
                            predicate: "http://lab.ponciano.info/ont/spalod#latitude",
                            object: parseFloat(latitude),
                        };
                        this.updateTripleData(tripleData, 'add', () => {
                            console.log(JSON.stringify(tripleData) + ' added');
                        });
                        tripleData = {
                            subject: triplet.subject.replace(/ /g, '_'),
                            predicate: predicate,
                            object: triplet.object.replace(/ /g, '_')
                        };
                    } else {
                        tripleData = {
                            subject: triplet.subject.replace(/ /g, '_'),
                            predicate: predicate,
                            object: encodeURIComponent(triplet.object).replace(/%20/g, "_")
                        };
                    }
                    this.updateTripleData(tripleData, 'add', () => {
                        $('#btn' + index).text('Added').addClass('added');
                        console.log(JSON.stringify(tripleData) + ' added');
                    }); 

                tripleData = {
                    subject: 'http://lab.ponciano.info/ont/spalod#' + this.metadata.identifier,
                    predicate: 'http://lab.ponciano.info/ont/spalod#hasItem',
                    object: triplet.subject.replace(/ /g, '_')
                };
                this.updateTripleData(tripleData, 'add', () => {
                    console.log(JSON.stringify(tripleData) + ' linked to the dataset');
                });
            }
        },
        filterResults(predicate, index) {
            this.activeInput = index;
            this.filteredResults = this.predicateOptions.filter((result) => result.toLowerCase().includes(predicate.toLowerCase()));
            this.showResults = true;
            this.areAllPredicatesKnown();
        },
        selectResult(result, index) {
            this.rdfData[index].predicate = decodeURIComponent(result.split('#')[1]).replace(/ /g,"").replace(/-/g,"");
            this.showResults = false;
        },
        selectAll(areAllSelected) {
            if (areAllSelected) {
                this.selectedTriplets = this.rdfData;
            } else {
                this.selectedTriplets = [];
            }
        },
        addSelected() {
            this.loadPredicates();
            if (this.areAllPredicatesKnown()) {
                this.selectedTriplets.forEach((triplet) => {
                    const index = this.rdfData.findIndex((rdfTriplet) => {
                        return rdfTriplet === triplet;
                    });
                    this.addTriplet(triplet, index);
                });
                this.selectedTriplets = [];
            } else {
                alert("Some predicates are not known :\n" + this.unkownPredicates + "\nPlease add them to the ontology first.");
            }
        },
        removeSelected() {
            this.selectedTriplets.forEach((triplet) => {
                const index = this.rdfData.findIndex((rdfTriplet) => {
                    return rdfTriplet === triplet;
                });
                this.rdfData.splice(index, 1);
            });
            this.selectedTriplets = [];
        },
        getGeoJSON() {
            const geoJSON = {
                type: "FeatureCollection",
                name: this.name,
                features: [],
            };

            const subjects = [...new Set(this.rdfData.map((triplet) => triplet.subject))];

            subjects.forEach(subject => {
                const feature = {
                    type: "Feature",
                    geometry: {
                        type: "Point",
                        coordinates: [],
                    },
                    properties: {},
                };

                this.rdfData.forEach(triplet => {
                    if (triplet.subject === subject) {
                        if (triplet.predicate === "coordinates") {
                            const [longitude, latitude] = triplet.object.split(',');
                            feature.geometry.coordinates = [parseFloat(longitude), parseFloat(latitude)];
                        } else {
                            feature.properties[triplet.predicate] = triplet.object;
                        }
                        feature.properties["item"] = subject;
                    }
                });

                if (feature.geometry.coordinates.length > 0) {
                    geoJSON.features.push(feature);
                }
            });
            return geoJSON;
        },
        downloadGeoJSON() {
            const geoJSON = JSON.stringify(this.getGeoJSON());
            const url = window.URL.createObjectURL(new Blob([geoJSON]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', "geodata.json");
            document.body.appendChild(link);
            link.click();
        },
        downloadOwl() {
            const geoJSON = this.getGeoJSON();
            const blob = new Blob([JSON.stringify(geoJSON)], { type: "application/json" });
            const file = new File([blob], "geodata.json", { type: "application/json" });
            if (file) {
                let formData = new FormData();
                formData.append('file', file);
                $.ajax({
                    url: 'https://localhost:8081/api/uplift',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (response) {
                        $.ajax({
                            url: `https://localhost:8081/download/data/${response}`,
                            method: 'GET',
                            xhrFields: {
                                responseType: 'blob',
                            },
                            success(response) {
                                console.log(response);
                                const url = window.URL.createObjectURL(new Blob([response]));
                                const link = document.createElement('a');
                                link.href = url;
                                link.setAttribute('download', "Spalod.owl");
                                document.body.appendChild(link);
                                link.click();
                            },
                            error(xhr, status, error) {
                                console.error(`Erreur lors du téléchargement du fichier : ${error}`);
                            },
                        });
                    },
                    error: function (error) {
                        console.log(error);
                    }
                });
            }
        },
        downloadJson(url, feature) {
            console.log(url);
            $.ajax({
                headers: {
                    'Content-Type': 'application/json'
                },
                url: url,
                type: 'POST',
                dataType: 'json',
                success: (response) => {
                    const json = JSON.stringify(response);
                    const url = window.URL.createObjectURL(new Blob([json]));
                    const link = document.createElement('a');
                    link.href = url;
                    link.setAttribute('download', feature + ".json");
                    document.body.appendChild(link);
                    link.click();
                },
                error: (error) => {
                    console.log(error);
                }
            });
        },
        validateForm() {
            const requiredQueryables = this.queryables.filter(queryable => queryable.required);
            const invalidQueryables = requiredQueryables.filter(queryable => queryable.v === false);

            return invalidQueryables.length === 0;
        },
        validateMetadata(data) {
            var queryable = this.queryables.find(queryable => queryable.q === data);
            if(this.metadata[queryable.q] !== '' && this.metadata[queryable.q] !== undefined && this.selectedOption !== '') {
                if(queryable.q === 'identifier') {
                    var data = {
                        subject: "http://lab.ponciano.info/ont/spalod#" + this.options.find(option => option.name === this.selectedOption).id,
                        predicate: this.queryables.find(queryable => queryable.q === 'title').p,
                        object: String(this.options.find(option => option.name === this.selectedOption).name),
                    };
                    this.updateTripleData(data, 'add', () => {
                        console.log("Catalog title added");
                    });
                    data = {
                        subject: "http://lab.ponciano.info/ont/spalod#" + this.options.find(option => option.name === this.selectedOption).id,
                        predicate: this.queryables.find(queryable => queryable.q === 'description').p,
                        object: this.options.find(option => option.name === this.selectedOption).desc,
                    };
                    this.updateTripleData(data, 'add', () => {
                        console.log("Catalog description added");
                    });
                    data = {
                        subject: "http://lab.ponciano.info/ont/spalod#" + this.options.find(option => option.name === this.selectedOption).id,
                        predicate: this.queryables.find(queryable => queryable.q === 'publisher').p,
                        object: this.options.find(option => option.name === this.selectedOption).publisher.startsWith("http://lab.ponciano.info/ont/spalod#") ? this.options.find(option => option.name === this.selectedOption).publisher : "http://lab.ponciano.info/ont/spalod#" + this.options.find(option => option.name === this.selectedOption).publisher,
                    };
                    this.updateTripleData(data, 'add', () => {
                        console.log("Catalog publisher added");
                    });
                    data = {
                        subject: "http://lab.ponciano.info/ont/spalod#" + this.options.find(option => option.name === this.selectedOption).id,
                        predicate: "http://www.w3.org/ns/dcat#dataset",
                        object: "http://lab.ponciano.info/ont/spalod#" + this.metadata['identifier'],
                    };
                    this.updateTripleData(data, 'add', () => {
                        console.log("Catalog added");
                        this.isCatalogDisabled = true;
                    });
                }
                if (queryable.q === 'publisher') {
                    // Delete the old triplets
                    var data = {
                        query: 'SELECT ?o WHERE{?s <' + queryable.p + '> ?o . FILTER(?s = <' + 'http://lab.ponciano.info/ont/spalod#' + this.metadata.identifier + '>)}',
                        triplestore: "http://localhost:7200/repositories/Spalod"
                    };
                    $.ajax({
                        headers: {
                            'Accept': 'application/json',
                            'Content-Type': 'application/json'
                        },
                        'type': 'POST',
                        'url': 'https://localhost:8081/api/sparql-select',
                        'data': JSON.stringify(data),
                        'dataType': 'json',
                        success: (data) => {
                            if (data.results.bindings.length > 0) {
                                const object = data.results.bindings[0].o.value
                                const tripleData = {
                                    subject: 'http://lab.ponciano.info/ont/spalod#' + this.metadata.identifier,
                                    predicate: queryable.p,
                                    object: object,
                                };
                                this.updateTripleData(tripleData, 'remove', () => {
                                    console.log('Triple removed');
                                });
                            }
                        },
                        error: function (error) {
                            console.log(error);
                        }
                    });

                    data = {
                        query: 'SELECT ?o WHERE{?s <http://xlmns.com/foaf/0.1/name> ?o . FILTER(?s = <' + 'http://lab.ponciano.info/ont/spalod#' + String(this.metadata[queryable.q]).replace(/ /g, '_') + '>)}',
                        triplestore: "http://localhost:7200/repositories/Spalod"
                    };
                    $.ajax({
                        headers: {
                            'Accept': 'application/json',
                            'Content-Type': 'application/json'
                        },
                        'type': 'POST',
                        'url': 'https://localhost:8081/api/sparql-select',
                        'data': JSON.stringify(data),
                        'dataType': 'json',
                        success: (data) => {
                            if (data.results.bindings.length > 0) {
                                const object = data.results.bindings[0].o.value
                                const tripleData = {
                                    subject: 'http://lab.ponciano.info/ont/spalod#' + String(this.metadata[queryable.q]).replace(/ /g, '_'),
                                    predicate: 'http://xlmns.com/foaf/0.1/name',
                                    object: object,
                                };
                                this.updateTripleData(tripleData, 'remove', () => {
                                    console.log('Triple removed');
                                });
                            }
                        },
                        error: function (error) {
                            console.log(error);
                        }
                    });

                    // Add the new triplets
                    var tripleData = {
                        subject: 'http://lab.ponciano.info/ont/spalod#' + this.metadata.identifier,
                        predicate: queryable.p,
                        object: 'http://lab.ponciano.info/ont/spalod#' + this.uid,
                    };
                    this.updateTripleData(tripleData, 'add', () => queryable.v = true);

                    tripleData = {
                        subject: 'http://lab.ponciano.info/ont/spalod#' + this.uid,
                        predicate: 'http://xlmns.com/foaf/0.1/name',
                        object: String(this.metadata[queryable.q]).replace(/ /g, '_'),
                    }
                    this.updateTripleData(tripleData, 'add', () => queryable.v = true);
                } else if (queryable.q === 'keywords' || queryable.q === 'theme') {
                    String(this.metadata[queryable.q]).split(',').forEach(keyword => {
                        // Add the new triplets
                        var tripleData = {
                            subject: 'http://lab.ponciano.info/ont/spalod#' + this.metadata.identifier,
                            predicate: queryable.p,
                            object: keyword,
                        };
                        this.updateTripleData(tripleData, 'add', () => queryable.v = true);
                    });
                } else if(queryable.q === 'issued' || queryable.q === 'modified') {
                    // Delete the old triplet
                    var data = {
                        query: 'SELECT ?o WHERE{?s <' + queryable.p + '> ?o . FILTER(?s = <' + 'http://lab.ponciano.info/ont/spalod#' + this.metadata.identifier + '>)}',
                        triplestore: "http://localhost:7200/repositories/Spalod"
                    };
                    $.ajax({
                        headers: {
                            'Accept': 'application/json',
                            'Content-Type': 'application/json'
                        },
                        'type': 'POST',
                        'url': 'https://localhost:8081/api/sparql-select',
                        'data': JSON.stringify(data),
                        'dataType': 'json',
                        success: (data) => {
                            if (data.results.bindings.length > 0) {
                                const object = data.results.bindings[0].o.value
                                const tripleData = {
                                    subject: 'http://lab.ponciano.info/ont/spalod#' + this.metadata.identifier,
                                    predicate: queryable.p,
                                    object: object,
                                };
                                this.updateTripleData(tripleData, 'remove', () => {
                                    console.log('Triple removed');
                                });
                            }
                        },
                        error: function (error) {
                            console.log(error);
                        }
                    });

                    // Add the new triplet
                    var tripleData = {
                        subject: 'http://lab.ponciano.info/ont/spalod#' + this.metadata.identifier,
                        predicate: queryable.p,
                        object: this.metadata[queryable.q] + '^^http://www.w3.org/2001/XMLSchema#dateTime',
                    };
                    this.updateTripleData(tripleData, 'add', () => queryable.v = true);
                } else {
                    // Delete the old triplet
                    var data = {
                        query: 'SELECT ?o WHERE{?s <' + queryable.p + '> ?o . FILTER(?s = <' + 'http://lab.ponciano.info/ont/spalod#' + this.metadata.identifier + '>)}',
                        triplestore: "http://localhost:7200/repositories/Spalod"
                    };
                    $.ajax({
                        headers: {
                            'Accept': 'application/json',
                            'Content-Type': 'application/json'
                        },
                        'type': 'POST',
                        'url': 'https://localhost:8081/api/sparql-select',
                        'data': JSON.stringify(data),
                        'dataType': 'json',
                        success: (data) => {
                            if (data.results.bindings.length > 0) {
                                const object = data.results.bindings[0].o.value
                                const tripleData = {
                                    subject: 'http://lab.ponciano.info/ont/spalod#' + this.metadata.identifier,
                                    predicate: queryable.p,
                                    object: object,
                                };
                                this.updateTripleData(tripleData, 'remove', () => {
                                    console.log('Triple removed');
                                });
                            }
                        },
                        error: function (error) {
                            console.log(error);
                        }
                    });

                    // Add the new triplet
                    var tripleData = {
                        subject: 'http://lab.ponciano.info/ont/spalod#' + this.metadata.identifier,
                        predicate: queryable.p,
                        /*object: queryable.literal ? String(this.metadata[queryable.q]).replace(/ /g, '_').normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/&/g, "&amp;").replace(/&/g, "&amp;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;") : 'http://lab.ponciano.info/ont/spalod#' + encodeURIComponent(String(this.metadata[queryable.q]).replace(/ /g, '_')),
                        */
                        object: encodeURIComponent(this.metadata[queryable.q]).replace(/%20/g,"_")
                    };
                    this.updateTripleData(tripleData, 'add', () => queryable.v = true);
                }
            } else {
                if(this.selectedOption === ''){
                    alert('Please select a Catalog for this Dataset');
                }
                else{
                    alert('Please enter a ' + queryable.q);
                }
            }
        },
        updateTripleData(tripleData, operation, callback) {
            const addOperation = {
                operation: operation,
                tripleData: tripleData,
            };
            $.ajax({
                url: 'https://localhost:8081/api/update',
                type: 'POST',
                data: JSON.stringify(addOperation),
                contentType: 'application/json',
                success: callback,
                error: function (error) {
                    console.log(error);
                }
            });
        },
        uriClick(uri, head) {
            if(uri.startsWith('https://') || uri.startsWith('http://')) {
                if (uri.startsWith('http://www.opengis.net/')) {
                    window.open(uri, '_blank').focus();
                } else if (head === 'collections') {
                    window.location.href = '/collections/' + uri.split('#')[1];
                } else if (head === 'dataset') {
                    var collectionId = window.location.href;
                    if (collectionId.includes('/collections/')) {
                        collectionId = collectionId.split('/collections/')[1];
                        if (collectionId.includes('/items')) {
                            collectionId = collectionId.split('/items')[0];
                        }
                    } else {
                        collectionId = 'undefined';
                    }
                    window.location.href = '/collections/' + collectionId + '/items/' + uri.split('#')[1];
                } else if (head === 'HTML') {
                    window.location.href = uri;
                }
            }
        }
    },
};
</script>

<style>
.select-all {
    display: flex;
    flex-direction: row;
    margin: 10px;
}

.select-all > input {
    margin: 0 10px 0 0;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-direction: row;
    padding: 0 0 10px 0;
}

.title {
    display: flex;
    flex-direction: row;
}

.title > h2 {
    margin: 10px;
    cursor: pointer;
    font-weight: normal;
}

.title > h2.selected {
    font-weight: bold;
}

.rdf-data {
    padding: 20px;
    border-radius: 5px;
    resize: both;
    overflow: auto;
    min-width: 500px;
    min-height: 150px;
    max-width: calc(100vw - 380px);
    align-items: center;
}

.rdf-data.dark {
    background-color: #1A202C;
    color: #fff;
}

.rdf-data {
    background-color: rgb(241, 241, 241);
    color: #1A202C;
}

h2 {
    margin-top: 0;
    font-weight: bold;
}

p {
    font-size: 16px;
    font-weight: bold;
}

.metadata {
    border-radius: 5px;
    border: none;
    background-color: none;
    margin: 10px;
}

.metadata p {
    padding: 6px 20px;
    border: none;
    background-color: none;
    color: inherit;
    cursor: pointer;
    font-size: 22px;
    font-weight: bold;
    width: 100%;
    text-align: center;
}
.metadata:hover{
    background-color: #dee1e6;
    transition: background-color 0.3s ease;
}
.rdf-data.dark .metadata:hover {
    background-color: #4A5568;
    color: white;
    transition: background-color 0.3s ease;
}
.metadata.metadata.active{
    background-color: #dee1e6;
}
.rdf-data.dark .metadata.active {
    background-color: #4A5568;
    color: white;
}
.metadata-Catalog{
    display:flex;
    align-items: center;
    margin-bottom: 5px;
}
.metadata-Catalog p{
    font-size: 1.17em;
    cursor: default;
    text-align:start;
    width: fit-content;
    padding: 0 5px 0 10px;
}
.metadata-Catalog select{
    display: block;
    text-align: center;
    font-size: 13px;
    font-weight: bold;
    padding: 9px;
    width: 250px;
    margin: 10px 0;
    left: 23%;
    border: 0px solid #1A202C;
    border-radius: 5px;
    background-color: rgb(241, 241, 241);
    color: black;
    appearance: none;
    cursor: pointer;
}
th{
    color: white;
}
.metadata-Catalog button{
    background-color: #0baaa7;
    margin-right: 40px;
}
.metadata-container {
    display: flex;
    flex-direction: column;
    cursor: default;
}

.metadata-element {
    display: grid;
    grid-template-columns: 1fr 1fr;
    margin: 10px 0;
    width: 75%;
}

.metadata-element h3 {
    font-weight: bold;
    margin: 5px 0 0 10px;
}

.metadata-input {
    display: flex;
    flex-direction: row;
}

.metadata-input > button:hover {
    background-color: #1A202C;
    color: white;
    transition: background-color 0.3s ease;
}

.predicate {
    color: #EF4444;
    font-weight: bold;
    font-size: 16px;
    border-radius: 5px;
    padding: 10px;
    border: none;
    text-align: center;
}

.predicate.unknown {
    background-color: #fb7171;
    color: white;
    border: 3px solid #EF4444;
}

.add-selected {
    background-color: #0baaa7;
    margin-left: 10px;
}
.refresh{
    font-weight:bold;
    font-size: medium;
    background-color:#1A202C;
    margin-bottom: 30px;
    margin-left: 10px;
}

.rdf-data.dark .refresh{
    background-color:#4A5568;
    transition: background-color 0.3s ease;
}

.rdf-data.dark .refresh:hover{
    background-color: #1A202C;
}

button {
    border: none;
    cursor: pointer;
    background-color: #EF4444;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    padding: 5px 10px;
    border-radius: 5px;
}

button:hover {
    background-color: #4A5568;
    transition: background-color 0.5s ease;
}

.download {
    text-align: right;
}

.download-button {
    border: none;
    cursor: pointer;
    background-color: #EF4444;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    padding: 5px 10px;
    border-radius: 5px;
    margin: 10px 0 10px 10px;
}

.subject,
.object,
/* .dataset-title, */
.metadata-textbox {
    border: none;
    border-radius: 5px;
    padding: 11px;
    margin: 10px 5px;
    width: 250px;
    font-size: 14px;
}

/* .dataset-title {
    margin: 0px 15px;
    width: 300px;
    text-align: center;
} */

.metadata-textbox {
    margin: 0px 15px;
}

.delete-button,
.add-button {
    border: none;
    cursor: pointer;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    padding: 9px;
    border-radius: 5px;
    margin: 10px 5px;
    width: 100px;
}

.delete-button {
    background-color: #EF4444;
}

.add-button {
    background-color: #0baaa7;
}

.autocomplete-results {
    position: absolute;
    z-index: 1;
    width: 100%;
    max-height: 200px;
    overflow-y: auto;
    background-color: #dee1e6;
    border-radius: 5px;
    padding: 10px;
    margin: 0px;
    list-style: none;
}
.rdf-data.dark .autocomplete-results{
    background-color: #4A5568;
}

.autocomplete-results li {
    cursor: pointer;
    font-size: 18px;
    font-weight: bold;
    padding: 10px;
    border-radius: 5px;
}

.autocomplete-results li:hover {
    background-color: #0baaa7;
    transition: background-color 0.5s ease;
}

.validate, .view-html-button {
    background-color: #0baaa7;
}

.added {
    transition: all 0.5s ease-in-out;
    background-color: transparent;
    pointer-events: none;
    cursor: default;
}

.custom-predicate {
    font-size: 16px;
    text-align: center;
    color: #1A202C;
}
.rdf-data.dark .custom-predicate{
    color: white;
}

label {
    font-size: 16px;
    font-weight: bold;
    margin: 10px;
}

table {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 20px;
}

th, td {
  padding: 8px;
  text-align: left;
  border: 1px solid #ddd;
}

th {
  background-color: #4A5568;
  font-weight: bold;
}

.clickable {
    cursor: pointer;
    text-decoration: underline;
}

.clickable:hover {
    color: #0baaa7;
    transition: color 0.5s ease;
}

</style>