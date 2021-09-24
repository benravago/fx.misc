package fx.rich.text;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import org.junit.jupiter.api.Test;

import javafx.event.EventTarget;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import fx.text.junit.TestCase;

// This class requires to be in this package, as it requires access to GenericStyledAreaBehavior.
class AcceleratorsTests extends TestCase {

  @Test
  void typing_alt_control_combinations_dont_consume_events_if_they_dont_have_any_character_assigned_w() {
    assumeTrue(isWindows());
    eval(
      // CHARACTER WITHOUT MODIFIERS
      new Ev(area, "f", KeyCode.F, false, false, true),
      // CHARACTER WITH CONTROL
      new Ev(area, "\0", KeyCode.F, true, false, false),
      // CHARACTER WITH ALT
      new Ev(area, "\0", KeyCode.F, false, true, false),
      // CHARACTER WITH ALT + CONTROL / ALTGR on Windows
      new Ev(area, "\0", KeyCode.K, true, true, false),
      //ALT + CONTROL / ALTGR on Windows with an assigned special character (E -> Euro on spanish keyboard)
      new Ev(area, "\u20AC", KeyCode.E, true, true, true)
    );
  }

  @Test
  void typing_alt_control_combinations_dont_consume_events_if_they_dont_have_any_character_assigned_l() {
    assumeTrue(isLinux());
    eval(
      // CHARACTER WITHOUT MODIFIERS
      new Ev(area, "f", KeyCode.F, false, false, true),
      // CHARACTER WITH CONTROL
      new Ev(area, "\0", KeyCode.F, true, false, false),
      // CHARACTER WITH ALT
      /* new Ev(area, "\0", KeyCode.F, false, true, false), */ // TODO: review this
      //CHARACTER WITH ALT + CONTROL
      new Ev(area, "\0", KeyCode.F, true, true, false)
    );
  }

  void eval(Ev... events) {
    for (int i = 0; i < events.length; i++) {
      var helper = events[i];
      assertEquals(
        helper.expectedConsumeResult,
        GenericStyledAreaBehavior.isControlKeyEvent(helper.keyEvent),
        "Event " + i + " unexpected result.");
    }
  }

  // Small helper class. Allows to make tests faster.
  class Ev {
    KeyEvent keyEvent;
    boolean expectedConsumeResult;
    Ev(EventTarget source, String character, KeyCode key, boolean controlDown, boolean altDown, boolean expected) {
      keyEvent = new KeyEvent(source, source, KeyEvent.KEY_TYPED, character, key.getName(), key, false, controlDown, altDown, false);
      expectedConsumeResult = expected;
    }
  }

}
