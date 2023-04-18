

 $(document).ready(function () {

    /**
     * Update the view according to the data received
     * @param {*} data 
     */
    $.update=function(data){
        dataArray=data.data
        rdfDownload(data.output,"download")
          dataArray=data.data
        createsTable(dataArray);
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

//       // when the document is ready, ask if data are available
//       $.ajax({
//             url: "http://localhost:8081/dataluplift"//local URI that should be adapt to the SERVER URI
//               }).then(function(data) {//retrieves the data provided by the server

//               });
var sq = new Object();
sq.query=$("#sparqlText").val();
sq.triplestore= $('input[name="triplestore"]:checked').val();

$.postJSON("http://localhost:8081/dataluplift", sq, $.update)


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
            td.appendChild(document.createTextNode(item));
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
    var linkText = document.createTextNode("Download data");
    a.appendChild(linkText);
    a.title = "Download data";
    a.href = rdf;
    title.appendChild(a);
}