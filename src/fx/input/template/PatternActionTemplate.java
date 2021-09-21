package fx.input.template;

import java.util.Objects;
import java.util.function.BiFunction;

import fx.input.EventPattern;
import fx.input.InputHandler;
import fx.input.InputHandler.Result;
import javafx.event.Event;

class PatternActionTemplate<S, T extends Event, U extends T> extends InputMapTemplate<S, U> {

  static final BiFunction<Object, Object, Result> CONST_IGNORE = (x, y) -> Result.IGNORE;

  final EventPattern<T, ? extends U> pattern;
  final BiFunction<? super S, ? super U, InputHandler.Result> action;

  PatternActionTemplate(EventPattern<T, ? extends U> pattern, BiFunction<? super S, ? super U, InputHandler.Result> action) {
    this.pattern = pattern;
    this.action = action;
  }

  @Override
  protected InputHandlerTemplateMap<S, U> getInputHandlerTemplateMap() {
    var ihtm = new InputHandlerTemplateMap<S, U>();
    InputHandlerTemplate<S, T> iht = (s, t) -> pattern.match(t).map(u -> action.apply(s, u)).orElse(Result.PROCEED);
    pattern.getEventTypes().forEach(et -> ihtm.insertAfter(et, iht));
    return ihtm;
  }

  @Override
  public boolean equals(Object other) {
    return (other instanceof PatternActionTemplate<?,?,?> that)
      ? Objects.equals(this.pattern, that.pattern) && Objects.equals(this.action, that.action)
      : false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(pattern, action);
  }

}