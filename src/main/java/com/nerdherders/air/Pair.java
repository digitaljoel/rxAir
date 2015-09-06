package com.nerdherders.air;

public class Pair {

  public final int x;
  public final int y;
  public Pair( int x, int y ) {
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString() {
    return "Pair [x=" + x + ", y=" + y + "]";
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + x;
    result = prime * result + y;
    return result;
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Pair other = (Pair) obj;
    if (x != other.x)
      return false;
    if (y != other.y)
      return false;
    return true;
  }
}
