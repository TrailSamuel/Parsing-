# Parsing-
# Robot Game Project

## Overview
A Java-based robot game where two robots compete for fuel on a 12x12 grid. The project features a custom programming language for controlling robot behavior, complete with a parser and interpreter system.

## Key Features
- Two robots (red and blue) competing to collect fuel and survive
- Custom programming language for robot control
- Recursive descent parser for interpreting robot programs
- Relative coordinate system for intuitive robot programming
- Real-time simulation with conflict resolution
- Visual feedback with animations in Java Swing

## Robot Programming Language
Robots are programmed with a simple language featuring:

### Actions
- Movement commands (move, turnL, turnR, turnAround)
- Fuel collection (takeFuel)
- Shield control (shieldOn/shieldOff)

### Sensors
- Position sensing (oppLR, oppFB, barrelLR, barrelFB, wallDist)
- Resource detection (fuelLeft, numBarrels)

### Control Structures
- Variables, conditionals, loops, and arithmetic operations

## Example Program
while(gt(fuelLeft, 0)) {
  if(eq(numBarrels, 0)) {
    takeFuel();
  }
  else {
    $x = barrelLR;
    $y = barrelFB;
    if(and(eq($x, 0), eq($y, 0))) {
      takeFuel();
    }
    else if(gt($y, 0)) {
      move();
    }
    else if(lt($x, 0)) {
      turnL();
    }
    else {
      turnR();
    }
  }
}
