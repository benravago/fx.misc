package fx.react.collection;

import java.util.function.BinaryOperator;

import javafx.collections.ObservableList;

import fx.react.Subscription;
import fx.react.value.ValBase;
import fx.util.tree.FingerTree;
import fx.util.tree.ToSemigroup;

class ListReduction<T> extends ValBase<T> {

  final ObservableList<T> input;
  final BinaryOperator<T> reduction;
  final ToSemigroup<T, T> monoid;

  FingerTree<T, T> tree = null;

  ListReduction(ObservableList<T> input, BinaryOperator<T> reduction) {
    this.input = input;
    this.reduction = reduction;

    monoid = new ToSemigroup<T, T>() {
      @Override
      public T apply(T t) {
        return t;
      }
      @Override
      public T reduce(T left, T right) {
        return reduction.apply(left, right);
      }
    };
  }

  @Override
  protected Subscription connect() {
    assert tree == null;
    tree = FingerTree.mkTree(input, monoid);
    return LiveList
      .observeChanges(input, ch -> {
        for (var mod : ch) {
          FingerTree<T, T> left = tree.split(mod.getFrom()).a();
          FingerTree<T, T> right = tree.split(mod.getFrom() + mod.getRemovedSize()).b();
          FingerTree<T, T> middle = FingerTree.mkTree(mod.getAddedSubList(), monoid);
          tree = left.join(middle).join(right);
        }
        invalidate();
      })
      .and(() -> tree = null);
  }

  protected int getFrom(int max) {
    return 0;
  }

  protected int getTo(int max) {
    return max;
  }

  @Override
  protected final T computeValue() {
    if (isObservingInputs()) {
      assert tree != null;
      var max = tree.getLeafCount();
      return tree.getSummaryBetween(getFrom(max), getTo(max)).orElse(null);
    } else {
      assert tree == null;
      var max = input.size();
      return input.subList(getFrom(max), getTo(max)).stream().reduce(reduction).orElse(null);
    }
  }

}