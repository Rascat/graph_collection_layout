package org.rascat.gcl.functions;

public enum VertexType {
  TAIL("tail_x", "tail_y", "tail_ids"),
  HEAD("head_x", "head_y", "head_ids");

  private String keyX;
  private String keyY;
  private String keyGraphIds;

  VertexType(String keyX, String keyY, String keyGraphIds) {
    this.keyX = keyX;
    this.keyY = keyY;
    this.keyGraphIds = keyGraphIds;
  }

  public String getKeyX() {
    return keyX;
  }

  public String getKeyY() {
    return keyY;
  }

  public String getKeyGraphIds() {
    return keyGraphIds;
  }
}
