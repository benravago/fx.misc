package fx.input.template;

import java.util.Arrays;

import javafx.event.Event;

class TemplateChain<S, E extends Event> extends InputMapTemplate<S, E> {
  final InputMapTemplate<S, ? extends E>[] templates;

  @SafeVarargs
  TemplateChain(InputMapTemplate<S, ? extends E>... templates) {
    this.templates = templates;
  }

  @Override
  protected InputHandlerTemplateMap<S, E> getInputHandlerTemplateMap() {
    InputHandlerTemplateMap<S, E> ihtm = new InputHandlerTemplateMap<>();
    for (InputMapTemplate<S, ? extends E> imt : templates) {
      imt.getInputHandlerTemplateMap().forEach(ihtm::insertAfter);
    }
    return ihtm;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof TemplateChain) {
      TemplateChain<?, ?> that = (TemplateChain<?, ?>) other;
      return Arrays.equals(this.templates, that.templates);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(templates);
  }
}