package parser;

import java.util.List;
import java.util.Objects;

import robotGame.OuterWorld;
//Notes to self
// Program represents the AST of robot program.
//
// This class is the top-level container for all statements in a robot program.
// It manages the execution of the program in the robot's world, ensuring that
// exactly one action is performed per step of execution.
//
// Program, ExamplePrograms, and Parser are the only public classes in the parser package.
// All other types are only used inside this package to implement the robot language.

/**Program represents the AST of a robot program.
 * Program, ExamplePrograms and Parser are the only public class in the package parser.
 * All other types are only used inside this package.
 */
public record Program(List<Stm> ss){
  // Ensures that the statement list is not null
  public Program{ Objects.requireNonNull(ss); }

  // Executes the program in the given world context.
  // This continues execution until either:
  // 1. An action is performed (w.used() becomes true)
  // 2. The program is completed (statements list becomes empty)
  public Program execute(OuterWorld w){
    //Note: The code of Program is fully provided. Try to understand how it works,
    //so that you can use this understanding to complete the rest.
    var self= this;
    while(!w.used() && !self.ss.isEmpty()){
      var stm= self.ss.getFirst().execute(w);
      self = new Program(Util.updateFirst(stm,self.ss));
    }
    return self;
  }

  // Returns a string representation of the program
  @Override public String toString(){ return "Program"+ss; }
}