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
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/jsonData")
public class DataServlet2 extends HttpServlet {
    private ArrayList<String> comments = new ArrayList();

  

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String json = convertToJson(comments);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  private String convertToJson(ArrayList<String> comments){
      String json = "{";
      for(int i = 0; i < comments.size(); ++i){
          if (i != 0){
              json += ", ";
          }
          json += "\"Comment " + (i + 1) + "\": ";
          json += "\"" + comments.get(i) + "\"";
      }
      json += "}";
      return json;
  }
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      comments.add(request.getParameter("userComment"));
      response.sendRedirect("/index.html");
  }      
}
