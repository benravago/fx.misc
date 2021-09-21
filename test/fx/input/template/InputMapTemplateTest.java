package fx.input.template;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCombination.*;
import static javafx.scene.input.KeyEvent.*;

import fx.input.InputHandler;
import fx.input.InputMap;
import fx.input.Nodes;
import fx.jupiter.Fx;

import static fx.input.EventPattern.*;
import static fx.input.template.InputMapTemplate.*;

import static fx.input.EventFoo.*;

class InputMapTemplateTest {

  @Test
  void test() {
    var res = new SimpleStringProperty();

    InputMapTemplate<Node, KeyEvent> imt1 = consume(keyPressed(A), (s, e) -> res.set("A"));
    InputMapTemplate<Node, KeyEvent> imt2 = consume(keyPressed(B), (s, e) -> res.set("B"));
    var imt = imt1.orElse(imt2);
    var ignA = InputMap.ignore(keyPressed(A));

    var node1 = new Region();
    var node2 = new Region();

    Nodes.addInputMap(node1, ignA);
    InputMapTemplate.installFallback(imt, node1);
    InputMapTemplate.installFallback(imt, node2);

    var aPressed = new KeyEvent(KEY_PRESSED, "", "", A, false, false, false, false);
    var bPressed = new KeyEvent(KEY_PRESSED, "", "", B, false, false, false, false);

    dispatch(aPressed, node1);
    assertNull(res.get());
    assertFalse(aPressed.isConsumed());

    dispatch(aPressed, node2);
    assertEquals("A", res.get());
    assertTrue(aPressed.isConsumed());

    dispatch(bPressed, node1);
    assertEquals("B", res.get());
    assertTrue(bPressed.isConsumed());
  }

  static final InputMapTemplate<TextArea, InputEvent> INPUT_MAP_TEMPLATE =
    unless(TextArea::isDisabled,
      sequence(
        consume(keyPressed(A, SHORTCUT_DOWN), (area, evt) -> area.selectAll()),
        consume(keyPressed(C, SHORTCUT_DOWN), (area, evt) -> area.copy())
      /* ... */
    ));

  @Test @Fx
  void textAreaExample() {
    var area1 = new TextArea();
    var area2 = new TextArea();

    InputMapTemplate.installFallback(INPUT_MAP_TEMPLATE, area1);
    InputMapTemplate.installFallback(INPUT_MAP_TEMPLATE, area2);
  }

  @Test
  void testIfConsumed() {
    var counter = new SimpleIntegerProperty(0);

    var baseIMT = InputMapTemplate.<Node, KeyEvent>sequence(
      consume(keyPressed(UP)),
      consume(keyPressed(DOWN)),
      consume(keyPressed(LEFT)),
      consume(keyPressed(RIGHT))
    );

    var imtPP = baseIMT.ifConsumed((n, e) -> counter.set(counter.get() + 1));

    var node = new Region();
    InputMapTemplate.installFallback(imtPP, node);

    var a = new KeyEvent(KEY_PRESSED, "", "", A, false, false, false, false);
    var up = new KeyEvent(KEY_PRESSED, "", "", UP, false, false, false, false);
    var down = new KeyEvent(KEY_PRESSED, "", "", DOWN, false, false, false, false);
    var b = new KeyEvent(KEY_PRESSED, "", "", B, false, false, false, false);
    var left = new KeyEvent(KEY_PRESSED, "", "", LEFT, false, false, false, false);
    var right = new KeyEvent(KEY_PRESSED, "", "", RIGHT, false, false, false, false);

    dispatch(a, node);
    assertEquals(0, counter.get());
    assertFalse(a.isConsumed());

    dispatch(up, node);
    assertEquals(1, counter.get());
    assertTrue(up.isConsumed());

    dispatch(down, node);
    assertEquals(2, counter.get());
    assertTrue(down.isConsumed());

    dispatch(b, node);
    assertEquals(2, counter.get());
    assertFalse(b.isConsumed());

    dispatch(left, node);
    assertEquals(3, counter.get());
    assertTrue(left.isConsumed());

    dispatch(right, node);
    assertEquals(4, counter.get());
    assertTrue(right.isConsumed());
  }

  @Test
  void testIfIgnored() {
    var counter = new SimpleIntegerProperty(0);

    var baseIMT = InputMapTemplate.<Node, KeyEvent>sequence(
      ignore(keyPressed(UP)),
      ignore(keyPressed(DOWN)),
      ignore(keyPressed(LEFT)),
      ignore(keyPressed(RIGHT))
    );

    var imtPP = baseIMT.ifIgnored((n, e) -> counter.set(counter.get() + 1));

    var node = new Region();
    InputMapTemplate.installFallback(imtPP, node);

    var a = new KeyEvent(KEY_PRESSED, "", "", A, false, false, false, false);
    var up = new KeyEvent(KEY_PRESSED, "", "", UP, false, false, false, false);
    var down = new KeyEvent(KEY_PRESSED, "", "", DOWN, false, false, false, false);
    var b = new KeyEvent(KEY_PRESSED, "", "", B, false, false, false, false);
    var left = new KeyEvent(KEY_PRESSED, "", "", LEFT, false, false, false, false);
    var right = new KeyEvent(KEY_PRESSED, "", "", RIGHT, false, false, false, false);

    dispatch(a, node);
    assertEquals(0, counter.get());
    assertFalse(a.isConsumed());

    dispatch(up, node);
    assertEquals(1, counter.get());
    assertFalse(up.isConsumed());

    dispatch(down, node);
    assertEquals(2, counter.get());
    assertFalse(down.isConsumed());

    dispatch(b, node);
    assertEquals(2, counter.get());
    assertFalse(b.isConsumed());

    dispatch(left, node);
    assertEquals(3, counter.get());
    assertFalse(left.isConsumed());

    dispatch(right, node);
    assertEquals(4, counter.get());
    assertFalse(right.isConsumed());
  }

  @Test
  void testIfProceeded() {
    var counter = new SimpleIntegerProperty(0);

    var returnVal = InputHandler.Result.PROCEED;

    var baseIMT = InputMapTemplate.<Node, KeyEvent>sequence(
      consume(keyPressed(A)),
      consume(keyPressed(B)),
      process(keyPressed(UP), (n, e) -> returnVal),
      process(keyPressed(DOWN), (n, e) -> returnVal),
      process(keyPressed(LEFT), (n, e) -> returnVal),
      process(keyPressed(RIGHT), (n, e) -> returnVal)
    );

    var imtPP = baseIMT.ifProcessed((n, e) -> counter.set(counter.get() + 1));

    var node = new Region();
    InputMapTemplate.installFallback(imtPP, node);

    var a = new KeyEvent(KEY_PRESSED, "", "", A, false, false, false, false);
    var up = new KeyEvent(KEY_PRESSED, "", "", UP, false, false, false, false);
    var down = new KeyEvent(KEY_PRESSED, "", "", DOWN, false, false, false, false);
    var b = new KeyEvent(KEY_PRESSED, "", "", B, false, false, false, false);
    var left = new KeyEvent(KEY_PRESSED, "", "", LEFT, false, false, false, false);
    var right = new KeyEvent(KEY_PRESSED, "", "", RIGHT, false, false, false, false);

    dispatch(a, node);
    assertEquals(0, counter.get());
    assertTrue(a.isConsumed());

    dispatch(up, node);
    assertEquals(1, counter.get());
    assertFalse(up.isConsumed());

    dispatch(down, node);
    assertEquals(2, counter.get());
    assertFalse(down.isConsumed());

    dispatch(b, node);
    assertEquals(2, counter.get());
    assertTrue(b.isConsumed());

    dispatch(left, node);
    assertEquals(3, counter.get());
    assertFalse(left.isConsumed());

    dispatch(right, node);
    assertEquals(4, counter.get());
    assertFalse(right.isConsumed());
  }

}
