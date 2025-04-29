package parser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
//Notes to self
// The Parser class is responsible for converting the textual representation
// of a robot program into an AST.
//
// This class implements a recursive descent parser for the robot language
// grammar, using the Tokenizer to break the input into tokens, and then
// constructing AST nodes (Programs, Statements, Expressions, Conditions)
// that represent the program structure.
public class Parser {
  private final Tokenizer t;

  // Creates a parser for the given text
  public Parser(String text){ t= new Tokenizer(text); }

  // Creates a parser that reads from the given file path
  public Parser(Path path){ t= new Tokenizer(readFromPath(path)); }

  // Utility method to read text from a file path
  static String readFromPath(Path path){//This code show how to read a file
    try {return Files.readString(path); }//in modern Java
    catch (IOException ieo){ throw new UncheckedIOException(ieo); }
  }

  // The main parsing method that parses a complete program
  // A program consists of a sequence of statements
  public Program parse(){
    ArrayList<Stm> ss= new ArrayList<>();
    while (t.hasNext()){ ss.add(parseSingleStmt()); }
    return new Program(Collections.unmodifiableList(ss));
  }

  // Parses a single statement, which could be an action, block, loop, if, while, or assignment
  Stm parseSingleStmt() {
    if (t.hasNext("move")) return parseMove();
    if (t.hasNext("turnL")) return parseTurnL();
    if (t.hasNext("turnR")) return parseTurnR();
    if (t.hasNext("turnAround")) return parseTurnAround();
    if (t.hasNext("takeFuel")) return parseTakeFuel();
    if (t.hasNext("wait")) return parseWait();
    if (t.hasNext("loop")) return parseLoop();
    if (t.hasNext("if")) return parseIf();
    if (t.hasNext("while")) return parseWhile();
    if (t.hasNext("shieldOn")) return parseShieldOn();
    if (t.hasNext("shieldOff")) return parseShieldOff();
    if (t.hasNext("{")) return parseBlock();
    // Check for variable assignment
    if (t.hasNextVar()) {
      return parseAssignment();
    }
    throw t.errNoStmt(t.next());
  }
  //declare methods like
  //Stm parseMove() {..}
  //and so on, for many, many methods

  // Parses a variable assignment statement
  Stm parseAssignment() {
    String varNameWithDollar = t.nextVar();
    String varName = varNameWithDollar.substring(1); // Remove $ prefix
    t.next("=");
    Exp value = parseExpression();
    t.next(";");
    return new Ass(varName, value);
  }

  // Parses a variable reference expression
  Exp parseVar() {
    String varNameWithDollar = t.nextVar();
    String varName = varNameWithDollar.substring(1); // Remove $ prefix
    return new Var(varName);
  }

  // Parses an if statement with elif clauses
  // This handles the complex case where an if is followed by elif clauses
  Stm parseIfWithElif(Cond firstCond, Block firstBlock) {
    t.next("elif");
    t.next("(");
    Cond elifCond = parseCondition();
    t.next(")");
    Block elifBlock = parseBlock();
    Block otherwiseBlock = null;
    if (t.hasNext("elif")) {
      otherwiseBlock = new Block(List.of(parseIfWithElif(elifCond, elifBlock)));
    } else if (t.hasNext("else")) {
      t.next("else");
      Block elseBlock = parseBlock();
      otherwiseBlock = new Block(List.of(new If(elifCond, elifBlock, elseBlock)));
    } else {
      otherwiseBlock = new Block(List.of(new If(elifCond, elifBlock, null)));
    }
    return new If(firstCond, firstBlock, otherwiseBlock);
  }

  // Parses a move action, which may have an optional argument
  Stm parseMove() {
    t.next("move");
    Exp steps = null;
    if (t.hasNext("(")) {
      t.next("(");
      steps = parseExpression();
      t.next(")");
    }
    t.next(";");
    return new Move(steps);
  }

  // Parses a turnL action
  Stm parseTurnL() {
    t.next("turnL");
    t.next(";");
    return new TurnL();
  }

  // Parses a turnR action
  Stm parseTurnR() {
    t.next("turnR");
    t.next(";");
    return new TurnR();
  }

  // Parses a turnAround action
  Stm parseTurnAround() {
    t.next("turnAround");
    t.next(";");
    return new TurnAround();
  }

  // Parses a takeFuel action
  Stm parseTakeFuel() {
    t.next("takeFuel");
    t.next(";");
    return new TakeFuel();
  }

  // Parses a wait action, which may have an optional argument
  Stm parseWait() {
    t.next("wait");
    Exp time = null;
    if (t.hasNext("(")) {
      t.next("(");
      time = parseExpression();
      t.next(")");
    }
    t.next(";");
    return new Wait(time);
  }

  // Parses a loop statement
  Stm parseLoop() {
    t.next("loop");
    Block block = parseBlock();
    return new Loop(block);
  }

  // Parses a block of statements enclosed in curly braces
  Block parseBlock() {
    t.next("{");
    List<Stm> statements = new ArrayList<>();
    if (!t.hasNext()) {
      throw t.fail("Unexpected end of input. Expected statement or '}'");
    }
    if (t.hasNext("}")) {
      throw t.emptyBlock();
    }
    do {
      statements.add(parseSingleStmt());
    } while (t.hasNext() && !t.hasNext("}"));
    t.next("}");
    return new Block(Collections.unmodifiableList(statements));
  }

  // Parses a shieldOn action
  Stm parseShieldOn() {
    t.next("shieldOn");
    t.next(";");
    return new ShieldOn();
  }

  // Parses a shieldOff action
  Stm parseShieldOff() {
    t.next("shieldOff");
    t.next(";");
    return new ShieldOff();
  }

  // Parses an if statement, which has a condition and then block,
  // and may have an elif sequence or else block
  Stm parseIf() {
    t.next("if");
    t.next("(");
    Cond cond = parseCondition();
    t.next(")");
    Block thenBlock = parseBlock();

    // Check for elif or else
    if (t.hasNext("elif")) {
      return parseIfWithElif(cond, thenBlock);
    } else if (t.hasNext("else")) {
      t.next("else");
      Block elseBlock = parseBlock();
      return new If(cond, thenBlock, elseBlock);
    }

    return new If(cond, thenBlock, null);
  }

  // Parses a while statement
  Stm parseWhile() {
    t.next("while");
    t.next("(");
    Cond cond = parseCondition();
    t.next(")");
    Block body = parseBlock();

    return new While(cond, body);
  }

  // Parses a condition, which could be a comparison or logical operation
  Cond parseCondition() {
    if (t.hasNext("lt")) return parseLt();
    if (t.hasNext("gt")) return parseGt();
    if (t.hasNext("eq")) return parseEq();
    if (t.hasNext("and")) return parseAnd();
    if (t.hasNext("or")) return parseOr();
    if (t.hasNext("not")) return parseNot();
    throw t.fail("Expected condition (lt, gt, eq, and, or, not)");
  }

  // Parses a logical AND condition
  Cond parseAnd() {
    t.next("and");
    t.next("(");
    Cond left = parseCondition();
    t.next(",");
    Cond right = parseCondition();
    t.next(")");
    return new And(left, right);
  }

  // Parses a logical OR condition
  Cond parseOr() {
    t.next("or");
    t.next("(");
    Cond left = parseCondition();
    t.next(",");
    Cond right = parseCondition();
    t.next(")");
    return new Or(left, right);
  }

  // Parses a logical NOT condition
  Cond parseNot() {
    t.next("not");
    t.next("(");
    Cond cond = parseCondition();
    t.next(")");
    return new Not(cond);
  }

  // Parses a less-than comparison condition
  Cond parseLt() {
    t.next("lt");
    t.next("(");
    Exp left = parseExpression();
    t.next(",");
    Exp right = parseExpression();
    t.next(")");
    return new Lt(left, right);
  }

  // Parses a greater-than comparison condition
  Cond parseGt() {
    t.next("gt");
    t.next("(");
    Exp left = parseExpression();
    t.next(",");
    Exp right = parseExpression();
    t.next(")");
    return new Gt(left, right);
  }

  // Parses an equality comparison condition
  Cond parseEq() {
    t.next("eq");
    t.next("(");
    Exp left = parseExpression();
    t.next(",");
    Exp right = parseExpression();
    t.next(")");
    return new Eq(left, right);
  }

  // Parses an expression, which could be a number, sensor, variable, or arithmetic operation
  Exp parseExpression() {
    if (t.hasNext("fuelLeft") || t.hasNext("oppLR") || t.hasNext("oppFB") || t.hasNext("numBarrels") || t.hasNext("barrelLR") || t.hasNext("barrelFB") || t.hasNext("wallDist")) {
      return parseSens();
    }
    if (t.hasNext("add")) return parseAdd();
    if (t.hasNext("sub")) return parseSub();
    if (t.hasNext("mul")) return parseMul();
    if (t.hasNext("div")) return parseDiv();
    if (t.hasNextVar()) { return parseVar(); }
    if (t.hasNextNumber()) { return parseNumber(); }
    throw t.fail("Expected expression");
  }
  Sens parseSens() {
    if (t.hasNext("fuelLeft")) return parseFuelLeft();
    if (t.hasNext("oppLR")) return parseOppLR();
    if (t.hasNext("oppFB")) return parseOppFB();
    if (t.hasNext("numBarrels")) return parseNumBarrels();
    if (t.hasNext("barrelLR")) return parseBarrelLR();
    if (t.hasNext("barrelFB")) return parseBarrelFB();
    if (t.hasNext("wallDist")) return parseWallDist();


    throw t.fail("Expected sensor expression");
  }


  // Parses an addition operation
  Exp parseAdd() {
    t.next("add");
    t.next("(");
    Exp left = parseExpression();
    t.next(",");
    Exp right = parseExpression();
    t.next(")");
    return new Add(left, right);
  }

  // Parses a subtraction operation
  Exp parseSub() {
    t.next("sub");
    t.next("(");
    Exp left = parseExpression();
    t.next(",");
    Exp right = parseExpression();
    t.next(")");
    return new Sub(left, right);
  }

  // Parses a multiplication operation
  Exp parseMul() {
    t.next("mul");
    t.next("(");
    Exp left = parseExpression();
    t.next(",");
    Exp right = parseExpression();
    t.next(")");
    return new Mul(left, right);
  }

  // Parses a division operation
  Exp parseDiv() {
    t.next("div");
    t.next("(");
    Exp left = parseExpression();
    t.next(",");
    Exp right = parseExpression();
    t.next(")");
    return new Div(left, right);
  }

  // Parses a fuelLeft sensor expression
  Sens parseFuelLeft() {
    t.next("fuelLeft");
    return new FuelLeft();
  }

  // Parses an oppLR sensor expression
  Sens parseOppLR() {
    t.next("oppLR");
    return new OppLR();
  }

  // Parses an oppFB sensor expression
  // Parses an oppFB sensor expression
  Sens parseOppFB() {
    t.next("oppFB");
    return new OppFB();
  }

  // Parses a numBarrels sensor expression
  Sens parseNumBarrels() {
    t.next("numBarrels");
    return new NumBarrels();
  }

  // Parses a barrelLR sensor expression with an optional index
  Sens parseBarrelLR() {
    t.next("barrelLR");
    Exp index = null;
    if (t.hasNext("(")) {
      t.next("(");
      index = parseExpression();
      t.next(")");
    }
    return new BarrelLR(index);
  }

  // Parses a barrelFB sensor expression with an optional index
  Sens parseBarrelFB() {
    t.next("barrelFB");
    Exp index = null;
    if (t.hasNext("(")) {
      t.next("(");
      index = parseExpression();
      t.next(")");
    }
    return new BarrelFB(index);
  }

  // Parses a wallDist sensor expression
  Sens parseWallDist() {
    t.next("wallDist");
    return new WallDist();
  }

  // Parses a number literal
  Exp parseNumber() {
    String numStr = t.nextNumber();
    try {
      int value = Integer.parseInt(numStr);
      return new Num(value);
    } catch (NumberFormatException e) {
      throw t.fail("Invalid number format: " + numStr);
    }
  }
}