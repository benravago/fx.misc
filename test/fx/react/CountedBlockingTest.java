package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import fx.react.value.Var;
import fx.Counter;

class CountedBlockingTest {

  @Test
  void testIndicator() {
    var a = new SuspendableNo();
    var g = a.suspend();
    var h = a.suspend();
    g.close();
    assertTrue(a.get());
    g.close();
    assertTrue(a.get());
    h.close();
    assertFalse(a.get());
  }

  @Test
  void testSuspendableVal() {
    var a = Var.<String>newSimpleVar(null).suspendable();
    var counter = new Counter();
    a.addListener(obs -> counter.inc());
    var g = a.suspend();
    a.setValue("x");
    assertEquals(0, counter.get());
    var h = a.suspend();
    g.close();
    assertEquals(0, counter.get());
    g.close();
    assertEquals(0, counter.get());
    h.close();
    assertEquals(1, counter.get());
  }

}
