package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

class SuspendedWhenTest {

  @Test
  void test() {
    var p = new SimpleObjectProperty<>(0);
    var suspended = new SimpleBooleanProperty(true);
    var emitted = new ArrayList<Integer>();
    var pausable = EventStreams.valuesOf(p).pausable();
    var sub = pausable.suspendedWhen(suspended).subscribe(emitted::add);

    // test that the stream started suspended
    assertEquals(Arrays.asList(), emitted);

    suspended.set(false);
    assertEquals(Arrays.asList(0), emitted);

    p.setValue(1);
    assertEquals(Arrays.asList(0, 1), emitted);

    suspended.set(true);
    p.setValue(2);
    p.setValue(3);
    p.setValue(4);
    assertEquals(Arrays.asList(0, 1), emitted);

    List<Integer> emitted2 = new ArrayList<>();
    pausable.subscribe(emitted2::add);
    assertEquals(Arrays.asList(), emitted2);

    suspended.set(false);
    assertEquals(Arrays.asList(0, 1, 2, 3, 4), emitted);
    assertEquals(Arrays.asList(2, 3, 4), emitted2);

    suspended.set(true);
    p.setValue(5);
    p.setValue(6);
    assertEquals(Arrays.asList(2, 3, 4), emitted2);
    sub.unsubscribe(); // testing resume on unsubscribe
    assertEquals(Arrays.asList(0, 1, 2, 3, 4), emitted);
    assertEquals(Arrays.asList(2, 3, 4, 5, 6), emitted2);
  }

}
