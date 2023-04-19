<template>
    <div class="user-actions" :class="{dark: isDarkMode}">
        <button class="navbar_button" @click="toggleNavBar">Menu</button>
        <div class="side_pannel">
            <select v-model="selectedOption">
                <option v-for="(option, index) in options" :key="index" :value="option.value">
                {{ option.label }}
                </option>
            </select>
            <button @click="filterData">Filter</button>
            <button @click="addData">Add Data</button>
            <button @click="confirmRequest" class="confirm">Confirm Request</button>
        </div>
        <div class="navbar-menu" :class="{ active: menuOpen, dark: isDarkMode }">
            <ul class="navbar-nav">
                <li>
                    <select v-model="selectedOption">
                        <option v-for="(option, index) in options" :key="index" :value="option.value">
                        {{ option.label }}
                        </option>
                    </select>
                </li>
                <li class="filterButton">
                    <button @click="filterData">Filter</button>
                </li>
                <li class="adddataButton">
                    <button @click="addData">Add Data</button>
                </li>
                <li class="confirmButton">
                    <button @click="confirmRequest" class="confirm">Confirm Request</button>
                </li>
            </ul>
        </div>
    </div>
</template>

<script>
import axios from "axios";

export default {
    data() {
        return {
            isDarkMode: false,
            menuOpen: false,
            selectedOption: "schools",
            options: [
                { label: 'Schule (Q3914)', value: 'schools' },
                { label: '20 biggest cities in Germany', value: 'twentyBiggestCities' },
                { label: '10 biggest football stadiums in Germany', value: 'tenBiggestStadiums' },
                { label: 'Krankenhaus (Q16917)', value: 'hospitals' },
                { label: 'Polizeistation (Q861951)', value: 'policeStations' },
                { label: 'Feuerwache (Q1195942)', value: 'fireStations' },
                { label: 'Supermarkt (Q180846)', value: 'supermarkets' },
                { label: 'Museen (Q33506)', value: 'museums' },
                { label: 'Bibliotheken (Q7075)', value: 'libraries' },
                { label: 'Bahnhöfe (Q55488)', value: 'trainStations' },
                { label: 'Banken (Q22687)', value: 'banks' },
                { label: 'Restaurants (Q11707)', value: 'restaurants' },
                { label: 'Kinos (Q41253)', value: 'cinemas' },
                { label: 'Denkmäler (Q4989906)', value: 'monuments' },
                { label: 'Hotels (Q27686)', value: 'hotels' },
                { label: 'Flughäfen (Q1248784)', value: 'airports' },
                { label: 'Stadien (Q483110)', value: 'stadiums' },
                { label: 'Schwimmbäder (Q200023)', value: 'swimmingPools' },
                { label: 'Tankstellen (Q205495)', value: 'serviceStation' },
                { label: 'Wetterstationen (Q190107)', value: 'weatherStation' },
                { label: 'Forschungslaboratorien (Q483242)', value: 'researchLaboratory' },
                { label: 'Häfen (Q44782)', value: 'port' },
                { label: 'Städte (Q515)', value: 'cities' },
            ],
            queries: {
                schools: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q3914} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                twentyBiggestCities: 'SELECT DISTINCT ?city ?cityLabel ?latitude ?longitude ?instanceOfCity ?population WHERE {\n SERVICE wikibase:label { bd:serviceParam wikibase:language "de". } \n VALUES ?instanceOfCity { \n wd:Q515 \n  } \n  ?city (wdt:P31/(wdt:P279*)) ?instanceOfCity; \n wdt:P17 wd:Q183;\n  p:P625 ?statement. \n ?statement psv:P625 ?coordinate_node. \n ?coordinate_node wikibase:geoLatitude ?latitude; \n wikibase:geoLongitude ?longitude.\nOPTIONAL { ?city wdt:P1082 ?population. } \n } \nORDER BY DESC (?population) \nLIMIT 20',
                tenBiggestStadiums: 'SELECT ?item ?itemLabel ?latitude ?longitude ?category ?capacity WHERE {\n?item wdt:P31 wd:Q1154710;\nwdt:P17 wd:Q183;\n  p:P625 ?statement.\n  ?statement psv:P625 ?coordinate_node.\n?coordinate_node wikibase:geoLatitude ?latitude;\nwikibase:geoLongitude ?longitude.\n  ?item wdt:P31 ?category .\nSERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de". }\n OPTIONAL { ?item wdt:P1083 ?capacity. }\n}\nORDER BY DESC (?capacity)\nLIMIT 10',
                hospitals: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q16917} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                policeStations: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q861951} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                fireStations: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q1195942 } \n   ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                supermarkets: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q180846} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                museums: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q33506} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                libraries: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q7075} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                trainStations: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q55488} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                banks: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q22687} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                restaurants: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q11707} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                cinemas: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q41253} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                monuments: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q4989906} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                hotels: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q27686} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                airports: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q1248784} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                stadiums: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q483110} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                swimmingPools: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q200023} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                serviceStation: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q205495} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                weatherStation: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q190107} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                researchLaboratory: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q483242} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                port: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE { \n  VALUES ?category{ wd:Q44782} \n  ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement .\n   ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\nSERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
                cities: 'SELECT ?category ?itemLabel ?latitude ?longitude ?item WHERE {\n  VALUES ?category{ wd:Q515 }\n ?item wdt:P17 wd:Q183.\n  ?item wdt:P31 ?category .\n  ?item p:P625 ?statement . \n  ?statement psv:P625 ?coordinate_node .\n  ?coordinate_node wikibase:geoLatitude ?latitude .\n  ?coordinate_node wikibase:geoLongitude ?longitude .\n  SERVICE wikibase:label {\n    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],de".\n  }\nFILTER(?latitude <= 86.42397134276521).\nFILTER(?latitude >= -63.39152174400882).\nFILTER(?longitude <= 219.02343750000003).\nFILTER(?longitude >= -202.85156250000003)\n}\nLIMIT ',
            },
            latNorthEast: 86.42397134276521,
            latSouthWest: -63.39152174400882,
            lngNorthEast: 219.02343750000003,
            lngSouthWest: -202.85156250000003,
        };
    },
    mounted() {
        this.detectDarkMode();
        window.addEventListener("resize", this.closeNavBar);
        window.matchMedia('(prefers-color-scheme: dark)').addListener(event => {
            this.isDarkMode = event.matches;
        });
    },
    beforeDestroy(){
        window.removeEventListener("resize", this.closeNavBar);
    },
    methods: {
        search() {
            // TODO: Implement search
        },
        toggleNavBar(){
            this.menuOpen = !this.menuOpen;
        },
        closeNavBar()
        {
            this.menuOpen=false;
        },
        filterData() {
            // TODO: Implement filter
        },
        addData() {
            // TODO: Implement add data
        },
        confirmRequest() {
            var query = "http://localhost:8081/api/sparql-select -H 'Content-type:application/json' -d '{\"query\":\"" + this.queries[this.selectedOption] + "500\", \"triplestore\": \"http://query.wikidata.org/sparql\"}'";
            console.log(query);
            // axios.post(query).then(response => {
            //     console.log(response);
            // }).catch(error => {
            //     console.log(error);
            // });
            axios.post("Content-type:application/json' -d '{\"query\":\"SELECT ?item ?itemLabel ?latitude ?longitude ?category ?capacity WHERE {?item wdt:P31 wd:Q1154710;wdt:P17 wd:Q183;p:P625 ?statement.?statement psv:P625 ?coordinate_node.?coordinate_node wikibase:geoLatitude ?latitude;wikibase:geoLongitude ?longitude.?item wdt:P31 ?category .SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],de\". }OPTIONAL { ?item wdt:P1083 ?capacity. }}ORDER BY DESC (?capacity)LIMIT 10\",\"triplestore\": \"http://query.wikidata.org/sparql%22%7D\"").then(response => {
                console.log(response);
            }).catch(error => {
                console.log(error);
            });
        },
        detectDarkMode() {
            this.isDarkMode = window.matchMedia('(prefers-color-scheme: dark)').matches;
        },
    },
};
</script>

<style scoped>
.user-actions {
    padding: 20px;
    border-radius: 5px;
    flex-direction: column;
    display: flex;
    align-items: start;
    height: calc(100vh - 100px);
    resize: horizontal;
    overflow: auto;
    width: 320px;
    min-width: 220px;
}

.user-actions.dark {
    background-color: #1A202C;
    color: #fff;
}

.user-actions.light {
    background-color: #fff;
    color: #1A202C;
}

select {
    display: block;
    font-size: 16px;
    font-weight: bold;
    padding: 10px;
    width: 100%;
    border: 2px solid #1A202C;
    border-radius: 5px;
    background-color: #4A5568;
    color: white;
    appearance: none;
    -webkit-appearance: none;
    -moz-appearance: none;
    cursor: pointer;
}

.navbar_button{
    display: none;
}

.navbar-menu{
    display: none;
}

button {
    padding: 10px 20px;
    border-radius: 5px;
    border: none;
    background-color: transparent;
    color: inherit;
    cursor: pointer;
    transition: background-color 0.2s ease-in-out;
    font-size: 18px;
    font-weight: bold;
    margin-top: 10px;
    width: 100%;
    text-align: left;
}

.confirm {
    background-color: #EF4444;
    color: #fff;
}

.navbar_button:hover{
    background-color: #81818a;
    color: white;
}

button:hover {
    background-color: #4A5568;
    color: #fff;
}
@media screen and (max-width: 768px) {

    .side_pannel{
        display: none;
    }
    .user-actions{
        resize: none;
        display:contents;
        flex: none;
        width: fit-content;
        min-width: 20px;
        height: fit-content;
        min-height: fit-content;
        resize: none;
        padding: 0px;
    }
    .user-actions.dark{
        width: fit-content;
    }
    .navbar_button{
        display: block;
        margin-left: -100px;
        padding: 10px;
        border-radius: 5px;
        border: none;
        background-color: rgba(194, 194, 194, 0.603);
        color: inherit;
        cursor: pointer;
        transition: background-color 0.3s ease-in-out;
        font-size: 18px;
        font-weight:lighter;
        margin-top: 0px;
        width:fit-content;
    }
    .navbar-menu.active{
        padding: 15px 20px 15px 0px;
        margin-left: -100px;
        border-radius: 15px;
        flex-direction: column;
        display: flex;
        align-items: start;
        height: fit-content;
        resize: horizontal;
        overflow: auto;
        background-color: white;
    }
    .navbar-menu.dark{
        background-color: #1A202C;
    }
}
</style>