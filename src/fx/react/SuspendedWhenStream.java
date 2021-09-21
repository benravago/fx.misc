package fx.react;

import javafx.beans.value.ObservableValue;

class SuspendedWhenStream<T> extends EventStreamBase<T> {

  final SuspendableEventStream<T> source;
  final ObservableValue<Boolean> condition;

  SuspendedWhenStream(SuspendableEventStream<T> source, ObservableValue<Boolean> condition) {
    this.source = source;
    this.condition = condition;
  }

  @Override
  protected Subscription observeInputs() {
    var s1 = source.suspendWhen(condition);
    var s2 = source.subscribe(this::emit);
    return s1.and(s2);
  }

}
