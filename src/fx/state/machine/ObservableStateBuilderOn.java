package fx.state.machine;

import java.util.Optional;
import java.util.function.BiFunction;

public interface ObservableStateBuilderOn<S, I> {

  ObservableStateBuilder<S> transition(BiFunction<? super S, ? super I, ? extends S> f);

  <O> StatefulStreamBuilder<S, O> emit(BiFunction<? super S, ? super I, Optional<O>> f);

  <O> StatefulStreamBuilder<S, O> transmit(BiFunction<? super S, ? super I, Transmission<S, Optional<O>>> f);

}
