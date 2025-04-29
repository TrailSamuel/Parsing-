package robotGame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.nio.file.Path;
import java.util.*;

import parser.ExamplePrograms;
import parser.Program;
import robotGame.RobotBody.Colour;

/** Simulation of the robots in their world */
public class World {
  private final Set<Point> availableFuel= new HashSet<>();
  private final Random rand = new Random(0);
  
  private final RobotBody red;
  private Program redProgram;
  private final RobotBody blue;
  
  private final Map<String,Integer> redMap= new HashMap<>();
  private Program blueProgram;
  private final Map<String,Integer> blueMap= new HashMap<>();
  
  static Program load(Path code){
    if( code == null) { return ExamplePrograms.defaultProgramCode(); }
    return new parser.Parser(code).parse();
  }
  World(Path redCode, Path blueCode){
    redProgram=  load(redCode);
    blueProgram= load(blueCode);
    var redStart=  new Point(0,0);
    var blueStart= new Point(Point.coordSize - 1, Point.coordSize - 1);
    red=  new RobotBody(Colour.Red,  redStart,Direction.South);
    blue= new RobotBody(Colour.Blue, blueStart, Direction.North);
    addFuel(); addFuel(); //add some initial fuel
  }
  /**crucially, order of turn must be not relevant. Thus we divide think and act.*/ 
  public void nextTurn(){
    //commit last operation
    red.commitAction();
    blue.commitAction();
    //think
    var redWorld= new RealOuterWorld(availableFuel,red,blue,redMap);
    var blueWorld= new RealOuterWorld(availableFuel,blue,red,blueMap);
    redProgram = redProgram.execute(redWorld);//Question: what would happen if we swap this line with the above one? 
    blueProgram = blueProgram.execute(blueWorld);//Answer: the blueWorld may see one barrel less, making behavior order dependent.
    //resolve movement conflicts
    setNextPos(red);
    setNextPos(blue);
    red.consumeFuel();
    blue.consumeFuel();
    if(rand.nextDouble() <= 0.1){ addFuel(); } //The world updates too
  }  
  /**position for next turn, can be different from targetPos field if the cell is occupied*/
  void setNextPos(RobotBody robot){
    if(!robot.isMoving()){ return; }
    RobotBody other= robot==red?blue:red;
    assert other != robot;
    assert robot.currentPos.equals(robot.nextPos);
    boolean occupied= robot.targetPos.equals(other.targetPos)
                   || robot.targetPos.equals(other.currentPos);
    if(!occupied){ robot.nextPos= robot.targetPos; }
  }  
  private void addFuel(){
    int x= rand.nextInt(Point.coordSize);
    int y= rand.nextInt(Point.coordSize);
    availableFuel.add(new Point(x, y));
  }
  //Code below could be decomposed better using SWEN221/SWEN225 techniques
  //It is kind of sad to have a Graphics2D inside 'World' since it should not have
  //rendering responsibilities.
  void drawMap(Graphics2D g, double time){
    for (Point fuel : availableFuel){ GameImage.Fuel.draw(g, fuel.x(),fuel.y()); }
    drawRobot(g, time, red);
    drawRobot(g, time, blue);
  }
  void drawRobot(Graphics2D g, double time, RobotBody r){
    double xGrid= r.xAtTime(time);
    double yGrid= r.yAtTime(time);
    double x = xGrid * WorldComponent.gridSize + WorldComponent.gridSize / 2d;
    double y = yGrid * WorldComponent.gridSize + WorldComponent.gridSize / 2d;
    double angle = r.angleAtTime(time);
    AffineTransform trans= new AffineTransform();
    trans.translate(x, y);
    trans.rotate(angle);
    r.imgAtTime(time).draw(g,trans);
    if (r.currentShield){ GameImage.ShieldImage.draw(g, xGrid, yGrid); } 
    drawFuel(g, x, y, r.fuelAtTime(time));
  }
  private void drawFuel(Graphics2D g, double x, double y, double fuelCurrent){
    fuelCurrent= Math.max(0, fuelCurrent);
    Arc2D fuelArc = new Arc2D.Double(x - 10, y - 10, 20, 20, -90, 360d * fuelCurrent / 100d, Arc2D.OPEN);
    g.setStroke(new BasicStroke(3));
    g.setColor(Color.GREEN);
    g.draw(fuelArc);
  }
  public String gameStatus(){//could be Optional<String> with SWEN221
    if (!red.isDead()  && !blue.isDead()){ return ""; }
    String msg= "Both robots";
    if (!red.isDead()){ msg = "Robot 2 (blue)"; } 
    if (!blue.isDead()){ msg = "Robot 1 (red)"; }
    return msg+" ran out of fuel!";
  }
}