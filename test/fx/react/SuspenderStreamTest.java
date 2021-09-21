package fx.react;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import fx.react.value.SuspendableVar;
import fx.react.value.Var;

class SuspenderStreamTest {

  @Test
  void test() {
    SuspendableVar<String> a = Var.newSimpleVar("foo").suspendable();
    Counter counter = new Counter();
    a.addListener((obs, oldVal, newVal) -> counter.inc());

    EventSource<Void> src = new EventSource<>();
    EventStream<Void> suspender = src.suspenderOf(a);

    suspender.hook(x -> a.setValue("bar")).subscribe(x -> {
      assertEquals(0, counter.get());
    });

    src.push(null);
    assertEquals(1, counter.get());
  }

}
