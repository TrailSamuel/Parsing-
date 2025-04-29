package robotGame;

/**Relative point represents a position relative to another point.
 * Thus, the x and y can also be negative.*/
public record RelativePoint(int x, int y) {
  public RelativePoint{
    int range = Point.coordSize - 1;
    boolean ok = x >= -range && x <= range && y >= -range && y <= range;
    if (!ok){ throw Point.outOfRange("RelativePoint",x,y,-range,Point.coordSize); }     
    }
}