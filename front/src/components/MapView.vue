<script>
import L from "leaflet";
import { getAllGeo, getFeature } from "../services/geo";

export default {
  props: {
    file: File,
  },
  watch: {
    file(newFile) {
      const lecteur = new FileReader();

      this.dataArray = [];
      lecteur.readAsText(newFile);
      lecteur.onload = () => {
        const contenu = lecteur.result;
        const object = JSON.parse(contenu);

        console.log(object);

        object.features.forEach((feature) => {
          let coordinates =
            feature.properties.Koordinate ?? feature.geometry.coordinates;
          console.log(coordinates);
          if (coordinates.length > 0) {
            if (coordinates.includes("(")) {
              coordinates = coordinates.split("(")[1];
              coordinates = coordinates.split(")")[0];
              coordinates = coordinates
                .split(" ")
                .map((coord) => parseFloat(coord));
            }
            if (!feature.properties.itemID) return;
            const label =
              feature.properties.itemLabel ?? feature.properties.Objektname;
            this.dataArray.push([
              object.name,
              decodeURIComponent(label).replace(/_/g, " "),
              coordinates[1],
              coordinates[0],
            ]);
          }
        });
        this.updateMap();
      };
    },
  },
  data() {
    return {
      dataArray: [],
      sizeOfArray: 0,
      path: null,
      query: null,
      map: null,
      feature: null,
    };
  },
  methods: {
    objectSize(obj) {
      let size = 0,
        key;
      for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
      }
      return size;
    },
    initMap() {
      // Path to where the files are hosted
      this.path = "/pictures/signaturen/";

      // Creation of one layer of points
      this.query = new L.layerGroup();

      // Custom icon class
      let MarkerIcon = L.Icon.extend({
        options: {
          iconSize: [20, 20],
          shadowSize: [0, 0],
          iconAnchor: [10, 10],
          popupAnchor: [0, -10],
        },
      });
      this.sizeOfArray = this.objectSize(this.dataArray);

      for (let i = 0; i < this.sizeOfArray; i++) {
        L.marker([this.dataArray[i][2], this.dataArray[i][3]], {
          icon: new MarkerIcon({
            iconUrl: this.path + this.dataArray[i][0] + ".png",
          }),
        })
          .bindPopup(
            this.dataArray[i][2] +
              "," +
              this.dataArray[i][3] +
              " " +
              this.dataArray[i][1]
          )
          .addTo(this.query);
      }

      let mbAttr =
          'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, ' +
          'Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
        mbUrl =
          "https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token=pk.eyJ1IjoiZmVkZXJkaXNwaSIsImEiOiJjbGdreWlncHcwd3F0M2hsdnhscGg5Yzc1In0.Ud5vRdMf9cbtUUd5ufgKXQ";

      let rUrl = "https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png";

      let topplusUrl = "http://sgx.geodatenzentrum.de/wms_topplus_web_open";
      let topplusAttr =
        '&copy Bundesamt für Kartographie und Geodäsie 2017, <a href="http://sg.geodatenzentrum.de/web_public/Datenquellen_TopPlus_Open.pdf">Datenquellen</a>';

      let grayscale = L.tileLayer(mbUrl, {
          id: "mapbox/light-v9",
          tileSize: 512,
          zoomOffset: -1,
          attribution: mbAttr,
        }),
        streets = L.tileLayer(mbUrl, {
          id: "mapbox/streets-v11",
          tileSize: 512,
          zoomOffset: -1,
          attribution: mbAttr,
        }),
        relief = L.tileLayer(rUrl, {
          maxZoom: 17,
          attribution:
            'Map data: &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, <a href="http://viewfinderpanoramas.org">SRTM</a> | Map style: &copy; <a href="https://opentopomap.org">OpenTopoMap</a> (<a href="https://creativecommons.org/licenses/by-sa/3.0/">CC-BY-SA</a>)',
        }),
        topplus = L.tileLayer.wms(topplusUrl, {
          layers: "web",
          format: "image/png",
          transparent: true,
          attribution: topplusAttr,
        });

      // Definition of the map, and setup of details : coordinates of the center, zoom level and layers that appear on loading
      this.map = L.map("map", {
        center: [49.99, 8.24],
        zoom: 6,
        minZoom: 2,
        fullscreenControl: true,
        layers: [streets, this.query],
      });

      this.map.attributionControl.setPrefix(false);

      // Definition of layers
      let baseLayers = {
        Grayscale: grayscale,
        Streets: streets,
        Relief: relief,
        Topplus: topplus,
      };

      // Definition of point layers
      let overlays = {
        Query: this.query,
      };

      // Add layers and points layers to the map
      L.control.layers(baseLayers, overlays).addTo(this.map);
    },
    updateMap() {
      this.query.clearLayers();
      this.sizeOfArray = this.objectSize(this.dataArray);

      let MarkerIcon = L.Icon.extend({
        options: {
          iconSize: [20, 20],
          shadowSize: [0, 0],
          iconAnchor: [10, 10],
          popupAnchor: [0, -10],
        },
      });

      for (let i = 0; i < this.sizeOfArray; i++) {
        L.marker([this.dataArray[i][2], this.dataArray[i][3]], {
          icon: new MarkerIcon({
            iconUrl: this.path + this.dataArray[i][0] + ".png",
          }),
        })
          .bindPopup(
            this.dataArray[i][2] +
              "," +
              this.dataArray[i][3] +
              " " +
              this.dataArray[i][1]
          )
          .addTo(this.query);
      }
    },
    async displayGeo() {
      const allGeos = await getAllGeo();
      const mapObjList = [];

      const addPolyline = (id, pointList) => {
        const mapObj = new L.Polyline(pointList, {
          color: "blue",
          weight: 3,
          opacity: 0.5,
          smoothFactor: 1,
        });
        mapObj.addTo(this.map);
        mapObj.on("click", (event) => {
          this.displayFeature(event.target.spalodId);
        });
        mapObj.spalodId = id;

        mapObjList.push(mapObj);
      };

      for (const { geo, type, id } of allGeos) {
        // TODO: handle other types
        if (type === "LINESTRING") {
          addPolyline(
            id,
            geo.map(([lat, lng]) => new L.LatLng(lat, lng))
          );
        } else if (type === "MULTILINESTRING") {
          for (const polyline of geo) {
            addPolyline(
              id,
              polyline.map(([lat, lng]) => new L.LatLng(lat, lng))
            );
          }
        }
      }

      this.map.fitBounds(mapObjList.map((x) => x.getBounds()).flat(1));
    },
    async displayFeature(featureId) {
      this.feature = await getFeature(featureId);
    },
    closeFeature() {
      this.feature = null;
    },
  },
  mounted() {
    this.initMap();
    this.displayGeo();
  },
};
</script>

<template>
  <div class="map-view" id="map"></div>
  <div class="feature-container" v-if="feature">
    <div class="feature" v-for="item of feature">
      <div>{{ item.key }}</div>
      <div>{{ item.value }}</div>
    </div>
    <div class="close" @click="closeFeature()"><button>Close</button></div>
  </div>
</template>

<style lang="scss">
.map-view {
  background-color: #4a5568;
  height: 100%;
  color: white;
  resize: both;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.feature-container {
  background-color: white;
  color: black;
  position: absolute;
  z-index: 1000;
  top: 10px;
  right: 10px;
  border-radius: 5px;
  padding: 20px;
  box-shadow: 0px 0px 10px 0px #a3a3a3;
  margin-left: 10px;

  .feature {
    display: flex;

    > div {
      flex: 1;
    }
  }
}
</style>
