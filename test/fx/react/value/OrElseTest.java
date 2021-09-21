package fx.react.value;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import javafx.beans.property.SimpleStringProperty;

import fx.Counter;

class OrElseTest {

  @Test
  void testCorrectness() {
    var s1 = new SimpleStringProperty("a");
    var s2 = new SimpleStringProperty("b");
    var s3 = new SimpleStringProperty("c");

    var firstNonNull = Val.orElse(s1, s2).orElse(s3);
    assertEquals("a", firstNonNull.getValue());

    s2.set(null);
    assertEquals("a", firstNonNull.getValue());

    s1.set(null);
    assertEquals("c", firstNonNull.getValue());

    s2.set("b");
    assertEquals("b", firstNonNull.getValue());

    s2.set(null);
    s3.set(null);
    assertNull(firstNonNull.getValue());
  }

  @Test
  void testInvalidationEfficiency() {
    var s1 = new SimpleStringProperty("a");
    var s2 = new SimpleStringProperty("b");
    var s3 = new SimpleStringProperty("c");

    var firstNonNull = Val.orElse(s1, s2).orElse(s3);

    var invalidationCounter = new Counter();
    firstNonNull.addListener(obs -> invalidationCounter.inc());

    assertEquals(0, invalidationCounter.get());

    firstNonNull.getValue();
    assertEquals(0, invalidationCounter.get());

    s2.set("B");
    assertEquals(0, invalidationCounter.get());

    s3.set("C");
    assertEquals(0, invalidationCounter.get());

    s1.set("A");
    assertEquals(1, invalidationCounter.getAndReset());
  }

  @Test
  void testLaziness() {
    var s1 = (SimpleVar<String>) Var.newSimpleVar("a");
    var s2 = (SimpleVar<String>) Var.newSimpleVar("b");
    var s3 = (SimpleVar<String>) Var.newSimpleVar("c");

    var firstNonNull = Val.orElse(s1, s2).orElse(s3);

    assertFalse(s1.isObservingInputs());
    assertFalse(s2.isObservingInputs());
    assertFalse(s2.isObservingInputs());

    var sub = firstNonNull.pin();

    assertTrue(s1.isObservingInputs());
    assertTrue(s2.isObservingInputs());
    assertTrue(s2.isObservingInputs());

    sub.unsubscribe();

    assertFalse(s1.isObservingInputs());
    assertFalse(s2.isObservingInputs());
    assertFalse(s2.isObservingInputs());
  }

}
