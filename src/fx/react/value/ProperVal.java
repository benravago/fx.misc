package fx.react.value;

import java.util.function.Consumer;

import fx.react.ProperObservable;
import fx.react.util.NotificationAccumulator;

public interface ProperVal<T> extends Val<T>, ProperObservable<Consumer<? super T>, T> {

  @Override
  default NotificationAccumulator<Consumer<? super T>, T, ?> defaultNotificationAccumulator() {
    return NotificationAccumulator.retainOldestValNotifications();
  }

}
