package fx.react.util;

interface HomotypicAccumulation<T> extends AccumulationFacility<T, T> {

  @Override
  default T initialAccumulator(T value) {
    return value;
  }

}