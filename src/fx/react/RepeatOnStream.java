package fx.react;

/**
 * See {@link EventStream#repeatOn(EventStream)}
 */
class RepeatOnStream<T> extends EventStreamBase<T> {

  final EventStream<T> source;
  final EventStream<?> impulse;

  boolean hasValue = false;
  T value = null;

  RepeatOnStream(EventStream<T> source, EventStream<?> impulse) {
    this.source = source;
    this.impulse = impulse;
  }

  @Override
  protected Subscription observeInputs() {
    var s1 = source.subscribe(v -> {
      hasValue = true;
      value = v;
      emit(v);
    });
    var s2 = impulse.subscribe(i -> {
      if (hasValue) {
        emit(value);
      }
    });
    return s1.and(s2);
  }

}
