package fx.react.util;

import java.util.function.BinaryOperator;

class ReducingStreamNotifications<T> extends ReducingStreamNotificationsBase<T> {

  final BinaryOperator<T> reduction;

  ReducingStreamNotifications(BinaryOperator<T> reduction) {
    this.reduction = reduction;
  }

  @Override
  public T reduce(T accum, T value) {
    return reduction.apply(accum, value);
  }

}