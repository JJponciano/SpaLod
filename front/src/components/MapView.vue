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
  <div class="loader" v-if="total > 0">
    <div class="title">Loading data</div>
    <div class="container">
      <div class="progress" :style="{ width: getProgressWidth() }"></div>
    </div>
  </div>
</template>

<style lang="scss">
.leaflet-pane.leaflet-shadow-pane {
  display: none;
}
</style>

<style lang="scss" scoped>
.map-view {
  background-color: #4a5568;
  height: 100%;
  color: white;
  resize: both;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.loader {
  position: absolute;
  z-index: 1000;
  bottom: 10px;
  left: 10px;
  height: 30px;
  display: flex;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;

  .title {
    position: absolute;
    left: 0px;
    z-index: 1;
    padding-left: 5px;
  }

  .container {
    width: 200px;
    padding: 2px;
    height: 100%;

    .progress {
      background-color: transparentize($color: green, $amount: 0.5);
      height: 100%;
    }
  }
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
      margin-top: 10px;
      border: none;
      flex: 1;
    }
  }

  .close {
    margin-top: 10px;
  }
}
</style>

<script>
import L from "leaflet";
import { getFeature } from "../services/api-geo";
import {
  subscribeFeatureVisibiltyChange,
  subscribeFeatureClick,
} from "../services/geo";

export default {
  data() {
    return {
      map: null,
      feature: null,
      mapObjList: [],
      unsubscribe: [],
      pointcloudUrl: null,
      total: 0,
      actual: 0,
    };
  },
  methods: {
    initMap() {
      const mbAttr =
        'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, ' +
        'Imagery © <a href="https://www.mapbox.com/">Mapbox</a>';
      const mbUrl =
        "https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token=pk.eyJ1IjoiZmVkZXJkaXNwaSIsImEiOiJjbGdreWlncHcwd3F0M2hsdnhscGg5Yzc1In0.Ud5vRdMf9cbtUUd5ufgKXQ";

      const rUrl = "https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png";

      const topplusUrl = "http://sgx.geodatenzentrum.de/wms_topplus_web_open";
      const topplusAttr =
        '&copy Bundesamt für Kartographie und Geodäsie 2017, <a href="http://sg.geodatenzentrum.de/web_public/Datenquellen_TopPlus_Open.pdf">Datenquellen</a>';

      const grayscale = L.tileLayer(mbUrl, {
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
        layers: [streets],
      });

      this.map.attributionControl.setPrefix(false);

      // Definition of layers
      const baseLayers = {
        Grayscale: grayscale,
        Streets: streets,
        Relief: relief,
        Topplus: topplus,
      };

      // Add layers and points layers to the map
      L.control.layers(baseLayers).addTo(this.map);

      this.unsubscribe.push(
        ...[
          subscribeFeatureVisibiltyChange(
            this.onFeatureVisibilityChange.bind(this)
          ),
          subscribeFeatureClick(this.onFeatureClick.bind(this)),
        ]
      );
    },
    async displayFeature(featureId, catalogId) {
      const res = await getFeature(featureId, catalogId);
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

    createMapObj(feature) {
      let mapObj;

      if (feature.wkt.type === "LINESTRING") {
        mapObj = new L.Polyline(
          feature.wkt.geo.map(([lng, lat]) => new L.LatLng(lat, lng)),
          {
            color: "blue",
            weight: 3,
            opacity: 0.5,
            smoothFactor: 1,
          }
        );
      } else if (feature.wkt.type === "MULTILINESTRING") {
        mapObj = new L.multiPolyline(
          feature.wkt.geo.map((x) =>
            x.map(([lng, lat]) => new L.LatLng(lat, lng))
          ),
          {
            color: "blue",
            weight: 3,
            opacity: 0.5,
            smoothFactor: 1,
          }
        );
      } else if (feature.wkt.type === "POLYGON") {
        mapObj = new L.Polygon(
          feature.wkt.geo.map(([lng, lat]) => new L.LatLng(lat, lng)),
          {
            color: "blue",
            weight: 3,
            opacity: 0.5,
            smoothFactor: 1,
          }
        );
      } else if (feature.wkt.type === "POINT") {
        mapObj = new L.marker(
          new L.LatLng(feature.wkt.geo[1], feature.wkt.geo[0])
        );
      }

      if (mapObj) {
        mapObj.on("click", (event) => {
          this.displayFeature(
            event.target.spalodId,
            event.target.spalodCatalogId
          );
        });
        mapObj.spalodId = feature.id;
        mapObj.spalodCatalogId = feature.catalogId;
        mapObj.visible = false;

        this.mapObjList.push(mapObj);
        return mapObj;
      } else {
        throw new Error("Unkown feature type");
      }
    },

    onFeatureVisibilityChange(
      features,
      remove,
      doZoom = false,
      firstCall = true
    ) {
      if (firstCall) {
        this.total += features.length;
      }

      const chunkSize = 10;
      const chunkFeatures = features.slice(0, chunkSize);
      const remainingFeatures = features.slice(chunkSize);

      for (const feature of chunkFeatures) {
        let mapObj = this.mapObjList.find(
          ({ spalodId }) => spalodId === feature.id
        );

        if (!mapObj && feature.wkt) {
          mapObj = this.createMapObj(feature);
        }

        if (mapObj) {
          if (feature.visible && !remove) {
            mapObj.addTo(this.map);
            mapObj.visible = true;
          } else {
            mapObj.removeFrom(this.map);
            mapObj.visible = false;

            if (remove) {
              this.mapObjList.splice(this.mapObjList.indexOf(mapObj), 1);
            }
          }

          doZoom = true;
        }
      }

      this.actual += chunkSize;

      if (remainingFeatures.length > 0) {
        setTimeout(() => {
          this.onFeatureVisibilityChange(
            remainingFeatures,
            remove,
            doZoom,
            false
          );
        }, 10);
      } else {
        if (this.actual >= this.total) {
          this.actual = 0;
          this.total = 0;
        }

        const objs = this.mapObjList.filter((x) => x.visible);
        if (doZoom && objs.length > 0) {
          this.map.options.maxZoom = 17;
          this.fitBounds(objs.map((x) => this.getObjBounds(x)).flat(1));
        }
      }
    },

    getObjBounds(obj) {
      if (typeof obj.getBounds === "function") {
        return obj.getBounds();
      } else if (typeof obj.getLatLng === "function") {
        const latLngs = [obj.getLatLng()];
        const markerBounds = L.latLngBounds(latLngs);
        return markerBounds;
      }
    },

    onFeatureClick(featureId) {
      const mapObj = this.mapObjList.find(
        ({ spalodId }) => spalodId === featureId
      );

      this.fitBounds(this.getObjBounds(mapObj));
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

    getProgressWidth() {
      return Math.floor((this.actual / this.total) * 100) + "%";
    },
  },
  mounted() {
    this.initMap();
  },
  unmounted() {
    this.unsubscribe.forEach((x) => x());
    // clearFeatures();
  },
};
</script>
