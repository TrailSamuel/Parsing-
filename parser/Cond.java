package parser;

import robotGame.OuterWorld;
//Notes to self
// The Cond interface represents conditional expressions in the robot language.
// Conditions are used in if statements and while loops to control program flow.
// All conditions implement an evaluate method that returns a boolean value
// when executed in the robot's world context.

//TODO: define a Cond interface with an evaluate method
//With such interface, declare records like the one below
// record Lt(Exp left, Exp right) implements Cond{
//  public boolean evaluate(OuterWorld w){ return left.evaluate(w) < right.evaluate(w); }
//  @Override public String toString(){ return "Lt["+left+", "+right+"]"; }
//}

interface Cond{
    boolean evaluate(OuterWorld w);
}


// Less Than comparison: Checks if the left expression is less than the right expression
record Lt(Exp left, Exp right) implements Cond{
    public boolean evaluate(OuterWorld w){ return left.evaluate(w) < right.evaluate(w); }
    @Override public String toString(){ return "Lt["+left+", "+right+"]"; }
}

// Greater Than comparison: Checks if the left expression is greater than the right expression
record Gt(Exp left, Exp right) implements Cond{
    public boolean evaluate(OuterWorld w){ return left.evaluate(w) > right.evaluate(w); }
    @Override public String toString(){ return "Gt["+left+", "+right+"]"; }
}

// Equal comparison: Checks if the left expression equals the right expression
record Eq(Exp left, Exp right) implements Cond{
    public boolean evaluate(OuterWorld w){ return left.evaluate(w) == right.evaluate(w); }
    @Override public String toString(){ return "Eq["+left+", "+right+"]"; }
}

// Logical AND: Returns true if both conditions are true
record And(Cond left, Cond right) implements Cond {
    public boolean evaluate(OuterWorld w) {
        boolean leftResult = left.evaluate(w);
        boolean rightResult = right.evaluate(w);
        return leftResult && rightResult;
    }
    @Override public String toString() { return "And[" + left + ", " + right + "]"; }
}

// Logical OR: Returns true if either condition is true
record Or(Cond left, Cond right) implements Cond {
    public boolean evaluate(OuterWorld w) {
        boolean leftResult = left.evaluate(w);
        boolean rightResult = right.evaluate(w);
        return leftResult || rightResult;
    }
    @Override public String toString() { return "Or[" + left + ", " + right + "]"; }
}

// Logical NOT: Returns the negation of the condition
record Not(Cond cond) implements Cond {
    public boolean evaluate(OuterWorld w) {
        return !cond.evaluate(w);
    }
    @Override public String toString() {
        return "Not[" + cond + "]";
    }
}