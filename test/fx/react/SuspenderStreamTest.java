package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import fx.react.value.Var;
import fx.Counter;

class SuspenderStreamTest {

  @Test
  void test() {
    var a = Var.newSimpleVar("foo").suspendable();
    var counter = new Counter();
    a.addListener((obs, oldVal, newVal) -> counter.inc());

    var src = new EventSource<Void>();
    var suspender = src.suspenderOf(a);

    suspender.hook(x -> a.setValue("bar")).subscribe(x -> {
      assertEquals(0, counter.get());
    });

    src.push(null);
    assertEquals(1, counter.get());
  }

}
