package fx.react.collection;

import java.util.function.BinaryOperator;

import fx.react.Subscription;
import fx.react.value.Val;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexRange;

class ListReductionRange<T> extends ListReduction<T> {

  final ObservableValue<IndexRange> range;

  ListReductionRange(ObservableList<T> input, ObservableValue<IndexRange> range, BinaryOperator<T> reduction) {
    super(input, reduction);
    this.range = range;
  }

  @Override
  protected Subscription connect() {
    return super.connect().and(Val.observeInvalidations(range, obs -> invalidate()));
  }

  @Override
  protected int getFrom(int max) {
    return Math.min(range.getValue().getStart(), max);
  }

  @Override
  protected int getTo(int max) {
    return Math.min(range.getValue().getEnd(), max);
  }

}