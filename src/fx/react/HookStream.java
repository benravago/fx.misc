package fx.react;

import java.util.function.Consumer;

class HookStream<T> extends EventStreamBase<T> {

  final EventStream<T> source;
  final Consumer<? super T> sideEffect;
  boolean sideEffectInProgress = false;

  HookStream(EventStream<T> source, Consumer<? super T> sideEffect) {
    this.source = source;
    this.sideEffect = sideEffect;
  }

  @Override
  protected Subscription observeInputs() {
    return source.subscribe(t -> {
      if (sideEffectInProgress) {
        throw new IllegalStateException("Side effect is not allowed to cause recursive event emission");
      }
      sideEffectInProgress = true;
      try {
        sideEffect.accept(t);
      } finally {
        sideEffectInProgress = false;
      }
      emit(t);
    });
  }

}
