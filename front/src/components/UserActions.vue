<template>
  <div class="user-actions dark">
    <button class="navbar_button" @click="toggleNavBar">Menu</button>
    <div class="side_pannel">
      <button
        class="data-title"
        @click="showData = !showData"
        :class="{ active: showData }"
      >
        <div>Data</div>

        <button class="addfile" @click="addData" v-if="getProgress() === -1">
          Import file
        </button>
        <div class="addfile-alt" v-else>
          <div v-if="getProgress() < 100" class="progress">
            Upload: {{ getProgress() }}%
          </div>
          <div v-else class="progress">Treatment...</div>
          <div class="loader"></div>
        </div>
      </button>
      <div class="data" :class="{ active: showData }" v-if="showData">
        <VirtualScroller
          :items="getGeoItems()"
          :itemSize="24"
          style="height: 100%"
        >
          <template v-slot:item="{ item: geoItem, options }">
            <div class="geo-item">
              <div
                class="catalog title-container"
                v-if="geoItem.type === 'catalog'"
              >
                <div
                  class="arrow-container"
                  @click="expandCatalog(geoItem.item.id)"
                >
                  <div
                    class="arrow"
                    :class="{
                      down: geoItem.item.expanded,
                      right: !geoItem.item.expanded,
                    }"
                    v-if="
                      geoItem.item.type !== 'SPARQL_QUERY' ||
                      geoItem.item.datasets.length > 0
                    "
                  ></div>
                </div>
                <div class="checkbox-container">
                  <input
                    type="checkbox"
                    v-model="geoItem.item.visible"
                    @change="onCatalogVisibilityChange(geoItem.item)"
                    v-if="
                      geoItem.item.type !== 'SPARQL_QUERY' ||
                      geoItem.item.datasets.length > 0
                    "
                  />
                </div>
                <div class="title" @click="expandCatalog(geoItem.item.id)">
                  {{ displayItem(geoItem.item) }}
                </div>
                <button @click="onClickDeleteCatalog(geoItem.item)">🗑</button>
              </div>

              <!-- <div
            class="dataset alt"
            v-if="
              catalog.type !== 'SPARQL_QUERY' && catalog.datasets.length === 0
            "
          >
            <div class="title alt">Loading datasets...</div>
            <div class="loader"></div>
          </div> -->

              <div
                class="dataset title-container"
                v-if="geoItem.type === 'dataset'"
              >
                <div
                  class="arrow-container"
                  @click="expandDataset(geoItem.item.id)"
                  v-if="
                    geoItem.item.type !== 'SPARQL_QUERY' ||
                    geoItem.item.features.length > 0
                  "
                >
                  <div
                    class="arrow"
                    :class="{
                      down: geoItem.item.expanded,
                      right: !geoItem.item.expanded,
                    }"
                  ></div>
                </div>
                <div class="checkbox-container">
                  <input
                    type="checkbox"
                    v-model="geoItem.item.visible"
                    @change="onDatasetVisibilityChange(geoItem.item)"
                  />
                </div>
                <div
                  class="title"
                  @dblclick="onDoubleClickDataset(geoItem.item.id)"
                >
                  {{ displayItem(geoItem.item) }}
                </div>

                <button
                  style="font-size: 12px"
                  @click="onClickSparqlQueryCsv(geoItem.item)"
                  v-if="geoItem.item.type === 'SPARQL_QUERY'"
                >
                  csv
                </button>

                <button
                  @click="onClickDatasetMap(geoItem.item.id)"
                  v-if="geoItem.item.type !== 'SPARQL_QUERY'"
                >
                  🗺️
                </button>

                <button
                  style="font-size: 12px"
                  @click="onClickDatasetOwl(geoItem.item.id)"
                  v-if="geoItem.item.type !== 'SPARQL_QUERY'"
                >
                  owl
                </button>
                <button @click="onClickDeleteDataset(geoItem.item)">🗑</button>
              </div>

              <!-- <div class="feature alt" v-if="dataset.features.length === 0">
            <div class="title alt">Loading features...</div>
            <div class="loader"></div>
          </div> -->

              <div class="feature" v-if="geoItem.type === 'feature'">
                <input
                  type="checkbox"
                  v-model="geoItem.item.visible"
                  @change="onFeatureVisibilityChange(geoItem.item)"
                />
                <div
                  class="title"
                  @click="onClickFeature(geoItem.item.id)"
                  @dblclick="onDoubleClickFeature(geoItem.item.id)"
                >
                  {{ displayItem(geoItem.item) }}
                </div>
                <button @click="onClickDeleteFeature(geoItem.item.id)">
                  🗑
                </button>
              </div>
            </div>
          </template>
        </VirtualScroller>
      </div>

      <div class="advancedMenu" :class="{ active: advancedMenuOpen }">
        <button @click="advancedMenuOpen = !advancedMenuOpen">
          Advanced Mode
        </button>
        <div class="textcontainer" v-if="advancedMenuOpen">
          <input
            v-model="queryName"
            placeholder="Name of the query"
            spellcheck="false"
          />
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
            @change="updateRange(true)"
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
                @input="updateRange(false)"
              />
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
  max-height: calc(100vh - 57px);
  display: flex;
  flex-direction: column;
  flex: 1;
}

.user-actions {
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
  border-radius: 0px;
  padding: 10px;
  border: none;
  background-color: transparent;
  color: inherit;
  cursor: pointer;
  transition: background-color 0.2s ease-in-out;
  font-size: 18px;
  font-weight: bold;
  width: 100%;
  text-align: left;

  &:hover {
    background-color: #4a5568;
    color: white;
  }

  &.confirm {
    background-color: #ef4444;
    border-radius: 5px;

    &:hover {
      background-color: color.scale(#ef4444, $lightness: 10%);
    }
  }
}

.data-title {
  border: none;
  background-color: none;
  display: flex;
  align-items: center;

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

  > div:first-child {
    flex: 1;
    font-weight: bold;
    font-size: 18px;
  }

  > button {
    margin-top: 0px;
    padding: 5px 10px;
    width: auto;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: 5px;
    background-color: #ef4444;
    font-weight: normal;
    font-size: 14px;

    &:hover {
      background-color: lighten(#ef4444, 20%);
    }
  }

  .addfile,
  .addfile-alt {
    display: flex;
    align-items: center;

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
      animation: l20-1 0.8s infinite linear alternate,
        l20-2 1.6s infinite linear;
    }
  }
}

.data {
  border-top-right-radius: 0px;
  border-top-left-radius: 0px;
  border: none;
  background-color: none;
  overflow: auto;
  padding-bottom: 10px;
  flex: 1;

  .geo-item {
    &:last-child {
      margin-bottom: 30px;
    }
  }

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
    margin-left: 31px;
    border-left: 1px solid rgba(255, 255, 255, 0.5);
    padding-left: 9px;
    position: relative;

    &::before {
      content: "";
      position: absolute;
      left: -22px;
      height: 100%;
      width: 1px;
      background-color: rgba(255, 255, 255, 0.5);
    }

    &.alt {
      justify-content: center;
    }

    > input {
      width: 25px;
    }

    > div {
      cursor: pointer;
      user-select: none;
      font-size: 12px;
      overflow: hidden;
      word-break: break-all;
      height: 24px;
      line-height: 24px;
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
        background-color: transparent;
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

  .dataset {
    margin-left: 10px;
    border-left: 1px solid rgba(255, 255, 255, 0.5);
    padding-left: 10px;
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

.filter {
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

.advancedMenu {
  border: none;
  background-color: none;
  margin-top: 5px;

  &.active {
    background-color: #4a5568;
    color: white;
    border-bottom-right-radius: 0px;
    border-bottom-left-radius: 0px;
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

  input {
    margin-top: 8px;
    border-radius: 5px;
    margin: 0px 10px;
    width: calc(100% - 20px);
    font-size: 15px;
    padding: 5px 10px;
    margin-bottom: 8px;
    border: none;
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

  .confirm {
    margin: 0px 10px 10px 10px;
    width: calc(100% - 20px);
  }
}

.textcontainer {
  width: 100%;
}

.navbar_button:hover {
  background-color: #81818a;
  color: white;
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
import VirtualScroller from "primevue/virtualscroller";
import {
  getAllCatalogs,
  setFeatureVisibility,
  setCatalogVisibility,
  setDatasetVisibility,
  triggerFeatureClick,
  triggerFeatureDoubleClick,
  triggerDatasetDoubleClick,
  expandCatalog,
  addSparqlQueryResult,
  expandDataset,
  subscribeLabelChange,
  getGeoItems,
  subscribeDataRefresh,
  init as initGeo,
} from "../services/geo";
import { ref } from "vue";
import { removeFeature, getDataset, sparqlQuery } from "../services/api-geo";
import { sparqlQueries } from "../services/constants";
import { getProgress } from "../services/geo-upload";

$.ajaxSetup({
  xhrFields: {
    withCredentials: true,
  },
});

export default {
  emits: ["fileSelected"],
  components: {
    VirtualScroller,
  },
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
    const queryName = ref("");
    const unsubscribeLabelChange = null;
    const unsubscribeDataRefresh = null;

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
      queryName,
      options: sparqlQueries.options,
      queries: sparqlQueries.queries,
      catalogs,
      getProgress,
      unsubscribeLabelChange,
      getGeoItems,
      unsubscribeDataRefresh,
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
    this.unsubscribeLabelChange = subscribeLabelChange(() => {
      this.$forceUpdate();
    });
    initGeo().then(() => {
      this.$forceUpdate();
    });
    this.unsubscribeDataRefresh = subscribeDataRefresh(() => {
      this.$forceUpdate();
    });
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.closeNavBar);
    this.unsubscribeLabelChange();
  },
  methods: {
    toggleNavBar() {
      this.menuOpen = !this.menuOpen;
    },
    closeNavBar() {
      this.menuOpen = false;
    },
    updateRange(fromTextarea) {
      const regex = /LIMIT\s+(\d+)\s*$/;
      const match = this.inputAdvanced.match(regex);
      if (this.inputAdvanced.endsWith("LIMIT ")) {
        this.inputAdvanced += this.rangeValue;
      } else if (this.inputAdvanced.endsWith("LIMIT")) {
        this.inputAdvanced += " ";
        this.inputAdvanced += this.rangeValue;
      } else if (match) {
        let number = parseInt(match[1]);

        if (fromTextarea) {
          if (number >= 1000) {
            number = 1000;

            const newInput = this.inputAdvanced.replace(
              regex,
              `LIMIT ${number}`
            );
            this.inputAdvanced = newInput;
          }
          this.rangeValue = number;
        } else {
          const newInput = this.inputAdvanced.replace(
            regex,
            `LIMIT ${this.rangeValue}`
          );
          this.inputAdvanced = newInput;
        }
      } else {
        this.inputAdvanced = this.inputAdvanced.concat(
          "\n LIMIT ",
          this.rangeValue
        );
      }
    },
    addData(event) {
      event.stopPropagation();
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
        addSparqlQueryResult(result, this.queryName);
        this.$forceUpdate();
      } else {
        alert("The query fetch no data");
      }
    },
    async onFeatureVisibilityChange(feature) {
      await setFeatureVisibility(feature.id, feature.visible);
      this.$forceUpdate();
    },
    onClickFeature(featureId) {
      triggerFeatureClick(featureId);
    },
    onDoubleClickDataset(datasetId) {
      triggerDatasetDoubleClick(datasetId);
    },
    onDoubleClickFeature(featureId) {
      triggerFeatureDoubleClick(featureId);
    },
    async onClickDeleteFeature(featureId) {
      setFeatureVisibility(featureId, false, true);
      await removeFeature(featureId);
      this.$forceUpdate();
    },
    async onCatalogVisibilityChange(catalog) {
      await setCatalogVisibility(catalog.id, catalog.visible);
      this.$forceUpdate();
    },
    async onDatasetVisibilityChange(dataset) {
      await setDatasetVisibility(dataset.id, dataset.visible);
      this.$forceUpdate();
    },
    async onClickDeleteCatalog(catalog) {
      await setCatalogVisibility(catalog.id, false, true);
      this.$forceUpdate();
    },
    async onClickDeleteDataset(dataset) {
      await setDatasetVisibility(dataset.id, false, true);
      this.$forceUpdate();
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
    async onClickDatasetMap(datasetId) {
      const res = await getDataset(datasetId);
      const mapUrl = res
        .map((x) => x.metadatas)
        .find(
          (x) => x.key === "https://geovast3d.com/ontologies/spalod#hasFile"
        )?.value;

      window.open(mapUrl, "_blank");
    },
    async onClickDatasetOwl(datasetId) {
      const res = await getDataset(datasetId);
      const owlUrl = res
        .map((x) => x.metadatas)
        .find(
          (x) => x.key === "https://geovast3d.com/ontologies/spalod#hasOWL"
        )?.value;

      window.open(owlUrl, "_blank");
    },
    async expandCatalog(catalogId) {
      await expandCatalog(catalogId);
      this.$forceUpdate();
    },
    async expandDataset(datasetId) {
      await expandDataset(datasetId);
      this.$forceUpdate();
    },
    displayItem(item) {
      if (item.label) {
        return item.label.replace(/.*\//, "").replace(/.*#/, "");
      } else if (item.id) {
        return item.id.replace(/.*\//, "").replace(/.*#/, "");
      } else {
        return item;
      }
    },
  },
};
</script>
