package fx.react.collection;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import javafx.beans.property.SimpleIntegerProperty;

import fx.Counter;

class MemoizationListTest {

  @Test
  void test() {
    var source = new LiveArrayList<>("1", "22", "333");
    var counter = new SimpleIntegerProperty(0);

    var memoizing = LiveList.map(source, s -> {
      counter.set(counter.get() + 1);
      return s.length();
    }).memoize();

    var memoized = memoizing.memoizedItems();
    var memoMirror = new ArrayList<Integer>();
    memoized.observeModifications(mod -> {
      memoMirror.subList(mod.getFrom(), mod.getFrom() + mod.getRemovedSize()).clear();
      memoMirror.addAll(mod.getFrom(), mod.getAddedSubList());
    });

    assertEquals(0, memoized.size());

    source.add("4444");
    assertEquals(Collections.emptyList(), memoized);
    assertEquals(0, memoMirror.size());
    assertEquals(0, counter.get());

    memoizing.get(2);
    assertEquals(Arrays.asList(3), memoized);
    assertEquals(Arrays.asList(3), memoMirror);
    assertEquals(1, counter.get());

    counter.set(0);
    memoizing.get(0);
    assertEquals(Arrays.asList(1, 3), memoized);
    assertEquals(Arrays.asList(1, 3), memoMirror);
    assertEquals(1, counter.get());

    counter.set(0);
    source.subList(2, 4).replaceAll(s -> s + s);
    assertEquals(Arrays.asList(1), memoized);
    assertEquals(Arrays.asList(1), memoMirror);
    assertEquals(0, counter.get());

    counter.set(0);
    memoizing.observeModifications(mod -> {
      if (mod.getAddedSize() == 3) { // when three items added
        mod.getAddedSubList().get(0); // force evaluation of the first
        mod.getAddedSubList().get(1); // and second one
        source.remove(0); // and remove the first element from source
      }
    });
    source.remove(1, 4);
    assertEquals(Arrays.asList(1), memoized);
    assertEquals(Arrays.asList(1), memoMirror);
    assertEquals(0, counter.get());
    source.addAll("22", "333", "4444");
    assertEquals(Arrays.asList(2, 3), memoized);
    assertEquals(Arrays.asList(2, 3), memoMirror);
    assertEquals(2, counter.get());

    assertEquals(Arrays.asList(2, 3, 4), memoizing);
    assertEquals(3, counter.get());
    assertEquals(Arrays.asList(2, 3, 4), memoized);
    assertEquals(Arrays.asList(2, 3, 4), memoMirror);
  }

  @Test
  void testForce() {
    var source = new LiveArrayList<>(0, 1, 2, 3, 4, 5, 6);
    var memoizing = source.memoize();
    var memoized = memoizing.memoizedItems();

    memoizing.pin(); // otherwise no memoization takes place

    memoizing.get(3);
    // _ _ _ 3 _ _ _
    assertEquals(Collections.singletonList(3), memoized);

    var counter = new Counter();
    memoized.observeChanges(ch -> {
      counter.inc();
      assertEquals(2, ch.getModificationCount());
      var mod1 = ch.getModifications().get(0);
      var mod2 = ch.getModifications().get(1);
      assertEquals(0, mod1.getFrom());
      assertEquals(0, mod1.getRemovedSize());
      assertEquals(Arrays.asList(1, 2), mod1.getAddedSubList());
      assertEquals(3, mod2.getFrom());
      assertEquals(0, mod2.getRemovedSize());
      assertEquals(Arrays.asList(4, 5), mod2.getAddedSubList());
    });

    memoizing.force(1, 6);
    assertEquals(1, counter.get());
  }

  @Test
  void testForget() {
    var source = new LiveArrayList<>(0, 1, 2, 3, 4, 5, 6);
    var memoizing = source.memoize();
    var memoized = memoizing.memoizedItems();

    memoizing.pin(); // otherwise no memoization takes place

    memoizing.force(0, 7);
    assertEquals(7, memoized.size());

    memoizing.forget(2, 4);
    assertEquals(5, memoized.size());

    memoizing.forget(3, 5);
    assertEquals(4, memoized.size());

    var counter = new Counter();
    memoized.observeQuasiChanges(ch -> {
      counter.inc();
      assertEquals(1, ch.getModificationCount());
      QuasiModification<?> mod = ch.getModifications().get(0);
      assertEquals(1, mod.getFrom());
      assertEquals(Arrays.asList(1, 5), mod.getRemoved());
      assertEquals(0, mod.getAddedSize());
    });

    memoizing.forget(1, 6);
    assertEquals(1, counter.get());
  }

  @Test
  void testMemoizationOnlyStartsWhenObsesrved() {
    var list = new LiveArrayList<>(0, 1, 2, 3, 4, 5, 6).memoize();
    var memoized = list.memoizedItems();

    list.get(0);
    assertEquals(0, memoized.size());

    memoized.pin();
    list.get(0);
    assertEquals(1, memoized.size());
  }

  @Test
  void testForceIsNotAllowedWhenUnobserved() {
    assertThrows(IllegalStateException.class, () -> {
      var list = new LiveArrayList<>(0, 1, 2, 3, 4, 5, 6).memoize();
      list.force(2, 4);
    });
  }

  @Test
  void testForgetIsNotAllowedWhenUnobserved() {
    assertThrows(IllegalStateException.class, () -> {
      var list = new LiveArrayList<>(0, 1, 2, 3, 4, 5, 6).memoize();
      list.forget(2, 4);
    });
  }

  @Test
  void testRecursionWithinForce() {
    var src = new LiveArrayList<>(0, 1, 2);
    var memo1 = src.memoize();
    var memo2 = memo1.map(Function.identity()).memoize();
    memo1.memoizedItems().sizeProperty().observeInvalidations(__ -> memo2.force(1, 2));

    var memo2Mirror = new ArrayList<Integer>();
    memo2.memoizedItems().observeModifications(mod -> {
      memo2Mirror.subList(mod.getFrom(), mod.getFrom() + mod.getRemovedSize()).clear();
      memo2Mirror.addAll(mod.getFrom(), mod.getAddedSubList());
    });

    assertEquals(Collections.emptyList(), memo1.memoizedItems());
    assertEquals(Collections.emptyList(), memo2.memoizedItems());
    assertEquals(Collections.emptyList(), memo2Mirror);

    memo2.force(1, 2); // causes an immediate change in memo1.memoizedItems(),
                       // which recursively calls memo2.force(1, 2).

    assertEquals(Arrays.asList(1), memo2Mirror);
  }

  @Test
  void testMemoizedItemsChangeWithinForce() {
    var src = new LiveArrayList<>(1, 2, 4, 8, 16, 32);
    var memo1 = src.memoize();
    var memo2 = memo1.map(Function.identity()).memoize();
    var memo1Sum = memo1.memoizedItems().reduce((a, b) -> a + b).orElseConst(0);
    memo1Sum.addListener((obs, oldVal, newVal) -> memo2.forget(0, memo2.size()));

    var memo2Mirror = new ArrayList<Integer>();
    memo2.memoizedItems().observeModifications(mod -> {
      memo2Mirror.subList(mod.getFrom(), mod.getFrom() + mod.getRemovedSize()).clear();
      memo2Mirror.addAll(mod.getFrom(), mod.getAddedSubList());
      // the main part of this test is that it does not throw IndexOutOfBoundsException
    });
    memo2.force(3, 6);

    assertEquals(Arrays.asList(32), memo2Mirror);
  }

}
