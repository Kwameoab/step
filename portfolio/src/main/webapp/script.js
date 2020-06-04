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
  fetch("/jsonData")
    .then((response) => response.json())
    .then((jsonData) => {
      console.log(jsonData);
      var commentContainer = document.getElementById("comment-container");
      var allComments = "";
      for (content in jsonData) {
        console.log(content);
        allComments += jsonData[content];
        allComments += "<br>";
      }
      commentContainer.innerHTML = allComments;
    });
}
