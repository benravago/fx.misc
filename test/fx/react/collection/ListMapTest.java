package fx.react.collection;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;

import fx.react.value.Var;

class ListMapTest {

  @Test
  void testGet() {
    var strings = FXCollections.observableArrayList("1", "22", "333");
    var lengths = LiveList.map(strings, String::length);

    assertEquals(Arrays.asList(1, 2, 3), lengths);
  }

  @Test
  void testChanges() {
    var strings = FXCollections.observableArrayList("1", "22", "333");
    var lengths = LiveList.map(strings, String::length);

    var removed = new ArrayList<Integer>();
    var added = new ArrayList<Integer>();
    lengths.observeChanges(ch -> {
      for (var mod : ch.getModifications()) {
        removed.addAll(mod.getRemoved());
        added.addAll(mod.getAddedSubList());
      }
    });

    strings.set(1, "4444");

    assertEquals(Arrays.asList(2), removed);
    assertEquals(Arrays.asList(4), added);
  }

  @Test
  void testLaziness() {
    var strings = FXCollections.observableArrayList("1", "22", "333");
    var evaluations = new SimpleIntegerProperty(0);
    var lengths = LiveList.map(strings, s -> {
      evaluations.set(evaluations.get() + 1);
      return s.length();
    });

    lengths.observeChanges(ch -> {
    });
    strings.remove(1);

    assertEquals(0, evaluations.get());
  }

  @Test
  void testLazinessOnChangeAccumulation() {
    var strings = FXCollections.observableArrayList("1", "22", "333");
    var evaluations = new SimpleIntegerProperty(0);
    var lengths = LiveList.map(strings, s -> {
      evaluations.set(evaluations.get() + 1);
      return s.length();
    });
    var suspendable = lengths.suspendable();

    suspendable.observeChanges(ch -> {
    });
    suspendable.suspendWhile(() -> {
      strings.remove(1);
      strings.set(1, "abcd");
    });

    assertEquals(0, evaluations.get());
  }

  @Test
  void testDynamicMap() {
    var strings = new LiveArrayList<>("1", "22", "333");
    var fn = Var.<Function<String, Integer>>newSimpleVar(String::length);
    var ints = strings.mapDynamic(fn).suspendable();

    assertEquals(2, ints.get(1).intValue());

    ints.observeChanges(ch -> {
      for (var mod : ch) {
        assertEquals(Arrays.asList(1, 2, 3), mod.getRemoved());
        assertEquals(Arrays.asList(1, 16, 9), mod.getAddedSubList());
      }
    });

    ints.suspendWhile(() -> {
      strings.set(1, "4444");
      fn.setValue(s -> s.length() * s.length());
    });
  }

}
