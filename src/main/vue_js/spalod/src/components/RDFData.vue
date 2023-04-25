<template>
    <div class="rdf-data" :class="{ dark: isDarkMode }">
        <h2>RDF Data</h2>
        <p v-for="(triplet, index) in rdfData" :key="triplet.id">
            <input type="text" v-model="triplet.subject" class="subject" />
            <!-- <select v-model="triplet.predicate" class="predicate">
                <option v-for="option in predicateOptions" :value="option">{{ option.split('#')[1] }}</option>
            </select> -->
            <input type="text" v-model="triplet.predicate" class="predicate" @input="filterResults(triplet.predicate, index)"/>
            <ul v-if="showResults && activeInput === index" class="autocomplete-results">
                <li v-for="(result, i) in filteredResults" :key="i" @click="selectResult(result, index)">
                    {{ result.split('#')[1] }}
                </li>
            </ul>
            <input type="text" v-model="triplet.object" class="object" />
            <button class="delete-button" @click="deleteTriplet(index)">Delete</button>
            <button class="add-button" @click="addTriplet(triplet)">Add</button>
            <br v-if="rdfData[index + 1] && rdfData[index + 1].subject !== rdfData[index].subject">
            <br v-if="rdfData[index + 1] && rdfData[index + 1].subject !== rdfData[index].subject">
        </p>
        <div v-if="rdfData && rdfData.length > 0" class="download">
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
            showResults: false,
            activeInput: null,
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
        addTriplet(triplet) {
            const tripleData = {
                subject: triplet.subject,
                predicate: triplet.predicate,
                object: triplet.object,
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
                            console.log(response);
                        },
                        error: function (error) {
                            console.log(error);
                        }
                    });
                },
            });
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
    },
};
</script>

<style>
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
    margin-top: 10px;
    margin-right: 10px;
}

.download-button:hover {
    background-color: #4A5568;
    transition: background-color 0.5s ease;
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

.delete-button:hover,
.add-button:hover {
    background-color: #4A5568;
    transition: background-color 0.5s ease;
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

</style>