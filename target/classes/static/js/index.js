/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

var ws = new WebSocket('wss://'+location.host+'/kurento');

var gtcExtVideo;
var gtcIntVideo;
var webRtcPeer;
var webs = new Map;
var state = null;

var stun = {
	"urls" : "stun:localhost:3478"
};

var turn = {
	"urls" : "turn:localhost:3478",
	"username" : "guest",
	"credential" : "12345"
}

var created = false;

var conn;

window.onload = function() {
	console.log("Page loaded ...");
	console = new Console('console', console);
	gtcExtVideo = document.getElementById('videoExt');
	gtcIntVideo = document.getElementById('videoInt');
	//init();
}

window.onbeforeunload = function() {
	stop();
	ws.close();
}

ws.onmessage = function(message) {
	var parsedMessage = JSON.parse(message.data);
	console.info('Received message: ' + message.data);

	switch (parsedMessage.id) {
	case 'sdpAnswer':
		startVisualice(parsedMessage);
		break;
	case 'error':
		onError("Error message from server: " + parsedMessage.message);
		break;
	case 'stopStream':
		stop();
		break;
	case 'iceCandidate':
	    webs.get(parsedMessage.idCam).addIceCandidate(parsedMessage.candidate, function (error) {
	        if (error) {
		      console.error("Error adding candidate: " + error);
		      return;
	        }
	    });
	    break;
	default:
		onError('Unrecognized message', parsedMessage);
	}
}

function init() {
	console.log("Starting WebRTC")
	showSpinner(gtcIntVideo, gtcExtVideo);

	console.log("Creating WebRtcPeer and generating local sdp offer ...");

	var options = {
		remoteVideo: gtcIntVideo,
		onicecandidate: onIceCandidate,
		configuration : { iceServers: [stun, turn]}
    }

	webs.set("gtcInt", createWebRcpPeers(options, "gtcInt"));

	options = {
		remoteVideo: gtcExtVideo,
		onicecandidate: onIceCandidate,
		configuration : { iceServers: [stun, turn]}
    }
	
	webs.set("gtcExt", createWebRcpPeers(options, "gtcExt"));

	created = true;
}

function createWebRcpPeers(options, cam) {
	return new kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(options,
		function (error) {
		  if(error) {
			  return console.error(error);
		  }
		  this.generateOffer (function (error, offerSdp) {
			if (error) return console.error (error);
			console.info('Invoking SDP offer callback function ' + location.host);
			var message = {
				id : 'sdpOffer',
				idCam : cam,
				sdpOffer : offerSdp
			}
			
			sendMessage(message);	
		});
	});
}

function onIceCandidate(candidate) {
	  console.log("Local candidate" + JSON.stringify(candidate));

	  var message = {
	    id: 'onIceCandidate',
	    candidate: candidate
	  };
	  sendMessage(message);
}

function onError(error) {
	roisValues = new Array();
	console.error(error);
}

function startVisualice(message) {
	console.log("SDP answer received from server. Processing ...");
	webs.get(message.idCam).processAnswer (message.sdpAnswer, function (error) {
		if (error) return console.error (error);
	});
}

function stop() {
	webs.forEach(function(value, key) {
		value.dispose();
	});
	webs.clear();

	var message = {
		id : 'stopStream'
	}
	sendMessage(message);

	hideSpinner(gtcExtVideo, gtcIntVideo);
}

function refresh() {
	stop();
	init();
}

function sendMessage(message) {
	var jsonMessage = JSON.stringify(message);
	console.log('Sending message: ' + jsonMessage);
	ws.send(jsonMessage);
}

function showSpinner() {
	for (var i = 0; i < arguments.length; i++) {
		arguments[i].poster = './img/transparent-1px.png';
		arguments[i].style.background = "center transparent url('./img/spinner.gif') no-repeat";
	}
}

function hideSpinner() {
	for (var i = 0; i < arguments.length; i++) {
		arguments[i].src = '';
		arguments[i].poster = './img/webrtc.png';
		arguments[i].style.background = '';
	}
}

function updateValue(val, name) {
	document.getElementById(name).value = val;
	changeProcessingWidth (val);
}

function changeProcessingWidth (width){
	value = document.getElementById('rangeValue12').value;
	var message = {
		id : 'changeProcessingWidth',
		width : value
	}
	sendMessage(message);
}
	
/**
 * Lightbox utility (to display media pipeline image in a modal dialog)
 */
$(document).delegate('*[data-toggle="lightbox"]', 'click', function(event) {
	event.preventDefault();
	$(this).ekkoLightbox();
});
