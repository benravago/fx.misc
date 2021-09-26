package fx.state.machine;

import java.util.Optional;
import java.util.function.BiFunction;

public interface StatefulStreamBuilderOn<S, O, I> {

  StatefulStreamBuilder<S, O> transition(BiFunction<? super S, ? super I, ? extends S> f);

  StatefulStreamBuilder<S, O> emit(BiFunction<? super S, ? super I, Optional<O>> f);

  StatefulStreamBuilder<S, O> transmit(BiFunction<? super S, ? super I, Transmission<S, Optional<O>>> f);

}
