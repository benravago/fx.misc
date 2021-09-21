package fx.react;

import fx.react.util.NotificationAccumulator;

/**
 * See {@link EventStream#forgetful()}
 */
class ForgetfulEventStream<T> extends ReducibleEventStreamBase<T> {

  ForgetfulEventStream(EventStream<T> source) {
    super(source, NotificationAccumulator.retainLatestStreamNotifications());
  }

}