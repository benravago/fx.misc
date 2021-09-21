package fx.react;

import fx.util.Dictionaries;
import fx.util.Dictionary;

public class ConnectableEventSource<T> extends EventStreamBase<T> implements ConnectableEventStream<T>, ConnectableEventSink<T> {

  Dictionary<EventStream<? extends T>, Subscription> subscriptions = null;

  @Override
  public void push(T value) {
    emit(value);
  }

  @Override
  public Subscription connectTo(EventStream<? extends T> input) {
    if (Dictionaries.containsKey(subscriptions, input)) {
      throw new IllegalStateException("Already connected to event stream " + input);
    }
    var sub = isObservingInputs() ? subscribeToInput(input) : null;
    subscriptions = Dictionaries.put(subscriptions, input, sub);
    return () -> {
      var s = Dictionaries.get(subscriptions, input);
      subscriptions = Dictionaries.remove(subscriptions, input);
      if (s != null) {
        s.unsubscribe();
      }
    };
  }

  @Override
  protected Subscription observeInputs() {
    Dictionaries.replaceAll(subscriptions, (input, sub) -> subscribeToInput(input));
    return () -> Dictionaries.replaceAll(subscriptions, (input, sub) -> {
      sub.unsubscribe();
      return null;
    });
  }

  Subscription subscribeToInput(EventStream<? extends T> input) {
    return input.subscribe(this::push);
  }

}
