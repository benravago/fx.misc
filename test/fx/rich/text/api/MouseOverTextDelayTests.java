package fx.rich.text.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;

import fx.rich.text.event.MouseOverTextEvent;

import java.time.Duration;

import fx.text.junit.TestCase;

class MouseOverTextDelayTests extends TestCase {

  SimpleBooleanProperty beginFired = new SimpleBooleanProperty();
  SimpleBooleanProperty endFired = new SimpleBooleanProperty();

  void resetBegin() {
    beginFired.set(false);
  }

  void resetEnd() {
    endFired.set(false);
  }

  @BeforeEach
  void start() {
    r.interact(() -> {
      area.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> beginFired.set(true));
      area.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, e -> endFired.set(true));

      area.replaceText("a long line of some example text");

      resetBegin();
      resetEnd();

      // insure mouse is off of area
      moveMouseOutsideOfArea();
    });
  }

  void moveMouseOutsideOfArea() {
    r.root(Pos.TOP_LEFT).moveTo(-20,-20); /// moveTo(point(scene).atPosition(Pos.TOP_LEFT).atOffset(-20, -20));
  }

  void setDelay(Duration delay) {
    r.interact(() -> area.setMouseOverTextDelay(delay));
  }

  void setDelay(long milliseconds) {
    r.interact(() -> setDelay(Duration.ofMillis(milliseconds)) );
  }

  @Test
  void null_delay_never_fires() {
    setDelay(null);

    toFirstLine().sleep(300); /// moveTo(firstLineOfArea()).sleep(300);
    assertFalse(beginFired.get());
    assertFalse(endFired.get());
  }

  @Disabled("END events are fired multiple times when BEGIN event hasn't yet fired")
  @Test
  void events_fire_after_delay_and_post_move() {
    setDelay(100);

    toFirstLine().sleep(300); /// moveTo(firstLineOfArea()).sleep(300);
    assertTrue(beginFired.get());
    assertFalse(endFired.get()); // fails here

    resetBegin();

    r.moveTo(20, 0); /// moveBy(20, 0);
    assertFalse(beginFired.get());
    assertTrue(endFired.get());
  }

  @Disabled("setting delay while mouse is over text fires END event when BEGIN event hasn't yet fired")
  @Test
  void setting_delay_while_mouse_is_over_text_does_not_fire_event() {
    setDelay(null);

    toFirstLine().sleep(300); /// moveTo(firstLineOfArea()).sleep(300);
    assertFalse(beginFired.get());
    assertFalse(endFired.get());

    setDelay(100);
    assertFalse(beginFired.get());
    assertFalse(endFired.get());

    r.moveTo(20, 0); /// moveBy(20, 0);
    assertTrue(beginFired.get()); // fails here
    assertFalse(endFired.get());
  }

  @Disabled("this test is only important when above two tests get fixed")
  @Test
  void setting_delay_before_end_fires_prevents_end_from_firing() {
    setDelay(100);

    toFirstLine().sleep(200); /// moveTo(firstLineOfArea()).sleep(200);
    assertTrue(beginFired.get());
    assertFalse(endFired.get());

    resetBegin();
    setDelay(null);

    moveMouseOutsideOfArea();
    assertFalse(beginFired.get());
    assertFalse(endFired.get());

    setDelay(100);
    assertFalse(beginFired.get());
    assertFalse(endFired.get());

    toFirstLine().sleep(300); /// moveTo(firstLineOfArea()).sleep(300);
    assertTrue(beginFired.get());
    assertFalse(endFired.get());

    resetBegin();

    r.moveTo(20, 0); /// moveBy(20, 0);
    assertFalse(beginFired.get());
    assertTrue(endFired.get());
  }

}