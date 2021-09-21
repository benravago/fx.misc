package fx.react;

import java.util.function.Function;

import javafx.concurrent.Task;

class MappedToTaskStream<T, U> extends MappedStream<T, Task<U>> implements TaskStream<U> {

  MappedToTaskStream(EventStream<T> input, Function<? super T, Task<U>> f) {
    super(input, f);
  }

}
