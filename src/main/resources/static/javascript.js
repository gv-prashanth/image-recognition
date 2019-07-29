var correct = 0;
var wrong = 0;

$('#load')
		.mousedown(
				function(e) {
					document.getElementById("notification1").innerHTML = "If you have saved a network from previous exercise, You can simply paste your network into the text area below.";
				});

$('#create')
		.mousedown(
				function(e) {
					document.getElementById("notification1").innerHTML = "Constructing a random network. Please wait...";
					var xmlhttp = new XMLHttpRequest(); // new HttpRequest
					// instance
					xmlhttp.open("GET", "/network");
					xmlhttp.onreadystatechange = function() {
						if (this.readyState == 4 && this.status == 200) {
							document.getElementById("notification1").innerHTML = "Network created. You can see the network in text area.";
							document.getElementById("networkJson").value = this.responseText;
						}
					};
					xmlhttp.send();
				});

$('#measure')
		.mousedown(
				function(e) {
					if (document.getElementById("networkJson").value != '') {
						turnOffTrain();
						document.getElementById("notification3").innerHTML = "Running MNIST test data on the network. Please wait...";
						var xmlhttp = new XMLHttpRequest(); // new HttpRequest
						// instance
						xmlhttp.open("POST", "/network/score");
						xmlhttp.setRequestHeader("Content-Type",
								"application/json");
						xmlhttp.onreadystatechange = function() {
							if (this.readyState == 4 && this.status == 200) {
								correct = 0;
								wrong = 0;
								var jsonResponse = JSON
										.parse(this.responseText);
								myLoop(jsonResponse, jsonResponse.length - 1);
							}
						};
						xmlhttp
								.send(document.getElementById("networkJson").value);
					} else {
						document.getElementById("notification3").innerHTML = "You dont have any network. Please try creating a random network.";
					}
				});

$('#train1')
.mousedown(
		function(e) {
			train("stochastic");
		});

$('#train2')
.mousedown(
		function(e) {
			train("minibatch");
		});

$('#train3')
.mousedown(
		function(e) {
			train("fullbatch");
		});

function train(type){
	if (document.getElementById("networkJson").value != '') {
		turnOffTrain();
		document.getElementById("notification2").innerHTML = "Training in progress. Please wait...";
		var xmlhttp = new XMLHttpRequest(); // new HttpRequest
		// instance
		xmlhttp.open("PUT", "/network/"+type);
		xmlhttp.setRequestHeader("Content-Type",
				"application/json");
		xmlhttp.onreadystatechange = function() {
			if (this.readyState == 4 && this.status == 200) {
				turnOnTrain();
				document.getElementById("notification2").innerHTML = "Training complete and the network in the text area is updated. Click measure to see network quality. Note that you can always re-train the network if you are not satisfied with results.";
				document.getElementById("networkJson").value = this.responseText;
				if(document.getElementById("train4").checked){
					train(type);
				}
			}
		};
		xmlhttp
				.send(document.getElementById("networkJson").value);
	} else {
		document.getElementById("notification2").innerHTML = "You dont have any network. Please try creating a random network.";
	}
}


function myLoop(jsonResponse, i) {
	setTimeout(
			function() {

					document.getElementById("result").innerHTML = "";

					if (jsonResponse[i]['networkAnswer'] == jsonResponse[i]['actualAnswer']) {
						document.getElementById("result").style.color = "green";
						correct++;
					} else {
						document.getElementById("result").style.color = "red";
						wrong++;
					}
					document.getElementById("notification3").innerHTML = "Network Accuracy is: "
							+ Math.round(((correct) / (correct + wrong)) * 100)
							+ "%";
					document.getElementById("measure").disabled = true;
					for (var j = 0; j < jsonResponse[i]['inputImage'].length; j++) {
						document.getElementById("result").innerHTML += jsonResponse[i]['inputImage'][j]
								+ "<br>";
					}
					if (--i){
						myLoop(jsonResponse, i); // decrement i and call
					// myLoop again if i > 0
					}else{
						document.getElementById("result").innerHTML = "";
						document.getElementById("measure").disabled = false;
						turnOnTrain();
					}
			}, 50)
};

function turnOffTrain() {
	$('#train :input').attr('disabled', true);
	document.getElementById("measure").disabled = true;
}

function turnOnTrain() {
	$('#train :input').removeAttr('disabled');
	document.getElementById("measure").disabled = false;
}