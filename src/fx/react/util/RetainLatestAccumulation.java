package fx.react.util;

interface RetainLatestAccumulation<T> extends HomotypicAccumulation<T> {

  @Override
  default T reduce(T accum, T value) {
    return value;
  }

}