package fx.react;

/**
 * {@link EventStream#emitBothOnEach(EventStream)}
 */
class EmitBothOnEachStream<A, I> extends EventStreamBase<EventStreams.Di<A, I>> {

  final EventStream<A> source;
  final EventStream<I> impulse;

  boolean hasValue = false;
  A a = null;

  EmitBothOnEachStream(EventStream<A> source, EventStream<I> impulse) {
    this.source = source;
    this.impulse = impulse;
  }

  @Override
  protected Subscription observeInputs() {
    var s1 = source.subscribe(a -> {
      hasValue = true;
      this.a = a;
    });
    var s2 = impulse.subscribe(i -> {
      if (hasValue) {
        emit(new EventStreams.Di<>(a, i));
      }
    });
    return s1.and(s2);
  }

}
