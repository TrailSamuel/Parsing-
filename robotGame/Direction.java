package robotGame;

public enum Direction{
  North, West, South, East;
  
  public Direction turnLeft(){
    return switch (this){
      case North -> West;                            
      case West  -> South;
      case South -> East;
      case East  -> North;
    };
  }
  
  public Direction turnRight(){
    return switch (this){  
      case North -> East;
      case West  -> North;
      case South -> West;
      case East  -> South;
    };
  }
}