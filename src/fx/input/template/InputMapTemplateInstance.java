package fx.input.template;

import java.util.Objects;

import fx.input.InputMap;
import javafx.event.Event;

class InputMapTemplateInstance<S, E extends Event> implements InputMap<E> {

  final InputMapTemplate<S, E> template;
  final S target;

  InputMapTemplateInstance(InputMapTemplate<S, E> template, S target) {
    this.template = template;
    this.target = target;
  }

  @Override
  public void forEachEventType(HandlerConsumer<? super E> hc) {
    template.forEachEventType(InputMapTemplate.HandlerTemplateConsumer.from(hc, target));
  }

  @Override
  public boolean equals(Object other) {
    return (other instanceof InputMapTemplateInstance<?,?> that)
      ? Objects.equals(this.template, that.template) && Objects.equals(this.target, that.target)
      : false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(template, target);
  }

}