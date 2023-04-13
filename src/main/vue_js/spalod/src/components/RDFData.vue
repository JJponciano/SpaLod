<template>
    <div class="rdf-data" :class="{dark: isDarkMode}">
        <h2>RDF Data</h2>
        <!-- TODO -->
        <h3>C3 hasCity Mainz</h3>
        <h3>C3.2 hasCoordinates 3424234.234243, 4242342.2342344</h3>
    </div>
</template>

<script>
export default {
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
    },
};
</script>

<style>
.rdf-data {
    padding: 20px;
    border-radius: 5px;
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
}
</style>