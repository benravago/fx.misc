package fx.react;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import fx.react.value.Var;

class DefaultEventStreamTest {

  @Test
  void test() {
    EventCounter countsTwice = new EventCounter();
    EventCounter countsOnce = new EventCounter();

    EventSource<Boolean> source = new EventSource<>();
    EventStream<Boolean> stream = source.withDefaultEvent(true);

    stream.subscribe(countsTwice::accept);
    source.push(false);
    stream.subscribe(countsOnce::accept);

    assertEquals(2, countsTwice.get(), "Counts Twice failed");
    assertEquals(1, countsOnce.get(), "Counts Once failed");
  }

  @Test
  void testAutoEmittingStream() {
    List<Integer> emitted = new ArrayList<>();

    Var<Integer> source = Var.newSimpleVar(1);
    EventStream<Integer> stream = source.values().withDefaultEvent(0);

    stream.subscribe(emitted::add);

    assertEquals(Arrays.asList(1), emitted);

    source.setValue(2);

    assertEquals(Arrays.asList(1, 2), emitted);
  }
}
