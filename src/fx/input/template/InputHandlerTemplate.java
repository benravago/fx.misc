package fx.input.template;

import fx.input.InputHandler.Result;
import javafx.event.Event;

/**
 * Template version of {@link fx.input.InputHandler}.
 *
 * @param <S> the type of the object that will be passed into the {@link InputHandlerTemplate}'s block of code.
 * @param <E> the event type for which this InputMap's {@link fx.input.EventPattern} matches
 */
@FunctionalInterface
public interface InputHandlerTemplate<S, E extends Event> {

  Result process(S state, E event);

}