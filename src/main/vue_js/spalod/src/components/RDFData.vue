<template>
    <div class="rdf-data" :class="{ dark: isDarkMode }" @click="() => showResults = false">
        <div class="header">
            <h2>RDF Data</h2>
            <div v-if="selectedTriplets.length > 0">
                <button @click="removeSelected">Remove Selected</button>
                <button @click="addSelected" class="add-selected">Add Selected</button>
            </div>
        </div>
        <p v-for="(triplet, index) in rdfData" :key="triplet.id">
            <input type="checkbox" v-model="selectedTriplets" :value="triplet" />
            <input type="text" v-model="triplet.subject" class="subject" />
            <input type="text" v-model="triplet.predicate" class="predicate"
                @input="filterResults(triplet.predicate, index)" @focus="activeInput = index" />
        <ul v-if="showResults && activeInput === index" class="autocomplete-results">
            <div class="custom-predicate">
                <h2>Custom predicate</h2>
                <input type="radio" id="datatype-property" value="DatatypeProperty" v-model="picked">
                <label for="datatype-property">DatatypeProperty</label>
                <input type="radio" id="object-property" value="ObjectProperty" v-model="picked">
                <label for="object-property">ObjectProperty</label>
                <br>
            </div>
            <h2 v-if="filteredResults.length > 0">Predicate Options</h2>
            <li v-for="(result, i) in filteredResults" :key="i" @click="selectResult(result, index)">
                {{ result.split('#')[1] }}
            </li>
        </ul>
        <input type="text" v-model="triplet.object" class="object" />
        <button class="delete-button" @click="deleteTriplet(index)">Delete</button>
        <button :id="'btn' + index" class="add-button" @click="addTriplet(triplet, index)">Add</button>
        <br v-if="rdfData[index + 1] && rdfData[index + 1].subject !== rdfData[index].subject">
        <br v-if="rdfData[index + 1] && rdfData[index + 1].subject !== rdfData[index].subject">
        </p>
        <div v-if="rdfData && rdfData.length > 0" class="download">
            <button @click="downloadGeoJSON">Download GeoJSON</button>
            <button class="download-button" @click="download">Download Owl</button>
        </div>
    </div>
</template>

<script>
import $ from 'jquery';

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
            isDarkMode: false,
            rdfData: null,
            predicateOptions: [],
            filteredResults: [],
            selectedTriplets: [],
            showResults: false,
            activeInput: null,
            picked: "DatatypeProperty"
        };
    },
    mounted() {
        this.detectDarkMode();
        window.matchMedia('(prefers-color-scheme: dark)').addListener(event => {
            this.isDarkMode = event.matches;
        });
        this.loadPredicates();
    },
    methods: {
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
                'url': 'http://localhost:8081/api/sparql-select',
                'data': JSON.stringify(data),
                'dataType': 'json',
                success: (data) => {
                    this.predicateOptions = data.results.bindings.map(binding => binding.property.value);
                    console.log(this.predicateOptions);
                }
            });
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
                geoJson.features.forEach(feature => {
                    const properties = feature.properties;
                    const subject = properties['item'];
                    if (!subject) return;
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

                    const predicate = 'coordinates';
                    const coordinates = feature.geometry.coordinates;
                    const object = coordinates[0] + ', ' + coordinates[1];
                    this.rdfData.push({
                        subject,
                        predicate,
                        object,
                    });
                });
            };
        },
        download() {
            if (this.file !== null) {
                let formData = new FormData();
                formData.append('file', this.file);
                $.ajax({
                    url: 'http://localhost:8081/api/uplift',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (response) {
                        $.ajax({
                            url: `http://localhost:8081/download/data/${response}`,
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
        deleteTriplet(index) {
            if (confirm("Are you sure you want to delete this triplet?")) {
                this.rdfData.splice(index, 1);
            }
        },
        addTriplet(triplet, index) {
            const predicate = "http://lab.ponciano.info/ont/spalod#" + triplet.predicate;
            var tripleData = {
                subject: triplet.subject,
                predicate: predicate,
                object: encodeURIComponent(triplet.object),
            };
            const removeOperation = {
                operation: "remove",
                tripleData: tripleData,
            };
            const addOperation = {
                operation: "add",
                tripleData: tripleData,
            };
            $.ajax({
                url: 'http://localhost:8081/api/update',
                type: 'POST',
                data: JSON.stringify(removeOperation),
                contentType: 'application/json',
                success: function (response) {
                    $.ajax({
                        url: 'http://localhost:8081/api/update',
                        type: 'POST',
                        data: JSON.stringify(addOperation),
                        contentType: 'application/json',
                        success: function (response) {
                            $('#btn' + index).text('Added').addClass('added');
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
                $.ajax({
                    url: 'http://localhost:8081/api/update',
                    type: 'POST',
                    data: JSON.stringify(removeOperation),
                    contentType: 'application/json',
                    success: function (response) {
                        $.ajax({
                            url: 'http://localhost:8081/api/update',
                            type: 'POST',
                            data: JSON.stringify(addOperation),
                            contentType: 'application/json',
                            success: function (response) {
                                $('#btn' + index).text('Added').addClass('added');
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
        },
        selectResult(result, index) {
            this.rdfData[index].predicate = result.split('#')[1];
            this.showResults = false;
        },
        addSelected() {
            if(confirm("If there are custom predicates, they will be added to the ontology as " + this.picked + ". Are you sure you want to continue?")) {
                this.selectedTriplets.forEach((triplet) => {
                const index = this.rdfData.findIndex((rdfTriplet) => {
                    return rdfTriplet === triplet;
                });
                console.log(index);
                this.addTriplet(triplet, index);
            });
            this.selectedTriplets = [];
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
    },
};
</script>

<style>
.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-direction: row;
    padding: 0 0 40px 0;
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

.add-selected {
    background-color: #0baaa7;
    margin-left: 10px;
}

button {
    border: none;
    cursor: pointer;
    background-color: #EF4444;
    color: #fff;
    font-size: 22px;
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
    font-size: 22px;
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
}

label {
    font-size: 16px;
    font-weight: bold;
    margin: 10px;
}
</style>