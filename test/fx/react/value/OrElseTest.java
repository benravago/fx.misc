package fx.react.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import fx.react.Counter;
import fx.react.Subscription;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

class OrElseTest {

  @Test
  void testCorrectness() {
    StringProperty s1 = new SimpleStringProperty("a");
    StringProperty s2 = new SimpleStringProperty("b");
    StringProperty s3 = new SimpleStringProperty("c");

    Val<String> firstNonNull = Val.orElse(s1, s2).orElse(s3);
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
    StringProperty s1 = new SimpleStringProperty("a");
    StringProperty s2 = new SimpleStringProperty("b");
    StringProperty s3 = new SimpleStringProperty("c");

    Val<String> firstNonNull = Val.orElse(s1, s2).orElse(s3);

    Counter invalidationCounter = new Counter();
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
    SimpleVar<String> s1 = (SimpleVar<String>) Var.newSimpleVar("a");
    SimpleVar<String> s2 = (SimpleVar<String>) Var.newSimpleVar("b");
    SimpleVar<String> s3 = (SimpleVar<String>) Var.newSimpleVar("c");

    Val<String> firstNonNull = Val.orElse(s1, s2).orElse(s3);

    assertFalse(s1.isObservingInputs());
    assertFalse(s2.isObservingInputs());
    assertFalse(s2.isObservingInputs());

    Subscription sub = firstNonNull.pin();

    assertTrue(s1.isObservingInputs());
    assertTrue(s2.isObservingInputs());
    assertTrue(s2.isObservingInputs());

    sub.unsubscribe();

    assertFalse(s1.isObservingInputs());
    assertFalse(s2.isObservingInputs());
    assertFalse(s2.isObservingInputs());
  }
}
