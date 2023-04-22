<template>
    <div class="rdf-data" :class="{ dark: isDarkMode }">
        <h2>RDF Data</h2>
        <p v-for="(triplet, index) in rdfData" :key="triplet.id">
            {{ triplet.subject }}, <span class="predicate">{{ triplet.predicate }}</span>, {{ triplet.object }}
            <br v-if="rdfData[index + 1] && rdfData[index + 1].subject !== rdfData[index].subject">
            <br v-if="rdfData[index + 1] && rdfData[index + 1].subject !== rdfData[index].subject">
        </p>
        <div class="download">
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
        };
    },
    mounted() {
        this.detectDarkMode();
        window.matchMedia('(prefers-color-scheme: dark)').addListener(event => {
            this.isDarkMode = event.matches;
        });
    },
    methods: {
        detectDarkMode() {
            this.isDarkMode = window.matchMedia('(prefers-color-scheme: dark)').matches;
        },
        processContent(file) {
            this.rdfData = null;
            this.rdfData = [];
            const fileReader = new FileReader();
            fileReader.readAsText(file);
            fileReader.onload = () => {
                const geoJson = JSON.parse(fileReader.result);
                geoJson.features.forEach(feature => {
                    const properties = feature.properties;
                    const subject = properties['item'];
                    if(!subject) return;
                    for(const key in properties) {
                        if(key === 'item') continue;
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
    margin-top: 10px;
    margin-right: 10px;
}
</style>