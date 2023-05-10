

 $(document).ready(function () {

    /**
     * Update the view according to the data received
     * @param {*} data 
     */
    $.update=function(data){
        dataArray=data.data
        rdfDownload(data.rdf,"rdf_download")
        createsTable(dataArray);
        updateMap(dataArray)
    }


    $.postJSON = function(url, data, callback) {
        return jQuery.ajax({
        headers: { 
            'Accept': 'application/json',
            'Content-Type': 'application/json' 
        },
        'type': 'POST',
        'url': url,
        'data': JSON.stringify(data),
        'dataType': 'json',
        'success': callback
        });
    };

        $( "#submitquery" ).click(function() { 
            var sq = new Object();
            sq.query=$("#sparqlText").val();
            sq.triplestore= ""
            console.log(sq);
            $.postJSON("https://localhost:8081/ressem", sq, $.update)
        });
      

});


    // -----------------------------------------------Update after SPARQL query results-----------------------------------------------

function createsTable(dataArray) {
    var results = document.getElementById("tableContentSPARQL");
    var tbl = document.createElement('table');
    tbl.style.width = '100%';
    tbl.setAttribute('border', '1');
    var tbdy = document.createElement('tbody');
    for (let i = 0; i < dataArray.length; i++) {
        const row = dataArray[i];
        var tr = document.createElement('tr');
        for (let j = 0; j < row.length; j++) {
            const item = row[j];
            var td = document.createElement('td');
            console.log(item)
            if(item && item.startsWith("https")){
                var a = document.createElement('a');
                var linkText = document.createTextNode(item);
                a.appendChild(linkText);
                a.title = item;
                a.href = item;
                td.appendChild(a);
            }else{
                td.appendChild(document.createTextNode(item));
            }
            tr.appendChild(td);
        }
        tbdy.appendChild(tr);
    }
    tbl.appendChild(tbdy);
    results.appendChild(tbl);
}
function rdfDownload(rdf,id) {
    var title= document.getElementById(id); 

    var a = document.createElement('a');
    var linkText = document.createTextNode("Download RDF file");
    a.appendChild(linkText);
    a.title = "Download RDF file";
    a.href = rdf;
    title.appendChild(a);
}

function updateMap(dataArray) {
    sizeOfArray=dataArray.length
      // Loop to add every point of the results to the map 
      //starting at 1 because the first row is the headers
    // search the index containing the awkt
    var ind=0;
    
    



      for (let i = 0; i < dataArray[i].length; i++) {
        if(dataArray[1][i].includes("wktLiteral"))
        ind=i;
      }

      for (let i = 1; i < sizeOfArray; i++) {
      var str=dataArray[i][ind]
      var point = str.substring(
          str.indexOf("(") + 1,
          str.lastIndexOf(")")
      );
      /**
       * ICONE
       */
       var iconeName;
       switch (dataArray[i][0]) {
           case "https://schema.org/School"://Q3914
               iconeName = "HS";
               break;
           case "https://schema.org/FireStation"://Q1195942
               iconeName = "Feuerwehr";
               break;
           case "https://schema.org/Hospital"://Q16917
               iconeName = "KHV";
               break;
           case "https://schema.org/Museum"://Q33506
               iconeName = "Museen";
               break;
           case "https://schema.org/GasStation"://Q205495
               iconeName = "Tankstellen";
               break;
        //    case "": //Q2140665 or Q1477760
        //        iconeName = "LadeSt";
        //        break;
        //    case ""://Q180846
        //        iconeName = "Supermarkt";
        //        break;
           case "https://schema.org/Library"://Q7075
               iconeName = "Bibliothek";
               break;
        //    case ""://Q44782
        //        iconeName = "Seehaefen";
        //        break;
        //    case ""://Q55488
        //        iconeName = "Bahnhof";
        //        break;
        //    case ""://Q11707
        //        iconeName = "Restaurant";
        //        break;
        //    case ""://Q41253
        //        iconeName = "Kino";
        //        break;
        //    case ""://Q4989906
        //        iconeName = "Denkmal";
        //        break;
        //    case ""://Q861951
        //        iconeName = "BPOL";
        //        break;
        //    case ""://Q27686
        //        iconeName = "Hotel";
        //        break;
           case "https://schema.org/StadiumOrArena"://Q483110
               iconeName = "Stadium";
               break;
        //    case ""://Q1248784
        //        iconeName = "Flughaefen";
        //        break;
        //    case ""://Q22908
        //        iconeName = "Seniorenheime";
        //        break;
        //    case ""://Q200023
        //        iconeName = "Schwimmbad";
        //        break;
        //    case ""://Q19010
        //        iconeName = "Wetterstation";
        //        break;
        //    case ""://Q483242
        //        iconeName = "Laboratorium";
        //        break;
        //    case ""://Q364005
        //        iconeName = "Kita";
        //        break;
           default:
               iconeName = "KmBAB";
       }
      /*
       * END ICONE
       */
        var ll=point.split(' ');
        var longitude=ll[0];
        var latitude =ll[1];

           L.marker(
                [latitude, longitude],
                { icon: new MarkerIcon({iconUrl: path + iconeName +'.png'}) }
           ).bindPopup(dataArray[i][2]+","+dataArray[i][3]+" "+dataArray[i][1]).addTo(query);
      }
}