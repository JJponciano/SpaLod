<template>
  <div class="container">
    <h2 class="file">{{ file?.name }}</h2>
    <div class="metadatas">
      <div class="metadata-element">
        <h3>
          Catalog: *
          <!-- <button @click="addNewCatalog">+</button> -->
        </h3>
        <div class="metadata-input">
          <input
            type="text"
            v-model="metadata.catalog"
            class="metadata-textbox"
            placeholder="The name of the catalog"
            @focus="$event.target.select()"
            spellcheck="false"
          />
          <!-- <select v-model="selectedOption">
            <option value="" disabled selected hidden>Choose a Catalog</option>
            <option v-for="option in options">
              {{ option.name }}
            </option>
          </select>
          <div class="validate"></div> -->
        </div>
      </div>
      <div
        v-for="(queryable, index) in queryables"
        :key="index"
        class="metadata-element"
      >
        <h3 v-if="queryable.required">{{ queryable.q }}: *</h3>
        <h3 v-else>{{ queryable.q }}:</h3>
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
          <button
            :id="queryable.q"
            class="validate"
            @click="validateMetadata(queryable.q)"
            :class="{ added: queryable.v }"
            :disabled="queryable.q !== 'identifier' && !queryables[0].v"
            v-show="queryable.q === 'identifier' || queryables[0].v"
          >
            {{ queryable.v ? "Validated" : "Validate" }}
          </button>
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
}

.metadata-input > button:hover {
  background-color: #1a202c;
  color: white;
  transition: background-color 0.3s ease;
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
import { $ajax } from "../services/api";
import { init as initGeo } from "../services/geo";

export default {
  props: {
    file: File,
  },
  data() {
    return {
      queryables,
      metadata: {},
      options: [],
      selectedOption: "",
    };
  },
  methods: {
    onClickCancel() {
      this.$emit("close");
    },
    onClickOk() {
      if (!this.validateMetadatas()) {
        return;
      }
      let formData = new FormData();
      formData.append("file", this.file);
      formData.append("metadata", JSON.stringify(this.metadata));
      this.post_checkont("/api/upload-file/", formData);
      this.$emit("close");
    },
    post_checkont(url, data) {
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
        success: (response) => this.seek_unknown(response),
        error: () => {
          alert("Error while checking ontology!");
        },
      });
    },
    seek_unknown(response) {
      initGeo();
    },
    validateMetadatas() {
      for (const { required, q } of this.queryables) {
        if (required && !this.metadata[q]) {
          return false;
        }
      }
      return true;
    },
  },
};
</script>
