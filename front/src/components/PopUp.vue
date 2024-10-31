<template>
  <div class="popup-overlay" v-if="isPopupVisible" @click="closePopup"></div>
  <div
    v-bind:class="{ popup: true, visible: isPopupVisible }"
    :class="{ dark: isDarkMode }"
  >
    <div class="Title">
      <p>Please select the desired icon for this file</p>
    </div>
    <div class="SelectAction">
      <select v-model="selectedOption">
        <option value="Apotheken">Apotheken</option>
        <option value="Arzt">Artz</option>
        <option value="Bahnhof">Bahnhof</option>
        <option value="Bank">Bank</option>
        <option value="BFW.png">BFW</option>
        <option value="Bibliothek">Bibliothek</option>
        <option value="BotKon">BotKon</option>
        <option value="BPOL">BPOL</option>
        <option value="Briefzentren">Briefzentren</option>
        <option value="DenKmal">DenKmal</option>
        <option value="Feuerwehr">Feuerwehr</option>
        <option value="Flughaefen">Flughaefen</option>
        <option value="Gerichte">Gerichte</option>
        <option value="Hotel">Hotel</option>
        <option value="HS">HS</option>
        <option value="JVA">JVA</option>
        <option value="KHV">KHV</option>
        <option value="Kino">Kino</option>
        <option value="Kita">Kita</option>
        <option value="KmBAB">KmBAB</option>
        <option value="Laboratorium">Laboratorium</option>
        <option value="LadeSt">LadeSt</option>
        <option value="Museen">Museen</option>
        <option value="Post">Post</option>
        <option value="Restaurant">Restaurant</option>
        <option value="RHV">RHV</option>
        <option value="Schwimmbad">Schwimmbad</option>
        <option value="Seehaefen">Seehaefen</option>
        <option value="Seniorenheime">Seniorenheime</option>
        <option value="Stadium">Stadium</option>
        <option value="Supermarkt">Supermarkt</option>
        <option value="Tankstellen">Tankstellen</option>
        <option value="THW">THW</option>
        <option value="Wetterstation">Wetterstation</option>
        <option value="Zoll">Zoll</option>
      </select>
    </div>
    <div class="ButtonSelect">
      <button @click="closePopup">Close</button>
      <button @click="onValid" class="choose">Choose</button>
      <input
        type="file"
        ref="fileInput"
        style="display: none"
        :accept="fileType"
        @change="handleFile"
      />
    </div>
  </div>
</template>

<script>
import Papa from "papaparse";

export default {
  props: {
    chooseCSV: Boolean,
    chooseJson: Boolean,
    popup: Boolean,
  },
  watch: {
    chooseCSV(newValue) {
      if (newValue) {
        this.csv = true;
        this.fileType = "text/csv";
        this.$emit("CSVBack");
      }
    },
    chooseJson(newValue) {
      if (newValue) {
        this.json = true;
        this.fileType = "application/json";
        this.$emit("JsonBack");
      }
    },
    popup(newValue) {
      if (newValue) {
        this.showPopup();
      }
    },
  },
  data() {
    return {
      isDarkMode: false,
      isPopupVisible: false,
      selectedOption: "Apotheken",
      json: false,
      csv: false,
      fileType: "",
    };
  },
  mounted() {
    this.detectDarkMode();
    window.matchMedia("(prefers-color-scheme: dark)").addListener((event) => {
      this.isDarkMode = event.matches;
    });
  },
  methods: {
    detectDarkMode() {
      this.isDarkMode = window.matchMedia(
        "(prefers-color-scheme: dark)"
      ).matches;
    },
    showPopup() {
      this.isPopupVisible = true;
    },
    closePopup() {
      this.json = false;
      this.csv = false;
      this.isPopupVisible = false;
      this.$emit("popupBack");
    },
    onValid() {
      this.$refs.fileInput.click();
    },
    uuidv4() {
      return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, (c) =>
        (
          c ^
          (crypto.getRandomValues(new Uint8Array(1))[0] & (15 >> (c / 4)))
        ).toString(16)
      );
    },
    handleFile() {
      if (this.json) {
        const file = event.target.files[0];
        const fileReader = new FileReader();
        fileReader.readAsText(file);

        fileReader.onload = () => {
          const jsonArray = JSON.parse(fileReader.result);
          const featureCollection = {
            type: "FeatureCollection",
            name: this.selectedOption,
            features: [],
          };

          for (let i = 0; i < jsonArray.length; i++) {
            const obj = jsonArray[i];
            var feature = {
              type: "Feature",
              geometry: {
                type: "Point",
                coordinates: [],
              },
              properties: {
                itemID: "spalod:" + this.uuidv4(),
              },
            };
            if (obj.geo === undefined || obj.Koordinate === undefined) {
              feature.geometry.coordinates = [
                parseFloat(obj.longitude) ?? parseFloat(obj["X Koordina"]),
                parseFloat(obj.latitude) ?? parseFloat(obj["Y Koordina"]),
              ];
            } else {
              var coordinates = String(obj.geo).split("(")[1];
              if (coordinates === undefined) continue;
              coordinates = coordinates.split(")")[0].split(" ");
              feature.geometry.coordinates = [
                parseFloat(coordinates[0]),
                parseFloat(coordinates[1]),
              ];
            }
            Object.keys(obj).forEach((key) => {
              if (key !== "geo" && key !== "latitude" && key !== "longitude") {
                feature.properties[key] = obj[key];
              }
            });
            featureCollection.features.push(feature);
          }
          const geoJSON = JSON.stringify(featureCollection);
          const url = window.URL.createObjectURL(new Blob([geoJSON]));
          const link = document.createElement("a");
          link.href = url;
          link.setAttribute("download", "geodata.json");
          document.body.appendChild(link);
          link.click();
        };
      } else if (this.csv) {
        const file = event.target.files[0];
        const fileReader = new FileReader();
        fileReader.readAsText(file);

        fileReader.onload = () => {
          const csv = fileReader.result;
          const jsonArray = Papa.parse(csv, { header: true }).data;
          const featureCollection = {
            type: "FeatureCollection",
            name: this.selectedOption,
            features: [],
          };

          for (let i = 0; i < jsonArray.length; i++) {
            const obj = jsonArray[i];
            var feature = {
              type: "Feature",
              geometry: {
                type: "Point",
                coordinates: [],
              },
              properties: {
                itemID: "spalod:" + this.uuidv4(),
              },
            };
            if (obj.geo === undefined || obj.Koordinate === undefined) {
              feature.geometry.coordinates = [
                parseFloat(obj.longitude) ?? parseFloat(obj["X Koordina"]),
                parseFloat(obj.latitude) ?? parseFloat(obj["Y Koordina"]),
              ];
            } else {
              var coordinates = String(obj.geo).split("(")[1];
              if (coordinates === undefined) continue;
              coordinates = coordinates.split(")")[0].split(" ");
              feature.geometry.coordinates = [
                parseFloat(coordinates[0]),
                parseFloat(coordinates[1]),
              ];
            }
            Object.keys(obj).forEach((key) => {
              if (key !== "geo" && key !== "latitude" && key !== "longitude") {
                feature.properties[key] = obj[key];
              }
            });
            featureCollection.features.push(feature);
          }

          const geoJSON = JSON.stringify(featureCollection);
          const url = window.URL.createObjectURL(new Blob([geoJSON]));
          const link = document.createElement("a");
          link.href = url;
          link.setAttribute("download", "geodata.json");
          document.body.appendChild(link);
          link.click();
        };
      }
      this.closePopup();
    },
  },
};
</script>

<style scoped>
.popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: transparent;
}

.popup {
  background-color: white;
  padding: 30px;
  box-shadow: 0px 0px 15px rgba(0, 0, 0, 0.5);
  border-style: solid;
  border-radius: 8px;
  border-width: 1px;
  z-index: 900;
  display: none;
}

.popup.dark {
  background-color: #1a202c;
  color: white;
}

.popup.visible {
  display: block;
}

.SelectAction {
  display: flex;
  justify-content: center;
  padding: 10px;
}

.SelectAction select {
  display: block;
  text-align: center;
  font-size: 13px;
  font-weight: bold;
  padding: 9px;
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

.ButtonSelect {
  display: flex;
  flex-direction: row;
}

button {
  padding: 5px 10px 5px 10px;
  border: none;
  border-radius: 5px;
  background-color: #ef4444;
  color: inherit;
  cursor: pointer;
  transition: background-color 0.2s ease-in-out;
  font-size: 15px;
  font-weight: bold;
  margin: 10px 15px 0 15px;
  width: 40%;
  text-align: center;
  color: white;
}

.choose {
  background-color: #0baaa7;
}

button:hover {
  background-color: #4a5568;
}
</style>
