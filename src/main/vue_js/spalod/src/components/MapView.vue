<script>
import L from 'leaflet';

export default {
    props:{
        file:File
    },
    watch:{
        file(newFile){
            const lecteur=new FileReader();

            this.dataArray=[];
            lecteur.readAsText(newFile);
            lecteur.onload = () => {
                const contenu = lecteur.result;
                const object = JSON.parse(contenu);

                object.features.forEach(feature => {
                    if(!feature.properties.item) return;
                    this.dataArray.push([object.name, feature.properties.itemLabel, feature.geometry.coordinates[1], feature.geometry.coordinates[0]]);
                });
                this.updateMap();
            };
        }
    },
    data() {
        return {
            dataArray: [],
            sizeOfArray: 0,
            path: null,
            query:null,
        };
    },
    methods: {
        objectSize(obj) {
            var size = 0, key;
            for (key in obj) {
                if (obj.hasOwnProperty(key)) size++;
            }
            return size;
        },
        initMap(){
            // Path to where the files are hosted
            this.path = 'src/pictures/signaturen/';

            // Creation of one layer of points
            this.query = new L.layerGroup();

            // Custom icon class
            var MarkerIcon = L.Icon.extend({
                options: {
                    iconSize: [20, 20],
                    shadowSize: [0, 0],
                    iconAnchor: [10, 10],
                    popupAnchor: [0, -10],
                }
            });
            this.sizeOfArray = this.objectSize(this.dataArray);

            for (let i = 0; i < this.sizeOfArray; i++) {
                L.marker(
                    [this.dataArray[i][2], this.dataArray[i][3]],
                    {
                        icon: new MarkerIcon({
                            iconUrl: this.path + this.dataArray[i][0] + '.png'
                        })
                    }
                ).bindPopup(this.dataArray[i][2] + "," + this.dataArray[i][3] + " " + this.dataArray[i][1]).addTo(this.query);
            }

            var mbAttr = 'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, ' +
                                        'Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
                mbUrl = 'https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token=pk.eyJ1IjoiZmVkZXJkaXNwaSIsImEiOiJjbGdreWlncHcwd3F0M2hsdnhscGg5Yzc1In0.Ud5vRdMf9cbtUUd5ufgKXQ';

            var rUrl = 'https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png';

            var topplusUrl = 'http://sgx.geodatenzentrum.de/wms_topplus_web_open';
            var topplusAttr = '&copy Bundesamt für Kartographie und Geodäsie 2017, <a href="http://sg.geodatenzentrum.de/web_public/Datenquellen_TopPlus_Open.pdf">Datenquellen</a>';

            var grayscale = L.tileLayer(mbUrl, {id: 'mapbox/light-v9', tileSize: 512, zoomOffset: -1, attribution: mbAttr}),
                streets = L.tileLayer(mbUrl, {id: 'mapbox/streets-v11', tileSize: 512, zoomOffset: -1, attribution: mbAttr}),
                relief = L.tileLayer(rUrl, {maxZoom: 17, attribution: 'Map data: &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, <a href="http://viewfinderpanoramas.org">SRTM</a> | Map style: &copy; <a href="https://opentopomap.org">OpenTopoMap</a> (<a href="https://creativecommons.org/licenses/by-sa/3.0/">CC-BY-SA</a>)'}),
                topplus = L.tileLayer.wms(topplusUrl, {layers: 'web', format: 'image/png',transparent: true, attribution: topplusAttr});
                        
            // Definition of the map, and setup of details : coordinates of the center, zoom level and layers that appear on loading
            var map = L.map('map', {
                center: [49.99, 8.24],
                zoom: 6,
                minZoom: 2,
                fullscreenControl: true,
                layers: [streets, this.query]
            });

            map.attributionControl.setPrefix(false);

            // Definition of layers
            var baseLayers = {
                "Grayscale": grayscale,
                "Streets": streets,
                "Relief" : relief,
                "Topplus" : topplus
            };

            // Definition of point layers
            var overlays = {
                "Query": this.query
            };

            // Add layers and points layers to the map
            L.control.layers(baseLayers, overlays).addTo(map);
        },
        updateMap(){
            this.query.clearLayers();
            this.sizeOfArray = this.objectSize(this.dataArray);

            var MarkerIcon = L.Icon.extend({
                options: {
                    iconSize: [20, 20],
                    shadowSize: [0, 0],
                    iconAnchor: [10, 10],
                    popupAnchor: [0, -10],
                }
            });

            for (let i = 0; i < this.sizeOfArray; i++) {
                L.marker(
                    [this.dataArray[i][2], this.dataArray[i][3]],
                    {
                        icon: new MarkerIcon({
                            iconUrl: this.path + this.dataArray[i][0] + '.png'
                        })
                    }
                ).bindPopup(this.dataArray[i][2] + "," + this.dataArray[i][3] + " " + this.dataArray[i][1]).addTo(this.query);
            }
        },
    },
    mounted() {
        this.initMap();
    },
};
</script>

<template>
    <div class="map-view" id="map">
        <h2>Map</h2>
    </div>
</template>

<style>
.map-view {
    padding: 20px;
    border-radius: 5px;
    background-color: #4A5568;
    height: calc(100vh - 200px);
    color: white;
    resize: both;
    overflow: auto;
    min-width: 500px;
    min-height: 150px;
}

.h2 {
    margin-top: 0;
}
</style>
