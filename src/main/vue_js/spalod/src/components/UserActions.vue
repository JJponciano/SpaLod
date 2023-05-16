<template>
    <div class="user-actions" :class="{ dark: isDarkMode }">
        <button class="navbar_button" @click="toggleNavBar">Menu</button>
        <div class="side_pannel">
            <select v-model="selectedOption">
                <option v-for="(option, index) in options" :key="index" :value="option.value">
                    {{ option.label }}
                </option>
            </select>
            <div class="filter" :class="{active:showFilter}">
                <p @click="showFilter = !showFilter">Filter</p>
                <div class="filtercontainer" v-if="showFilter">
                    <p>Max items number</p>
                    <input class="inputbar" type="range" :min="min" :max="max" :step="step" v-model="rangeValue" @input="updateRange" />
                    <p>{{ rangeValue }}</p>
                </div>
            </div>
            <div class="addfile" :class="{active: showAddMenu}">
                <p @click="showAddMenu = !showAddMenu">Add Data</p>
                <div class="addfileButton" v-if="showAddMenu">
                    <button @click="addDataCSV">CSV to GeoJSON</button>
                    <button @click="addDataJSON">JSON to GeoJSON</button>
                    <button @click="addDataGeo">Add GeoJSON</button>
                    <input type="file" ref="fileInputGeo" style="display: none;" accept="application/json"
                        @change="handleFileInputGeo">
                    <button @click="addDataOwl">Add Owl</button>
                    <input type="file" ref="fileInputOwl" style="display: none;" accept=".owl" @change="handleFileInputOwl">
                </div>
            </div>
            <div class="advancedMenu" :class="{active: advancedMenuOpen}">
                <p @click="advancedMenuOpen = !advancedMenuOpen">Advanced Mode</p>
                <div class="textcontainer" v-if="advancedMenuOpen">
                    <textarea v-model="inputAdvanced" :placeholder="placeholders" spellcheck="false"></textarea>
                </div>
            </div>
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
                    <input type="file" ref="fileInput" style="display: none;" accept="application/geojson"
                        @change="handleFileInput">
                </li>
                <li class="confirmButton">
                    <button @click="confirmRequest" class="confirm">Confirm Request</button>
                </li>
            </ul>
        </div>
    </div>
</template>

<script>
import $ from "jquery";

$.ajaxSetup({
    xhrFields: {
        withCredentials: true
    }
});


export default {
    data() {
        return {
            isDarkMode: false,
            menuOpen: false,
            showAddMenu: false,
            showFilter: false,
            min:100,
            max:1000,
            rangeValue:150,
            step:50,
            inputAdvanced:"",
            advancedMenuOpen:false,
            placeholders:"Write your custom request here",
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
                schools: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q3914> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                twentyBiggestCities: 'SELECT DISTINCT ?city ?cityLabel ?latitude ?longitude ?instanceOfCity ?population WHERE {\n SERVICE wikibase:label { bd:serviceParam wikibase:language "de". } \n VALUES ?instanceOfCity { \n wd:Q515 \n  } \n  ?city (wdt:P31/(wdt:P279*)) ?instanceOfCity; \n wdt:P17 wd:Q183;\n  p:P625 ?statement. \n ?statement psv:P625 ?coordinate_node. \n ?coordinate_node wikibase:geoLatitude ?latitude; \n wikibase:geoLongitude ?longitude.\nOPTIONAL { ?city wdt:P1082 ?population. } \n } \nORDER BY DESC (?population) \nLIMIT 20',
                hospitals: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q16917> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                policeStations: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q861951> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                fireStations: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q1195942> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                supermarkets: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q180846> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                museums: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q33506> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                libraries: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q7075> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                trainStations: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q55488> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                banks: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q22687> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                restaurants: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q11707> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                cinemas: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q41253> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                monuments: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q4989906> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                hotels: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q27686> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                airports: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q1248784> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                stadiums: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q483110> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                swimmingPools: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q200023> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                serviceStation: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q205495> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                weatherStation: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q190107> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                researchLaboratory: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q483242> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                port: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q44782> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
                cities: 'SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <http://lab.ponciano.info/ont/spalod#category> <http://www.wikidata.org/entity/Q515> .\n  ?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n  ?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates .\n ?item <http://lab.ponciano.info/ont/spalod#category> ?category .} LIMIT ',
            },
            icons: {
                schools: 'HS',
                twentyBiggestCities: 'BotKon',
                hospitals: 'KHV',
                policeStations: 'BFW',
                fireStations: 'Feuerwehr',
                supermarkets: 'Supermarkt',
                museums: 'Museen',
                libraries: 'Bibliothek',
                trainStations: 'Bahnhof',
                banks: 'Bank',
                restaurants: 'Restaurant',
                cinemas: 'Kino',
                monuments: 'Denkmal',
                hotels: 'Hotel',
                airports: 'Flughafen',
                stadiums: 'Stadium',
                swimmingPools: 'Schwimmbad',
                serviceStation: 'Tankstellen',
                weatherStation: 'Wetterstation',
                researchLaboratory: 'Laboratorium',
                port: 'Seehaefen',
                cities: 'BotKon',
            },
        };
    },
    watch:{
        selectedOption(){
            this.inputAdvanced = this.queries[this.selectedOption] + this.rangeValue;
        }
    },
    mounted() {
        this.detectDarkMode();
        window.addEventListener("resize", this.closeNavBar);
        window.matchMedia('(prefers-color-scheme: dark)').addListener(event => {
            this.isDarkMode = event.matches;
        });
        this.inputAdvanced=this.queries[this.selectedOption] + this.rangeValue;
    },
    beforeDestroy() {
        window.removeEventListener("resize", this.closeNavBar);
    },
    methods: {
        toggleNavBar() {
            this.menuOpen = !this.menuOpen;
        },
        closeNavBar() {
            this.menuOpen = false;
        },
        updateRange(event){
            this.rangeValue=parseInt(event.target.value);
            //check si ecrit LIMIT a la fin pour rajouter this.rangeValue a inputAdvanced
            const match = this.inputAdvanced.match(/LIMIT\s+(\d+)$/);
            if(this.inputAdvanced.endsWith('LIMIT ')){
                this.inputAdvanced += this.rangeValue;
            }
            else if(this.inputAdvanced.endsWith('LIMIT')){
                this.inputAdvanced += " ";
                this.inputAdvanced += this.rangeValue;
            }
            else if (match) {
                const number = parseInt(match[1]);
                const newInput = this.inputAdvanced.replace(`LIMIT ${number}`, `LIMIT ${this.rangeValue}`);
                this.inputAdvanced = newInput;
            }
            else{
                this.inputAdvanced=this.inputAdvanced.concat('\n LIMIT ', this.rangeValue);
            }
        },
        addDataCSV() {
            this.$emit('CSVSelected');
            this.$emit('popupShow');
            //this.$refs.fileInputCSV.click();
        },
        addDataJSON() {
            this.$emit('JsonSelected');
            this.$emit('popupShow');
        },
        addDataGeo() {
            this.$refs.fileInputGeo.click();
        },
        addDataOwl() {
            this.$refs.fileInputOwl.click();
        },
        handleFileInputGeo() {
            const file = event.target.files[0];
            this.$emit('file-selected', file);
        },
        handleFileInputOwl() {
            const file = event.target.files[0];
            let formData = new FormData();
            formData.append('file', file);
            $.ajax({
                url: 'https://localhost:8081/api/check-ontology',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (response) {
                    if(response == '[]') {
                        $.ajax({
                            url: 'https://localhost:8081/api/enrich',
                            type: 'POST',
                            data: formData,
                            processData: false,
                            contentType: false,
                            success: function () {
                                alert('Ontology enriched successfully!');
                            },
                            error: function () {
                                alert('Error while enriching ontology!');
                            }
                        });
                    } else {
                        console.log(response);
                    }
                },
                error: function () {
                    alert('Error while checking ontology!');
                }
            });
        },
        confirmRequest() {
            const url = 'https://localhost:8081/api/sparql-select';
            const data = {
                query: this.inputAdvanced,
                triplestore: ''
            };
            this.postJSON(url, data, this.handleResponse);
        },
        detectDarkMode() {
            this.isDarkMode = window.matchMedia('(prefers-color-scheme: dark)').matches;
        },
        postJSON(url, data, callback) {
            $.ajax({
            headers: { 
                'Accept': 'application/json',
                'Content-Type': 'application/json' 
            },
            'type': 'POST',
            'url': url,
            'data': JSON.stringify(data),
            'dataType': 'json',
            'success': callback,
            error: (error) => {
                this.$notify({
                title: 'Unauthorized access.',
                group:"notLoggedIn",
                text: 'You need to login to continue. Click here to go to the login page.',
                type: 'error',
                duration: 5000
            });
              console.error(error);
            }
            });
        },
        handleResponse(response) {
            const geoJSON = {
                type: 'FeatureCollection',
                name: this.icons[this.selectedOption],
                features: [],
            };
            const header = response['head']['vars'];
            response['results']['bindings'].forEach(item => {
                const feature = {
                    type: 'Feature',
                    geometry: {
                        type: 'Point',
                        coordinates: []
                    },
                    properties: {}
                };

                header.forEach(predicate => {
                    if (predicate === 'coordinates') {
                        var coords = String(item[predicate]['value']).split('/');
                        coords = coords[coords.length - 1];
                        coords = coords.split(',_');
                        if (coords) {
                            feature.geometry.coordinates = [parseFloat(coords[0]), parseFloat(coords[1])];
                        }
                    } else if (predicate === 'itemLabel') {
                        var label = String(item[predicate]['value']).split('/');
                        label = label[label.length - 1];
                        label = label.replace(/_/g, ' ');
                        feature.properties[predicate] = label;
                    } else {
                        feature.properties[predicate] = item[predicate]['value'];
                    }
                });

                geoJSON.features.push(feature);
            });

            console.log(JSON.stringify(geoJSON));
            // console.log(geoJSON);
            const blob = new Blob([JSON.stringify(geoJSON)], { type: 'application/json' });
            const file = new File([blob], 'data.json', { type: 'application/json' });
            this.$emit('file-selected', file);
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
.user-actions{
    background-color: rgb(241, 241, 241);
    border-radius: 10px;
}
.user-actions.dark {
    background-color: #1A202C;
    color: #fff;
}

.user-actions.light {
    background-color: #ffffff;
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

.navbar_button {
    display: none;
}

.navbar-menu {
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
.filter{
    border-radius: 5px;
    border: none;
    background-color: none;
    margin-top: 5px;
}
.filter p{
    padding: 6px 20px;
    border: none;
    background-color: none;
    color: inherit;
    cursor: pointer;
    font-size: 18px;
    font-weight: bold;
}
.filter:hover{
    background-color: #dee1e6;
}
.user-actions.dark .filter:hover{
    background-color: #4A5568;
}
.filter.active{
    background-color: #dee1e6;
}
.user-actions.dark .filter.active{
    background-color: #4A5568;
    color: white;
    transition: background-color 0.2s ease-in-out;
}
.inputbar{
    margin-left: 10px;
    margin-right: 10px;
}
.filtercontainer{
    display: flex;
    flex-direction: column;
    cursor: default;
}
.filtercontainer :nth-child(1){
    font-weight:normal;
    cursor: default;
}
.filtercontainer p{
    text-align: center;
    padding: 10px;
    cursor: default;
}
.addfile {
    border-radius: 5px;
    border: none;
    background-color: none;
    margin-top: 5px;
}
.addfile.active{
    background-color:#dee1e6;
}
.user-actions.dark .filter.active{
    background-color: #4A5568;
    color: white;
}
.addfile p {
    padding: 6px 20px;
    border: none;
    background-color: none;
    color: inherit;
    cursor: pointer;
    transition: background-color 0.2s ease-in-out;
    font-size: 18px;
    font-weight: bold;
}

.addfile:hover {
    background-color: #dee1e6;
}
.user-actions.dark .addfile:hover{
    background-color: #4A5568;
    color: white;
    transition: background-color 0.2s ease-in-out;
}

.addfileButton {
    flex-direction: column;
    display: flex;
    align-items: center;
}
.advancedMenu{
    border-radius: 5px;
    border: none;
    background-color: none;
    margin-top: 5px;
}
.advancedMenu p{
    padding: 6px 20px;
    border: none;
    background-color: none;
    color: inherit;
    cursor: pointer;
    transition: background-color 0.2s ease-in-out;
    font-size: 18px;
    font-weight: bold;
}
.advancedMenu:hover{
    background-color: #dee1e6;
}
.user-actions.dark .advancedMenu:hover{
    background-color: #4A5568;
    color: white;
    transition: background-color 0.2s ease-in-out;
}
.advancedMenu.active{
    background-color: #dee1e6;
}
.user-actions.dark .advancedMenu.active{
    background-color: #4A5568;
    color: white;
}
.textcontainer{
    width: 100%;
}
.advancedMenu textarea{
    margin-top: 8px;
    border-radius: 5px;
    width: 100%;
    min-height: 200px;
    font-size: 15px;
    resize: vertical;
}
.confirm {
    background-color: #EF4444;
    color: #fff;
}
.confirm:hover{
    background-color: #4A5568;
}

.navbar_button:hover {
    background-color: #81818a;
    color: white;
}

button:hover {
    background-color: #dee1e6;
}
.user-actions.dark button:hover{
    background-color: #4A5568;
    color: white;
}

.addfileButton>button {
    width: 95%;
    margin-bottom: 10px;
}
.addfileButton>button:hover{
    background-color: rgb(241, 241, 241);
}
.user-actions.dark .addfileButton>button:hover {
    background-color: #1A202C;
    color: #fff;
}

@media screen and (max-width: 768px) {

    .side_pannel {
        display: none;
    }

    .user-actions {
        resize: none;
        display: contents;
        flex: none;
        width: fit-content;
        min-width: 20px;
        height: fit-content;
        min-height: fit-content;
        resize: none;
        padding: 0px;
    }

    .user-actions.dark {
        width: fit-content;
    }

    .navbar_button {
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
        font-weight: lighter;
        margin-top: 0px;
        width: fit-content;
    }

    .navbar-menu.active {
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

    .navbar-menu.dark {
        background-color: #1A202C;
    }
}</style>