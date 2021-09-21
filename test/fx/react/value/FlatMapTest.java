package fx.react.value;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import fx.Counter;

class FlatMapTest {

  static class A {
    final SimpleVar<B> b = (SimpleVar<B>) Var.<B>newSimpleVar(null);
  }

  static class B {
    final SimpleVar<String> s = (SimpleVar<String>) Var.<String>newSimpleVar(null);
  }

  @Test
  void flatMapTest() {
    var base = new SimpleObjectProperty<A>();
    var flat = Val.flatMap(base, a -> a.b).flatMap(b -> b.s);

    var invalidationCounter = new Counter();
    flat.addListener(obs -> invalidationCounter.inc());

    assertNull(flat.getValue());

    var a = new A();
    var b = new B();
    b.s.setValue("s1");
    a.b.setValue(b);
    base.setValue(a);
    assertEquals(1, invalidationCounter.getAndReset());
    assertEquals("s1", flat.getValue());

    a.b.setValue(new B());
    assertEquals(1, invalidationCounter.getAndReset());
    assertNull(flat.getValue());

    b.s.setValue("s2");
    assertEquals(0, invalidationCounter.getAndReset());
    assertNull(flat.getValue());

    a.b.getValue().s.setValue("x");
    assertEquals(1, invalidationCounter.getAndReset());
    assertEquals("x", flat.getValue());

    a.b.setValue(null);
    assertEquals(1, invalidationCounter.getAndReset());
    assertNull(flat.getValue());

    a.b.setValue(b);
    assertEquals(1, invalidationCounter.getAndReset());
    assertEquals("s2", flat.getValue());
  }

  @Test
  void selectPropertyTest() {
    var base = new SimpleObjectProperty<A>();
    var selected = Val.flatMap(base, a -> a.b).selectVar(b -> b.s);

    var invalidationCounter = new Counter();
    selected.addListener(obs -> invalidationCounter.inc());

    assertNull(selected.getValue());

    selected.setValue("will be discarded");
    assertNull(selected.getValue());
    assertEquals(0, invalidationCounter.getAndReset());

    var src = new SimpleStringProperty();

    selected.bind(src);
    assertNull(selected.getValue());
    assertEquals(0, invalidationCounter.getAndReset());

    src.setValue("1");
    assertNull(selected.getValue());
    assertEquals(0, invalidationCounter.getAndReset());

    var a = new A();
    var b = new B();
    b.s.setValue("X");
    a.b.setValue(b);
    base.setValue(a);

    assertEquals(1, invalidationCounter.getAndReset());
    assertEquals("1", selected.getValue());
    assertEquals("1", b.s.getValue());

    src.setValue("2");
    assertEquals(1, invalidationCounter.getAndReset());
    assertEquals("2", selected.getValue());
    assertEquals("2", b.s.getValue());

    var b2 = new B();
    b2.s.setValue("Y");
    a.b.setValue(b2);
    assertEquals(1, invalidationCounter.getAndReset());
    assertEquals("2", b2.s.getValue());
    assertEquals("2", selected.getValue());

    src.setValue("3");
    assertEquals(1, invalidationCounter.getAndReset());
    assertEquals("3", b2.s.getValue());
    assertEquals("3", selected.getValue());
    assertEquals("2", b.s.getValue());

    base.setValue(null);
    assertEquals(1, invalidationCounter.getAndReset());
    assertNull(selected.getValue());
    assertFalse(b2.s.isBound());

    base.setValue(a);
    assertEquals(1, invalidationCounter.getAndReset());
    assertEquals("3", selected.getValue());
    assertTrue(b2.s.isBound());

    selected.unbind();
    assertEquals(0, invalidationCounter.getAndReset());
    src.setValue("4");
    assertEquals("3", b2.s.getValue());
    assertEquals("3", selected.getValue());
    assertEquals("2", b.s.getValue());

    a.b.setValue(b);
    selected.setValue("5");
    assertEquals("5", b.s.getValue());

    a.b.setValue(null);
    selected.bind(src);
    a.b.setValue(b2);
    assertTrue(b2.s.isBound());
  }

  @Test
  void selectPropertyResetTest() {
    var base = new SimpleObjectProperty<A>();
    var selected = Val.flatMap(base, a -> a.b).selectVar(b -> b.s, "X");
    var source = new SimpleStringProperty("A");

    selected.bind(source);

    assertEquals(null, selected.getValue());

    var a = new A();
    var b = new B();
    a.b.setValue(b);
    base.setValue(a);
    assertEquals("A", selected.getValue());
    assertEquals("A", b.s.getValue());

    var b2 = new B();
    a.b.setValue(b2);
    assertEquals("A", b2.s.getValue());
    assertEquals("X", b.s.getValue());

    base.setValue(null);
    assertEquals("X", b2.s.getValue());
  }

  @Test
  void lazinessTest() {
    var base = (SimpleVar<A>) Var.<A>newSimpleVar(null);
    var flatMapped = base.flatMap(a -> a.b);
    var selected = flatMapped.selectVar(b -> b.s);

    assertFalse(base.isObservingInputs());

    var a = new A();
    var b = new B();
    a.b.setValue(b);
    base.setValue(a);

    assertFalse(base.isObservingInputs());
    assertFalse(a.b.isObservingInputs());
    assertFalse(b.s.isObservingInputs());

    var sub = selected.pin();

    assertTrue(base.isObservingInputs());
    assertTrue(a.b.isObservingInputs());
    assertTrue(b.s.isObservingInputs());

    var b2 = new B();
    a.b.setValue(b2);

    assertFalse(b.s.isObservingInputs()); // stopped observing b.s
    assertTrue(base.isObservingInputs());
    assertTrue(a.b.isObservingInputs());
    assertFalse(b2.s.isObservingInputs()); // no need to observe b2.s yet

    selected.setValue("Y");

    assertFalse(b2.s.isObservingInputs()); // still no need to observe b2.s

    selected.getValue();

    assertTrue(b2.s.isObservingInputs()); // now we have to observe b2.s for invalidations

    sub.unsubscribe();

    assertFalse(base.isObservingInputs());
    assertFalse(a.b.isObservingInputs());
    assertFalse(b2.s.isObservingInputs());
    assertFalse(((ValBase<B>) flatMapped).isObservingInputs());
  }

}
