package fx.react.value;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import fx.Counter;

class SuspendableVarTest {

  @Test
  void test() {
    var property = Var.newSimpleVar(false).suspendable();
    var changes = new Counter();
    var invalidations = new Counter();
    property.addListener((obs, old, newVal) -> changes.inc());
    property.addListener(obs -> invalidations.inc());

    property.setValue(true);
    property.setValue(false);
    assertEquals(2, changes.getAndReset());
    assertEquals(2, invalidations.getAndReset());

    var g = property.suspend();
    property.setValue(true);
    property.setValue(false);
    g.close();
    assertEquals(0, changes.get());
    assertEquals(1, invalidations.get());
  }

}
