package robotGame;

import java.util.*;

/**This is the implementation of OuterWorld.
 * Reading this code can help you to understand the way actions are
 * propagated onto robots, but you will only rely on the behavior of
 * the interface OuterWorld itself.*/
class RealOuterWorld implements OuterWorld{
  private final RobotBody robot;
  private final RobotBody other;
  private final RelativePoint relative;
  private final Map<String,Integer> map;
  private final List<RelativePoint> barrels;
  private final Set<Point> availableFuel;
  private boolean used= false;
  
  public RealOuterWorld(Set<Point> availableFuel, RobotBody robot, RobotBody other,Map<String,Integer> map){
    this.robot= robot;
    this.other= other;
    this.relative= other.currentPos.toRelative(robot.currentPos,robot.currentDir);
    this.map= map;
    this.availableFuel= availableFuel;
    this.barrels= preComputeBarrels();
    }
  
  @Override public boolean used(){ return used; }
  private void useIt(){
    if(used){ throw new Error("Attempint to use OuterWorld twice"); }
    used= true;
  }
  @Override public Integer readVar(String name){ return map.get(name); }
  @Override public void setVar(String name, int value){ map.put(name,value); }
  @Override public void doMove(){
    useIt();
    robot.targetPos = robot.currentPos.move(robot.currentDir);
  }
  @Override public void doTurnL(){
    useIt();
    robot.targetDir = robot.currentDir.turnLeft();
  }
  @Override public void doTurnR(){
    useIt();
    robot.targetDir = robot.currentDir.turnRight();
  }
  @Override public void doTurnAround(){
    useIt();
    robot.targetDir = robot.currentDir.turnRight().turnRight();
  }
  @Override public void doTakeFuel(){
    useIt();
    var pointHasFuel= availableFuel.remove(robot.currentPos);
      if (pointHasFuel){ robot.targetFuel= RobotBody.maxFuel; }
      boolean canTakeFuel= !other.currentShield 
        && robot.currentPos.move(robot.currentDir).equals(other.currentPos);
      if (!canTakeFuel){ return; }
      int tookFuel= other.extractableFuel();
      other.stolenFuel=tookFuel;
      robot.targetFuel= Math.min(RobotBody.maxFuel, robot.currentFuel + tookFuel);
    }
  @Override public void doWait(){ useIt();  }
  @Override public void setShield(boolean flag){ robot.targetShield = flag; }    
  @Override public int readFuelLeft(){ return robot.currentFuel; }
  @Override public int readOppLR(){ return relative.x(); }
  @Override public int readOppFB(){ return relative.y(); }
  @Override public int readNumBarrels(){ return barrels.size(); }  
  @Override public int readBarrelLR(int n){
    return (n >= barrels.size()) ? Integer.MAX_VALUE : barrels.get(n).x();
  }
  @Override public int readBarrelFB(int n) {
    return (n >= barrels.size()) ? Integer.MAX_VALUE : barrels.get(n).y();
  }
  @Override public int readWallDist() {
    return switch (robot.currentDir) {
      case North -> robot.currentPos.y();
      case South -> (Point.coordSize-1) - robot.currentPos.y();
      case West  -> robot.currentPos.x();
      case East  -> (Point.coordSize-1) - robot.currentPos.x();
    };
  }
  private List<RelativePoint> preComputeBarrels(){    
    //Best code for barrels; but uses streams, we will teach about it in SWEN221
    return availableFuel.stream()
      .map(p->p.toRelative(robot.currentPos, robot.currentDir))
      .sorted(Comparator.comparingInt(p -> Math.abs(p.x()) + Math.abs(p.y())))
      .toList();
    }
  //Worst code, equivalent to the above but using only features you know
  /*List<RelativePoint> barrels = new ArrayList<>();
  for(Point fuelP: availableFuel){
    barrels.add(fuelP.toRelative(robot.currentPos, robot.currentDir));
    }
  Collections.sort(barrels, (p1, p2)->{
    int d1 = Math.abs(p1.x()) + Math.abs(p1.y());
    int d2 = Math.abs(p2.x()) + Math.abs(p2.y());
    return d1 - d2;
  });
  return barrels;
  }*/
}