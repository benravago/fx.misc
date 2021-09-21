package fx.react.util;

interface IllegalAccumulation<T, A> extends AccumulationFacility<T, A> {

  @Override
  default A reduce(A accum, T value) {
    throw new IllegalStateException();
  }

}