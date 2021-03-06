// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings = [
    '"Carpe diem. Seize the day, boys. Make your lives extraordinary.”' +
      " -Dead Poets Society",
    "\"Roads? Where we're going we don't need roads.\"" +
      " -Back to the Future",
    "I was born in Kumasi, Ghana.",
    "My favorite video game to play is League of Legends.",
  ];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById("greeting-container");
  greetingContainer.innerText = greeting;
}

function getData() {
  console.log("We are Here;");
  const responsePromise = fetch("/data");

  responsePromise.then(handleResponse);
}

function handleResponse(response) {
  console.log("Handling the response.");

  const textPromise = response.text();

  textPromise.then(addHelloToDom);
}

function addHelloToDom(hello) {
  console.log("Adding Hello to dom: " + hello);

  const quoteContainer = document.getElementById("data-container");
  quoteContainer.innerText = hello;
}

function getJson() {
  var max = document.getElementById("maxForm").value;
  fetch("/jsonData?maxCount=" + max)
    .then((response) => response.json())
    .then((jsonData) => {
      var commentContainer = document.getElementById("comment-container");
      var allComments = "";
      for (content in jsonData) {
        allComments += "<b>";
        allComments += jsonData[content].userEmail;
        allComments += "</b>";
        allComments += ": ";
        allComments += jsonData[content].message;
        allComments += "<br>";
      }
      commentContainer.innerHTML = allComments;
    });
}

function deleteText() {
  fetch("/delete-data", { method: "POST" });
  window.location.reload();
}

function passwordCheck() {
  var password = document.getElementById("password").value;
  if (password == "Marcus-Aurelius") {
    window.alert("Correct! Felicior Augusto, melior Traiano");
    deleteText();
  } else {
    window.alert("Wrong Password, Try Again.");
  }
}

async function checkStatus() {
  const response = await fetch("/User");
  const data = await response.text();
  document.getElementById("userContainer").innerHTML = data;
  var status = response.status;
  if (status >= 200 && status <= 299) {
    whenLoggedIn();
  } else {
    document.getElementById("userContainer").classList.add("hide");
  }
}

function whenLoggedIn() {
  var HelloWhileLoggedOut = document.getElementById("htmlLoggedOut");
  HelloWhileLoggedOut.classList.add("hide");

  var submitComment = document.getElementById("commentForm");
  submitComment.classList.remove("hide");
  var deleteComment = document.getElementById("deleteForm");
  deleteComment.classList.remove("hide");
}

google.charts.load("current", { packages: ["corechart"] });
google.charts.setOnLoadCallback(drawChart);

/** Creates a chart and adds it to the page. */
async function drawChart() {
  const response = await fetch("/GameDataServlet");
  const gameVotes = await response.json();
  const data = new google.visualization.DataTable();
  data.addColumn("string", "Game");
  data.addColumn("number", "Votes");
  Object.keys(gameVotes).forEach((title) => {
    data.addRow([title, gameVotes[title]]);
  });

  const options = {
    title: "Favorite Games",
    width: 600,
    height: 500,
  };

  const chart = new google.visualization.ColumnChart(
    document.getElementById("chart-container")
  );
  chart.draw(data, options);
}
