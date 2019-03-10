<%--
  Created by IntelliJ IDEA.
  User: lenovo
  Date: 2018-8-14
  Time: 16:09
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>websocket</title>
  </head>
  <body onload="init()">
  <div>
    <p id="welcome"></p><input type="button" id="send" onclick="sendTo()" value="发送消息"/><br/>

  </div>
 <script>
var ws;

   function init(){
     var myName=prompt("请输入您的姓名");
     document.getElementById("welcome").innerText="欢迎您，"+myName;
     var url="ws://192.168.43.238:8080/doEvent/";
     if("WebSocket" in window){
       ws=new WebSocket(url+myName);
       ws.onopen=function () {
         console.log("websocket已打开");
       }
       ws.onmessage=function (ev) { console.log(ev.data) }
     }else {
       alert("浏览器不支持websocket")
     }
   }

   function sendTo(){
     if(!ws){
       alert("您尚未与服务器建立连接，请稍等");
       return;
     }
     var sendTo=prompt("你想发给谁");
     if(sendTo){
       var msg=prompt("请输入你想发送内容");
       var json="{\"receiver\":\""+sendTo+"\",\"message\":\""+msg+"\"}";
       ws.send(json);
     }else {
       alert("没有发送人");
     }
   }
 </script>
  </body>
</html>
