<template>
  <div class="popup-container" @click="closeFeature()">
    <div
      class="feature-container"
      @click="stopPropagation"
      :class="{ 'has-pointcloud': pointcloudUrl }"
    >
      <table>
        <tr class="feature" v-for="item of getFeatureItems()">
          <td class="title">
            <div v-if="!item.new">{{ displayLastPortion(item.key) }}</div>
            <input v-else type="text" v-model="item.key" />
          </td>
          <td>
            <div style="display: flex">
              <div
                :id="'div-' + item.key"
                v-if="!item.hasFocus && !item.new"
                @click="setItemInput(item)"
              >
                {{ item.value }}
              </div>
              <input
                v-else-if="!item.new"
                :id="'btn-' + item.key"
                type="text"
                v-model="item.value"
                @blur="item.hasFocus = false"
                :style="getInputStyle()"
                @input="changeProperty(item)"
              />
              <input v-else type="text" v-model="item.value" />
              <button
                style="margin-left: auto"
                v-if="item.new"
                @click="confirmAddProperty(item)"
              >
                ✔
              </button>
            </div>
          </td>
          <td>
            <button
              v-if="
                item.key !==
                  'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' /*&&
                !item.key.startsWith(
                  'https://geovast3d.com/ontologies/spalod#has'
                )*/ &&
                !item.key.startsWith('http://www.opengis.net/ont/geosparql#has')
              "
              @click="deleteFeatureProperty(item)"
            >
              🗑
            </button>
          </td>
        </tr>
      </table>
      <iframe v-if="pointcloudUrl" :src="pointcloudUrl"></iframe>
      <div class="feature-images" v-if="getFeatureItems('Image').length > 0">
        <h3>Images</h3>
        <div class="images">
          <div
            class="image"
            v-for="item of getFeatureItems('Image')"
            @click="openInBrowser(item.value)"
          >
            <img :src="item.value" />
            <button @click="deleteFeatureProperty(item, $event)">🗑</button>
          </div>
        </div>
      </div>
      <div class="feature-videos" v-if="getFeatureItems('Video').length > 0">
        <h3>Videos</h3>
        <div class="videos">
          <div
            class="video"
            v-for="item of getFeatureItems('Video')"
            @click="openInBrowser(item.value)"
          >
            <video>
              <source :src="item.value" />
              Your browser does not support the video tag.
            </video>
            <button @click="deleteFeatureProperty(item, $event)">🗑</button>
          </div>
        </div>
      </div>

      <div
        class="feature-items"
        v-for="kind of ['Pdf', 'File', 'Document', '3D', 'PointCloud']"
      >
        <h3 v-if="getFeatureItems(kind).length > 0">{{ kind }}s</h3>
        <div class="items">
          <div class="item" v-for="item of getFeatureItems(kind)">
            <span @click="openInBrowser(item.value)">{{
              displayLastPortion(item.value)
            }}</span>
            <button @click="deleteFeatureProperty(item, $event)">🗑</button>
          </div>
        </div>
      </div>
      <div class="insert">
        <button v-if="showAddLabel()" @click="addLabel()">Add Label</button>
        <button @click="addProperty()">Add Property</button>
        <button v-if="!feature.uploading" @click="addFile()">Add File</button>
      </div>
      <div class="close"><button @click="closeFeature()">Close</button></div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.popup-container {
  position: absolute;
  z-index: 1000;
  left: 0px;
  top: 0px;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.5);

  .feature-container {
    background-color: white;
    color: black;
    border-radius: 5px;
    padding: 20px;
    box-shadow: 0px 0px 10px 0px #a3a3a3;
    display: flex;
    flex-direction: column;
    max-width: calc(100% - 20px);
    max-height: calc(100% - 20px);
    overflow: auto;

    &.has-pointcloud {
      width: calc(100% - 20px);
      height: calc(100% - 20px);
    }

    table {
      border-spacing: 0px;

      .feature {
        word-break: break-all;

        &:nth-child(odd) {
          background-color: #f2f2f2;
        }

        .title {
          // width: 20%;
          padding-right: 30px;
          padding-left: 10px;
          padding-top: 5px;
          padding-bottom: 5px;
        }

        td {
          margin: 0;
          border: none;

          div {
            min-height: 24px;
          }

          input {
            padding: 5px;
            font-size: 15px;
            margin: 0;
          }
        }
      }
    }

    iframe {
      margin-top: 10px;
      border: none;
      flex: 1;
    }

    .feature-images,
    .feature-videos {
      > h3 {
        margin-top: 20px;
      }

      .images,
      .videos {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;

        .image,
        .video {
          width: 100px;
          height: 100px;
          overflow: hidden;
          border-radius: 5px;
          cursor: pointer;

          > button {
            position: absolute;
            top: 0px;
            right: 0px;
            padding: 3px 6px;
          }

          img,
          video {
            width: 100px;
            height: 100px;
            object-fit: cover;
          }
        }
      }
    }

    .feature-items {
      > h3 {
        margin-top: 20px;
      }

      .items {
        .item {
          > span {
            cursor: pointer;
            text-decoration: underline;
            color: blue;

            &:hover {
              color: darkblue;
            }
          }

          > button {
            margin-left: 5px;
            padding: 0px 5px;
          }
        }
      }
    }
  }

  .insert {
    margin-top: 10px;
  }

  .close {
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }
}
</style>

<script>
import {
  updateFeature,
  deleteFeatureProperty,
  addFileToFeature,
} from "../services/geo";
import { getFeature } from "../services/api-geo";

export default {
  emits: ["close", "featureUpdated"],
  props: {
    feature: Object,
    pointcloudUrl: String,
  },
  data() {
    return {
      inputMinWidth: 0,
    };
  },
  methods: {
    closeFeature() {
      this.$emit("close");
    },

    stopPropagation(event) {
      event.stopPropagation();
    },

    displayLastPortion(item) {
      return item.replace(/.*\//, "").replace(/.*#/, "").replace(/.*?_/, "");
    },

    setItemInput(item) {
      if (
        item.key === "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" ||
        item.key.startsWith("http://www.opengis.net/ont/geosparql#has") ||
        item.key.startsWith("https://geovast3d.com/ontologies/spalod#has")
      ) {
        return;
      }
      item.hasFocus = true;
      this.inputMinWidth = document.getElementById(
        `div-${item.key}`
      ).offsetWidth;
      setTimeout(() => {
        document.getElementById(`btn-${item.key}`).focus();
      });
    },

    getInputStyle() {
      return {
        "min-width": this.inputMinWidth + "px",
      };
    },

    changeProperty(item) {
      updateFeature(this.feature.id, item.key, item.value, item.new);
      item.new = false;
    },

    deleteFeatureProperty(item, event) {
      if (event) {
        event.stopPropagation();
      }
      this.feature.items = this.feature.items.filter(
        (x) => x.key !== item.key || x.value !== item.value
      );
      deleteFeatureProperty(this.feature.id, item.key, item.value);
    },

    addProperty() {
      this.feature.items.push({
        key: "",
        value: "",
        new: true,
      });
    },

    confirmAddProperty(item) {
      updateFeature(this.feature.id, item.key, item.value, item.new);
      item.new = false;
    },

    showAddLabel() {
      return !this.feature?.items?.some(
        (x) =>
          x.key === "rdfs:label" ||
          x.key === "http://www.w3.org/2000/01/rdf-schema#label"
      );
    },

    addLabel() {
      this.feature.items.push({
        key: "http://www.w3.org/2000/01/rdf-schema#label",
        value: "",
        new: true,
      });
    },

    addFile() {
      const input = document.createElement("input");
      input.type = "file";
      input.click();
      input.onchange = async () => {
        await addFileToFeature(this.feature.id, input.files[0]);
        this.$emit("featureUpdated", this.feature.id);
      };
    },

    getFeatureItems(kind = undefined) {
      if (kind === undefined) {
        return this.feature.items.filter(
          (x) =>
            !x.key.startsWith("https://geovast3d.com/ontologies/spalod#has")
        );
      } else {
        return this.feature.items.filter(
          (x) => x.key === `https://geovast3d.com/ontologies/spalod#has${kind}`
        );
      }
    },

    openInBrowser(value) {
      window.open(value, "_blank");
    },
  },
};
</script>
