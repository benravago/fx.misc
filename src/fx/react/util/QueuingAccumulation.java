package fx.react.util;

import java.util.Deque;
import java.util.LinkedList;

interface QueuingAccumulation<T> extends AccumulationFacility<T, Deque<T>> {

  @Override
  default Deque<T> initialAccumulator(T value) {
    var res = new LinkedList<T>();
    res.add(value);
    return res;
  }

  @Override
  default Deque<T> reduce(Deque<T> accum, T value) {
    accum.addLast(value);
    return accum;
  }

}