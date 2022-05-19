// Add and update the mini map
var miniMap = new L.Control.MiniMap(mini_streets).addTo(map);
map.on('baselayerchange', function (e) {
    if (e.name == "Grayscale"){
        miniMap.changeLayer(mini_grayscale);
    } else if (e.name == "Relief"){
        miniMap.changeLayer(mini_relief);
    } else if (e.name == "Streets"){
        miniMap.changeLayer(mini_streets);
    } else if (e.name == "Topplus"){
        miniMap.changeLayer(mini_topplus);
    }
});