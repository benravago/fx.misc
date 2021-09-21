package fx.input.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javafx.event.Event;
import javafx.event.EventType;

import fx.input.InputHandler.Result;
import fx.input.template.InputMapTemplate.HandlerTemplateConsumer;
import fx.input.tree.Ops;
import fx.input.tree.PrefixTree;

class InputHandlerTemplateMap<S, E extends Event> {

  static <S, E extends Event> InputHandlerTemplate<S, E> sequence(InputHandlerTemplate<S, ? super E> h1, InputHandlerTemplate<S, ? super E> h2) {
    return (s, evt) ->
      switch (h1.process(s, evt)) {
        case PROCEED -> h2.process(s, evt);
        case CONSUME -> Result.CONSUME;
        case IGNORE -> Result.IGNORE;
        default -> { throw new AssertionError("unreachable code"); }
      };
  }

  static <S, E extends Event> Ops<EventType<? extends E>, InputHandlerTemplate<S, ? super E>> ops() {
    return new Ops<EventType<? extends E>, InputHandlerTemplate<S, ? super E>>() {
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
        var c = (EventType<? extends E>) common;
        return c;
      }

      @Override
      public InputHandlerTemplate<S, ? super E> promote(InputHandlerTemplate<S, ? super E> h, EventType<? extends E> subTpe, EventType<? extends E> supTpe) {
        if (Objects.equals(subTpe, supTpe)) {
          return h;
        }
        return (s, evt) -> {
          @SuppressWarnings("unchecked")
          var e = (EventType<? extends E>) evt.getEventType();
          return (isPrefixOf(subTpe, e)) ? h.process(s, evt) : Result.PROCEED;
        };
      }

      @Override
      public InputHandlerTemplate<S, E> squash(InputHandlerTemplate<S, ? super E> v1, InputHandlerTemplate<S, ? super E> v2) {
        return sequence(v1, v2);
      }
    };
  }

  static final List<EventType<?>> toList(EventType<?> t) {
    var l = new ArrayList<EventType<?>>();
    while (t != null) {
      l.add(t);
      t = t.getSuperType();
    }
    Collections.reverse(l);
    return l;
  }

  PrefixTree<EventType<? extends E>, InputHandlerTemplate<S, ? super E>> handlerTree;

  public InputHandlerTemplateMap() {
    this(PrefixTree.empty(ops()));
  }

  InputHandlerTemplateMap(PrefixTree<EventType<? extends E>, InputHandlerTemplate<S, ? super E>> handlerTree) {
    this.handlerTree = handlerTree;
  }

  public <F extends E> void insertAfter(EventType<? extends F> t, InputHandlerTemplate<S, ? super F> h) {
    @SuppressWarnings("unchecked")
    var handler = (InputHandlerTemplate<S, ? super E>) h;
    handlerTree = handlerTree.insert(t, handler, (h1, h2) -> sequence(h1, h2));
  }

  public <T> InputHandlerTemplateMap<T, E> map(Function<? super InputHandlerTemplate<S, ? super E>, ? extends InputHandlerTemplate<T, E>> f) {
    return new InputHandlerTemplateMap<>(handlerTree.map(f, ops()));
  }

  void forEach(HandlerTemplateConsumer<S, ? super E> f) {
    handlerTree.entries().forEach(th -> f.accept(th.getKey(), th.getValue()));
  }

}