<template>
  <div class="user-actions" :class="{ dark: isDarkMode }">
    <button class="navbar_button" @click="toggleNavBar">Menu</button>
    <div class="side_pannel">
      <div class="data-title" :class="{ active: showData }">
        <p @click="showData = !showData">Data</p>
      </div>
      <div class="data" :class="{ active: showData }" v-if="showData">
        <div class="catalog" v-for="catalog of catalogs">
          <div class="title-container">
            <div
              class="arrow"
              :class="{
                down: catalog.expanded,
                right: !catalog.expanded,
              }"
              @click="expandCatalog(catalog.id)"
            ></div>
            <input
              type="checkbox"
              v-model="catalog.visible"
              @change="onCatalogVisibilityChange(catalog)"
            />
            <div class="title" @click="expandCatalog(catalog.id)">
              {{ catalog.id }}
            </div>
            <button @click="onClickCatalogMap(catalog.id)">🗺️</button>
            <button
              style="font-size: 12px"
              @click="onClickCatalogOwl(catalog.id)"
            >
              owl
            </button>
            <button @click="onClickDeleteCatalog(catalog.id)">🗑</button>
          </div>
          <div class="feature-container" v-if="catalog.expanded">
            <div class="feature" v-for="feature of catalog.features">
              <input
                type="checkbox"
                v-model="feature.visible"
                @change="onFeatureVisibilityChange(feature)"
              />
              <div @click="onClickFeature(feature.id)">
                {{ feature.id }}
              </div>
              <button @click="onClickDeleteFeature(feature.id)">🗑</button>
            </div>
          </div>
        </div>
      </div>
      <!-- <div class="filter" :class="{ active: showFilter }">
        <p @click="showFilter = !showFilter">Filter</p>
        <div class="filtercontainer" v-if="showFilter">
          <p>Max items number</p>
          <input
            class="inputbar"
            type="range"
            :min="min"
            :max="max"
            :step="step"
            v-model="rangeValue"
            @input="updateRange"
          />
          <p>{{ rangeValue }}</p>
        </div>
      </div> -->
      <!-- <div class="addfile" :class="{ active: showAddMenu }"> -->
      <div>
        <!-- <button @click="addDataCSV">CSV to GeoJSON</button>
          <button @click="addDataJSON">JSON to GeoJSON</button>
          <button @click="addDataGeo">Add GeoJSON</button>
          <input type="file" ref="fileInputGeo" style="display: none" accept=".json, .geojson"
            @change="handleFileInputGeo" /> -->
        <button class="addfile" @click="addDataOwl">Add Data</button>
        <input
          type="file"
          ref="fileInputOwl"
          style="display: none"
          accept=".owl, .json, .geojson, .las, .laz"
          @change="handleFileInputOwl"
        />
      </div>
      <div class="advancedMenu" :class="{ active: advancedMenuOpen }">
        <p @click="advancedMenuOpen = !advancedMenuOpen">Advanced Mode</p>
        <div class="textcontainer" v-if="advancedMenuOpen">
          <select v-model="selectedOption">
            <option
              v-for="(option, index) in options"
              :key="index"
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
          <textarea
            v-model="inputAdvanced"
            :placeholder="placeholders"
            spellcheck="false"
          ></textarea>
          <button ref="confirmRequest" @click="confirmRequest" class="confirm">
            Confirm Request
          </button>
        </div>
      </div>
      <!-- ---------------- DATASET--------------------------- -->
      <!-- <div id="app">
        <Dataset @change="handleChangeDataset" />
      </div> -->
      <!-- ---------------- END DATASET--------------------------- -->
    </div>

    <div class="navbar-menu" :class="{ active: menuOpen, dark: isDarkMode }">
      <ul class="navbar-nav">
        <li>
          <select v-model="selectedOption">
            <option
              v-for="(option, index) in options"
              :key="index"
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
        </li>
        <li class="filterButton">
          <button @click="filterData">Filter</button>
        </li>
        <li class="adddataButton">
          <button @click="addData">Add Data</button>
          <input
            type="file"
            ref="fileInput"
            style="display: none"
            accept="application/geojson"
            @change="handleFileInput"
          />
        </li>
        <li class="confirmButton">
          <button @click="confirmRequest" class="confirm">
            Confirm Request
          </button>
        </li>
      </ul>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.side_pannel {
  width: 100%;
  max-height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
}

.user-actions {
  padding: 20px;
  // border-radius: 5px;
  flex-direction: column;
  display: flex;
  align-items: start;
  // max-height: calc(100vh - 200px);
  height: 100%;
  resize: horizontal;
  overflow: auto;
  width: 320px;
  min-width: 220px;
  background-color: rgb(241, 241, 241);
  border-radius: 10px;
}

.user-actions.dark {
  background-color: #1a202c;
  color: #fff;
}

.user-actions.light {
  background-color: #ffffff;
  color: #1a202c;
}

select {
  display: block;
  font-size: 16px;
  font-weight: bold;
  padding: 10px;
  width: 100%;
  border: 2px solid #1a202c;
  border-radius: 5px;
  background-color: #4a5568;
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

.data-title {
  border-radius: 5px;
  border: none;
  background-color: none;
  margin-top: 5px;

  &:hover {
    background-color: #4a5568;
  }

  &.active {
    background-color: #4a5568;
    color: white;
    transition: background-color 0.2s ease-in-out;
    border-bottom-right-radius: 0px;
    border-bottom-left-radius: 0px;
  }

  p {
    padding: 6px 20px;
    border: none;
    background-color: none;
    color: inherit;
    cursor: pointer;
    font-size: 18px;
    font-weight: bold;
  }
}

.data {
  border-radius: 5px;
  border-top-right-radius: 0px;
  border-top-left-radius: 0px;
  border: none;
  background-color: none;
  overflow: auto;
  padding-bottom: 10px;

  &:hover {
    background-color: #4a5568;
  }

  &.active {
    background-color: #4a5568;
    color: white;
    transition: background-color 0.2s ease-in-out;
  }

  p {
    padding: 6px 20px;
    border: none;
    background-color: none;
    color: inherit;
    cursor: pointer;
    font-size: 18px;
    font-weight: bold;
  }

  .feature {
    display: flex;
    align-items: center;
    margin-left: 10px;
    border-left: 1px solid rgba(255, 255, 255, 0.5);
    padding-left: 10px;

    > input {
      margin-left: 10px;
    }

    > div {
      cursor: pointer;
      user-select: none;
      margin-left: 10px;
      word-break: break-all;
      font-size: 12px;
    }

    > button {
      width: auto;
      padding: 0px 5px;

      &:hover {
        color: #ddd;
      }
    }
  }

  .catalog {
    + .catalog {
      margin-top: 10px;
    }

    .title-container {
      display: flex;
      align-items: center;

      .arrow {
        border: solid white;
        border-width: 0 3px 3px 0;
        display: inline-block;
        padding: 3px;
        margin-right: 10px;
        margin-left: 5px;
        cursor: pointer;

        &.right {
          transform: rotate(-45deg);
        }

        &.down {
          transform: rotate(45deg);
        }
      }

      .title {
        font-size: 12px;
        word-break: break-all;
        flex: 1;
        cursor: pointer;
        margin-left: 10px;
        user-select: none;
      }

      button {
        width: auto;
        margin: 0px;
        padding: 0px 5px;

        &:hover {
          color: #ddd;
        }
      }
    }
  }
}

.filter {
  border-radius: 5px;
  border: none;
  background-color: none;
  margin-top: 5px;

  p {
    padding: 6px 20px;
    border: none;
    background-color: none;
    color: inherit;
    cursor: pointer;
    font-size: 18px;
    font-weight: bold;
  }
}

.user-actions.dark .filter:hover {
  background-color: #4a5568;
}

.filter.active {
  background-color: #dee1e6;
}

.user-actions.dark .filter.active {
  background-color: #4a5568;
  color: white;
  transition: background-color 0.2s ease-in-out;
}

.inputbar {
  margin-left: 10px;
  margin-right: 10px;
}

.filtercontainer {
  display: flex;
  flex-direction: column;
  cursor: default;
}

.filtercontainer :nth-child(1) {
  font-weight: normal;
  cursor: default;
}

.filtercontainer p {
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

.addfile.active {
  background-color: #dee1e6;
}

.user-actions.dark .addfile.active {
  background-color: #4a5568;
}

.user-actions.dark .filter.active {
  background-color: #4a5568;
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

.user-actions.dark .addfile:hover {
  background-color: #4a5568;
  color: white;
  transition: background-color 0.2s ease-in-out;
}

.addfileButton {
  flex-direction: column;
  display: flex;
  align-items: center;
}

.advancedMenu {
  border-radius: 5px;
  border: none;
  background-color: none;
  margin-top: 5px;
}

.advancedMenu p {
  padding: 6px 20px;
  border: none;
  background-color: none;
  color: inherit;
  cursor: pointer;
  transition: background-color 0.2s ease-in-out;
  font-size: 18px;
  font-weight: bold;
}

.advancedMenu:hover {
  background-color: #dee1e6;
}

.user-actions.dark .advancedMenu:hover {
  background-color: #4a5568;
  color: white;
  transition: background-color 0.2s ease-in-out;
}

.advancedMenu.active {
  background-color: #dee1e6;
}

.user-actions.dark .advancedMenu.active {
  background-color: #4a5568;
  color: white;
}

.textcontainer {
  width: 100%;
}

.advancedMenu textarea {
  margin-top: 8px;
  border-radius: 5px;
  width: 100%;
  min-height: 200px;
  font-size: 15px;
  resize: vertical;
}

.confirm {
  background-color: #ef4444;
  color: #fff;
}

.confirm:hover {
  background-color: #4a5568;
}

.navbar_button:hover {
  background-color: #81818a;
  color: white;
}

button:hover {
  background-color: #dee1e6;
}

.user-actions.dark button:hover {
  background-color: #4a5568;
  color: white;
}

.addfileButton > button {
  width: 95%;
  margin-bottom: 10px;
}

.addfileButton > button:hover {
  background-color: rgb(241, 241, 241);
}

.user-actions.dark .addfileButton > button:hover {
  background-color: #1a202c;
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
    background-color: #1a202c;
  }
}
</style>

<script>
import $ from "jquery";
import { $ajax } from "../services/api";
import Dataset from "./Dataset.vue";
import { cookies } from "../services/login";
import {
  getAllCatalogs,
  setFeatureVisibility,
  setCatalogVisibility,
  triggerFeatureClick,
  expandCatalog,
} from "../services/map";
import { ref } from "vue";
import { removeFeature, removeCatalog, getCatalog } from "../services/api-geo";

$.ajaxSetup({
  xhrFields: {
    withCredentials: true,
  },
});

export default {
  name: "User action",
  components: {
    Dataset,
  },
  setup() {
    const catalogs = getAllCatalogs();
    const isDarkMode = ref(false);
    const menuOpen = ref(false);
    const showAddMenu = ref(false);
    const showFilter = ref(false);
    const showCatalog = ref(false);
    const showData = ref(true);
    const min = ref(100);
    const max = ref(1000);
    const rangeValue = ref(150);
    const step = ref(50);
    const inputAdvanced = ref("");
    const inputCatalog = ref("");
    const advancedMenuOpen = ref(false);
    const selectedOption = ref("default");

    return {
      isDarkMode,
      menuOpen,
      showAddMenu,
      showFilter,
      showCatalog,
      showData,
      min,
      max,
      rangeValue,
      step,
      inputAdvanced,
      inputCatalog,
      advancedMenuOpen,
      placeholders: "Write your custom request here",
      selectedOption,
      options: [
        { label: "Default", value: "default" },
        { label: "Schule (Q3914)", value: "schools" },
        { label: "20 biggest cities in Germany", value: "twentyBiggestCities" },
        {
          label: "10 biggest football stadiums in Germany",
          value: "tenBiggestStadiums",
        },
        { label: "Krankenhaus (Q16917)", value: "hospitals" },
        { label: "Polizeistation (Q861951)", value: "policeStations" },
        { label: "Feuerwache (Q1195942)", value: "fireStations" },
        { label: "Supermarkt (Q180846)", value: "supermarkets" },
        { label: "Museen (Q33506)", value: "museums" },
        { label: "Bibliotheken (Q7075)", value: "libraries" },
        { label: "Bahnhöfe (Q55488)", value: "trainStations" },
        { label: "Banken (Q22687)", value: "banks" },
        { label: "Restaurants (Q11707)", value: "restaurants" },
        { label: "Kinos (Q41253)", value: "cinemas" },
        { label: "Denkmäler (Q4989906)", value: "monuments" },
        { label: "Hotels (Q27686)", value: "hotels" },
        { label: "Flughäfen (Q1248784)", value: "airports" },
        { label: "Stadien (Q483110)", value: "stadiums" },
        { label: "Schwimmbäder (Q200023)", value: "swimmingPools" },
        { label: "Tankstellen (Q205495)", value: "serviceStation" },
        { label: "Wetterstationen (Q190107)", value: "weatherStation" },
        {
          label: "Forschungslaboratorien (Q483242)",
          value: "researchLaboratory",
        },
        { label: "Häfen (Q44782)", value: "port" },
        { label: "Städte (Q515)", value: "cities" },
      ],
      queries: {
        default:
          "SELECT ?d ?itemID ?itemLabel ?coordinates WHERE { ?d rdf:type dcat:Dataset . ?d spalod:hasFeature ?itemID. ?itemID geosparql:hasGeometry ?g. ?g geosparql:asWKT ?coordinates . ?itemID spalod:itemlabel ?itemLabel} LIMIT ",
        schools:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q3914> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        twentyBiggestCities:
          'SELECT DISTINCT ?city ?cityLabel ?latitude ?longitude ?instanceOfCity ?population WHERE {\n SERVICE wikibase:label { bd:serviceParam wikibase:language "de". } \n VALUES ?instanceOfCity { \n wd:Q515 \n  } \n  ?city (wdt:P31/(wdt:P279*)) ?instanceOfCity; \n wdt:P17 wd:Q183;\n  p:P625 ?statement. \n ?statement psv:P625 ?coordinate_node. \n ?coordinate_node wikibase:geoLatitude ?latitude; \n wikibase:geoLongitude ?longitude.\nOPTIONAL { ?city wdt:P1082 ?population. } \n } \nORDER BY DESC (?population) \nLIMIT 20',
        hospitals:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q16917> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        policeStations:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q861951> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        fireStations:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q1195942> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        supermarkets:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q180846> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        museums:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q33506> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        libraries:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q7075> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        trainStations:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q55488> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        banks:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q22687> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        restaurants:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q11707> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        cinemas:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q41253> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        monuments:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q4989906> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        hotels:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q27686> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        airports:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q1248784> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        stadiums:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q483110> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        swimmingPools:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q200023> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        serviceStation:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q205495> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        weatherStation:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q190107> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        researchLaboratory:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q483242> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        port: "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q44782> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
        cities:
          "SELECT ?item ?itemLabel ?coordinates ?category WHERE {\n ?item <spalod:category> <http://www.wikidata.org/entity/Q515> .\n  ?item <spalod:itemLabel> ?itemLabel .\n  ?item <spalod:coordinates> ?coordinates .\n ?item <spalod:category> ?category .} LIMIT ",
      },
      icons: {
        default: "default",
        schools: "HS",
        twentyBiggestCities: "BotKon",
        hospitals: "KHV",
        policeStations: "BFW",
        fireStations: "Feuerwehr",
        supermarkets: "Supermarkt",
        museums: "Museen",
        libraries: "Bibliothek",
        trainStations: "Bahnhof",
        banks: "Bank",
        restaurants: "Restaurant",
        cinemas: "Kino",
        monuments: "Denkmal",
        hotels: "Hotel",
        airports: "Flughafen",
        stadiums: "Stadium",
        swimmingPools: "Schwimmbad",
        serviceStation: "Tankstellen",
        weatherStation: "Wetterstation",
        researchLaboratory: "Laboratorium",
        port: "Seehaefen",
        cities: "BotKon",
      },
      catalogs,
    };
  },
  watch: {
    selectedOption() {
      this.inputAdvanced = this.queries[this.selectedOption] + this.rangeValue;
    },
  },
  mounted() {
    this.detectDarkMode();
    window.addEventListener("resize", this.closeNavBar);
    window.matchMedia("(prefers-color-scheme: dark)").addListener((event) => {
      this.isDarkMode = event.matches;
    });
    this.inputAdvanced = this.queries[this.selectedOption] + this.rangeValue;

    // Implementing OGC API - Records
    this.getdateset();
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.closeNavBar);
  },
  methods: {
    // ---------------- DATASET---------------------------
    getdateset() {
      const url = new URL(window.location.href);
      const queryString = url.pathname;
      console.log(url);
      if (
        queryString.includes("collections") ||
        queryString.includes("conformance") ||
        queryString === "/"
      ) {
        $ajax({
          url: "/api/spalodWFS" + queryString + url.search,
          type: "GET",
          // dataType: "json",
          processData: false,
          contentType: false,
          success: (response, textStatus, xhr) => {
            const fileUrl = response;
            if (fileUrl.endsWith(".json")) {
              let parts = queryString.split("/");
              let datasetID = parts.pop();
              const url = "/api/sparql-query/";
              const data = {
                query:
                  "SELECT ?itemID ?coordinates WHERE {  spalod:" +
                  datasetID +
                  " spalod:hasFeature ?itemID. ?itemID geosparql:hasGeometry ?g. ?g geosparql:asWKT ?coordinates . }",
                triplestore:
                  import.meta.env.VITE_APP_GRAPH_DB + "/repositories/Spalod",
              };
              this.postJSON(url, data, this.handleResponse);
              //TODO download the file but crash the view of data
              //window.location.href = fileUrl;
            }
          },
          error: (xhr, textStatus, errorThrown) => {
            console.log(error);
          },
        });
      }
    },
    handleChangeDataset(newValue) {
      console.log("New value selected:", newValue);
      // Here, you can handle the new selected value as needed
    },
    toggleNavBar() {
      this.menuOpen = !this.menuOpen;
    },
    closeNavBar() {
      this.menuOpen = false;
    },
    updateRange(event) {
      this.rangeValue = parseInt(event.target.value);
      //check si ecrit LIMIT a la fin pour rajouter this.rangeValue a inputAdvanced
      const match = this.inputAdvanced.match(/LIMIT\s+(\d+)$/);
      if (this.inputAdvanced.endsWith("LIMIT ")) {
        this.inputAdvanced += this.rangeValue;
      } else if (this.inputAdvanced.endsWith("LIMIT")) {
        this.inputAdvanced += " ";
        this.inputAdvanced += this.rangeValue;
      } else if (match) {
        const number = parseInt(match[1]);
        const newInput = this.inputAdvanced.replace(
          `LIMIT ${number}`,
          `LIMIT ${this.rangeValue}`
        );
        this.inputAdvanced = newInput;
      } else {
        this.inputAdvanced = this.inputAdvanced.concat(
          "\n LIMIT ",
          this.rangeValue
        );
      }
    },
    updateTripleData(tripleData, operation, callback) {
      const addOperation = {
        operation: operation,
        tripleData: tripleData,
      };
      $ajax({
        url: "/api/update",
        type: "POST",
        data: JSON.stringify(addOperation),
        contentType: "application/json",
        success: callback,
        error: function (error) {
          console.log(error);
        },
      });
    },
    addDataCSV() {
      this.$emit("CSVSelected");
      this.$emit("popupShow");
      //this.$refs.fileInputCSV.click();
    },
    addDataJSON() {
      this.$emit("JsonSelected");
      this.$emit("popupShow");
    },
    addDataGeo() {
      this.$refs.fileInputGeo.click();
    },
    addDataOwl() {
      this.$refs.fileInputOwl.click();
    },
    handleFileInputGeo() {
      const file = event.target.files[0];
      // this.$emit('file-selected', file);
      const formData = new FormData();
      formData.append("file", file);

      $ajax({
        url: "/api/uplift",
        type: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: (response, textStatus, xhr) => {
          window.location.href = response;
        },
        error: (xhr, textStatus, errorThrown) => {
          // Display an error message
          alert("An error occurred while uploading the file.");
          console.error(errorThrown);
        },
      });
    },
    seek_unknown(response) {
      if (response == "[]") {
        alert("Ontology enriched successfully!");

        // $ajax({
        //   url: "/api/enrich",
        //   type: "POST",
        //   data: formData,
        //   processData: false,
        //   contentType: false,
        //   success: function () {
        //     alert("Ontology enriched successfully!");
        //   },
        //   error: function () {
        //     alert("Error while enriching ontology!");
        //   },
        // });
      } else {
        alert(
          "Ontology enriched successfully! Please indicate equivalent properties !"
        );

        console.log(response);
        this.$emit("properties_unknown", response);
      }
    },
    post_checkont(url, data, callback) {
      $ajax({
        xhr: () => {
          const xhr = new XMLHttpRequest();

          xhr.upload.onprogress = (event) => {
            const percentage =
              Math.floor((event.loaded / event.total) * 100) + "%";
            console.log(percentage);
          };

          return xhr;
        },
        url,
        type: "POST",
        data,
        processData: false,
        contentType: false,
        success: callback,
        error: () => {
          alert("Error while checking ontology!");
        },
      });
    },

    handleFileInputOwl(event) {
      this.$emit("fileSelected", event.target.files[0]);
    },
    confirmRequest() {
      const url = "/api/sparql-query/";
      const data = {
        query: this.inputAdvanced,
        triplestore: import.meta.env.VITE_APP_GRAPH_DB + "/repositories/Spalod",
      };
      this.postJSON(url, data, this.handleResponse);
    },
    detectDarkMode() {
      this.isDarkMode = window.matchMedia(
        "(prefers-color-scheme: dark)"
      ).matches;
    },
    postJSON(url, data, callback) {
      const vueInstance = this; // Store the Vue instance reference
      $ajax({
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
          "X-CSRFToken": cookies()["csrftoken"],
        },
        type: "POST",
        url: url,
        data: JSON.stringify(data),
        dataType: "json",
        success: callback,
        statusCode: {
          401: function () {
            vueInstance.$notify({
              title: "Unauthorized access.",
              group: "notLoggedIn",
              text: "You need to login to continue. Click here to go to the login page.",
              type: "error",
              duration: 5000,
            });
          },
          500: function () {
            vueInstance.$notify({
              title: "Bad Request",
              text: "Please check the syntax of your request.",
              type: "error",
              duration: 5000,
            });
          },
        },
        error: (error) => {
          if (error.status === 0 && !error.statusText && !error.responseText) {
            this.$notify({
              title: "Not Connected",
              text: "Please log in or register to continue.",
              group: "notLoggedIn",
              type: "error",
              duration: 2000, // notification will disappear after 5 seconds
            });
          } else {
            this.$notify({
              title: error.responseText,
              text: "Please log in or register to continue.",
              group: "notLoggedIn",
              type: "error",
              duration: 2000, // notification will disappear after 5 seconds
            });
          }
          console.log(error);
        },
      });
    },
    extractCoordinates(wkt) {
      const match = wkt.match(/POINT\s*\(([^)]+)\)/);
      if (match) {
        const [longitude, latitude] = match[1].split(" ");
        return {
          latitude: parseFloat(latitude),
          longitude: parseFloat(longitude),
        };
      } else {
        throw new Error("Invalid WKT format");
      }
    },
    handleResponse(response) {
      // HERE THE FUNCTION CREATE A JSON OBJECT THAT  IS USED LATER IN RDFData (JJ)
      const geoJSON = {
        type: "FeatureCollection",
        name: this.icons[this.selectedOption],
        features: [],
      };
      console.log("--------------------------------------");

      const header = response["head"]["vars"];
      response["results"]["bindings"].forEach((item) => {
        const feature = {
          type: "Feature",
          geometry: {
            type: "Point",
            coordinates: [],
          },
          properties: {},
        };

        header.forEach((variable) => {
          let value = String(item[variable]["value"]);
          // if the value is a wkt, update the coords
          const match = value.match(/POINT\s*\(([^)]+)\)/);
          if (match) {
            const [longitude, latitude] = match[1].split(" ");
            feature.geometry.coordinates = [
              parseFloat(longitude),
              parseFloat(latitude),
            ];
          }

          //   if (variable === "coordinates") {
          //     var coords = String(item[variable]["value"]).split("/");
          //     coords = coords[coords.length - 1];
          //     coords = coords.split("#");
          //     coords = coords[coords.length - 1];
          //     coords = coords.split(",_");
          //     if (coords) {
          //       feature.geometry.coordinates = [
          //         parseFloat(coords[0]),
          //         parseFloat(coords[1]),
          //       ];
          //     }
          //   } else
          if (variable === "itemLabel") {
            //TODO we should change this as I (jj) did for the coordinates just before
            var label = String(item[variable]["value"]).split("/");
            label = label[label.length - 1];
            // label = label.split('#');
            // label = label[label.length - 1];
            label = label.replace(/_/g, " ");
            feature.properties[variable] = label;
          } else {
            feature.properties[variable] = item[variable]["value"];
          }
        });

        geoJSON.features.push(feature);
      });

      console.log(geoJSON);

      const blob = new Blob([JSON.stringify(geoJSON)], {
        type: "application/json",
      });
      const file = new File([blob], "data.json", { type: "application/json" });
      this.$emit("file-selected", file);
    },
    onFeatureVisibilityChange(feature) {
      setFeatureVisibility(feature.id, feature.visible);
    },
    onClickFeature(featureId) {
      triggerFeatureClick(featureId);
    },
    onClickDeleteFeature(featureId) {
      setFeatureVisibility(featureId, false, true);
      removeFeature(featureId);
    },
    onCatalogVisibilityChange(catalog) {
      setCatalogVisibility(catalog.id, catalog.visible);
    },
    onClickDeleteCatalog(catalogId) {
      setCatalogVisibility(catalogId, false, true);
      removeCatalog(catalogId);
    },
    async onClickCatalogMap(catalogId) {
      const res = await getCatalog(catalogId);
      const mapUrl = res
        .map((x) => x.metadatas)
        .find((x) => x.key === "http://spalod/hasHTML")?.value;

      window.open(mapUrl, "_blank");
    },
    async onClickCatalogOwl(catalogId) {
      const res = await getCatalog(catalogId);
      const owlUrl = res
        .map((x) => x.metadatas)
        .find((x) => x.key === "http://spalod/hasOWL")?.value;

      window.open(owlUrl, "_blank");
    },
    expandCatalog(catalogId) {
      expandCatalog(catalogId);
    },
  },
};
</script>
