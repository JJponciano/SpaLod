<template>
  <div class="container">
    <h2 class="file">{{ file?.name }}</h2>
    <div class="metadatas">
      <div class="required">
        <div class="metadata-element">
          <h3>Catalog:</h3>
          <div :class="{ 'metadata-input': true, required: !metadata.catalog }">
            <AutoComplete
              placeholder="The name of the catalog"
              v-model="metadata.catalog"
              :suggestions="items"
              @complete="searchCatalogs"
              :completeOnFocus="true"
            ></AutoComplete>
          </div>
        </div>
        <div class="metadata-element" v-if="latlng">
          <h3>Dataset:</h3>
          <div :class="{ 'metadata-input': true, required: !metadata.dataset }">
            <AutoComplete
              placeholder="The name of the dataset"
              v-model="metadata.dataset"
              :suggestions="items"
              @complete="searchDatasets"
              :completeOnFocus="true"
            ></AutoComplete>
          </div>
        </div>
        <div
          v-for="(queryable, index) in queryables.filter((x) => x.required)"
          :key="index"
          class="metadata-element"
        >
          <h3>{{ queryable.q }}:</h3>
          <div
            :class="{
              'metadata-input': true,
              required: !metadata[queryable.q],
            }"
          >
            <input
              type="text"
              v-model="metadata[queryable.q]"
              class="metadata-textbox"
              :placeholder="queryable.d"
              @focus="$event.target.select()"
              @input="queryable.v = false"
              spellcheck="false"
            />
          </div>
        </div>
      </div>
      <button @click="showOptional = !showOptional">Other metadatas</button>
      <div v-if="showOptional" class="optional">
        <div
          v-for="(queryable, index) in queryables.filter((x) => !x.required)"
          :key="index"
          class="metadata-element"
        >
          <h3>{{ queryable.q }}:</h3>
          <div class="metadata-input">
            <input
              type="text"
              v-model="metadata[queryable.q]"
              class="metadata-textbox"
              :placeholder="queryable.d"
              @focus="$event.target.select()"
              @input="queryable.v = false"
              spellcheck="false"
            />
          </div>
        </div>
      </div>
    </div>
    <div class="actions">
      <button @click="onClickOk">Ok</button>
      <button @click="onClickCancel">Cancel</button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.container {
  max-width: 80vw;
  max-height: 80vh;
  background-color: #1a202c;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  box-shadow: 0px 0px 15px 2px #000000;

  .metadatas {
    overflow: auto;
    flex: 1;
    padding: 0px 20px;
    display: flex;
    flex-direction: column;

    .optional {
      flex: 1;
      overflow: auto;
    }
  }

  .actions {
    padding: 20px;
    text-align: center;

    button + button {
      margin-left: 10px;
    }
  }
}

h2 {
  margin-top: 0;
  font-weight: bold;
  text-align: center;
  padding-top: 10px;
  margin-bottom: 10px;
}

p {
  font-size: 16px;
  font-weight: bold;
}

select {
  display: block;
  text-align: center;
  font-size: 13px;
  font-weight: bold;
  padding: 9px;
  width: 250px;
  margin: 10px 0;
  left: 23%;
  border: 0px solid #1a202c;
  border-radius: 5px;
  background-color: rgb(241, 241, 241);
  color: black;
  appearance: none;
  cursor: pointer;
}

.metadata-container {
  display: flex;
  flex-direction: column;
  cursor: default;
}

.metadata-element {
  display: grid;
  grid-template-columns: 1fr 1fr;
  margin: 10px 0;
}

.metadata-element h3 {
  font-weight: bold;
  margin: 5px 0 0 10px;
}

.metadata-input {
  display: flex;
  flex-direction: row;
  border-left: 2px solid transparent;

  &.required {
    border-left: 2px solid #ef4444;
  }
}

button {
  border: none;
  cursor: pointer;
  background-color: #ef4444;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  padding: 5px 10px;
  border-radius: 5px;
}

button:hover {
  background-color: #4a5568;
  transition: background-color 0.5s ease;
}

.metadata-textbox {
  border: none;
  border-radius: 5px;
  padding: 11px;
  margin: 0px 15px;
  width: 250px;
  font-size: 14px;
}

.validate {
  background-color: #0baaa7;
}

.added {
  transition: all 0.5s ease-in-out;
  background-color: transparent;
  pointer-events: none;
  cursor: default;
}
</style>

<script>
import { queryables } from "../services/constants";
import { uploadGeo } from "../services/geo-upload";
import AutoComplete from "primevue/autocomplete";
import { getAllCatalogs, addGeoFeature } from "../services/geo";
import { getUsername } from "../services/login";
import { getAllDatasetsFromCatalogName } from "../services/api-geo";

export default {
  emits: ["close"],
  components: {
    AutoComplete,
  },
  props: {
    file: File,
    latlng: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      queryables,
      metadata: {},
      options: [],
      selectedOption: "",
      items: [],
      showOptional: false,
    };
  },
  watch: {
    file() {
      this.showOptional = false;
      this.metadata = {
        publisher: getUsername(),
      };
    },
    latlng() {
      this.showOptional = false;
      this.metadata = { publisher: getUsername() };
    },
  },
  methods: {
    onClickCancel() {
      this.$emit("close");
    },
    onClickOk() {
      if (!this.validateMetadatas()) {
        return;
      }
      if (this.file) {
        uploadGeo(this.file, this.metadata);
      } else if (this.latlng) {
        const catalogName = this.metadata["catalog"];
        const datasetName = this.metadata["dataset"];
        const label = this.metadata["title"];

        const metadata = {
          ...this.metadata,
        };

        delete metadata["catalog"];
        delete metadata["dataset"];
        delete metadata["title"];

        addGeoFeature(
          this.latlng.lat,
          this.latlng.lng,
          label,
          catalogName,
          datasetName,
          metadata
        );
      }
      this.onClickCancel();
    },
    validateMetadatas() {
      for (const { required, q } of this.queryables.concat([
        { required: true, q: "catalog" },
        { required: this.latlng, q: "dataset" },
      ])) {
        if (required && !this.metadata[q]) {
          return false;
        }
      }
      return true;
    },
    searchCatalogs(event) {
      this.items = [];
      this.items = getAllCatalogs()
        .map(({ label }) => label)
        .filter((label) =>
          label.toLowerCase().startsWith(event.query.toLowerCase())
        );
    },
    async searchDatasets(event) {
      this.items = [];
      this.items = (
        await getAllDatasetsFromCatalogName(this.metadata["catalog"])
      )
        .map(({ metadatas: { label } }) => label)
        .filter((label) =>
          label.toLowerCase().startsWith(event.query.toLowerCase())
        );
    },
  },
};
</script>
