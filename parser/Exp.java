package parser;

import robotGame.OuterWorld;
//Notes to self
// The Exp interface represents expressions in the robot language.
// Expressions evaluate to integer values and can be used in conditions,
// as arguments to commands, or in arithmetic operations.
//
// All expressions are side-effect free, meaning they only compute values
// without affecting the state of the robot or world.
interface Exp{
  int evaluate(OuterWorld w);
}
interface Sens extends Exp{}

//expressions
record Num(int inner) implements Exp{ //full provided code for one example expression
  public int evaluate(OuterWorld w){
    return inner;
  }
  @Override public String toString(){ return "Num["+inner+"]"; }
}

// Variable reference that retrieves a stored value by name.
// If the variable is not defined, it returns 0 as a default value.
record Var(String name) implements Exp {
  public int evaluate(OuterWorld w) {
    Integer value = w.readVar(name);
    return value != null ? value : 0; // Default to 0 if variable not defined
  }
  @Override public String toString() { return "Var[$" + name + "]"; }
}

// Addition operation that adds two expressions
record Add(Exp left, Exp right) implements Exp{
  public int evaluate(OuterWorld w){ return left.evaluate(w) + right.evaluate(w); }
  @Override public String toString(){ return "Add[" + left + ", " + right + "]"; }
}

// Subtraction operation that subtracts the right expression from the left
record Sub(Exp left, Exp right) implements Exp{
  public int evaluate(OuterWorld w){ return left.evaluate(w) - right.evaluate(w); }
  @Override public String toString(){ return "Sub[" + left + ", " + right + "]"; }
}

// Multiplication operation that multiplies two expressions
record Mul(Exp left, Exp right) implements Exp{
  public int evaluate(OuterWorld w){ return left.evaluate(w) * right.evaluate(w); }
  @Override public String toString(){ return "Mul[" + left + ", " + right + "]"; }
}

// Division operation that divides the left expression by the right
record Div(Exp left, Exp right) implements Exp{
  public int evaluate(OuterWorld w){ return left.evaluate(w) / right.evaluate(w); }
  @Override public String toString(){ return "Div[" + left + ", " + right + "]"; }
}

/*TODO: record FuelLeft... and many other types*/

// Sensor expression that reads the robot's remaining fuel
record FuelLeft() implements Sens {
  public int evaluate(OuterWorld w) { return w.readFuelLeft(); }
  @Override public String toString() { return "FuelLeft"; }
}

// Sensor expression that reads the opponent's left-right position relative to the robot
record OppLR() implements Sens {
  public int evaluate(OuterWorld w) { return w.readOppLR(); }
  @Override public String toString() { return "OppLR"; }
}

// Sensor expression that reads the opponent's front-back position relative to the robot
record OppFB() implements Sens {
  public int evaluate(OuterWorld w) { return w.readOppFB(); }
  @Override public String toString() { return "OppFB"; }
}

// Sensor expression that reads the number of fuel barrels in the world
record NumBarrels() implements Sens {
  public int evaluate(OuterWorld w) { return w.readNumBarrels(); }
  @Override public String toString() { return "NumBarrels"; }
}

// Sensor expression for the left-right position of a fuel barrel relative to the robot
// The index parameter selects which barrel to check, with 0 being the closest
record BarrelLR(Exp index) implements Sens {
  public BarrelLR {
  }
  public int evaluate(OuterWorld w) {
    if (index == null) {
      return w.readBarrelLR(0);
    }
    int idx = index.evaluate(w);
    return w.readBarrelLR(Math.max(0, idx));
  }
  @Override public String toString() { return index == null ? "BarrelLR" : "BarrelLR[" + index + "]"; }
}

// Sensor expression for the front-back position of a fuel barrel relative to the robot
// The index parameter selects which barrel to check, with 0 being the closest
record BarrelFB(Exp index) implements Sens {
  public BarrelFB {
  }
  public int evaluate(OuterWorld w) {
    if (index == null) {
      return w.readBarrelFB(0);
    }
    int idx = index.evaluate(w);
    return w.readBarrelFB(Math.max(0, idx));
  }
  @Override public String toString() { return index == null ? "BarrelFB" : "BarrelFB[" + index + "]"; }
}

// Sensor expression that reads the distance to the wall directly in front of the robot
record WallDist() implements Sens {
  public int evaluate(OuterWorld w) { return w.readWallDist(); }
  @Override public String toString() { return "WallDist"; }
}