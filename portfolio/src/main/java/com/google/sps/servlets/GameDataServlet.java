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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.gameChoice;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/GameDataServlet")
public class GameDataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    Query query = new Query("gameChoice");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    List<gameChoice> games = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      String game = (String) entity.getProperty("game");
      long count = (long) entity.getProperty("count");

      gameChoice gameVote = new gameChoice(game, count);
      games.add(gameVote);
    }

    response.setContentType("application/json;");
    response.getWriter().println(convertToJson(games));
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    String game = request.getParameter("game");
    Entity gameEntity = new Entity("gameChoice");
    if (!checkDuplicate(game)) {
      gameEntity.setProperty("game", game);
      gameEntity.setProperty("count", 1);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(gameEntity);
    }

    response.sendRedirect("/chart.html");
  }

  private String convertToJson(List<gameChoice> games) {
    String json = "{";
    for (int i = 0; i < games.size(); ++i) {
      if (i != 0) {
        json += ", ";
      }
      json += "\"" + games.get(i).getGame() + "\": ";
      json += games.get(i).getCount();
    }
    json += "}";
    return json;
  }

  private boolean checkDuplicate(String check) {
    Query query = new Query("gameChoice");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
      String game = (String) entity.getProperty("game");
      if (game.equals(check)) {
        long count = (long) entity.getProperty("count");
        long newCount = count + 1;
        entity.setProperty("count", newCount);
        datastore.put(entity);
        return true;
      }
    }
    return false;
  }
}
