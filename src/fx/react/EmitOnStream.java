package fx.react;

/**
 * @see fx.react.EventStream#emitOn(EventStream)
 */
class EmitOnStream<T> extends EventStreamBase<T> {

  final EventStream<T> source;
  final EventStream<?> impulse;

  boolean hasValue = false;
  T value = null;

  EmitOnStream(EventStream<T> source, EventStream<?> impulse) {
    this.source = source;
    this.impulse = impulse;
  }

  @Override
  protected Subscription observeInputs() {
    var s1 = source.subscribe(v -> {
      hasValue = true;
      value = v;
    });
    var s2 = impulse.subscribe(i -> {
      if (hasValue) {
        T val = value;
        hasValue = false;
        value = null;
        emit(val);
      }
    });
    return s1.and(s2);
  }

}