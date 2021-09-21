package fx.input;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyEvent.*;

import static fx.input.EventPattern.*;
import static fx.input.InputHandler.Result.*;
import static fx.input.InputMap.*;
import static fx.input.EventFoo.*;

class InputMapTest {

  @Test
  void overridePreviouslyAddedHandler() {
    var res = new SimpleStringProperty();

    var im1 = consume(keyPressed(), e -> res.set("handler 1"));
    var im2 = consume(keyPressed(A), e -> res.set("handler 2"));

    var aPressed = new KeyEvent(KEY_PRESSED, "", "", A, false, false, false, false);
    var bPressed = new KeyEvent(KEY_PRESSED, "", "", B, false, false, false, false);

    var im = im1.orElse(im2);
    dispatch(aPressed, im);
    assertEquals("handler 1", res.get());
    dispatch(bPressed, im);
    assertEquals("handler 1", res.get());

    im = im2.orElse(im1);
    dispatch(aPressed, im);
    assertEquals("handler 2", res.get());
    dispatch(bPressed, im);
    assertEquals("handler 1", res.get());
  }

  @Test
  void fallbackHandlerTest() {
    var res = new SimpleStringProperty();

    var fallback = consume(keyPressed(), e -> res.set("fallback"));
    var custom = consume(keyPressed(A), e -> res.set("custom"));

    var aPressed = new KeyEvent(KEY_PRESSED, "", "", A, false, false, false, false);
    var bPressed = new KeyEvent(KEY_PRESSED, "", "", B, false, false, false, false);

    var node = new Region();

    // install custom handler first, then fallback
    Nodes.addInputMap(node, custom);
    Nodes.addFallbackInputMap(node, fallback);

    // check that custom handler is not overridden by fallback handler
    dispatch(aPressed, node);
    assertEquals("custom", res.get());

    // check that fallback handler is in effect
    dispatch(bPressed, node);
    assertEquals("fallback", res.get());
  }

  @Test
  void ignoreTest() {
    var res = new SimpleStringProperty();

    var fallback = consume(keyPressed(), e -> res.set("consumed"));
    var ignore = ignore(keyPressed(A));

    var aPressed = new KeyEvent(KEY_PRESSED, "", "", A, false, false, false, false);
    var bPressed = new KeyEvent(KEY_PRESSED, "", "", B, false, false, false, false);

    var node = new Region();

    // install ignore handler first, then fallback
    Nodes.addInputMap(node, ignore);
    Nodes.addFallbackInputMap(node, fallback);

    // check that ignore works
    dispatch(aPressed, node);
    assertNull(res.get());
    assertFalse(aPressed.isConsumed());

    // check that other events are not ignored
    dispatch(bPressed, node);
    assertEquals("consumed", res.get());
    assertTrue(bPressed.isConsumed());
  }

  @Test
  void withoutTest() {
    var res = new SimpleStringProperty();

    var im1 = consume(keyPressed(B), e -> { res.set("1"); });
    var im2 = consume(keyPressed(A), e -> { res.set("2"); });
    var im3 = consume(keyPressed(A), e -> { res.set("3"); });
    var im4 = process(keyPressed(A), e -> { res.set("4"); return PROCEED; });

    var event = new KeyEvent(KEY_PRESSED, "", "", A, false, false, false, false);

    var im = sequence(im1, im2, im3, im4);
    dispatch(event, im);
    assertEquals("2", res.get());

    im = im.without(im2);
    event = event.copyFor(null, null); // obtain unconsumed event
    dispatch(event, im);
    assertEquals("3", res.get());

    im = im.without(im3);
    event = event.copyFor(null, null); // obtain unconsumed event
    dispatch(event, im);
    assertEquals("4", res.get());
    assertFalse(event.isConsumed());
  }

  @Test
  void whenTest() {
    var condition = new SimpleBooleanProperty(false);

    var im = when(condition::get, consume(keyPressed()));

    var event = new KeyEvent(KEY_PRESSED, "", "", A, false, false, false, false);

    dispatch(event, im);
    assertFalse(event.isConsumed());

    condition.set(true);
    dispatch(event, im);
    assertTrue(event.isConsumed());
  }

  @Test
  void removePreviousHandlerTest() {
    var res = new SimpleStringProperty();

    var fallback = consume(keyPressed(), e -> res.set("fallback"));
    var custom = consume(keyPressed(A), e -> res.set("custom"));

    var aPressed = new KeyEvent(KEY_PRESSED, "", "", A, false, false, false, false);
    var bPressed = new KeyEvent(KEY_PRESSED, "", "", B, false, false, false, false);

    var node = new Region();

    // install custom handler first, then fallback
    Nodes.addInputMap(node, custom);
    Nodes.addFallbackInputMap(node, fallback);

    // check that fallback handler works
    dispatch(bPressed, node);
    assertEquals("fallback", res.get());

    // remove fallback handler
    Nodes.removeInputMap(node, fallback);
    res.set(null);

    // check that fallback handler was removed
    dispatch(bPressed, node);
    assertNull(res.get());

    // check that custom handler still works
    dispatch(aPressed, node);
    assertEquals("custom", res.get());
  }

  void moveUp() {}
  void moveDown() {}
  void moveLeft() {}
  void moveRight() {}
  void move(double x, double y) {}

  void justKidding() {
    var node = new Region();
    var im = sequence(
      consume(keyPressed(UP), e -> moveUp()),
      consume(keyPressed(DOWN), e -> moveDown()),
      consume(keyPressed(LEFT), e -> moveLeft()),
      consume(keyPressed(RIGHT), e -> moveRight()),
      consume(mouseMoved()
        .onlyIf(MouseEvent::isPrimaryButtonDown), e -> move(e.getX(), e.getY()))
      );
    Nodes.addFallbackInputMap(node, im);
    Nodes.removeInputMap(node, im);
  }

  @Test
  void customEventTest() {
    var res = new SimpleStringProperty();

    // if event is not secret, assign its value to res.
    // Otherwise, don't consume the event.
    var im = consume(eventType(EventFoo.FOO).unless(EventFoo::isSecret), e -> res.set(e.getValue()));

    var secret = new EventFoo(true, "Secret");
    var open = new EventFoo(false, "Open");

    var node = new Region();
    Nodes.addInputMap(node, im);

    // check that secret event is not processed or consumed
    dispatch(secret, node);
    assertNull(res.get());
    assertFalse(secret.isConsumed());

    // check that open event is processed and consumed
    dispatch(open, node);
    assertEquals("Open", res.get());
    assertTrue(open.isConsumed());
  }

  @Test
  void ifConsumedTest() {
    var res = new SimpleStringProperty();
    var counter = new SimpleIntegerProperty(0);

    var im = InputMap
      .sequence(
        consume(keyPressed(UP), e -> res.set("Up")),
        consume(keyPressed(DOWN), e -> res.set("Down")),
        consume(keyPressed(LEFT), e -> res.set("Left")),
        consume(keyPressed(RIGHT), e -> res.set("Right"))
        )
      .ifConsumed(e -> counter.set(counter.get() + 1));

    var a = new KeyEvent(KEY_PRESSED, "", "", A, false, false, false, false);
    var up = new KeyEvent(KEY_PRESSED, "", "", UP, false, false, false, false);
    var down = new KeyEvent(KEY_PRESSED, "", "", DOWN, false, false, false, false);
    var left = new KeyEvent(KEY_PRESSED, "", "", LEFT, false, false, false, false);
    var right = new KeyEvent(KEY_PRESSED, "", "", RIGHT, false, false, false, false);

    dispatch(a, im);
    assertNull(res.get());
    assertEquals(0, counter.get());
    assertFalse(a.isConsumed());

    dispatch(up, im);
    assertEquals("Up", res.get());
    assertEquals(1, counter.get());
    assertTrue(up.isConsumed());

    dispatch(down, im);
    assertEquals("Down", res.get());
    assertEquals(2, counter.get());
    assertTrue(down.isConsumed());

    dispatch(left, im);
    assertEquals("Left", res.get());
    assertEquals(3, counter.get());
    assertTrue(left.isConsumed());

    dispatch(right, im);
    assertEquals("Right", res.get());
    assertEquals(4, counter.get());
    assertTrue(right.isConsumed());
  }

  @Test
  void ifIgnoredTest() {
    var counter = new SimpleIntegerProperty(0);

    var im = InputMap
      .sequence(
         ignore(keyPressed(UP)),
         ignore(keyPressed(DOWN)),
         ignore(keyPressed(RIGHT)),
         ignore(keyPressed(LEFT))
         )
      .ifIgnored(e -> counter.set(counter.get() + 1));

    var a = new KeyEvent(KEY_PRESSED, "", "", A, false, false, false, false);
    var up = new KeyEvent(KEY_PRESSED, "", "", UP, false, false, false, false);
    var down = new KeyEvent(KEY_PRESSED, "", "", DOWN, false, false, false, false);
    var left = new KeyEvent(KEY_PRESSED, "", "", LEFT, false, false, false, false);
    var right = new KeyEvent(KEY_PRESSED, "", "", RIGHT, false, false, false, false);

    dispatch(a, im);
    assertEquals(0, counter.get());
    assertFalse(a.isConsumed());

    dispatch(up, im);
    assertEquals(1, counter.get());
    assertFalse(up.isConsumed());

    dispatch(down, im);
    assertEquals(2, counter.get());
    assertFalse(down.isConsumed());

    dispatch(left, im);
    assertEquals(3, counter.get());
    assertFalse(left.isConsumed());

    dispatch(right, im);
    assertEquals(4, counter.get());
    assertFalse(right.isConsumed());
  }

  @Test
  void ifProceededTest() {
    var res = new SimpleStringProperty();
    var counter = new SimpleIntegerProperty(0);

    InputHandler.Result returnVal = InputHandler.Result.PROCEED;

    var im = InputMap
      .sequence(
         consume(keyPressed(A)),
         process(keyPressed(UP), e -> { res.set("Up"); return returnVal; }),
         process(keyPressed(DOWN), e -> { res.set("Down"); return returnVal; }),
         process(keyPressed(LEFT), e -> { res.set("Left"); return returnVal; }),
         process(keyPressed(RIGHT), e -> { res.set("Right"); return returnVal; })
       )
      .ifProcessed(e -> counter.set(counter.get() + 1));

    var a = new KeyEvent(KEY_PRESSED, "", "", A, false, false, false, false);
    var up = new KeyEvent(KEY_PRESSED, "", "", UP, false, false, false, false);
    var down = new KeyEvent(KEY_PRESSED, "", "", DOWN, false, false, false, false);
    var left = new KeyEvent(KEY_PRESSED, "", "", LEFT, false, false, false, false);
    var right = new KeyEvent(KEY_PRESSED, "", "", RIGHT, false, false, false, false);

    dispatch(a, im);
    assertNull(res.get());
    assertEquals(0, counter.get());
    assertTrue(a.isConsumed());

    dispatch(up, im);
    assertEquals("Up", res.get());
    assertEquals(1, counter.get());
    assertFalse(up.isConsumed());

    dispatch(down, im);
    assertEquals("Down", res.get());
    assertEquals(2, counter.get());
    assertFalse(down.isConsumed());

    dispatch(left, im);
    assertEquals("Left", res.get());
    assertEquals(3, counter.get());
    assertFalse(left.isConsumed());

    dispatch(right, im);
    assertEquals("Right", res.get());
    assertEquals(4, counter.get());
    assertFalse(right.isConsumed());
  }

  @Test
  void pushAndPopInputMap() {
    var res = new SimpleStringProperty();

    var node = new Region();
    Nodes.addInputMap(node, InputMap.consume(keyPressed(UP), e -> res.set("Up")));
    Supplier<KeyEvent> createUpKeyEvent = () -> new KeyEvent(KEY_PRESSED, "", "", UP, false, false, false, false);

    // regular input map works
    var up = createUpKeyEvent.get();

    dispatch(up, node);
    assertEquals("Up", res.get());
    assertTrue(up.isConsumed());

    // temporary input map works
    Nodes.pushInputMap(node, InputMap.consume(keyPressed(UP), e -> res.set("Down")));
    up = createUpKeyEvent.get();

    dispatch(up, node);
    assertEquals("Down", res.get());
    assertTrue(up.isConsumed());

    // popping reinstalls previous input map
    Nodes.popInputMap(node);
    up = createUpKeyEvent.get();

    dispatch(up, node);
    assertEquals("Up", res.get());
    assertTrue(up.isConsumed());

    // popping when no temporary input maps exist does nothing
    Nodes.popInputMap(node);
    up = createUpKeyEvent.get();

    // set value to something else to insure test works as expected
    res.set("Other value");

    dispatch(up, node);
    assertEquals("Up", res.get());
    assertTrue(up.isConsumed());
  }

}
