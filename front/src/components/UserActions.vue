<template>
  <div class="user-actions dark">
    <button class="navbar_button" @click="toggleNavBar">Menu</button>
    <div class="side_pannel">
      <div class="data-title" :class="{ active: showData }">
        <p @click="showData = !showData">Data</p>
      </div>
      <div class="data" :class="{ active: showData }" v-if="showData">
        <div class="catalog" v-for="catalog of catalogs" :key="catalog.id">
          <div class="title-container">
            <div class="arrow-container" @click="expandCatalog(catalog.id)">
              <div
                class="arrow"
                :class="{
                  down: catalog.expanded,
                  right: !catalog.expanded,
                }"
                v-if="
                  catalog.type !== 'SPARQL_QUERY' || catalog.features.length > 0
                "
              ></div>
            </div>
            <div class="checkbox-container">
              <input
                type="checkbox"
                v-model="catalog.visible"
                @change="onCatalogVisibilityChange(catalog)"
                v-if="
                  catalog.type !== 'SPARQL_QUERY' || catalog.features.length > 0
                "
              />
            </div>
            <div class="title" @click="expandCatalog(catalog.id)">
              {{ displayLastPortion(catalog.id) }}
            </div>
            <button
              @click="onClickCatalogMap(catalog.id)"
              v-if="catalog.type !== 'SPARQL_QUERY'"
            >
              🗺️
            </button>
            <button
              style="font-size: 12px"
              @click="onClickCatalogOwl(catalog.id)"
              v-if="catalog.type !== 'SPARQL_QUERY'"
            >
              owl
            </button>
            <button
              style="font-size: 12px"
              @click="onClickSparqlQueryCsv(catalog)"
              v-if="catalog.type === 'SPARQL_QUERY'"
            >
              csv
            </button>
            <button @click="onClickDeleteCatalog(catalog.id)">🗑</button>
          </div>
          <div class="feature-container" v-if="catalog.expanded">
            <div class="feature alt" v-if="catalog.features.length === 0">
              <div class="title alt">Loading features...</div>
              <div class="loader"></div>
            </div>
            <div
              class="feature"
              v-for="feature of catalog.features"
              :key="feature.id"
            >
              <input
                type="checkbox"
                v-model="feature.visible"
                @change="onFeatureVisibilityChange(feature)"
              />
              <div class="title" @click="onClickFeature(feature.id)">
                {{ displayLastPortion(feature.id) }}
              </div>
              <button @click="onClickDeleteFeature(feature.id)">🗑</button>
            </div>
          </div>
        </div>
      </div>

      <div>
        <button class="addfile" @click="addData" v-if="getProcess() === ''">
          Add Data
        </button>
        <div class="addfile-alt" v-else>
          <div class="title">Add Data</div>
          <div class="progress">
            {{ getProcess() }}
          </div>
          <div class="loader"></div>
        </div>
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
            placeholder="Write your custom request here"
            spellcheck="false"
            @change="updateRange"
          ></textarea>
          <div class="filter">
            <div class="filtercontainer">
              <p>Max items number</p>
              <input
                class="inputbar"
                type="range"
                min="1"
                max="1000"
                step="1"
                v-model="rangeValue"
                @input="updateRange"
              />
              <p>{{ rangeValue }}</p>
            </div>
          </div>
          <button ref="confirmRequest" @click="confirmRequest" class="confirm">
            Confirm Request
          </button>
        </div>
      </div>
    </div>

    <div class="navbar-menu dark" :class="{ active: menuOpen }">
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
@use "sass:color";

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
  width: 100%;
  text-align: left;

  &.confirm {
    background-color: #ef4444;

    &:hover {
      background-color: color.scale(#ef4444, $lightness: 10%);
    }
  }
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

    &.alt {
      justify-content: center;
    }

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

    > .title {
      flex: 1;

      &.alt {
        flex: none;
      }
    }

    > button {
      width: auto;
      padding: 0px 5px;

      &:hover {
        color: #ddd;
      }
    }

    .loader {
      margin-left: 10px;
      width: 18px;
      aspect-ratio: 1;
      border-radius: 50%;
      border: 3px solid white;
      animation: l20-1 0.8s infinite linear alternate,
        l20-2 1.6s infinite linear;
    }
  }

  .catalog {
    + .catalog {
      margin-top: 10px;
    }

    .title-container {
      display: flex;
      align-items: center;

      .arrow-container {
        width: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;

        .arrow {
          border: solid white;
          border-width: 0 3px 3px 0;
          display: inline-block;
          padding: 3px;

          &.right {
            transform: rotate(-45deg);
          }

          &.down {
            transform: rotate(45deg);
          }
        }
      }

      .checkbox-container {
        width: 25px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .title {
        font-size: 12px;
        word-break: break-all;
        flex: 1;
        cursor: pointer;
        user-select: none;
      }

      button {
        width: auto;
        margin: 0px;
        padding: 0px 5px;

        &:hover {
          color: #ddd;
          background-color: transparent;
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

.addfile,
.addfile-alt {
  border-radius: 5px;
  border: none;
  background-color: none;
  margin-top: 5px;
}

.addfile-alt {
  padding: 6px 20px;
  display: flex;
  align-items: center;
  cursor: default;

  .title {
    font-size: 18px;
    font-weight: bold;
    flex: 1;
  }

  .loader {
    margin-left: 10px;
    width: 25px;
    aspect-ratio: 1;
    border-radius: 50%;
    border: 4px solid white;
    animation: l20-1 0.8s infinite linear alternate, l20-2 1.6s infinite linear;
  }
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

.addfile:hover {
  background-color: #dee1e6;
}

.user-actions.dark .addfile:hover {
  background-color: #4a5568;
  color: white;
  transition: background-color 0.2s ease-in-out;
}

.advancedMenu {
  border-radius: 5px;
  border: none;
  background-color: none;
  margin-top: 5px;

  &:hover {
    background-color: #dee1e6;
  }

  &.active {
    background-color: #dee1e6;
  }

  p {
    padding: 6px 20px;
    border: none;
    background-color: none;
    color: inherit;
    cursor: pointer;
    transition: background-color 0.2s ease-in-out;
    font-size: 18px;
    font-weight: bold;
  }

  textarea {
    margin-top: 8px;
    border-radius: 5px;
    margin: 0px 10px;
    width: calc(100% - 20px);
    min-height: 200px;
    font-size: 15px;
    resize: vertical;
  }

  select {
    background-color: white;
    color: black;
    margin: 0px 10px;
    margin-bottom: 5px;
    width: calc(100% - 20px);
    border: none;
    appearance: none;
    background-image: url("data:image/svg+xml;utf8,<svg fill='%234a5568' height='24' viewBox='0 0 24 24' width='24' xmlns='http://www.w3.org/2000/svg'><path d='M7 10l5 5 5-5z'/><path d='M0 0h24v24H0z' fill='none'/></svg>");
    background-repeat: no-repeat;
    background-position-x: 100%;
    background-position-y: 5px;
  }
}

.user-actions.dark .advancedMenu:hover {
  background-color: #4a5568;
  color: white;
  transition: background-color 0.2s ease-in-out;
}

.user-actions.dark .advancedMenu.active {
  background-color: #4a5568;
  color: white;
}

.textcontainer {
  width: 100%;
}

.navbar_button:hover {
  background-color: #81818a;
  color: white;
}

button:hover {
  background-color: #dee1e6;
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

@keyframes l20-1 {
  0% {
    clip-path: polygon(50% 50%, 0 0, 50% 0%, 50% 0%, 50% 0%, 50% 0%, 50% 0%);
  }
  12.5% {
    clip-path: polygon(
      50% 50%,
      0 0,
      50% 0%,
      100% 0%,
      100% 0%,
      100% 0%,
      100% 0%
    );
  }
  25% {
    clip-path: polygon(
      50% 50%,
      0 0,
      50% 0%,
      100% 0%,
      100% 100%,
      100% 100%,
      100% 100%
    );
  }
  50% {
    clip-path: polygon(
      50% 50%,
      0 0,
      50% 0%,
      100% 0%,
      100% 100%,
      50% 100%,
      0% 100%
    );
  }
  62.5% {
    clip-path: polygon(
      50% 50%,
      100% 0,
      100% 0%,
      100% 0%,
      100% 100%,
      50% 100%,
      0% 100%
    );
  }
  75% {
    clip-path: polygon(
      50% 50%,
      100% 100%,
      100% 100%,
      100% 100%,
      100% 100%,
      50% 100%,
      0% 100%
    );
  }
  100% {
    clip-path: polygon(
      50% 50%,
      50% 100%,
      50% 100%,
      50% 100%,
      50% 100%,
      50% 100%,
      0% 100%
    );
  }
}
@keyframes l20-2 {
  0% {
    transform: scaleY(1) rotate(0deg);
  }
  49.99% {
    transform: scaleY(1) rotate(135deg);
  }
  50% {
    transform: scaleY(-1) rotate(0deg);
  }
  100% {
    transform: scaleY(-1) rotate(-135deg);
  }
}
</style>

<script>
import $ from "jquery";
import {
  getAllCatalogs,
  setFeatureVisibility,
  setCatalogVisibility,
  triggerFeatureClick,
  expandCatalog,
  addSparqlQueryResult,
} from "../services/geo";
import { ref } from "vue";
import {
  removeFeature,
  removeCatalog,
  getCatalog,
  sparqlQuery,
} from "../services/api-geo";
import { sparqlQueries } from "../services/constants";
import { getProcess } from "../services/geo-upload";

$.ajaxSetup({
  xhrFields: {
    withCredentials: true,
  },
});

export default {
  name: "User action",
  setup() {
    const catalogs = getAllCatalogs();
    const menuOpen = ref(false);
    const showAddMenu = ref(false);
    const showFilter = ref(false);
    const showCatalog = ref(false);
    const showData = ref(true);
    const rangeValue = ref(150);
    const inputAdvanced = ref("");
    const inputCatalog = ref("");
    const advancedMenuOpen = ref(false);
    const selectedOption = ref("default");

    return {
      menuOpen,
      showAddMenu,
      showFilter,
      showCatalog,
      showData,
      rangeValue,
      inputAdvanced,
      inputCatalog,
      advancedMenuOpen,
      selectedOption,
      options: sparqlQueries.options,
      queries: sparqlQueries.queries,
      catalogs,
      getProcess,
    };
  },
  watch: {
    selectedOption() {
      this.inputAdvanced = this.queries[this.selectedOption] + this.rangeValue;
    },
  },
  mounted() {
    window.addEventListener("resize", this.closeNavBar);
    this.inputAdvanced = this.queries[this.selectedOption] + this.rangeValue;
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
    updateRange() {
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
    addData() {
      const input = document.createElement("input");
      input.type = "file";
      input.accept = ".owl, .json, .geojson, .las, .laz";
      input.click();
      input.onchange = async () => {
        this.$emit("fileSelected", input.files[0]);
      };
    },
    async confirmRequest() {
      const result = await sparqlQuery(this.inputAdvanced);

      if (result.length > 0) {
        addSparqlQueryResult(result);
      } else {
        alert("The query fetch no data");
      }
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
    async onCatalogVisibilityChange(catalog) {
      await setCatalogVisibility(catalog.id, catalog.visible);
      this.$forceUpdate();
    },
    onClickDeleteCatalog(catalogId) {
      setCatalogVisibility(catalogId, false, true);
      removeCatalog(catalogId);
    },
    onClickSparqlQueryCsv(catalog) {
      if (catalog.raw?.length > 0) {
        const rows = [];

        rows.push(Object.keys(catalog.raw[0].metadatas));
        rows.push(...catalog.raw.map((x) => Object.values(x.metadatas)));

        const csvContent = rows
          .map((e) => e.map((x) => `"${x.replace(/"/g, '""')}"`).join(","))
          .join("\n");

        const blob = new Blob([csvContent], {
          type: "text/csv;charset=utf-8;",
        });

        const link = document.createElement("a");
        if (link.download !== undefined) {
          const url = URL.createObjectURL(blob);
          link.setAttribute("href", url);
          link.setAttribute("download", catalog.id);
          link.style.visibility = "hidden";
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
        }
      }
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
    displayLastPortion(str) {
      return str.replace(/.*\//, "").replace(/.*#/, "");
    },
  },
};
</script>
