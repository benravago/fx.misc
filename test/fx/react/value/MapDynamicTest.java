package fx.react.value;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.function.UnaryOperator;

class MapDynamicTest {

  @Test
  void test() {
    var src = Var.newSimpleVar(1);
    var fn = Var.newSimpleVar(UnaryOperator.<Integer>identity());
    var mapped = src.mapDynamic(fn);

    assertEquals(1, mapped.getValue().intValue());

    src.setValue(2);
    assertEquals(2, mapped.getValue().intValue());

    fn.setValue(i -> i + i);
    assertEquals(4, mapped.getValue().intValue());

    var sub = mapped.observeChanges((obs, oldVal, newVal) -> {
      assertEquals(4, oldVal.intValue());
      assertEquals(8, newVal.intValue());
    });
    fn.setValue(i -> i * i * i);
    sub.unsubscribe();

    fn.setValue(null);
    assertTrue(mapped.isEmpty());
  }

}
