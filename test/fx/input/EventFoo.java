package fx.input;

import static fx.input.EventPattern.eventType;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;

import fx.input.InputMap.HandlerConsumer;

public class EventFoo extends Event {

  static final EventType<EventFoo> FOO = new EventType<>("FOO");

  final boolean secret;
  final String value;

  EventFoo(boolean secret, String value) {
    super(FOO);
    this.secret = secret;
    this.value = value;
  }

  boolean isSecret() {
    return secret;
  }

  String getValue() {
    return value;
  }

  static void dispatch(Event event, InputMap<?> inputMap) {
    var matchingHandlers = new SimpleIntegerProperty(0);
    inputMap.forEachEventType(new HandlerConsumer<Event>() {
      @Override
      public <E extends Event> void accept(EventType<? extends E> t, InputHandler<? super E> h) {
        eventType(t).match(event).ifPresent(evt -> {
          h.handle(evt);
          matchingHandlers.set(matchingHandlers.get() + 1);
        });
      }
    });

    assertTrue(matchingHandlers.get() <= (1));
  }

  public static void dispatch(Event event, Node node) {
    dispatch(event, Nodes.getInputMap(node));
  }

  private static final long serialVersionUID = 1L;
}
