package fx.util.tree;

import static fx.util.Either.left;
import static fx.util.Either.right;
import static fx.util.Sequence.cons;

import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

import fx.util.SubSequence;
import fx.util.Either;
import fx.util.Sequence;
import fx.util.Lists;

class Branch<T, S> extends NonEmpty<T, S> {

  final SubSequence<NonEmpty<T, S>> children;
  final int depth;
  final int leafCount;
  final S summary;

  Branch(SubSequence<NonEmpty<T, S>> children) {
    super(children.head().semigroup);
    assert children.size() == 2 || children.size() == 3;
    var head = children.head();
    var headDepth = head.getDepth();
    assert children.all(n -> n.getDepth() == headDepth);
    this.children = children;
    this.depth = 1 + headDepth;
    this.leafCount = children.fold(0, (s, n) -> s + n.getLeafCount());
    this.summary = children.mapReduce1(NonEmpty<T, S>::getSummary, semigroup::reduce);
  }

  @Override
  public String toString() {
    return "Branch" + children;
  }

  @Override
  public int getDepth() {
    return depth;
  }

  @Override
  public int getLeafCount() {
    return leafCount;
  }

  @Override
  public List<T> asList() {
    return subList0(0, leafCount);
  }

  /**
   * Complexity of calling once: O(log(n)).
   * Complexity of calling subList recursively on the resulting list,
   * i.e. {@code tree.asList().subList(...).subList(...). ... .subList(...)}
   * up to n times: O(n).
   * When the resulting list has size m, it may prevent up to m additional
   * elements of the original tree from being garbage collected.
   */
  List<T> subList(int from, int to) {
    var len = to - from;
    if (2 * len >= getLeafCount()) {
      return subList0(from, to);
    } else {
      FingerTree<T, S> tree = this;
      if (2 * (getLeafCount() - to) > len) {
        tree = tree.split(to).a();
      }
      if (2 * from > len) {
        tree = tree.split(from).b();
        to -= from;
        from = 0;
      }
      return tree.asList().subList(from, to);
    }
  }

  List<T> subList0(int from, int to) {
    return new AbstractList<T>() {

      @Override
      public T get(int index) {
        Lists.checkIndex(index, to - from);
        return getLeaf(from + index);
      }

      @Override
      public int size() {
        return to - from;
      }

      @Override
      public List<T> subList(int start, int end) {
        Lists.checkRange(start, end, to - from);
        return Branch.this.subList(from + start, from + end);
      }

      @Override
      public Iterator<T> iterator() {
        return listIterator(0);
      }

      /**
       * Iterates in both directions in time O(n),
       * with at most O(log(n)) allocations (as the stack expands).
       */
      @Override
      public ListIterator<T> listIterator(int pos) {
        var dd = 3; // maximum depth for which we directly call get(idx)
        Lists.checkPosition(pos, size());
        return new ListIterator<T>() {

          int position = from + pos; // position within this finger tree
          int topOffset = 0; // absolute offset of top of the stack relative to this finger tree

          Deque<NonEmpty<T, S>> stack = new ArrayDeque<>();
          /*<init>*/ {
            stack.push(Branch.this);
          }

          @Override
          public boolean hasNext() {
            return position < to;
          }
          @Override
          public boolean hasPrevious() {
            return position > from;
          }
          @Override
          public int nextIndex() {
            return position - from;
          }
          @Override
          public int previousIndex() {
            return position - from - 1;
          }
          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
          @Override
          public void set(T e) {
            throw new UnsupportedOperationException();
          }
          @Override
          public void add(T e) {
            throw new UnsupportedOperationException();
          }

          @Override
          public T next() {
            if (position == topOffset + stack.peek().getLeafCount()) {
              up();
              return next();
            } else if (stack.peek().getDepth() <= dd) {
              return stack.peek().getLeaf(position++ - topOffset);
            } else {
              downR();
              return next();
            }
          }

          @Override
          public T previous() {
            if (position == topOffset) {
              up();
              return previous();
            } else if (stack.peek().getDepth() <= dd) {
              return stack.peek().getLeaf(--position - topOffset);
            } else {
              downL();
              return previous();
            }
          }

          void up() {
            var child = stack.pop();
            var top = (Branch<T, S>) stack.peek();
            var chOffsetInParent = 0;
            Sequence<? extends NonEmpty<T, S>> children = top.children;
            while (children.head() != child) {
              chOffsetInParent += children.head().getLeafCount();
              children = children.tail();
            }
            topOffset -= chOffsetInParent;
          }

          void downR() {
            downR(((Branch<T, S>) stack.peek()).children);
          }

          void downR(Sequence<? extends NonEmpty<T, S>> children) {
            var head = children.head();
            if (position - topOffset < head.getLeafCount()) {
              stack.push(head);
            } else {
              topOffset += head.getLeafCount();
              downR(children.tail());
            }
          }

          private void downL() {
            downL(((Branch<T, S>) stack.peek()).children);
          }

          private void downL(Sequence<? extends NonEmpty<T, S>> children) {
            var head = children.head();
            if (position - topOffset <= head.getLeafCount()) {
              stack.push(head);
            } else {
              topOffset += head.getLeafCount();
              downL(children.tail());
            }
          }
        };
      }
    };
  }

  @Override
  T getData() {
    throw new UnsupportedOperationException("Only leaf nodes hold data");
  }

  @Override
  T getLeaf0(int index) {
    assert Lists.isValidIndex(index, getLeafCount());
    return getLeaf0(index, children);
  }

  T getLeaf0(int index, Sequence<? extends FingerTree<T, S>> nodes) {
    var head = nodes.head();
    var headSize = head.getLeafCount();
    return (index < headSize) ? head.getLeaf0(index) : getLeaf0(index - headSize, nodes.tail());
  }

  @Override
  NonEmpty<T, S> updateLeaf0(int index, T data) {
    assert Lists.isValidIndex(index, getLeafCount());
    return branch(updateLeaf0(index, data, children));
  }

  SubSequence<NonEmpty<T, S>> updateLeaf0(int index, T data, Sequence<? extends NonEmpty<T, S>> nodes) {
    var head = nodes.head();
    var headSize = head.getLeafCount();
    return (index < headSize)
      ? cons(head.updateLeaf0(index, data), nodes.tail())
      : cons(head, updateLeaf0(index - headSize, data, nodes.tail()));
  }

  @Override
  Index locateProgressively0(ToIntFunction<? super S> metric, int position) {
    assert Lists.isValidPosition(position, measure(metric));
    return locateProgressively0(metric, position, children);
  }

  Index locateProgressively0(ToIntFunction<? super S> metric, int position, Sequence<? extends NonEmpty<T, S>> nodes) {
    var head = nodes.head();
    var headLen = head.measure(metric);
    return (position < headLen || (position == headLen && nodes.tail().isEmpty()))
      ? head.locateProgressively0(metric, position)
      : locateProgressively0(metric, position - headLen, nodes.tail()).major(head.getLeafCount());
  }

  @Override
  Index locateRegressively0(ToIntFunction<? super S> metric, int position) {
    assert Lists.isValidPosition(position, measure(metric));
    return locateRegressively0(metric, position, children);
  }

  Index locateRegressively0(ToIntFunction<? super S> metric, int position, Sequence<? extends NonEmpty<T, S>> nodes) {
    var head = nodes.head();
    var headLen = head.measure(metric);
    return  (position <= headLen)
      ? head.locateRegressively0(metric, position)
      : locateRegressively0(metric, position - headLen, nodes.tail()).major(head.getLeafCount());
  }

  @Override
  public final <R> R fold(R acc, BiFunction<? super R, ? super T, ? extends R> reduction) {
    return children.fold(acc, (r, n) -> n.fold(r, reduction));
  }

  @Override
  final <R> R foldBetween0(R acc, BiFunction<? super R, ? super T, ? extends R> reduction, int startLeaf, int endLeaf) {
    assert Lists.isNonEmptyRange(startLeaf, endLeaf, getLeafCount());
    return foldBetween0(acc, reduction, startLeaf, endLeaf, children);
  }

  <R> R foldBetween0(R acc, BiFunction<? super R, ? super T, ? extends R> reduction, int startLeaf, int endLeaf, Sequence<? extends FingerTree<T, S>> nodes) {
    var head = nodes.head();
    var headSize = head.getLeafCount();
    var headTo = Math.min(endLeaf, headSize);
    var tailFrom = Math.max(startLeaf - headSize, 0);
    var tailTo = endLeaf - headSize;
    if (startLeaf < headTo) {
      acc = head.foldBetween0(acc, reduction, startLeaf, headTo);
    }
    if (tailFrom < tailTo) {
      acc = foldBetween0(acc, reduction, tailFrom, tailTo, nodes.tail());
    }
    return acc;
  }

  @Override
  <R> R foldBetween0(R acc, BiFunction<? super R, ? super T, ? extends R> reduction, ToIntFunction<? super S> metric, int startPosition, int endPosition, _4f<? super R, ? super T, Integer, Integer, ? extends R> rangeReduction) {
    assert Lists.isNonEmptyRange(startPosition, endPosition, measure(metric));
    return foldBetween0(acc, reduction, metric, startPosition, endPosition, rangeReduction, children);
  }

  <R> R foldBetween0(R acc, BiFunction<? super R, ? super T, ? extends R> reduction, ToIntFunction<? super S> metric, int startPosition, int endPosition, _4f<? super R, ? super T, Integer, Integer, ? extends R> rangeReduction, Sequence<? extends FingerTree<T, S>> nodes) {
    var head = nodes.head();
    var headLen = head.measure(metric);
    var headTo = Math.min(endPosition, headLen);
    var tailFrom = Math.max(startPosition - headLen, 0);
    var tailTo = endPosition - headLen;
    if (startPosition < headTo) {
      acc = head.foldBetween0(acc, reduction, metric, startPosition, headTo, rangeReduction);
    }
    if (tailFrom < tailTo) {
      acc = foldBetween0(acc, reduction, metric, tailFrom, tailTo, rangeReduction, nodes.tail());
    }
    return acc;
  }

  @Override
  public S getSummary() {
    return summary;
  }

  @Override
  public Optional<S> getSummaryOpt() {
    return Optional.of(summary);
  }

  @Override
  S getSummaryBetween0(int startLeaf, int endLeaf) {
    assert Lists.isNonEmptyRange(startLeaf, endLeaf, getLeafCount());
    return (startLeaf == 0 && endLeaf == getLeafCount())
      ? summary : getSummaryBetween0(startLeaf, endLeaf, children);
  }

  S getSummaryBetween0(int startLeaf, int endLeaf, Sequence<? extends FingerTree<T, S>> nodes) {
    var head = nodes.head();
    var headSize = head.getLeafCount();
    var headTo = Math.min(endLeaf, headSize);
    var tailFrom = Math.max(startLeaf - headSize, 0);
    var tailTo = endLeaf - headSize;
    if (startLeaf < headTo && tailFrom < tailTo) {
      return semigroup.reduce(head.getSummaryBetween0(startLeaf, headTo), getSummaryBetween0(tailFrom, tailTo, nodes.tail()));
    } else if (startLeaf < headTo) {
      return head.getSummaryBetween0(startLeaf, headTo);
    } else if (tailFrom < tailTo) {
      return getSummaryBetween0(tailFrom, tailTo, nodes.tail());
    } else {
      throw new AssertionError("Didn't expect empty range: " + "[" + startLeaf + ", " + endLeaf + ")");
    }
  }

  @Override
  S getSummaryBetween0(ToIntFunction<? super S> metric, int startPosition, int endPosition, _3f<? super T, Integer, Integer, ? extends S> subSummary) {
    var len = measure(metric);
    assert Lists.isNonEmptyRange(startPosition, endPosition, len);
    return (startPosition == 0 && endPosition == len)
      ? getSummary() : getSummaryBetween0(metric, startPosition, endPosition, subSummary, children);
  }

  S getSummaryBetween0(ToIntFunction<? super S> metric, int startPosition, int endPosition, _3f<? super T, Integer, Integer, ? extends S> subSummary, Sequence<? extends FingerTree<T, S>> nodes) {
    var head = nodes.head();
    var headLen = head.measure(metric);
    var headTo = Math.min(endPosition, headLen);
    var tailFrom = Math.max(startPosition - headLen, 0);
    var tailTo = endPosition - headLen;
    if (startPosition < headTo && tailFrom < tailTo) {
      return semigroup.reduce(head.getSummaryBetween0(metric, startPosition, headTo, subSummary), getSummaryBetween0(metric, tailFrom, tailTo, subSummary, nodes.tail()));
    } else if (startPosition < headTo) {
      return head.getSummaryBetween0(metric, startPosition, headTo, subSummary);
    } else if (tailFrom < tailTo) {
      return getSummaryBetween0(metric, tailFrom, tailTo, subSummary, nodes.tail());
    } else {
      throw new AssertionError("Didn't expect empty range: [" + startPosition + ", " + endPosition + ")");
    }
  }

  @Override
  Either<Branch<T, S>, _2t<NonEmpty<T, S>, NonEmpty<T, S>>> appendLte(FingerTree<T, S> suffix) {
    assert suffix.getDepth() <= this.getDepth();
    if (suffix.getDepth() == this.getDepth()) {
      return right(new _2t<>(this, (NonEmpty<T, S>) suffix));
    } else if (children.size() == 2) {
      return mapFirst2(children, (left, right) -> {
        return right.appendLte(suffix).unify(
          r -> left(branch(left, r)),
          mr -> left(mr.map((m, r) -> branch(left, m, r)))
        );
      });
    } else {
      assert children.size() == 3;
      return mapFirst3(children, (left, middle, right) -> {
        return right.appendLte(suffix)
          .mapLeft(r -> branch(left, middle, r))
          .mapRight(mr -> new _2t<>(branch(left, middle), mr.map(FingerTree::branch))
        );
      });
    }
  }

  @Override
  Either<Branch<T, S>, _2t<NonEmpty<T, S>, NonEmpty<T, S>>> prependLte(FingerTree<T, S> prefix) {
    assert prefix.getDepth() <= this.getDepth();
    if (prefix.getDepth() == this.getDepth()) {
      return right(new _2t<>((NonEmpty<T, S>) prefix, this));
    } else if (children.size() == 2) {
      return mapFirst2(children, (left, right) -> {
        return left.prependLte(prefix).unify(
          l -> left(branch(l, right)),
          lm -> left(lm.map((l, m) -> branch(l, m, right)))
        );
      });
    } else {
      assert children.size() == 3;
      return mapFirst3(children, (left, middle, right) -> {
        return left.prependLte(prefix)
          .mapLeft(l -> branch(l, middle, right))
          .mapRight(lm -> new _2t<>(lm.map(FingerTree::branch), branch(middle, right))
        );
      });
    }
  }

  static <T,U> U mapFirst2(Sequence<T> s, BiFunction<? super T, ? super T, ? extends U> f) {
    var a = s.head();
    var b = s.tail().head();
    return f.apply(a,b);
  }

  static <T, U> U mapFirst3(Sequence<T> s, _3f<? super T, ? super T, ? super T, ? extends U> f) {
    var a = s.head(); var t = s.tail();
    var b = t.head();
    var c = t.tail().head();
    return f.apply(a,b,c);
  }

  @Override
  _2t<FingerTree<T, S>, FingerTree<T, S>> split0(int beforeLeaf) {
    assert Lists.isValidPosition(beforeLeaf, getLeafCount());
    return (beforeLeaf == 0) ? new _2t<>(empty(), this) : split0(beforeLeaf, children);
  }

  _2t<FingerTree<T, S>, FingerTree<T, S>> split0(int beforeLeaf, Sequence<? extends FingerTree<T, S>> nodes) {
    assert beforeLeaf > 0;
    var head = nodes.head();
    var headSize = head.getLeafCount();
    return (beforeLeaf <= headSize)
      ? head.split0(beforeLeaf).map((l, r) -> new _2t<>(l, concat(cons(r, nodes.tail()))))
      : split0(beforeLeaf - headSize, nodes.tail()).map((l, r) -> new _2t<>(head.join(l), r));
  }

  @Override
  Index locate0(BiFunction<? super S, Integer, Either<Integer, Integer>> navigate, int position) {
    assert navigate.apply(summary, position).isLeft();
    return locate0(navigate, position, children);
  }

  Index locate0(BiFunction<? super S, Integer, Either<Integer, Integer>> navigate, int position, Sequence<? extends NonEmpty<T, S>> nodes) {
    var head = nodes.head();
    return navigate.apply(head.getSummary(), position).unify(
      posInl -> head.locate0(navigate, posInl),
      posInr -> locate0(navigate, posInr, nodes.tail()).major(head.getLeafCount())
    );
  }

}
