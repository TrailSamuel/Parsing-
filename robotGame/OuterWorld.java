package robotGame;

/**This represents how the robot mind sees the outer world
 * It has all the sensory data and can set a desired
 * action */
public interface OuterWorld{
  /**The robot can do a single action any turn.
   * Calling multiple 'doXXX' methods causes an exception.
   * You can use used() to check if the action
   * has been already consumed */
  boolean used();  
  
  /**The robot mind stores a map from string names to int values.
   * This method reads those values*/
  Integer readVar(String name);

  /**The robot mind stores a map from string names to int values.
   * This method updates/initializes those values*/
  void setVar(String name, int value);
  
  /** Move forward one step. Sets used=true for this turn */
  void doMove();
  /**Turn 90 degrees to the left. Sets used=true for this turn */
  void doTurnL();
  /**Turn 90 degrees to the right. Sets used=true for this turn */
  void doTurnR();
  /**Turn 180 degrees. Sets used=true for this turn */
  void doTurnAround();
  /**Take fuel barrel or siphon fuel from other robot. Sets used=true for this turn */  
  void doTakeFuel();
  /**Do nothing and wait. Sets used=true for this turn */
  void doWait();
  
  /**Set the shield to true or false.
   * When the shield is on, the other robot cannot
   * steal fuel, but this robot will use up fuel faster*/
  void setShield(boolean flag);
  
  /**Gets the amount of fuel this robot has remaining*/
  int readFuelLeft();
  
  /**Gets the left-right-location of the other robot
   * relative to the current position and orientation.
   * @return 
   *   negative if to the left,
   *   positive if to the right and
   *   0 if directly in front or behind*/
  int readOppLR();
  
  /**Gets the front-back-location of the other robot relative
   * to the current position and orientation.
   * @return
   *   positive if in front,
   *   negative if behind and
   *   0 if directly to the left or right*/
  int readOppFB();
  
  /**@return The number of barrels currently in the world*/
  int readNumBarrels();
  
  /**Left-right-location of the nth fuel barrel relative
   * to the current position and orientation.
   * For parts 0-1-2 you should just pass zero as the argument
   * @return
   *   INFINTY if there are less than n barrels,
   *   negative if to the left,
   *   positive if to the right and
   *   0 if directly in front or behind*/
  int readBarrelLR(int index);
  
  /**Front-back-location of the nth fuel barrel relative
   * to the current position and orientation.
   * For parts 0-1-2 you should just pass zero as the argument
   * @return
   *   INFINITY if there are less than n barrels,
   *   positive if in front, 
   *   negative if behind and
   *   0 if directly to the left or right*/
  int readBarrelFB(int index);
  
  /**Distance to the wall directly in front of the robot
   * relative to its current orientation*/
  int readWallDist();
  }