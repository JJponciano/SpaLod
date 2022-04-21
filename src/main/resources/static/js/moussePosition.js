 L.Control.MousePosition = L.Control.extend({

_pos: null,

options: {
    position: 'bottomright',
    separator: ' , ',
    emptyString: 'Unavailable',
    lngFirst: false,
    numDigits: 5,
    lngFormatter: undefined,
    latFormatter: undefined,
    formatter: undefined,
    prefix: "",
    wrapLng: true,
},

onAdd: function (map) {
    this._container = L.DomUtil.create('div', 'leaflet-control-mouseposition');
    L.DomEvent.disableClickPropagation(this._container);
    map.on('mousemove', this._onMouseMove, this);
    this._container.innerHTML = this.options.emptyString;
    return this._container;
},

onRemove: function (map) {
    map.off('mousemove', this._onMouseMove)
},

getLatLng: function() {
    return this._pos;
},

_onMouseMove: function (e) {
    this._pos = e.latlng.wrap();
    var lngValue = this.options.wrapLng ? e.latlng.wrap().lng : e.latlng.lng;
    var latValue = e.latlng.lat;
    var lng;
    var lat;
    var value;
    var prefixAndValue;

    if (this.options.formatter) {
        prefixAndValue = this.options.formatter(lngValue, latValue);
    } else {
        lng = this.options.lngFormatter ? this.options.lngFormatter(lngValue) : L.Util.formatNum(lngValue, this.options.numDigits);
        lat = this.options.latFormatter ? this.options.latFormatter(latValue) : L.Util.formatNum(latValue, this.options.numDigits);
        value = this.options.lngFirst ? lng + this.options.separator + lat : lat + this.options.separator + lng;
        prefixAndValue = this.options.prefix + ' ' + value;
    }

    this._container.innerHTML = prefixAndValue;
}

});

L.Map.mergeOptions({
positionControl: false
});

L.Map.addInitHook(function () {
if (this.options.positionControl) {
    this.positionControl = new L.Control.MousePosition();
    this.addControl(this.positionControl);
}
});

L.control.mousePosition = function (options) {
return new L.Control.MousePosition(options);
};

var a = `.leaflet-container .leaflet-control-mouseposition {
      background-color: rgba(255, 255, 255, 0.7);
      box-shadow: 0 0 5px #bbb;
      padding: 0 5px;
      margin:0;
      color: #333;
      font: 11px/1.5 "Helvetica Neue", Arial, Helvetica, sans-serif;
    }`;
var styleSheet = document.createElement("style");
styleSheet.innerText = a;

document.head.appendChild(styleSheet);
L.control.mousePosition().addTo(map);