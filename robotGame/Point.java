package robotGame;
/** This represents points on the game grid.
 * Note the invariant checking x and y to be in the valid range */
public record Point(int x, int y) {
  public static final int coordSize= 12;
  
  public Point{
    boolean ok= x >= 0  && y >= 0 && x < coordSize && y < coordSize;
    if (!ok){ throw outOfRange("Point",x,y,0,coordSize); }
  }
  
  private int inRange(int c){ return Math.clamp(c, 0, coordSize-1); }
  /**Moves the Point in the required direction, but does not go out of the grid.*/
  Point move(Direction dir){
    return switch (dir) {
      case North -> new Point(x, inRange(y - 1));
      case West  -> new Point(inRange(x - 1), y);
      case South -> new Point(x, inRange(y + 1));
      case East  -> new Point(inRange(x + 1), y);
    };
  }
  
  /**Computes the RelativePoint, including an axis rotation of dir*/
  public RelativePoint toRelative(Point p, Direction dir) {
    int px = p.x();
    int py = p.y();
    return switch (dir) {
      case North -> new RelativePoint(x - px, py - y);
      case South -> new RelativePoint(px - x, y - py);
      case West ->  new RelativePoint(py - y, px - x);
      case East ->  new RelativePoint(y - py, x - px);
    };
  } 

  public static Error outOfRange(String type, int x, int y, int min, int max) {
    String msg= type + "(x="+x+", y="+y+") outside of range " +
      min+","+min+" (inclusive) -- "+max+","+max+" (exclusive)";
    throw new IllegalArgumentException(msg);
  }
}