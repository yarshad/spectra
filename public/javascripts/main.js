console.log("Welcome to your Play application's JavaScript!");


var websocket = new WebSocket("ws://localhost:9000/wtodo")

websocket.open = function(event){
    console.log(event)
    websocket.send("hello from play")
}

websocket.onmessage = function(event){
    console.log(event.data)
}

// websocket.onmessage = function(event) {
//     var out = document.getElementById("out");
//     out.textContent = event.data;
// }

websocket.onclose = function(event){

    console.log("-------------------------------")
    console.log(event)
    console.log("Connection closed")
}



window.setInterval(function(){
    var data = JSON.stringify({'msg': "test msg",'timestamp': Date.now()});
    console.log("send " + data);
    websocket.send(data);
}, 1000);