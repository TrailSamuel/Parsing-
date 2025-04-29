package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import parser.ExamplePrograms;
import parser.Program;
import robotGame.OuterWorld;

class MockOuterWorld implements OuterWorld {
  boolean used = false;
  Map<String,Integer> map= new HashMap<>();
  final List<String> log = new ArrayList<>();
  private final Iterator<Integer> outputs;
  public MockOuterWorld(List<Integer> predefinedInts) {
      this.outputs = predefinedInts.iterator();
  }
  @Override public boolean used(){ return used; }
  private void useIt(String msg){
    if (used){ throw new Error(); }
    used = true;
    log.add(msg);
  }
  private int nextInt(String msg) {
    if (!outputs.hasNext()){ throw new Error(); }
    int nextI= outputs.next();
    log.add(msg+"|output= "+nextI);
    return nextI;
  }
  @Override public Integer readVar(String name){ return map.get(name); }
  @Override public void setVar(String name, int value){ map.put(name, value); }
  @Override public void doMove(){ useIt("doMove"); }
  @Override public void doTurnL(){ useIt("doTurnL"); }
  @Override public void doTurnR(){ useIt("doTurnR"); }
  @Override public void doTurnAround(){ useIt("doTurnAround"); }
  @Override public void doTakeFuel(){ useIt("doTakeFuel"); }
  @Override public void doWait(){ useIt("doWait"); }
  @Override public void setShield(boolean flag){ log.add("setShield: " + flag); }
  @Override public int readFuelLeft(){ return nextInt("readFuelLeft"); }
  @Override public int readOppLR(){ return nextInt("readOppLR"); }
  @Override public int readOppFB(){ return nextInt("readOppFB"); }
  @Override public int readNumBarrels(){ return nextInt("readNumBarrels"); }
  @Override public int readBarrelLR(int index){ return nextInt("readBarrelLR: " + index); }
  @Override public int readBarrelFB(int index){ return nextInt("readBarrelFB: " + index); }
  @Override public int readWallDist(){ return nextInt("readWallDist"); }
  public void resetTurn(){
    if(!used){ throw new Error(); }
    log.clear();
    used = false; 
  }
  public void resetUsed(){
    if(!used){ throw new Error(); }
    used = false; 
  }
}
class IntegrationInterpretedTests{
  Program assertNext(List<String> expected, Program p, MockOuterWorld m){
    p = p.execute(m);
    assertEquals(expected, m.log);
    m.resetTurn();
    return p;
  }
  @Test void testLeftRightMove(){
    var m= new MockOuterWorld(List.of());
    var p= ExamplePrograms.leftRightMove();
    assertEquals(3,p.ss().size());
    p = assertNext(List.of("doTurnL"),p,m);
    assertEquals(2,p.ss().size());
    p = assertNext(List.of("doTurnR"),p,m);
    assertEquals(1,p.ss().size());
    p = assertNext(List.of("doMove"),p,m);
    assertEquals(0,p.ss().size());
  }
  Program testLRM(Program p,MockOuterWorld m){
    p = assertNext(List.of("doTurnL"),p,m);
    p = assertNext(List.of("doTurnR"),p,m);
    p = assertNext(List.of("doMove"),p,m);
    return p;
  }
  @Test void testLoopLeftRightMove(){
    var m= new MockOuterWorld(List.of());
    var p= ExamplePrograms.loopLeftRightMove();
    assertEquals(1,p.ss().size());
    p = assertNext(List.of("doTurnL"),p,m);
    assertEquals(1,p.ss().size());
    p = assertNext(List.of("doTurnR"),p,m);
    p = assertNext(List.of("doMove"),p,m);
    p = testLRM(p,m);
    p = testLRM(p,m);
    p = testLRM(p,m);
  }  
  @Test void testWhileLeftRightMove(){
    var m= new MockOuterWorld(List.of());
    var p= ExamplePrograms.whileLeftRightMove();
    p= testLRM(p, m);
    p= testLRM(p, m);
    p= testLRM(p, m);
    p= testLRM(p, m);
    p= testLRM(p, m);
    p= testLRM(p, m);
    p= testLRM(p, m);
    p= testLRM(p, m);
  }
  @Test void testAimAndGoTurnRight(){
    var m= new MockOuterWorld(List.of(
        1,0,  1,0,  1,0,  1,   1
        ));
    var p= ExamplePrograms.aimAndGo();
    p = assertNext(List.of(
      "readBarrelLR: 0|output= 1", "readBarrelFB: 0|output= 0",
      "readBarrelLR: 0|output= 1", "readBarrelFB: 0|output= 0",
      "readBarrelLR: 0|output= 1", "readBarrelFB: 0|output= 0",
      "readBarrelLR: 0|output= 1", "readBarrelLR: 0|output= 1",
      "doTurnR"),p,m);
  }
  
  @Test void testAimAndGo(){
    var m= new MockOuterWorld(List.of(
      0,0,
      0,1,  0,1,
      -1,0, -1,0, -1,0, -1,
      0,-1, 0,-1, 0,-1, 0,-1
        ));
    var p= ExamplePrograms.aimAndGo();
    p = assertNext(List.of("readBarrelLR: 0|output= 0", "readBarrelFB: 0|output= 0",
                           "doTakeFuel"),p,m);
    p = assertNext(List.of("readBarrelLR: 0|output= 0", "readBarrelFB: 0|output= 1",
                           "readBarrelLR: 0|output= 0", "readBarrelFB: 0|output= 1",
                           "doMove"),p,m);//repeats reading: no local variables
    p = assertNext(List.of(
      "readBarrelLR: 0|output= -1", "readBarrelFB: 0|output= 0",
      "readBarrelLR: 0|output= -1", "readBarrelFB: 0|output= 0",
      "readBarrelLR: 0|output= -1", "readBarrelFB: 0|output= 0",
      "readBarrelLR: 0|output= -1",
      "doTurnL"),p,m);
    p = assertNext(List.of(
      "readBarrelLR: 0|output= 0", "readBarrelFB: 0|output= -1",
      "readBarrelLR: 0|output= 0", "readBarrelFB: 0|output= -1",
      "readBarrelLR: 0|output= 0", "readBarrelFB: 0|output= -1",
      "doTurnAround"),p,m);
  }
  
  @Test void testAimAndGoLocVar(){
    var m= new MockOuterWorld(List.of(
      0,0,
      0,1,
      -1,0,
      1,0,
      0,-1
      ));
    var p= ExamplePrograms.aimAndGoLocVar();
    p = assertNext(List.of("readBarrelLR: 0|output= 0","readBarrelFB: 0|output= 0",
      "doTakeFuel"),p,m);
    p = assertNext(List.of("readBarrelLR: 0|output= 0","readBarrelFB: 0|output= 1",
        "doMove"),p,m);
    p = assertNext(List.of("readBarrelLR: 0|output= -1","readBarrelFB: 0|output= 0",
        "doTurnL"),p,m);
    p = assertNext(List.of("readBarrelLR: 0|output= 1","readBarrelFB: 0|output= 0",
        "doTurnR"),p,m);
    p = assertNext(List.of("readBarrelLR: 0|output= 0","readBarrelFB: 0|output= -1",
        "doTurnAround"),p,m);
  }  
}
