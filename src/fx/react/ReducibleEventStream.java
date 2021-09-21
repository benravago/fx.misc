package fx.react;

import java.util.function.BinaryOperator;

import fx.react.util.NotificationAccumulator;

/**
 * See {@link EventStream#reducible(BinaryOperator)}
 */
class ReducibleEventStream<T> extends ReducibleEventStreamBase<T> {

  ReducibleEventStream(EventStream<T> source, BinaryOperator<T> reduction) {
    super(source, NotificationAccumulator.reducingStreamNotifications(reduction));
  }

}