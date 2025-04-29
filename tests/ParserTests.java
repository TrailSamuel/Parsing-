package tests;
import parser.Parser;
import parser.ParserFailureException;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ParserTests{

  void valid(String expected,String text){
    String res= new Parser(text).parse().toString();
    assertEquals("Program["+expected+"]",res);
  }
  void invalid(String text){
    try{new Parser(text).parse(); fail("Exeception expected");}
    catch(ParserFailureException pfe){/*expected*/}
  }
  //----------Part0
  @Test void p0_moveAction(){ valid("Move","move;"); }
  @Test void p0_turnLAction(){ valid("TurnL","turnL;"); }
  @Test void p0_takeFuelAction(){ valid("TakeFuel", "takeFuel;"); }
  @Test void p0_waitAction(){ valid("Wait", "wait;"); }
  @Test void p0_sequenceOfActions(){ valid("Move, TurnL, TurnR, Move, TakeFuel", "move; turnL; turnR; move; takeFuel; "); }
  @Test void p0_loopWithABlockWithFourActions(){ valid("Loop[Move, Wait, TurnL, TurnR]", "loop{move; wait; turnL; turnR;}"); }
  @Test void p0_nestedLoop(){ valid("Loop[Move, Loop[TurnL]]", "loop{move; loop{turnL;}}"); }
  @Test void p0_allStage0Elements(){ valid("Move, TurnL, TurnR, Move, TakeFuel, Loop[Move, TurnR, Wait]", "move; turnL; turnR; move; takeFuel; loop{move; turnR; wait;}"); }
  @Test void p0_missingSemicolon(){ invalid("move; turnR move;"); }
  @Test void p0_invalidActionTurnRight(){ invalid("move; turnL; turnRight; move;"); }
  @Test void p0_blockInALoopWithNoStatements(){ invalid("loop{}"); }
  @Test void p0_blockWithNoCloseCurly(){ invalid("loop{move; turnL;"); }
  @Test void p0_nestedLoopWithOneMissingCloseCurlyOnBlock(){ invalid("loop{move; loop{turnL;}"); }
  //----------Part1
  @Test void p1_whileAndConditionUsingEqAndFuelLeft(){ valid("While[Eq[FuelLeft, Num[2]], [Wait]]", "while(eq(fuelLeft, 2)) { wait; }"); }
  @Test void p1_ifWithConditionUsingLtAndOppLR(){ valid("If[Lt[OppLR, Num[2]], [Wait]]", "if(lt(oppLR, 2)) { wait; }"); }
  @Test void p1_ifWithConditionUsingEqAndNumbBarrels(){ valid("If[Eq[NumBarrels, Num[1]], [TurnAround]]", "if(eq(numBarrels, 1)) {turnAround;}"); }
  @Test void p1_whileWithConditionUsingLtAndBarrelLR(){ valid("While[Lt[BarrelLR, Num[1]], [TurnR]]", "while(lt(barrelLR, 1)) {turnR;}"); }  
  @Test void p1_whileWithConditionUsingEqAndWallDis(){ valid("While[Eq[WallDist, Num[0]], [TurnL, Wait]]", "while(eq(wallDist, 0)) {turnL; wait;}"); }
  @Test void p1_ifWithNestedIf(){ valid("If[Gt[WallDist, Num[0]], [If[Eq[FuelLeft, Num[4]], [TurnL]]]]", "if(gt(wallDist, 0)) {if(eq(fuelLeft, 4)) {turnL;}}"); }
  @Test void p1_sequence4StatementsWithIfWhile(){ valid("Move, While[Gt[WallDist, Num[0]], [TurnL]], If[Eq[FuelLeft, Num[4]], [TurnL]], Wait", "move; while(gt(wallDist, 0)) {turnL;} if(eq(fuelLeft, 4)) {turnL;} wait;"); }
  @Test void p1_whileCanTHaveAnEmptyCondition(){ invalid("while(){move;}"); }
  @Test void p1_conditionInIfMustHaveClosingRound(){ invalid("if(eq(fuelLeft, 1) {move;}"); }
  @Test void p1_conditionInWhileMustHaveClosingRound(){ invalid("while(eq(fuelLeft, 1) {move;}"); }
  @Test void p1_ifMustHaveABlockStatement(){ invalid("if(eq(fuelLeft, 2) move;"); }
  @Test void p1_canTHaveAnActionAsABoolean(){ invalid("if(shieldOn){shieldOff;}"); }
  //----------Part2
  @Test void p2_moveWithNumberArgument(){ valid("Move[Num[3]]", "move(3);"); }
  @Test void p2_moveWithSensorArgument(){ valid("Move[FuelLeft]", "move(fuelLeft);"); }
  @Test void p2_moveWithAddArgument(){ valid("Move[Add[FuelLeft, Num[2]]]", "move(add(fuelLeft,2));"); }
  @Test void p2_waitWithNestedExpression(){ valid("Wait[Div[Add[Num[3], Num[5]], Sub[Mul[OppLR, Num[2]], Sub[Num[5], Num[6]]]]]", "wait(div(add(3, 5), sub(mul(oppLR,2),sub(5, 6))));"); }
  @Test void p2_ltOnExpressionsIfElse(){ valid("If[Lt[Add[Num[3], Num[4]], Sub[Num[10], Num[2]]], [Wait], [Move]]", "if (lt(add(3,4), sub(10,2))) { wait; } else {move;}"); }
  @Test void p2_conditionWithOr(){ valid("If[Or[Lt[Num[3], Num[4]], Gt[Num[10], Num[2]]], [Wait], [Move]]", "if (or(lt(3,4),gt(10,2))) { wait; } else {move;}"); }
  @Test void p2_conditionWithNot(){ valid("If[Not[Lt[Num[4], Num[3]]], [Wait], [Move]]", "if (not(lt(4,3))) { wait; } else {move;}"); }
  @Test void p2_ifMoveL(){ valid("If[Eq[BarrelFB, Num[3]], [Wait], [Move]]", "if (eq(barrelFB,3)) { wait; } else {move;}"); }
  @Test void p2_turnLNotHaveArgument(){ invalid("turnL(3);"); }
  @Test void p2_moveOpenNeedArgument(){ invalid("move();"); }
  @Test void p2_elseMustHaveBody(){ invalid("if(lt(3, 4)){move;} else"); }
  @Test void p2_andHasExaclyTwoArguments(){ invalid("while (and(lt(3,4), gt(5, 3), eq(2,2))) {move;}"); }
  @Test void p2_andNoOneArgument(){ invalid("while (and(lt(3,4))) {move;}"); }
  @Test void p2_andNoZeroArguments(){ invalid("while (and()) {move;}"); }
  @Test void p2_andHasCondArguments(){ invalid("while (and(3,4)) {move;}"); }
  @Test void p2_subNoOneArgument(){ invalid("wait(sub(5));"); }
  @Test void p2_subNoZeroArguments(){ invalid("wait(sub());"); }
  @Test void p2_addHasExpArguments(){ invalid("wait(add(5, lt(3, 4)));"); }
  //----------Part3
  @Test void p3_twoElifWithElse(){ valid("If[Lt[Num[3], Num[4]], [Wait], [If[Gt[Num[10], Num[2]], [Move], [If[Eq[Num[4], Num[3]], [TurnL], [TurnR]]]]]]", "if (lt(3,4)) {wait;} elif(gt(10,2)) {move;} elif(eq(4,3)) { turnL; } else {turnR;}"); }
  @Test void p3_oneElifNoElse(){ valid("If[Lt[Num[3], Num[4]], [Wait], [If[Gt[Num[10], Num[2]], [Move]]]]", "if (lt(3,4)) {wait;} elif(gt(10,2)) {move;}"); }
  @Test void p3_barrelLRNoArgument(){ valid("Wait[BarrelLR]", "wait(barrelLR);"); }
  @Test void p3_barrelFBWithArgument(){ valid("Wait[BarrelFB[Add[Num[1], FuelLeft]]]", "wait(barrelFB(add(1,fuelLeft)));"); }
  @Test void p3_variableAssWithExp(){ valid("Ass[$a= Num[3]], Ass[$b= Add[Var[$a], Num[3]]]", "$a = 3 ; $b = add($a, 3);"); }
  @Test void p3_longVarNameAndVarUse(){ valid("Ass[$abcd= Num[3]], Move[Var[$abcd]]", "$abcd = 3; move($abcd);"); }
  @Test void p3_variableAssWhile(){ valid("Ass[$a= Num[3]], While[Lt[Var[$a], FuelLeft], [Ass[$a= Add[Var[$a], Num[1]]], Move]]", "$a = 3; while(lt($a, fuelLeft)){$a = add($a,1); move;}"); }
  @Test void p3_elifNoRound(){ invalid("if (lt(3,4)) {wait;} elif gt(10,2) {move;}"); }
  @Test void p3_elifHaveCond(){ invalid("if (lt(3,4)) {wait;} elif (10) {move;}"); }
  @Test void p3_elifHaveBlock(){ invalid("if (lt(3,4)) {wait;} elif (gt(10,2)) move;"); }
  @Test void p3_elifHaveRound(){ invalid("if (lt(3,4)) {wait;} elif {move;}"); }
  @Test void p3_elifAfterElse(){ invalid("if (lt(3,4)) {wait;} else {turnL;} elif (gt(10,2)) {move;}"); }
  @Test void p3_invalidVarNameNoDollar(){ invalid("a = 3; move(a);"); }
  @Test void p3_variableAssHaveValue(){ invalid("$a = ;"); }
  @Test void p3_invalidVarNameDigits(){ invalid("%a1 = 3; move($b2c);"); }
  @Test void p3_variableAsCondition(){ invalid("if($a){wait(3);}"); }
}