package fx.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

class ListHelperTest {

  @Test
  void testRemoveWhileIterating() {
    Vector<Integer> lh = null;

    lh = Vectors.add(lh, 0);
    lh = Vectors.add(lh, 1);
    lh = Vectors.add(lh, 2);

    var it = Vectors.iterator(lh);
    var i = 2;
    while (it.hasNext()) {
      lh = Vectors.remove(lh, i--);
      it.next();
    }

    assertEquals(-1, i);
    assertEquals(0, Vectors.size(lh));
  }

  @Test
  void testAddWhileIterating() {
    Vector<Integer> lh = null;

    lh = Vectors.add(lh, 0);
    lh = Vectors.add(lh, 1);
    lh = Vectors.add(lh, 2);

    var it = Vectors.iterator(lh);
    var i = 2;
    while (it.hasNext()) {
      lh = Vectors.add(lh, i--);
      it.next();
    }

    assertEquals(-1, i);
    assertArrayEquals(new Integer[] { 0, 1, 2, 2, 1, 0 }, Vectors.toArray(lh, n -> new Integer[n]));

    it = Vectors.iterator(lh);
    assertFalse(lh == Vectors.add(lh, 5)); // test that a copy is made
    while (it.hasNext()) {
      it.next(); // drain the iterator
    }
    assertTrue(lh == Vectors.add(lh, 5)); // test that change is made in place
  }

  @Test
  void testRemoveInForEach() {
    var lh = new SimpleObjectProperty<Vector<Integer>>(null);
    var iterations = new SimpleIntegerProperty(0);

    lh.set(Vectors.add(lh.get(), 0));
    lh.set(Vectors.add(lh.get(), 1));
    lh.set(Vectors.add(lh.get(), 2));

    Vectors.forEach(lh.get(), i -> {
      lh.set(Vectors.remove(lh.get(), 2 - i));
      iterations.set(iterations.get() + 1);
    });

    assertEquals(3, iterations.get());
    assertEquals(0, Vectors.size(lh.get()));
  }

  @Test
  void testAddInForEach() {
    var lh = new SimpleObjectProperty<Vector<Integer>>(null);
    var iterations = new SimpleIntegerProperty(0);

    lh.set(Vectors.add(lh.get(), 0));
    lh.set(Vectors.add(lh.get(), 1));
    lh.set(Vectors.add(lh.get(), 2));

    Vectors.forEach(lh.get(), i -> {
      lh.set(Vectors.add(lh.get(), 2 - i));
      iterations.set(iterations.get() + 1);
    });

    assertEquals(3, iterations.get());
    assertArrayEquals(new Integer[] { 0, 1, 2, 2, 1, 0 }, Vectors.toArray(lh.get(), n -> new Integer[n]));
  }

}
