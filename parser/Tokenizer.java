package parser;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.StringReader;
//Notes to self
// The Tokenizer class is responsible for breaking down the input program text
// into individual tokens that the Parser can process.
//
// This class provides methods to:
// - Check if specific tokens are next in the input
// - Consume tokens and advance the input position
// - Handle error reporting with detailed position information
// - Validate tokens against expected patterns
//
// The tokenizer uses Java's Scanner class with custom delimiters to separate
// the input into meaningful tokens while handling whitespace appropriately.

//Note: as discussed in class, this code is intended as part of the specific
//parser and not as general reusable code

public class Tokenizer {
  private final Scanner s;
  // Define a pattern to separate tokens by whitespace and special characters
  private final Pattern delimiters= Pattern.compile("\\s+|(?=[{}(),;])|(?<=[{}(),;])");
  private final String input;

  // Create a new tokenizer for the given input string
  public Tokenizer(String input){
    this.input= input;
    this.s= new Scanner(input).useDelimiter(delimiters);
  }

  /**Unobvious way to extract position using java Scanner*/
  // Calculate the current position in the input for error reporting
  public String pos(){
    int remainingSize= !s.hasNext() ? 0 : s.useDelimiter("\\z").next().length();
    String allCode= new BufferedReader(new StringReader(input)).lines().collect(Collectors.joining("\n"));
    allCode = allCode.substring(0, allCode.length() - remainingSize);
    long line = allCode.lines().count();
    int lastNewlineIndex = allCode.lastIndexOf('\n');
    int col = allCode.length();
    if(lastNewlineIndex != -1){ col -= lastNewlineIndex; }
    return "\n\nat line "+line+", position " + col;
  }

  // Convert a string to a pattern for exact matching
  private Pattern patternOf(String str){ return Pattern.compile(Pattern.quote(str)); }

  // Check if there are any tokens left
  public boolean hasNext(){ return s.hasNext(); }

  // Check if the next token matches a specific string
  public boolean hasNext(String s){ return hasNext(patternOf(s)); }

  // Check if the next token matches a specific pattern
  public boolean hasNext(Pattern p){ return s.hasNext(p); }

  // Get the next token and advance
  public String next(){ //or try-catch
    if (s.hasNext()){ return s.next(); }
    throw fail("End of tokens");
  }

  // Get the next token if it matches the expected value, otherwise error
  public String next(String expectedValue) {
    return next(patternOf(expectedValue), expectedValue);
  }

  // Get the next token if it matches the expected pattern, otherwise error
  public String next(Pattern p, String humanReadable){
    if (s.hasNext(p)){ return s.next(p); }
    String next= s.hasNext()?s.next():"END OF INPUT";
    throw fail(next,humanReadable);
  }

  // Create an error with position information
  public Error fail(String msg) {throw new ParserFailureException(msg + pos()); }

  // Create an error when an unexpected token is found
  public Error fail(String token, List<String> expected) {
    throw fail("Unexpected token ["+token+"] Expected one of "+expected);
  }

  // Create an error when an unexpected token is found
  public Error fail(String token, String expected) {
    throw fail("Unexpected token ["+token+"] Expected ["+expected+"]");
  }

  // Check if the next token is one of a list of possible values
  public boolean hasNext(List<String> tokens){
    if (!hasNext()){ return false; }
    Pattern namesPat = Pattern.compile(String.join("|", tokens));
    return hasNext(namesPat);
  }

  // Get the next token if it's one of the expected values, otherwise error
  public String next(List<String> tokens){
    var name= next();//this throws if end of tokens
    if (!tokens.contains(name)){ throw fail(name,tokens); }
    return name;
  }

  //- Below here you can add more custom methods to handle specific tokens and error messages
  // Convenience methods for common token patterns
  void or(){ next("("); }
  void cr(){ next(")"); }//add your methods here

  // Pattern for matching number tokens
  private static final Pattern numPat = Pattern.compile("-?[1-9][0-9]*|0");

  // Check if the next token is a number
  boolean hasNextNumber() { return hasNext(numPat); }

  // Get the next token as a number if it matches the pattern
  String nextNumber() { return next(numPat, "number"); }

  // Pattern for matching variable names (must start with $)
  private static final Pattern varPat = Pattern.compile("\\$[A-Za-z][A-Za-z0-9]*");

  // Check if the next token is a variable name
  boolean hasNextVar() { return hasNext(varPat); }

  // Get the next token as a variable name if it matches the pattern
  String nextVar() { return next(varPat, "variable name"); }

  //example code for variable tokens
  //private static final Pattern varPat = Pattern.compile("\\$[A-Za-z][A-Za-z0-9]*");
  //boolean matchVar(String s){ return varPat.matcher(s).matches(); }
  //boolean hasNextVar(){ return hasNext(varPat); }
  //String nextVar() { return next(varPat,"variable name"); }

  //can you do it similarly for ints?
  //private static final Pattern numPat = Pattern.compile("-?[1-9][0-9]*|0");
  //boolean hasNextInt(){ return hasNext(numPat) && s.hasNextInt(); }
  //int nextInt() {
  //...
  //return s.nextInt();
  //}

  // Create an error specifically for when no valid statement is found
  Error errNoStmt(String token){
    var options= List.of("variable name","loop","if","while","move","turnL",
            "turnR","turnAround","shieldOn","shieldOff","takeFuel","wait");
    throw fail(token,options);
  }

  // Create an error specifically for empty blocks
  Error emptyBlock(){ throw fail("Block needs a non empty list of statements"); }
}