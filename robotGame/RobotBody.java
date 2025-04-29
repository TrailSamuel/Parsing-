package robotGame;

/**This class is the body of the robot, the part doing the
 * actions. The interpreter should be unable to access it.*/
public class RobotBody {
  enum Colour{Red,Blue}
  final Colour color;
  Direction currentDir;
  Direction targetDir;
  Point currentPos;
  Point targetPos;
  Point nextPos;//can be different from targetPos is targePos is obstructed
  int currentFuel= RobotBody.maxFuel;
  int targetFuel=  RobotBody.maxFuel;
  int stolenFuel=  0; //needs this extra field: could take fuel from barrel while fuel is removed
  boolean currentShield;//need to be current/target. Imagine a start where both robots look to each other and rise shield and steal fuel. They should both steal
  boolean targetShield;
  boolean isDead(){ return currentFuel <= 0; }
  RobotBody(Colour c, Point p, Direction d){
    this.color= c;
    this.currentPos= p;
    this.targetPos= p;
    this.nextPos= p;
    this.currentDir= d;
    this.targetDir= d;
  }
  public static final int waitFuelCost=     3;
  public static final int moveFuelCost=     3;//+3 for wait
  public static final int turnFuelCost=     2;//+3 for wait
  public static final int shieldFuelCost=   2;//15; extra
  public static final int maxFuel=        100;
  
  int extractableFuel(){
    return Math.min(maxFuel / 4, currentFuel / 2);
  }  
  void consumeFuel(){
    int res= waitFuelCost;
    if (currentShield){ res += shieldFuelCost; }
    if (!currentPos.equals(targetPos)){ res += moveFuelCost; }
    if (!currentDir.equals(targetDir)){ res += turnFuelCost; }
    targetFuel -= res;
  }
  void commitAction(){
    currentFuel = targetFuel - stolenFuel;
    targetFuel = currentFuel;
    stolenFuel = 0;
    currentPos = nextPos;
    targetPos = nextPos;
    currentDir = targetDir;
    currentShield = targetShield;
  }

  //Code below helps for drawing the robot in the middle of movements
  public double fuelAtTime(double time){
    double res= currentFuel * (1 - time) + (targetFuel-stolenFuel) * time;
    return Math.max(0, res);
  }
  public double xAtTime(double time){
    return currentPos.x() * (1 - time) + nextPos.x() * time;
  }
  public double yAtTime(double time){
    return currentPos.y() * (1 - time) + nextPos.y() * time;
  }
  public double angleAtTime(double time){
    double startAngle = getAngle(currentDir);
    double endAngle = getAngle(targetDir);
    double delta = endAngle - startAngle;
    delta = (delta + Math.PI) % (2 * Math.PI) - Math.PI;
    return startAngle + delta * time;
  }
  private double getAngle(Direction dir){
    return switch (dir) {
      case North -> 0;
      case South -> Math.toRadians(180);
      case East  -> Math.toRadians(90);
      case West  -> Math.toRadians(270);
    };
  }
  boolean isMoving(){ return !currentPos.equals(targetPos); }
  boolean isMovingOrRotating(){ return isMoving() || !currentDir.equals(targetDir); }
  public GameImage imgAtTime(double time){
    int animationSlowDownFactor= 10;
    boolean evenAnimation= ((int) (time * animationSlowDownFactor)) % 2 == 0;
    GameImage image1= color == Colour.Blue ? GameImage.RobotBlue1 : GameImage.RobotRed1;
    GameImage image2= color == Colour.Blue ? GameImage.RobotBlue2 : GameImage.RobotRed2;
    return evenAnimation && isMovingOrRotating() ? image1 : image2;
  }
}