package fx.react.util;

public interface AccumulationFacility<T, A> {

  A initialAccumulator(T value);

  A reduce(A accum, T value);

}
