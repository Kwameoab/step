package com.google.sps.data;

/** An item on a text list. */
public final class Text {
  private final long id;
  private final String userEmail;
  private final String message;
  private final long timestamp;

  public Text(long id, String userEmail, String message, long timestamp) {
    this.id = id;
    this.userEmail = userEmail;
    this.message = message;
    this.timestamp = timestamp;
  }
}
