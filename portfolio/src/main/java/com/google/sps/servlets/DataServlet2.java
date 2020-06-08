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
import com.google.sps.data.Text;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.util.List;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/jsonData")

public class DataServlet2 extends HttpServlet {  
  // This an array List that will be used in DeletedataSerlet.java to delete all comments

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Query query = new Query("Text").addSort("timestamp", SortDirection.DESCENDING);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);
      List<Text> texts = new ArrayList<>();

      String maxCommentsString = request.getParameter("maxCount");
      int maxComment;
      try {
        maxComment = Integer.parseInt(maxCommentsString);
      } catch (NumberFormatException e) {
        System.err.println("Could not convert to int: " + maxCommentsString);
        maxComment = -1;
      }

      // Check that the input is between 0 and 50
      if (maxComment < 0 || maxComment > 50) {
        System.err.println("User choice is out of range: " + maxCommentsString);
        maxComment = -1;
      }
      
      int currentComment = 0;
      for (Entity entity : results.asIterable()) {
        if(maxComment != -1){
            currentComment += 1;
            if (maxComment < currentComment){
                break;
            }
        }      
        long id = entity.getKey().getId();
        String title = (String) entity.getProperty("message");
        long timestamp = (long) entity.getProperty("timestamp");

      Text text = new Text(id, title, timestamp);
      texts.add(text);
    }

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(texts));
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      String message = request.getParameter("userComment");
      long timestamp = System.currentTimeMillis();
      Entity commentEntity = new Entity("Text");
      commentEntity.setProperty("message", message);
      commentEntity.setProperty("timestamp", timestamp);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(commentEntity);
      response.sendRedirect("/index.html");
  }
}
