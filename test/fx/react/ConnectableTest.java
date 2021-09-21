package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import fx.Counter;

class ConnectableTest {

  @Test
  void test() {
    var counter = new Counter();
    var cs = new ConnectableEventSource<Integer>();
    var observed = cs.hook(counter);
    var src1 = new EventSource<Integer>();
    var src2 = new EventSource<Integer>();
    var con1 = cs.connectTo(src1.filter(x -> {
      if (x == 666) {
        throw new IllegalArgumentException();
      } else {
        return true;
      }
    }));
    cs.connectTo(src2);

    // test laziness
    src1.push(1);
    src2.push(2);
    assertEquals(0, counter.get());

    // test event propagation
    var sub = observed.pin();
    src1.push(1);
    src2.push(2);
    assertEquals(2, counter.getAndReset());

    // test that disconnection works
    con1.unsubscribe();
    src1.push(1);
    src1.push(666);
    assertEquals(0, counter.get());
    src2.push(2);
    assertEquals(1, counter.getAndReset());

    // test that unsubscribe works
    sub.unsubscribe();
    src2.push(2);
    assertEquals(0, counter.get());
  }

}
