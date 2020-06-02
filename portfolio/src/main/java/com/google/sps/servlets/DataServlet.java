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

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import com.google.gson.Gson;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;");
    response.getWriter().println("Hello Kwame!");
    
    ArrayList<String> comments = new ArrayList<>();
    comments.add("This is the first comment.");
    comments.add("This is the second comment.");
    comments.add("This is a spicy comment.");

    String json = convertToJson(comments);

    response.getWriter().println(json);
  }


  private String convertToJson(ArrayList<String> comments){
      String json = "{";
      for(int i=0; i < comments.size(); ++i){
          if (i != 0){
              json += ", ";
          }
          json += "\"Comment " + (i+1) + "\": ";
          json += "\"" + comments.get(i) + "\"";
      }
      json += "}";
      return json;
  }
  
  /*
  private String convertToJson(ArrayList<String> comments){
      String json = new Gson().toJson(comments);
      return json;
  } */
}
