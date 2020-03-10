package org.rascat.gcl.layout;

import org.gradoop.flink.model.impl.epgm.GraphCollection;

public abstract class AbstractGraphCollectionLayout {
  public static String KEY_X_COORD = "X";
  public static String KEY_Y_COORD = "Y";
  protected final int width;
  protected final int height;

  public AbstractGraphCollectionLayout(int width, int height) {
    this.width = width;
    this.height = height;
  }

  /**
   * Computes the area of this layout (width * height).
   *
   * @return the area
   */
  public int area() {
    return this.width * this.height;
  }

  /**
   * Compute 2D-embedding for the given graph collection. The resulting position of each vertex from the input graph
   * collection is annotated as properties with the keys 'X' and 'Y'.
   *
   * @param collection the input graph collection
   * @return the input graph collection with positioned vertices
   * @throws Exception if something goes wrong
   */
  public abstract GraphCollection execute(GraphCollection collection) throws Exception;
}
