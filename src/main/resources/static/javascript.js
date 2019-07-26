var showMeasure = false;
var correct = 0;
var wrong = 0;

$('#stop').mousedown(function(e) {
	showMeasure = false;
	document.getElementById("result").innerHTML = "";
	document.getElementById("stop").style.display = "none";
	document.getElementById("measure").style.display = "inline";
	document.getElementById("train").style.display = "inline";
});

$('#load')
		.mousedown(
				function(e) {
					alert("If you have saved a network from previous exercise, You can simply paste your network into the text area below.");
				});

$('#create')
		.mousedown(
				function(e) {
					document.getElementById("notification").innerHTML = "Constructing a random network. Please wait...";
					var xmlhttp = new XMLHttpRequest(); // new HttpRequest
					// instance
					xmlhttp.open("GET", "/network");
					xmlhttp.onreadystatechange = function() {
						if (this.readyState == 4 && this.status == 200) {
							document.getElementById("notification").innerHTML = "Network created. You can see the network in text area.";
							document.getElementById("networkJson").value = this.responseText;
						}
					};
					xmlhttp.send();
				});

$('#measure')
		.mousedown(
				function(e) {
					if (document.getElementById("networkJson").value != '') {
						document.getElementById("notification").innerHTML = "Running MNIST test data on the network. Please wait...";
						var xmlhttp = new XMLHttpRequest(); // new HttpRequest
						// instance
						xmlhttp.open("POST", "/network/score");
						xmlhttp.setRequestHeader("Content-Type",
								"application/json");
						xmlhttp.onreadystatechange = function() {
							if (this.readyState == 4 && this.status == 200) {
								showMeasure = true;
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
						document.getElementById("notification").innerHTML = "You dont have any network. Please try creating a random network.";
					}
				});

$('#train')
		.mousedown(
				function(e) {
					if (document.getElementById("networkJson").value != '') {
						document.getElementById("notification").innerHTML = "Training in progress. Please wait...";
						var xmlhttp = new XMLHttpRequest(); // new HttpRequest
						// instance
						xmlhttp.open("PUT", "/network");
						xmlhttp.setRequestHeader("Content-Type",
								"application/json");
						xmlhttp.onreadystatechange = function() {
							if (this.readyState == 4 && this.status == 200) {
								document.getElementById("notification").innerHTML = "Training complete. Click measure to see network quality. Note that you can always re-train the network if you are not satisfied with results.";
								document.getElementById("networkJson").value = this.responseText;
							}
						};
						xmlhttp
								.send(document.getElementById("networkJson").value);
					} else {
						document.getElementById("notification").innerHTML = "You dont have any network. Please try creating a random network.";
					}
				});

function myLoop(jsonResponse, i) {
	setTimeout(
			function() {
				if (showMeasure) {
					document.getElementById("result").innerHTML = "";

					if (jsonResponse[i]['networkAnswer'] == jsonResponse[i]['actualAnswer']) {
						document.getElementById("result").style.color = "green";
						correct++;
					} else {
						document.getElementById("result").style.color = "red";
						wrong++;
					}
					document.getElementById("notification").innerHTML = "Network Accuracy is: "
							+ Math.round(((correct) / (correct + wrong)) * 100)	+ "%;
					document.getElementById("stop").style.display = "inline";
					document.getElementById("measure").style.display = "none";
					document.getElementById("train").style.display = "none";
					for (var j = 0; j < jsonResponse[i]['inputImage'].length; j++) {
						document.getElementById("result").innerHTML += jsonResponse[i]['inputImage'][j]
								+ "<br>";
					}
					if (--i)
						myLoop(jsonResponse, i); // decrement i and call
					// myLoop again if i > 0
				}
			}, 0)
};