<template>
    <div class="rdf-data" :class="{dark: isDarkMode}">
        <h2>RDF Data</h2>
        <p>{{ file }}</p>
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
      processContent(content) {
        console.log(content);
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
</style>