package fx.react.value;

import java.util.function.Function;

import javafx.beans.value.ObservableValue;

class FlatMappedVal<T, U, O extends ObservableValue<U>> extends FlatMapped<T, U, O> {

  FlatMappedVal(ObservableValue<T> src, Function<? super T, O> f) {
    super(src, f);
  }

}