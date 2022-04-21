// Add and update the mini map
var miniMap = new L.Control.MiniMap(mini_streets).addTo(map);
map.on('baselayerchange', function (e) {
    if (e.name == "Grayscale"){
        miniMap.changeLayer(mini_grayscale);
    } else if (e.name == "Relief"){
        miniMap.changeLayer(mini_relief);
    } else {
        miniMap.changeLayer(mini_streets);
    }
});