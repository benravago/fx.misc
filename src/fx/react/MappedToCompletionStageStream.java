package fx.react;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

class MappedToCompletionStageStream<T, U> extends MappedStream<T, CompletionStage<U>> implements CompletionStageStream<U> {

  MappedToCompletionStageStream(EventStream<T> input, Function<? super T, CompletionStage<U>> f) {
    super(input, f);
  }

}
