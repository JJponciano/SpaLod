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
            ]
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
            // TODO: Implement confirm request
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
        width: fit-content;
        min-width: fit-content;
        height: fit-content;
        min-height: fit-content;
        resize: none;
        padding: 0px;
    }
    .navbar_button{
        display: block;
        padding: 10px;
        border-radius: 5px;
        border: none;
        background-color: rgba(194, 194, 194, 0.603);
        color: inherit;
        cursor: pointer;
        transition: background-color 0.2s ease-in-out;
        font-size: 18px;
        font-weight:lighter;
        margin-top: 0px;
        width:fit-content;
    }
    .navbar-menu.active{
        padding: 15px 20px 15px 0px;
        border-radius: 15px;
        flex-direction: column;
        display: flex;
        align-items: start;
        height: fit-content;
        resize: horizontal;
        overflow: auto;
        width: fit-content;
        min-width: 220px;
        background-color: white;
    }
    .navbar-menu.dark{
        background-color: #1A202C;
    }
}
</style>