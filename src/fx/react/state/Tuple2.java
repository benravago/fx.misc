package fx.react.state;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class Tuple2<A, B> {
  public final A _1;
  public final B _2;

  public Tuple2(A a, B b) {
    _1 = a;
    _2 = b;
  }

  public A get1() { return _1; }
  public B get2() { return _2; }

  public <T> T map(BiFunction<? super A, ? super B, ? extends T> f) {
    return f.apply(_1, _2);
  }
  public void exec(BiConsumer<? super A, ? super B> f) {
    f.accept(_1, _2);
  }
}
