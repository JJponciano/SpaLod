<script>
import L from "leaflet";
import { getAllGeo, getFeature } from "../services/geo";
import {
  addFeatures,
  subscribeFeatureVisibiltyChange,
  clearFeatures,
  subscribeFeatureClick,
} from "../services/map";

export default {
  props: {
    file: File,
  },
  watch: {
    file(newFile) {
      return;
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
      mapObjList: [],
      unsubscribe: [],
      pointcloudUrl: null,
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

      addFeatures(allGeos.map(({ metadatas: { feature } }) => feature));
      this.unsubscribe.push(
        ...[
          subscribeFeatureVisibiltyChange(
            this.onFeatureVisibilityChange.bind(this)
          ),
          subscribeFeatureClick(this.onFeatureClick.bind(this)),
        ]
      );

      const addPolyline = (id, pointList) => {
        const mapObj = new L.Polyline(pointList, {
          color: "blue",
          weight: 3,
          opacity: 0.5,
          smoothFactor: 1,
        });
        mapObj.on("click", (event) => {
          this.displayFeature(event.target.spalodId);
        });
        mapObj.spalodId = id;
        mapObj.visible = false;

        this.mapObjList.push(mapObj);
      };

      for (const { geo, type, metadatas } of allGeos) {
        // TODO: handle other types
        if (type === "LINESTRING") {
          addPolyline(
            metadatas.feature,
            geo.map(([lat, lng]) => new L.LatLng(lat, lng))
          );
        } else if (type === "MULTILINESTRING") {
          for (const polyline of geo) {
            addPolyline(
              metadatas.feature,
              polyline.map(([lat, lng]) => new L.LatLng(lat, lng))
            );
          }
        } else if (type === "POLYGON") {
          const mapObj = new L.Polygon(
            geo.map(([lat, lng]) => new L.LatLng(lat, lng)),
            {
              color: "blue",
              weight: 3,
              opacity: 0.5,
              smoothFactor: 1,
            }
          );
          mapObj.on("click", (event) => {
            this.displayFeature(event.target.spalodId);
          });
          mapObj.spalodId = metadatas.feature;

          this.mapObjList.push(mapObj);
        }
      }
    },
    async displayFeature(featureId) {
      const res = await getFeature(featureId);
      this.feature = res
        .filter((x) => x.metadatas?.key && x.metadatas?.value)
        .map(({ metadatas: { key, value } }) => ({
          key,
          value,
        }));

      this.pointcloudUrl = res.filter((x) => x.pointcloudUrl)[0]?.pointcloudUrl;
    },
    closeFeature() {
      this.feature = null;
      this.pointcloudUrl = null;
    },

    onFeatureVisibilityChange(feature) {
      const mapObj = this.mapObjList.find(
        ({ spalodId }) => spalodId === feature.id
      );

      if (feature.visible) {
        mapObj.addTo(this.map);
        mapObj.visible = true;
      } else {
        mapObj.removeFrom(this.map);
        mapObj.visible = false;
      }

      const objs = this.mapObjList.filter((x) => x.visible);
      if (objs.length > 0) {
        this.map.options.maxZoom = 17;
        this.fitBounds(objs.map((x) => x.getBounds()).flat(1));
      }
    },

    onFeatureClick(featureId) {
      const mapObj = this.mapObjList.find(
        ({ spalodId }) => spalodId === featureId
      );

      this.fitBounds(mapObj.getBounds());
    },

    fitBounds(bounds) {
      const previousZoom = this.map.options.maxZoom;
      this.map.options.maxZoom = 15;
      this.map.fitBounds(bounds);
      this.map.options.maxZoom = previousZoom;
    },

    stopPropagation(event) {
      event.stopPropagation();
    },
  },
  mounted() {
    this.initMap();
    this.displayGeo();
  },
  unmounted() {
    this.unsubscribe.forEach((x) => x());
    clearFeatures();
  },
};
</script>

<template>
  <div class="map-view" id="map"></div>
  <div class="popup-container" v-if="feature" @click="closeFeature()">
    <div
      class="feature-container"
      @click="stopPropagation"
      :class="{ 'has-pointcloud': pointcloudUrl }"
    >
      <div class="feature" v-for="item of feature">
        <div>{{ item.key }}</div>
        <div>{{ item.value }}</div>
      </div>
      <iframe v-if="pointcloudUrl" :src="pointcloudUrl"></iframe>
      <div class="close" @click="closeFeature()"><button>Close</button></div>
    </div>
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

    &.has-pointcloud {
      width: calc(100% - 20px);
      height: calc(100% - 20px);
    }

    .feature {
      word-break: break-all;
      display: flex;

      > div {
        flex: 1;
      }
    }

    iframe {
      border: none;
      flex: 1;
    }
  }
}
</style>
