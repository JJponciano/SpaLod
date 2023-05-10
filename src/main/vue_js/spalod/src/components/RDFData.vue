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
        <div class="select-all" v-if="rdfData && rdfData.length > 0">
            <input type="checkbox" v-model="areAllSelected" @change="selectAll(areAllSelected)"/>
            <h3>Select all</h3>
        </div>
        <button @click="refreshMap" class="refresh">Refresh Map</button>
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
        <input type="text" v-model="triplet.object" class="object" /> <!-- TODO: ajouter un listener pour actualiser la carte en temps réel -->
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
            showResults: false,
            activeInput: null,
            picked: "DatatypeProperty",
            rdfView: true,
        };
    },
    mounted() {
        this.detectDarkMode();
        window.matchMedia('(prefers-color-scheme: dark)').addListener(event => {
            this.isDarkMode = event.matches;
        });
        this.loadPredicates();
    },
    computed: {
        keys() {
            if (this.queryResult.length > 0)
                return Object.keys(this.queryResult[0])
        }
    },
    methods: {
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
                        const removeOperation = {
                            operation: "remove",
                            tripleData: tripleData,
                        };
                        $.ajax({
                            url: 'https://localhost:8081/api/update',
                            type: 'POST',
                            data: JSON.stringify(removeOperation),
                            contentType: 'application/json',
                            success: function (response) {
                                console.log('Triple removed');
                            },
                            error: function (error) {
                                console.log(error);
                            }
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
                object: triplet.object.replace(/ /g, '_'),
            };
            console.log(tripleData);
            const addOperation = {
                operation: "add",
                tripleData: tripleData,
            };
            $.ajax({
                url: 'https://localhost:8081/api/update',
                type: 'POST',
                data: JSON.stringify(addOperation),
                contentType: 'application/json',
                success: function (response) {
                    $('#btn' + index).text('Added').addClass('added');
                    console.log('Triple added');
                },
                error: function (error) {
                    console.log(error);
                }
            });

            // Add the new predicate
            this.loadPredicates();
            if (!this.predicateOptions.includes(predicate)) {
                tripleData = {
                    subject: predicate,
                    predicate: "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                    object: "http://www.w3.org/2002/07/owl#" + this.picked,
                };
                const removeOperation = {
                    operation: "remove",
                    tripleData: tripleData,
                };
                const addOperation = {
                    operation: "add",
                    tripleData: tripleData,
                };
                var self = this;
                $.ajax({
                    url: 'https://localhost:8081/api/update',
                    type: 'POST',
                    data: JSON.stringify(removeOperation),
                    contentType: 'application/json',
                    success: function (response) {
                        $.ajax({
                            url: 'https://localhost:8081/api/update',
                            type: 'POST',
                            data: JSON.stringify(addOperation),
                            contentType: 'application/json',
                            success: function (response) {
                                // console.log(response);
                                self.predicateOptions.push(predicate);
                                // console.log(self.predicateOptions);
                                self.areAllPredicatesKnown();
                            },
                            error: function (error) {
                                console.log(error);
                            }
                        });
                    },
                    error: function (error) {
                        console.log(error);
                    }
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
}

.rdf-data.dark {
    background-color: #1A202C;
    color: #fff;
}

.rdf-data.light {
    background-color: #fff;
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
.object {
    border: none;
    border-radius: 5px;
    padding: 11px;
    margin: 10px 5px;
    width: 250px;
    font-size: 14px;
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
    background-color: #4A5568;
    border-radius: 5px;
    padding: 10px;
    margin: 0px;
    list-style: none;
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

.added {
    transition: all 0.5s ease-in-out;
    background-color: transparent;
    pointer-events: none;
    cursor: default;
}

.custom-predicate {
    font-weight: bold;
    font-size: 16px;
    text-align: center;
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

</style>