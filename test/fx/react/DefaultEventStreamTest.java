package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import fx.react.value.Var;
import fx.Counter;

class DefaultEventStreamTest {

  @Test
  void test() {
    var countsTwice = new Counter();
    var countsOnce = new Counter();

    var source = new EventSource<Boolean>();
    var stream = source.withDefaultEvent(true);

    stream.subscribe(countsTwice::accept);
    source.push(false);
    stream.subscribe(countsOnce::accept);

    assertEquals(2, countsTwice.get(), "Counts Twice failed");
    assertEquals(1, countsOnce.get(), "Counts Once failed");
  }

  @Test
  void testAutoEmittingStream() {
    var emitted = new ArrayList<Integer>();

    var source = Var.newSimpleVar(1);
    var stream = source.values().withDefaultEvent(0);

    stream.subscribe(emitted::add);

    assertEquals(Arrays.asList(1), emitted);

    source.setValue(2);

    assertEquals(Arrays.asList(1, 2), emitted);
  }

}
