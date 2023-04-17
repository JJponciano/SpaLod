<script>
import L from 'leaflet';

export default {
    data() {
        return {
            dataArray: 'default',
            sizeOfArray: 0,
        };
    },
    mounted() {
        // Creation of one layer of points
        var query = new L.layerGroup();

        // Custom icon class
        var MarkerIcon = L.Icon.extend({
            options: {
                iconSize: [20, 20],
                shadowSize: [0, 0],
                iconAnchor: [10, 10],
                popupAnchor: [0, -10],
            }
        });

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
            layers: [streets, query]
        });

        // Definition of layers
        var baseLayers = {
            "Grayscale": grayscale,
            "Streets": streets,
            "Relief" : relief,
            "Topplus" : topplus
        };

        // Definition of point layers
        var overlays = {
            "Query": query
        };

        // Add layers and points layers to the map
        L.control.layers(baseLayers, overlays).addTo(map);
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
