package com.google.sps.data;

/** An item on the game Choice list. */
public final class gameChoice {
  private String game;
  private long count;

  public gameChoice(String game, long count) {
    this.game = game;
    this.count = count;
  }

  public String getGame() {
    return this.game;
  }

  public long getCount() {
    return this.count;
  }
}
