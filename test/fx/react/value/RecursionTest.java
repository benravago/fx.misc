package fx.react.value;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

class RecursionTest {

  @Test
  void test() {
    var var = Var.newSimpleVar(0);
    var lastObserved1 = Var.newSimpleVar(var.getValue());
    var lastObserved2 = Var.newSimpleVar(var.getValue());
    var.addListener((obs, oldVal, newVal) -> {
      assertEquals(lastObserved1.getValue(), oldVal);
      lastObserved1.setValue(newVal);
      if (newVal == 1) {
        var.setValue(2);
      }
    });
    var.addListener((obs, oldVal, newVal) -> {
      assertEquals(lastObserved2.getValue(), oldVal);
      lastObserved2.setValue(newVal);
      if (newVal == 1) {
        var.setValue(2);
      }
    });
    var.setValue(1);
  }

  /**
   * This is not a test of ReactFX functionality, but rather a showcase of
   * JavaFX disfunctionality.
   */
  @Test
  void failingRecursionForJavaFxProperty() {
    var var = new SimpleIntegerProperty(0);
    var lastObserved1 = new SimpleIntegerProperty(var.get());
    var lastObserved2 = new SimpleIntegerProperty(var.get());
    var failedAsExpected = new SimpleBooleanProperty(false);
    var.addListener((obs, oldVal, newVal) -> {
      if (lastObserved1.get() != oldVal.intValue()) {
        failedAsExpected.set(true);
      }
      lastObserved1.set(newVal.intValue());
      if (newVal.intValue() == 1) {
        var.set(2);
      }
    });
    var.addListener((obs, oldVal, newVal) -> {
      if (lastObserved1.get() != oldVal.intValue()) {
        failedAsExpected.set(true);
      }
      lastObserved2.set(newVal.intValue());
      if (newVal.intValue() == 1) {
        var.set(2);
      }
    });
    var.set(1);
    assertTrue(failedAsExpected.get());
  }

}
