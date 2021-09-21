package fx.react.collection;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.IntFunction;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;

class ListRecursionTest {

  /**
   * Tests that list changes are accumulated on recursion.
   */
  @Test
  void testChangeAccumulation() {
    var strings = new LiveArrayList<>("1", "22", "333");
    var lengths = LiveList.map(strings, String::length);

    var firstListener = new SimpleIntegerProperty(0);

    var first1Removed = new ArrayList<Integer>();
    var first1Added = new ArrayList<Integer>();
    var first2Removed = new ArrayList<Integer>();
    var first2Added = new ArrayList<Integer>();
    var secondRemoved = new ArrayList<Integer>();
    var secondAdded = new ArrayList<Integer>();

    
    var listenerFactory = (IntFunction<ListChangeListener<Integer>>) id -> ch -> {
      while (ch.next()) {
        if (firstListener.get() == 0) {
          firstListener.set(id);
          first1Removed.addAll(ch.getRemoved());
          first1Added.addAll(ch.getAddedSubList());
          strings.add(2, "55555");
        } else if (firstListener.get() == id) {
          first2Removed.addAll(ch.getRemoved());
          first2Added.addAll(ch.getAddedSubList());
        } else {
          secondRemoved.addAll(ch.getRemoved());
          secondAdded.addAll(ch.getAddedSubList());
        }
      }
    };

    lengths.addListener(listenerFactory.apply(1));
    lengths.addListener(listenerFactory.apply(2));

    strings.set(1, "4444");

    assertEquals(Arrays.asList(2), first1Removed);
    assertEquals(Arrays.asList(4), first1Added);
    assertEquals(Arrays.asList(), first2Removed);
    assertEquals(Arrays.asList(5), first2Added);
    assertEquals(Arrays.asList(2), secondRemoved);
    assertEquals(Arrays.asList(4, 5), secondAdded);
  }

  @Test
  void testModificationAccumulation() {
    var list = new LiveArrayList<>(1, 2, 3, 4, 5);
    var mods = new ArrayList<MaterializedModification<? extends Integer>>();
    list.observeModifications(mod -> {
      mods.add(mod.materialize());
      if (list.size() == 3) {
        list.add(0, 0);
      } else if (list.size() == 4) {
        list.add(6);
      }
    });
    list.removeAll(3, 5);

    assertEquals(3, mods.size());

    assertEquals(2, mods.get(0).getFrom());
    assertEquals(Arrays.asList(3), mods.get(0).getRemoved());
    assertEquals(0, mods.get(0).getAddedSize());

    assertEquals(0, mods.get(1).getFrom());
    assertEquals(0, mods.get(1).getRemovedSize());
    assertEquals(Arrays.asList(0), mods.get(1).getAdded());

    assertEquals(4, mods.get(2).getFrom());
    assertEquals(Arrays.asList(5), mods.get(2).getRemoved());
    assertEquals(Arrays.asList(6), mods.get(2).getAdded());
  }

}
