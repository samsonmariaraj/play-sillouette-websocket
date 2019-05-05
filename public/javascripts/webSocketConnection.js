$(function() {

   var akkaSocketDiv = $('#akkasocketdiv');
   function webSocketConnection() {
       var ws = new ReconnectingWebSocket($("#akkasocketdiv").data("ws-url"));;
        ws.onopen = function(){
            // Web Socket is connected, send data using send()
            console.log("connected");
            ws.send('{"cmd":"refresh"}');
            ws.send('{"cmd":"refresh"}');
        };

        ws.onmessage = function (evt){
           // ws.send('{"cmd":"refresh"}');
            console.log(evt.data);
        };

        ws.onclose = function(){
            // websocket is closed.
            alert("Connection is closed...");
        };

   }
   akkaSocketDiv.click(webSocketConnection);
});