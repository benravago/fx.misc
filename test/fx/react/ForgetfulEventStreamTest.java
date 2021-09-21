package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

class ForgetfulEventStreamTest {

  @Test
  void test() {
    var source = new EventSource<Integer>();
    var suspendable = source.forgetful();
    var emitted = new ArrayList<Integer>();
    suspendable.subscribe(emitted::add);

    source.push(1);
    suspendable.suspendWhile(() -> {
      source.push(2);
      source.push(3);
    });
    source.push(4);

    assertEquals(Arrays.asList(1, 3, 4), emitted);
  }

  @Test
  void testResetOnUnsubscribe() {
    var source = new EventSource<Integer>();
    var suspendable = source.forgetful();
    var emitted = new ArrayList<>();
    var sub = suspendable.subscribe(emitted::add);

    var suspension = suspendable.suspend();
    source.push(1);
    source.push(2);
    assertEquals(Arrays.asList(), emitted);

    sub.unsubscribe(); // suspendable's stored value should have be reset now
    sub = suspendable.subscribe(emitted::add);
    suspension.close();
    assertEquals(Arrays.asList(), emitted);
  }

}
