package tests;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import parser.Parser;
import parser.ParserFailureException;
import parser.Program;

class IntegrationParserTests {
  
  @Test void p0_testBad1(){ fail("s0_bad1");  }
  //TODO: the error for bad2 contains lists of options, this changes over time when more stuff is added?
  @Test void p0_testBad2(){ fail(  "s0_bad2");    }
  @Test void p0_testBad3(){ fail(  "s0_bad3");    }
  @Test void p0_testBad4(){ fail(  "s0_bad4");    }
  @Test void p0_testFull(){ ok(    "s0_full");    }
  @Test void p0_testSimple(){ ok(  "s0_simple");   }
  @Test void p0_testProvided(){ ok("s0_provided"); }
  
  @Test void p1_testBad1(){ fail(  "s1_bad1");    }
  @Test void p1_testBad2(){ fail(  "s1_bad2");    }
  @Test void p1_testBad5(){ fail(  "s1_bad3");    }
  @Test void p1_testFull(){ ok(    "s1_full");    }
  @Test void p1_testSimple(){ ok(  "s1_simple");  }
  @Test void p1_testProvided(){ ok("s1_provided");}

  @Test void p2_testBad1(){   fail("s2_bad1");    }
  @Test void p2_testBad2(){   fail("s2_bad2");    }
  @Test void p2_testBad3(){   fail("s2_bad3");    }
  @Test void p2_testBad4(){   fail("s2_bad4");    }
  @Test void p2_testBad5(){   fail("s2_bad5");    }
  @Test void p2_testBad6(){   fail("s2_bad6");    }
  @Test void p2_testBad7(){   fail("s2_bad7");    }
  @Test void p2_testFull(){     ok("s2_full");    }
  @Test void p2_testSimple(){   ok("s2_simple");  }
  @Test void p2_testProvided(){ ok("s2_provided");}

  @Test void p3_testBad1(){   fail("s3_bad1");    }
  @Test void p3_testBad2(){   fail("s3_bad2");    }
  @Test void p3_testBad3(){   fail("s3_bad3");    }
  @Test void p3_testnoLocVar(){ ok("s3_noLocVar");}
  @Test void p3_testSimple(){   ok("s3_simple");  }
  @Test void p3_testFull(){     ok("s3_full");    }

  
  static String readFileStr(Path p){
    try { return Files.readString(p); }
    catch (IOException e) { throw new UncheckedIOException(e); }
  }
  static void fail(String test){
    var input=    Path.of("src","programs",test+".prog");
    Parser parser= new Parser(input);
    var err= ParserFailureException.class;
    ParserFailureException exc= assertThrows(err,()->parser.parse());
    assertFalse(exc.getMessage().isEmpty());//Checking exact error messages is very brittle
  }
  static String noIndent(Object o){
    return o.toString().replace(" ","").replace("\n","").replace("\r","");
  }
  static void ok(String test){
    var input=    Path.of("src","programs",test+".prog");
    var expected= Path.of("src","testResults",test+".txt");
    Parser parser= new Parser(input);
    Program prog= parser.parse();
    assertNotNull(prog);
    assertEquals(noIndent(readFileStr(expected)),noIndent(prog));
  }
}