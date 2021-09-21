package fx.input;

import java.util.Arrays;
import java.util.stream.Stream;

import javafx.event.Event;

class InputMapChain<E extends Event> implements InputMap<E> {

  final InputMap<? extends E>[] inputMaps;

  @SafeVarargs
  InputMapChain(InputMap<? extends E>... inputMaps) {
    this.inputMaps = inputMaps;
  }

  @Override
  public void forEachEventType(HandlerConsumer<? super E> f) {
    var ihm = new InputHandlerMap<E>();
    for (var im : inputMaps) {
      im.forEachEventType(ihm::insertAfter);
    }
    ihm.forEach(f);
  }

  @Override
  public InputMap<E> without(InputMap<?> that) {
    if (this.equals(that)) {
      return InputMap.empty();
    } else {
      @SuppressWarnings("unchecked")
      var ims = (InputMap<? extends E>[])
        Stream.of(inputMaps)
          .map(im -> im.without(that))
          .filter(im -> im != EMPTY)
          .toArray(n -> new InputMap<?>[n]);
      return switch (ims.length) {
        case 0 -> InputMap.empty();
        case 1 -> InputMap.upCast(ims[0]);
        default -> new InputMapChain<>(ims);
      };
    }
  }

  @Override
  public boolean equals(Object other) {
    return (other instanceof InputMapChain<?> that) ? Arrays.equals(this.inputMaps, that.inputMaps) : false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(inputMaps);
  }

}