package fx.input;

import java.util.Objects;
import java.util.function.Function;

import fx.input.InputHandler.Result;
import javafx.event.Event;

class PatternActionMap<T extends Event, U extends T> implements InputMap<U> {

  static final Function<Object, Result> CONST_IGNORE = x -> Result.IGNORE;

  final EventPattern<T, ? extends U> pattern;
  final Function<? super U, InputHandler.Result> action;

  PatternActionMap(EventPattern<T, ? extends U> pattern, Function<? super U, InputHandler.Result> action) {
    this.pattern = pattern;
    this.action = action;
  }

  @Override
  public void forEachEventType(HandlerConsumer<? super U> f) {
    InputHandler<T> h = t -> pattern.match(t).map(action::apply).orElse(Result.PROCEED);
    pattern.getEventTypes().forEach(et -> f.accept(et, h));
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof PatternActionMap) {
      var that = (PatternActionMap<?, ?>) other;
      return Objects.equals(this.pattern, that.pattern) && Objects.equals(this.action, that.action);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(pattern, action);
  }

}