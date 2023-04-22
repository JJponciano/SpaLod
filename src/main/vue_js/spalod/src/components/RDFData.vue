<template>
    <div class="rdf-data" :class="{ dark: isDarkMode }">
        <h2>RDF Data</h2>
        <p v-for="(triplet, index) in rdfData" :key="triplet.id">
            {{ triplet.subject }}, <span class="predicate">{{ triplet.predicate }}</span>, {{ triplet.object }}
            <br v-if="(index + 1) % 3 === 0">
            <br v-if="(index + 1) % 3 === 0">
        </p>
    </div>
</template>

<script>
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
            rdfData: [],
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
            const fileReader = new FileReader();
            fileReader.readAsText(file);
            fileReader.onload = () => {
                const geoJson = JSON.parse(fileReader.result);
                geoJson.features.forEach(feature => {
                    const properties = feature.properties;

                    const subject = properties['item'];
                    var predicate = 'hasLabel';
                    var object = properties['itemLabel'];
                    this.rdfData.push({
                        subject,
                        predicate,
                        object,
                    });

                    predicate = 'hasCategory';
                    object = properties['category'];
                    this.rdfData.push({
                        subject,
                        predicate,
                        object,
                    });

                    predicate = 'hasCoordinates';
                    const coordinates = feature.geometry.coordinates;
                    object = coordinates[0] + ', ' + coordinates[1];
                    this.rdfData.push({
                        subject,
                        predicate,
                        object,
                    });
                });
            };
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
}
</style>