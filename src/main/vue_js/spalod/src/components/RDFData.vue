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
        <!-- <div class="dataset" v-if="rdfData && rdfData.length > 0">
            <h2>Dataset:</h2>
            <input type="text" v-model="metadata['title']" class="dataset-title" placeholder="Dataset title" @focus="$event.target.select()" spellcheck="false"/>
            <button id="title" class="validate" @click="validateMetadata('title')">Validate</button>
        </div> -->
        <div class="metadata" :class="{ active: showMetadata }" v-if="rdfData && rdfData.length > 0">
            <p @click="showMetadata = !showMetadata">Show Metadata</p>
            <div class="metadata-container" v-if="showMetadata">
                <div v-for="(queryable, index) in queryables " :key="index" class="metadata-element">
                    <h3 v-if="queryable.required">{{ queryable.q }}: *</h3>
                    <h3 v-else>{{ queryable.q }}:</h3>
                    <div class="metadata-input">
                        <input type="text" v-model="metadata[queryable.q]" class="metadata-textbox" :placeholder="queryable.d" @focus="$event.target.select()" @input="queryable.v = false" spellcheck="false">
                        <button :id="queryable.q" class="validate" @click="validateMetadata(queryable.q)" :class="{ added: queryable.v }" :disabled="queryable.q !== 'recordId' && !queryables[0].v" v-show="queryable.q === 'recordId' || queryables[0].v">{{ queryable.v ? 'Validated' : 'Validate' }}</button>
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
                    {{ result.split('#')[1] }}
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
              <td v-for="key in keys">{{ result[key] }}</td>
            </tr>
          </tbody>
        </table>
    </div>
</template>

<script>
import $ from 'jquery';

$.ajaxSetup({
    xhrFields: {
        withCredentials: true
    }
});


export default {
    props: {
        file: File
    },
    watch: {
        file(newFile) {
            this.processContent(newFile);
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
            queryables: [
                {q: 'recordId', required: true, d: 'The unique identifier of the dataset', v: false, p: 'http://www.w3.org/ns/dcat#dataset'},
                {q: 'title', required: true, d: 'The name given to the resource', v: false, p: 'http://purl.org/dc/terms/title'},
                {q: 'description', required: true, d: 'Description of the resource', v: false, p: 'http://purl.org/dc/terms/description'},
                {q: 'distribution', required: true, d: 'The distribution of the dataset', v: false, p: 'http://www.w3.org/ns/dcat#distribution'},
                {q: 'publisher', required: true, d: 'Entity making the resource available', v: false, p: 'http://purl.org/dc/terms/publisher'},
                {q: 'keywords', required: false, d: "Tags separated by ','", v: false, p: 'http://www.w3.org/ns/dcat#keyword'},
                {q: 'theme', required: false, d: 'Main category', v: false, p: 'http://www.w3.org/ns/dcat#theme'},
                {q: 'type', required: false, d: 'The nature or genre of the resource', v: false, p: 'http://purl.org/dc/terms/type'},
                {q: 'contactPoint', required: false, d: 'An entity to contact', v: false, p: 'http://www.w3.org/ns/dcat#contactPoint'},
                {q: 'spatial', required: false, d: 'Spatial area or designed place', v: false, p: 'http://www.w3.org/ns/dcat#spatial'},
                {q: 'temporal', required: false, d: 'Time interval', v: false, p: 'http://purl.org/dc/terms/temporal'},
                {q: 'issued', required: false, d: 'The date the dataset was created', v: false, p: 'http://purl.org/dc/terms/issued'},
                {q: 'modified', required: false, d: 'The date the dataset was updated', v: false, p: 'http://purl.org/dc/terms/modified'},
                {q: 'language', required: false, d: 'Language of the resource', v: false, p: 'http://purl.org/dc/terms/language'},
                {q: 'formats', required: false, d: 'List of available distributions', v: false, p: 'http://www.w3.org/ns/dcat#distribution'},
                {q: 'license', required: false, d: 'License of the resource', v: false, p: 'http://purl.org/dc/terms/license'},
                {q: 'rights', required: false, d: 'Rights not addressed by the license', v: false, p: 'http://purl.org/dc/terms/rights'},
                {q: 'landingPage', required: false, d: 'Links to other resources', v: false, p: 'http://www.w3.org/ns/dcat#landingPage'},
            ],
            showResults: false,
            activeInput: null,
            picked: "DatatypeProperty",
            rdfView: true,
            showMetadata: false,
        };
    },
    mounted() {
        this.detectDarkMode();
        window.matchMedia('(prefers-color-scheme: dark)').addListener(event => {
            this.isDarkMode = event.matches;
        });
        this.loadPredicates();
        this.metadata['recordId'] = this.uuidv4();
        this.metadata['publisher'] = localStorage.getItem('username') || "";
        var today = new Date();
        var dd = String(today.getDate()).padStart(2, '0');
        var mm = String(today.getMonth() + 1).padStart(2, '0');
        var yyyy = today.getFullYear();
        this.metadata['created'] = dd + '/' + mm + '/' + yyyy;
        this.metadata['updated'] = dd + '/' + mm + '/' + yyyy;
    },
    computed: {
        keys() {
            if (this.queryResult.length > 0)
                return Object.keys(this.queryResult[0])
        }
    },
    methods: {
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
                triplestore: '' //intégrer graph DB ici
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
                    const subject = properties['item'];
                    for (const key in properties) {
                        if (key === 'item') continue;
                        const predicate = key;
                        const object = properties[key];
                        this.rdfData.push({
                            subject,
                            predicate,
                            object,
                        });
                    }

                    const coordinates = feature.geometry.coordinates;
                    if (coordinates.length > 0) {
                        const predicate = 'coordinates';
                        const object = coordinates[0] + ', ' + coordinates[1];
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
            var geojones=this.getGeoJSON();
            const blob = new Blob([JSON.stringify(geojones)], {type: "application/json"});
            const updatefile = new File([blob], "geodata.json", {type: "application/json"});
            this.$emit('update', updatefile);
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

            // Delete the old triplet
            const data = {
                query: 'SELECT ?o WHERE{?s <' + predicate + '> ?o . FILTER(?s = <' + triplet.subject + '>)}',
                triplestore: ''
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
                            subject: triplet.subject,
                            predicate: predicate,
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
                subject: triplet.subject,
                predicate: predicate,
                object: 'http://lab.ponciano.info/ont/spalod#' + triplet.object.replace(/ /g, '_'),
            };
            this.updateTripleData(tripleData, 'add', () => {
                $('#btn' + index).text('Added').addClass('added');
                console.log('Triple added');
            });

            // Add the new predicate
            this.loadPredicates();
            if (!this.predicateOptions.includes(predicate)) {
                tripleData = {
                    subject: predicate,
                    predicate: "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                    object: "http://www.w3.org/2002/07/owl#" + this.picked,
                };
                var self = this;
                this.updateTripleData(tripleData, 'remove', this.updateTripleData(tripleData, 'add', () => {
                    self.predicateOptions.push(predicate);
                    self.areAllPredicatesKnown();
                }));
            }
        },
        filterResults(predicate, index) {
            this.activeInput = index;
            this.filteredResults = this.predicateOptions.filter((result) => result.toLowerCase().includes(predicate.toLowerCase()));
            this.showResults = true;
            this.areAllPredicatesKnown();
        },
        selectResult(result, index) {
            this.rdfData[index].predicate = result.split('#')[1];
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
        validateForm() {
            const requiredQueryables = this.queryables.filter(queryable => queryable.required);
            const invalidQueryables = requiredQueryables.filter(queryable => queryable.v === false);

            return invalidQueryables.length === 0;
        },
        validateMetadata(data) {
            var queryable = this.queryables.find(queryable => queryable.q === data);
            if(this.metadata[queryable.q] !== '' && this.metadata[queryable.q] !== undefined) {
                if ( queryable.q === 'recordId') {
                    queryable.v = true;
                } else if (queryable.q === 'publisher') {
                    // Delete the old triplets
                    var data = {
                        query: 'SELECT ?o WHERE{?s <' + queryable.p + '> ?o . FILTER(?s = <' + 'http://lab.ponciano.info/ont/spalod#' + this.metadata.recordId + '>)}',
                        triplestore: ''
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
                                    subject: 'http://lab.ponciano.info/ont/spalod#' + this.metadata.recordId,
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
                        triplestore: ''
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
                        subject: 'http://lab.ponciano.info/ont/spalod#' + this.metadata.recordId,
                        predicate: queryable.p,
                        object: 'http://lab.ponciano.info/ont/spalod#' + String(this.metadata[queryable.q]).replace(/ /g, '_'), // TODO: UID of the publisher
                    };
                    this.updateTripleData(tripleData, 'add', () => queryable.v = true);

                    tripleData = {
                        subject: 'http://lab.ponciano.info/ont/spalod#' + String(this.metadata[queryable.q]).replace(/ /g, '_'), // TODO: UID of the publisher
                        predicate: 'http://xlmns.com/foaf/0.1/name',
                        object: 'http://lab.ponciano.info/ont/spalod#' + String(this.metadata[queryable.q]).replace(/ /g, '_'),
                    }
                    this.updateTripleData(tripleData, 'add', () => queryable.v = true);
                } else if (queryable.q === 'keywords') {
                    String(this.metadata.keywords).split(',').forEach(keyword => {
                        // Add the new triplets
                        var tripleData = {
                            subject: 'http://lab.ponciano.info/ont/spalod#' + this.metadata.recordId,
                            predicate: queryable.p,
                            object: 'http://lab.ponciano.info/ont/spalod#' + keyword,
                        };
                        this.updateTripleData(tripleData, 'add', () => queryable.v = true);
                    });
                } else {
                    // Delete the old triplet
                    var data = {
                        query: 'SELECT ?o WHERE{?s <' + queryable.p + '> ?o . FILTER(?s = <' + 'http://lab.ponciano.info/ont/spalod#' + this.metadata.recordId + '>)}',
                        triplestore: ''
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
                                    subject: 'http://lab.ponciano.info/ont/spalod#' + this.metadata.recordId,
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
                        subject: 'http://lab.ponciano.info/ont/spalod#' + this.metadata.recordId,
                        predicate: queryable.p,
                        object: 'http://lab.ponciano.info/ont/spalod#' + String(this.metadata[queryable.q]).replace(/ /g, '_'),
                    };
                    this.updateTripleData(tripleData, 'add', () => queryable.v = true);
                }
            } else {
                alert('Please enter a ' + queryable.q);
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

.metadata-container {
    display: flex;
    flex-direction: column;
    cursor: default;
}

.metadata-element {
    display: grid;
    grid-template-columns: 1fr 3fr;
    margin: 10px 0;
    width: 50%;
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

.validate {
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

/* .dataset {
    display: flex;
    flex-direction: row;
    margin: 10px;
} */

</style>