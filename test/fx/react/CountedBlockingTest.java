package fx.react;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import fx.react.value.SuspendableVar;
import fx.react.value.Var;

class CountedBlockingTest {

  @Test
  void testIndicator() {
    SuspendableNo a = new SuspendableNo();
    Guard g = a.suspend();
    Guard h = a.suspend();
    g.close();
    assertTrue(a.get());
    g.close();
    assertTrue(a.get());
    h.close();
    assertFalse(a.get());
  }

  @Test
  void testSuspendableVal() {
    SuspendableVar<String> a = Var.<String>newSimpleVar(null).suspendable();
    Counter counter = new Counter();
    a.addListener(obs -> counter.inc());
    Guard g = a.suspend();
    a.setValue("x");
    assertEquals(0, counter.get());
    Guard h = a.suspend();
    g.close();
    assertEquals(0, counter.get());
    g.close();
    assertEquals(0, counter.get());
    h.close();
    assertEquals(1, counter.get());
  }
}
