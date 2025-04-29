package tests;
import parser.Parser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
class InterpreterTests{
  
  void valid(List<String> expected, List<Integer> inputs, String text){
    long rounds= expected.stream().filter(s->s.startsWith("do")).count();
    var prog= new Parser(text).parse();    
    var m= new MockOuterWorld(inputs);
    if(rounds == 0){ prog = prog.execute(m); }
    while(rounds-->0){
      prog = prog.execute(m);
      m.resetUsed(); 
    }    
    assertEquals(expected,m.log);
  }
  
  void validWithAss(List<String> expected, List<Integer> inputs, String text){
    long rounds= expected.stream().filter(s->s.startsWith("do")).count();
    var prog= new Parser(text).parse();    
    var m= new MockOuterWorld(inputs);
    var mAss= new MockOuterWorld(List.of());
    var progAss= new Parser("loop{ $a = 100; $b = 100; $abcd = 100; $c = 100; wait; }").parse();
    if(rounds == 0){ prog = prog.execute(m); }
    while(rounds-- > 0){
      prog = prog.execute(m);
      progAss = progAss.execute(mAss);
      m.resetUsed();
      mAss.resetUsed();
    }    
    assertEquals(expected,m.log);
  }
  
  //----------Part0
  @Test void p0_moveAction(){ valid(List.of("doMove"),List.of(),"move;"); }
  @Test void p0_turnRAction(){ valid(List.of("doTurnR"),List.of(),"turnR;"); }
  @Test void p0_waitAction(){ valid(List.of("doWait"),List.of(),"wait;"); }
  @Test void p0_waitLoop(){ valid(List.of(
      "doWait", "doMove", "doTurnL", "doMove", "doTurnL", "doMove", "doTurnL", "doMove", "doTurnL", "doMove", "doTurnL", "doMove", "doTurnL", "doMove", "doTurnL", "doMove", "doTurnL", "doMove", "doTurnL", "doMove"),
      List.of(),"wait; loop { move; turnL;}"); }
  @Test void p0_waitLoopLoop(){ valid(List.of(
      "doWait", "doTurnR", "doMove","doMove","doMove","doMove","doMove","doMove","doMove","doMove","doMove","doMove","doMove","doMove","doMove","doMove","doMove","doMove","doMove","doMove","doMove","doMove","doMove","doMove","doMove"),
      List.of(),"wait; loop { turnR; loop { move; } turnL;}"); }
  //----------Part1
  @Test void p1_shieldOnAction(){ valid(List.of("setShield: true"),List.of(),"shieldOn;"); }
  @Test void p1_waitIfOppLr4(){ valid(List.of(
      "doWait", "readOppLR|output= 0", "doTurnL", "doMove"),List.of(0),"wait; if (lt(oppLR, 4)) { turnL; } move;"); }
  @Test void p1_waitIfOppLr2_1(){ valid(List.of(
      "doWait", "readOppLR|output= 1", "doTurnL","doMove"),List.of(1),"wait; if (lt(oppLR, 2)) { turnL; } move;"); }
  @Test void p1_waitIfEq_1(){ valid(List.of(
      "doWait", "readOppLR|output= 1", "doMove"),List.of(1),"wait; if (eq(oppLR, 4)) { turnL; } move;"); }
  @Test void p1_waitIfEq_3(){ valid(List.of(
      "doWait", "readOppLR|output= 3", "doMove"),List.of(3),"wait; if (eq(oppLR, 4)) { turnL; } move;"); }
  @Test void p1_turnLIfBarrelsLR_3(){ valid(List.of(
      "doTurnL", "readBarrelLR: 0|output= 3", "doMove"),List.of(3),"turnL; if (eq(barrelLR, 4)) { turnL; } move;"); }
  @Test void p1_loopIfBarrelsFB_456(){ valid(List.of(
      "doTurnR", "readBarrelFB: 0|output= 4", "doTurnL", "doMove","doTurnR", "readBarrelFB: 0|output= 5", "doMove", "doTurnR", "readBarrelFB: 0|output= 6", "doMove", "doTurnR"),
      List.of(4,5,6),"loop { turnR; if (eq(barrelFB, 4)) { turnL; } move; }"); }
  @Test void p1_loopIfBarrelsFB_234(){ valid(List.of(
      "doTurnR", "readBarrelFB: 0|output= 2", "doMove","doTurnR", "readBarrelFB: 0|output= 3", "doMove", "doTurnR", "readBarrelFB: 0|output= 4", "doTurnL", "doMove", "doTurnR"),
      List.of(2,3,4),"loop { turnR; if (eq(barrelFB, 4)) { turnL; } move; }"); }
  @Test void p1_wallDistanceLT(){ valid(List.of(
      "doWait", "readWallDist|output= 0", "doTurnL", "readWallDist|output= 1", "doTurnL", "readWallDist|output= 2", "doTurnL", "readWallDist|output= 3", "doMove"),
      List.of(0,1,2,3),"wait; while (lt(wallDist, 3)) { turnL; } move;"); }
  @Test void p1_wallDistanceGT(){ valid(List.of(
      "doTurnL", "readWallDist|output= 6", "doWait", "readWallDist|output= 5", "doWait", "readWallDist|output= 4", "doWait", "readWallDist|output= 3", "doMove"),
      List.of(6,5,4,3),"turnL; while (gt(wallDist, 3)) { wait; } move;"); }
  //----------Part2
  @Test void p2_moveIfElse(){ valid(List.of(
      "doMove", "doTurnR", "doMove"),List.of(),"move; if (eq(4, 3)) { turnL; } else {turnR;} move;"); }
  @Test void p2_move_3(){ valid(List.of(
      "doMove", "doMove", "doMove"),
      List.of(),"move(3);"); }
  @Test void p2_move_0(){ valid(List.of(
      "doMove"),
      List.of(),"move(0);"); }
  @Test void p2_move_m10(){ valid(List.of(
      "doMove"),
      List.of(),"move(-10);"); }
  @Test void p2_move_fuelLeftAdd2(){ valid(List.of(
      "readFuelLeft|output= 5", "doMove", "doMove", "doMove", "doMove", "doMove", "doMove", "doMove"),
      List.of(5),"move(add(fuelLeft,2));"); }
  @Test void p2_ifWaitMove1(){ valid(List.of("doWait"),
      List.of(),"if (lt(add(3,4), sub(10,2))) { wait; } else {move;}"); }  
  @Test void p2_ifWaitMove2(){ valid(List.of("doMove"),
      List.of(),"if (lt(add(3,4), sub(10,5))) { wait; } else {move;}"); }
  @Test void p2_ifNot1(){ valid(List.of("doTurnL"),
      List.of(),"if  (not(lt(4,3))) { turnL; } else {turnR;}"); }
  @Test void p2_ifNot2(){ valid(List.of("doTurnR"),
      List.of(),"if  (not(lt(3,4))) { turnL; } else {turnR;}"); }
  @Test void p2_ifEq1(){ valid(List.of("readOppLR|output= 1", "doTurnR"),
      List.of(1),"if (eq(oppLR,2)) { turnL; } else {turnR;}"); }
  @Test void p2_ifEq2(){ valid(List.of("readOppLR|output= 2", "doTurnL"),
      List.of(2),"if (eq(oppLR,2)) { turnL; } else {turnR;}"); }
  //----------Part3
  @Test void p3_elif1(){ valid(List.of("doMove"),
      List.of(),"if (lt(4,3)) {wait;} elif(gt(10,2)) {move;} elif(eq(4,3)) { turnL; } else {turnR;}"); }
  @Test void p3_elif2(){ valid(List.of("doTurnL"),
      List.of(),"if (lt(4,3)) {wait;} elif(gt(2,10)) {move;} elif(eq(4,4)) { turnL; } else {turnR;}"); }
  @Test void p3_waitArg(){ valid(List.of("readBarrelLR: 0|output= 4", "doWait", "doWait", "doWait", "doWait"),
      List.of(4),"wait(barrelLR);"); }
  @Test void p3_waitArgArg(){ valid(List.of("readBarrelLR: 3|output= 7", "doWait", "doWait", "doWait", "doWait", "doWait", "doWait", "doWait"),
      List.of(7),"wait(barrelLR(3));"); }
  @Test void p3_waitArgFB(){ valid(List.of("readBarrelFB: 0|output= 4", "doWait", "doWait", "doWait", "doWait"),
      List.of(4),"wait(barrelFB);"); }
  @Test void p3_assMove(){ validWithAss(List.of("doMove", "doMove", "doMove", "doTurnL", "doMove", "doMove", "doMove", "doMove", "doMove", "doTurnR"),
      List.of(),"$a = 3; move($a);turnL;$a = 5; move($a);turnR;"); }
  @Test void p3_assAABCDMove(){ validWithAss(List.of("doMove", "doMove", "doTurnR"),
      List.of(),"$a = 3; $abcd = 2; move($abcd);turnR;"); }
  @Test void p3_assWhile(){ validWithAss(List.of("readFuelLeft|output= 5", "doMove", "readFuelLeft|output= 5", "doMove", "readFuelLeft|output= 5", "doMove", "readFuelLeft|output= 5", "doTurnL"),
      List.of(5,5,5,5),"$a = 2; while(lt($a, fuelLeft)){$a = add($a,1); move;} turnL;"); }
  @Test void p3_assABIf1(){ validWithAss(List.of("doTurnR"),
      List.of(),"$a = 3; $b = 4; if(eq($a, mul($b,3))){if (lt(0,barrelLR(mul($a, $b)))) {move($b); turnL;}} else {turnR;}"); }
  @Test void p3_assABIf2(){ validWithAss(List.of("readBarrelLR: 48|output= 2", "doMove", "doMove", "doMove", "doMove", "doTurnL"),
      List.of(2),"$a = 12; $b = 4; if(eq($a, mul($b,3))){if (lt(0,barrelLR(mul($a, $b)))) {move($b); turnL;}} else {turnR;}"); }
  @Test void p3_assTwice(){ validWithAss(List.of(
      "doMove", "doMove", "doTurnL", "doWait", "doWait", "doWait", "doTurnR", "doMove", "doMove", "doMove", "doMove", "doMove", "doTakeFuel"),
      List.of(2,3),"$a = add($a, 2); move($a);turnL;$a = add($a,1); wait($a);turnR;$a = add($a,2); move($a);takeFuel;"); }
}