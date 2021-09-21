package fx.react.util;

interface RetainOldestAccumulation<T> extends HomotypicAccumulation<T> {

  @Override
  default T reduce(T accum, T value) {
    return accum;
  }

}