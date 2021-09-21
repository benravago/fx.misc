package fx.react;

import java.util.function.Consumer;

final class NeverStream<T> extends RigidObservable<Consumer<? super T>> implements EventStream<T> {

  private NeverStream() {}
  private static class Singleton {
    static final NeverStream<?> INSTANCE = new NeverStream<>();
  }
  static NeverStream<?> instance() {
    return Singleton.INSTANCE;
  }

}