package fx.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import javafx.event.Event;
import javafx.event.EventType;

import fx.input.InputHandler.Result;
import fx.input.InputMap.HandlerConsumer;
import fx.input.tree.Ops;
import fx.input.tree.PrefixTree;

class InputHandlerMap<E extends Event> {

  final BiFunction<InputHandler<? super E>, InputHandler<? super E>, InputHandler<E>> SEQ =
    (h1, h2) -> evt -> {
      return switch (h1.process(evt)) {
        case PROCEED -> h2.process(evt);
        case CONSUME -> Result.CONSUME;
        case IGNORE -> Result.IGNORE;
        default -> { throw new AssertionError("unreachable code"); }
      };
    };

  final Ops<EventType<? extends E>, InputHandler<? super E>> OPS =
    new Ops<EventType<? extends E>, InputHandler<? super E>>() {

      @Override
      public boolean isPrefixOf(EventType<? extends E> t1, EventType<? extends E> t2) {
        EventType<?> t = t2;
        while (t != null) {
          if (t.equals(t1)) {
            return true;
          } else {
            t = t.getSuperType();
          }
        }
        return false;
      }

      @Override
      public EventType<? extends E> commonPrefix(EventType<? extends E> t1, EventType<? extends E> t2) {
        var i1 = toList(t1).iterator();
        var i2 = toList(t2).iterator();
        EventType<?> common = null;
        while (i1.hasNext() && i2.hasNext()) {
          var c1 = i1.next();
          var c2 = i2.next();
          if (Objects.equals(c1, c2)) {
            common = c1;
          }
        }
        @SuppressWarnings("unchecked")
        var e = (EventType<? extends E>) common;
        return e;
      }

      @Override
      public InputHandler<? super E> promote(InputHandler<? super E> h, EventType<? extends E> subTpe, EventType<? extends E> supTpe) {
        if (Objects.equals(subTpe, supTpe)) {
          return h;
        }
        return evt -> {
          @SuppressWarnings("unchecked")
          var e = (EventType<? extends E>) evt.getEventType();
          return (isPrefixOf(subTpe, e)) ? h.process(evt) : Result.PROCEED;
        };
      }
      @Override
      public InputHandler<E> squash(InputHandler<? super E> v1, InputHandler<? super E> v2) {
        return SEQ.apply(v1, v2);
      }
    };

  static final List<EventType<?>> toList(EventType<?> t) {
    var l = new ArrayList<EventType<?>>();
    while (t != null) {
      l.add(t);
      t = t.getSuperType();
    }
    Collections.reverse(l);
    return l;
  }

  PrefixTree<EventType<? extends E>, InputHandler<? super E>> handlerTree = PrefixTree.empty(OPS);

  public <F extends E> void insertAfter(EventType<? extends F> t, InputHandler<? super F> h) {
    @SuppressWarnings("unchecked")
    var handler = (InputHandler<? super E>) h;
    handlerTree = handlerTree.insert(t, handler, SEQ);
  }

  void forEach(HandlerConsumer<? super E> f) {
    handlerTree.entries().forEach(th -> f.accept(th.getKey(), th.getValue()));
  }

}
