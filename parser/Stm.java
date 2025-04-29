package parser;

import java.util.List;
import java.util.Objects;

import robotGame.OuterWorld;

// The Stm interface represents statements in the robot language.
// Statements are the fundamental building blocks of robot programs, controlling
// both the flow of execution and the robot's actions.
//
// Each statement implements an execute method that performs one step of execution
// in the robot's world context. The execute method returns either null (if execution
// is complete) or another statement to be executed in the next step.
interface Stm{ Stm execute(OuterWorld w); }

//statements (other than actions)

// Interface marking statements that are robot actions (commands).
// Actions directly interact with the world by calling methods on OuterWorld.
interface Act extends Stm{}

// Loop statement that repeatedly executes a block of code.
// This is similar to while(true) in conventional languages.
record Loop(Block b) implements Stm{
  public Stm execute(OuterWorld w){
    return b.addLast(this);//This line is not obvious.
    //The hand out describes in the details why this work.
    //You will have to learn from this to do something similar in 'While'
  }
  @Override public String toString(){return "Loop"+b; }
}

// Block statement that contains a sequence of statements to be executed in order.
// Blocks are used in loops, if statements, and as the body of a program.
record Block(List<Stm> ss) implements Stm{
  Block{
    if(ss.isEmpty()){ throw new Error(); }
    for(Stm s:ss){ Objects.requireNonNull(s); }
  }
  public Block execute(OuterWorld w){
    var first= ss.getFirst().execute(w);
    if (first == null && ss.size() == 1){ return null; }
    if (first == null){
      return new Block(Util.removeFirst(ss));
    }
    return new Block(Util.updateFirst(first,ss));
  }

  // Creates a new block by concatenating this block with another list of statements.
  Block concat(List<Stm> other){ return new Block(Util.concat(ss,other)); }

  // Creates a new block by adding a statement to the end of this block.
  Block addLast(Stm s){ return new Block(Util.appendLast(ss,s)); }

  @Override public String toString(){ return ss.toString(); }
}


// Note: this 'If' can encode elif too.
// If statement for conditional execution.
// Evaluates a condition and executes one of two blocks depending on the result.
record If(Cond cond, Block then, Block otherwise) implements Stm{
  If {
    Objects.requireNonNull(cond);
    Objects.requireNonNull(then);
  }
  public Stm execute(OuterWorld w){
    var c = cond.evaluate(w);
    if (c) {
      return then;
    } else if (otherwise != null) {
      return otherwise;
    }
    return null;
  }
  @Override public String toString() {
    if (otherwise == null) {
      return "If[" + cond + ", " + then + "]";
    }
    return "If[" + cond + ", " + then + ", " + otherwise + "]";
  }
}

// While statement for conditional looping.
// Repeatedly executes a block as long as a condition is true.
record While(Cond cond, Block body) implements Stm{
  While {
    Objects.requireNonNull(cond);
    Objects.requireNonNull(body);
  }
  public Stm execute(OuterWorld w){
    if (cond.evaluate(w)) {
      return body.addLast(this); // Similar to Loop, but with a condition check
    }
    return null;
  }

  @Override public String toString() {
    return "While[" + cond + ", " + body + "]";
  }
}


//actions

// Move action that moves the robot forward.
// Can take an optional steps parameter to move multiple steps.
record Move(Exp steps) implements Act {
  public Move {
  }
  public Stm execute(OuterWorld w) {
    if (steps == null) {
      w.doMove();
      return null;
    }
    int numSteps = steps.evaluate(w);
    if (numSteps <= 0) {
      // Handle invalid steps (≤ 0) by moving once
      w.doMove();
      return null;
    }
    w.doMove();
    return numSteps > 1 ? new Move(new Num(numSteps - 1)) : null;
  }
  @Override public String toString() { return steps == null ? "Move" : "Move[" + steps + "]"; }
}

// TurnR action that turns the robot right (90 degrees).
record TurnR() implements Act{
  public Stm execute(OuterWorld w){
    w.doTurnR();
    return null;
  }
  @Override public String toString(){ return "TurnR"; }//Provided
}

// TurnL action that turns the robot left (90 degrees).
record TurnL() implements Act{
  public Stm execute(OuterWorld w){
    w.doTurnL();
    return null;
  }
  @Override public String toString(){ return "TurnL"; }//Provided
}

// TurnAround action that turns the robot around (180 degrees).
record TurnAround() implements Act{
  public Stm execute(OuterWorld w){
    w.doTurnAround();
    return null;
  }
  @Override public String toString(){ return "TurnAround"; }//Provided
}

// TakeFuel action that collects fuel from a barrel or steals from opponent.
record TakeFuel() implements Act{
  public Stm execute(OuterWorld w){
    w.doTakeFuel();
    return null;
  }
  @Override public String toString(){ return "TakeFuel"; }//Provided
}

// Wait action that makes the robot wait in place.
// Can take an optional time parameter to wait multiple steps.
record Wait(Exp time) implements Act {
  public Wait {
  }
  public Stm execute(OuterWorld w) {
    if (time == null) {
      w.doWait();
      return null;
    }
    int waitTime = time.evaluate(w);
    if (waitTime <= 0) {
      // Handle invalid time (≤ 0) by waiting once
      w.doWait();
      return null;
    }
    w.doWait();
    return waitTime > 1 ? new Wait(new Num(waitTime - 1)) : null;
  }
  @Override public String toString() { return time == null ? "Wait" : "Wait[" + time + "]"; }
}

// ShieldOn action that activates the robot's shield.
// While shielded, the robot uses more fuel but can't have fuel stolen.
record ShieldOn() implements Act {
  public Stm execute(OuterWorld w) {
    w.setShield(true);
    return null;
  }
  @Override public String toString() { return "ShieldOn"; }
}

// ShieldOff action that deactivates the robot's shield.
record ShieldOff() implements Act {
  public Stm execute(OuterWorld w) {
    w.setShield(false);
    return null;
  }
  @Override public String toString() { return "ShieldOff"; }
}

// Assignment statement that sets a variable to a value.
// Variables are stored in the OuterWorld and persist across execution steps.
record Ass(String varName, Exp value) implements Stm {
  public Stm execute(OuterWorld w) {
    w.setVar(varName, value.evaluate(w));
    return null;
  }
  @Override public String toString() { return "Ass[$" + varName + "= " + value + "]"; }
}