package fx.util.sparse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import fx.util.tree.FingerTree._2t;
import fx.util.Lists;
import fx.util.tree.FingerTree;
import fx.util.tree.ToSemigroup;
import javafx.scene.control.IndexRange;

public class SparseList<E> {

  static final ToSemigroup<Segment<?>, Stats> SEGMENT_STATS =
    new ToSemigroup<Segment<?>, Stats>() {
      @Override
      public Stats reduce(Stats left, Stats right) {
        return new Stats(left.size + right.size, left.presentCount + right.presentCount);
      }
      @Override
      public Stats apply(Segment<?> seg) {
        return new Stats(seg.getLength(), seg.getPresentCount());
      }
    };

  static <E> FingerTree<Segment<E>, Stats> emptyTree() {
    return FingerTree.empty(SEGMENT_STATS);
  }

  FingerTree<Segment<E>, Stats> tree;

  public SparseList() {
    tree = emptyTree();
  }

  public int size() {
    return tree.getSummary(Stats.ZERO).size;
  }

  public int getPresentCount() {
    return tree.getSummary(Stats.ZERO).presentCount;
  }

  public boolean isPresent(int index) {
    return tree.get(Stats::getSize, index, Segment::isPresent);
  }

  public E getOrThrow(int index) {
    return tree.get(Stats::getSize, index, Segment::getOrThrow);
  }

  public Optional<E> get(int index) {
    return tree.get(Stats::getSize, index, Segment::get);
  }

  public E getPresent(int presentIndex) {
    return tree.get(Stats::getPresentCount, presentIndex, Segment::getOrThrow);
  }

  public int getPresentCountBefore(int position) {
    Lists.checkPosition(position, size());
    return tree
      .getSummaryBetween(Stats::getSize, 0, position, Segment::getStatsBetween)
      .orElse(Stats.ZERO)
      .getPresentCount();
  }

  public int getPresentCountAfter(int position) {
    return getPresentCount() - getPresentCountBefore(position);
  }

  public int getPresentCountBetween(int from, int to) {
    Lists.checkRange(from, to, size());
    return getPresentCountBefore(to) - getPresentCountBefore(from);
  }

  public int indexOfPresentItem(int presentIndex) {
    Lists.checkIndex(presentIndex, getPresentCount());
    return tree.locateProgressively(Stats::getPresentCount, presentIndex).map(this::locationToPosition);
  }

  public IndexRange getPresentItemsRange() {
    if (getPresentCount() == 0) {
      return new IndexRange(0, 0);
    } else {
      var lowerBound = tree.locateProgressively(Stats::getPresentCount, 0).map(this::locationToPosition);
      var upperBound = tree.locateRegressively(Stats::getPresentCount, getPresentCount()).map(this::locationToPosition);
      return new IndexRange(lowerBound, upperBound);
    }
  }

  int locationToPosition(int major, int minor) {
    return tree.getSummaryBetween(0, major).orElse(Stats.ZERO).size + minor;
  }

  public List<E> collect() {
    List<E> acc = new ArrayList<E>(getPresentCount());
    return tree.fold(acc, (l, seg) -> seg.appendTo(l));
  }

  public List<E> collect(int from, int to) {
    List<E> acc = new ArrayList<E>(getPresentCountBetween(from, to));
    return tree.foldBetween(acc,
      (l, seg) -> seg.appendTo(l), Stats::getSize, from, to,
      (l, seg, start, end) -> seg.appendRangeTo(l, start, end)
    );
  }

  public void clear() {
    tree = emptyTree();
  }

  public void remove(int index) {
    remove(index, index + 1);
  }

  public void remove(int from, int to) {
    Lists.checkRange(from, to, size());
    if (from != to) {
      spliceSegments(from, to, Collections.emptyList());
    }
  }

  public void set(int index, E elem) {
    tree.get(Stats::getSize, index)
      .exec((seg, loc) -> {
        if (seg.isPresent()) {
          seg.setOrThrow(loc.minor, elem);
          // changing an element does not affect stats, so we're done
        } else {
          splice(index, index + 1, Collections.singleton(elem));
        }
      });
  }

  public boolean setIfAbsent(int index, E elem) {
    if (isPresent(index)) {
      return false;
    } else {
      set(index, elem);
      return true;
    }
  }

  public void insert(int position, E elem) {
    insertAll(position, Collections.singleton(elem));
  }

  public void insertAll(int position, Collection<? extends E> elems) {
    if (elems.isEmpty()) {
      return;
    }
    var seg = new PresentSegment<E>(elems);
    tree = tree.caseEmpty().unify(
      emptyTree -> emptyTree.append(seg),
      nonEmptyTree -> nonEmptyTree
        .split(Stats::getSize, position)
        .map((l, m, r) -> join(l, m, seg, m, r))
      );
  }

  public void insertVoid(int position, int length) {
    if (length < 0) {
      throw new IllegalArgumentException("length cannot be negative: " + length);
    } else if (length == 0) {
      return;
    }
    var seg = new AbsentSegment<E>(length);
    tree = tree.caseEmpty().unify(
      emptyTree -> emptyTree.append(seg),
      nonEmptyTree -> nonEmptyTree
        .split(Stats::getSize, position)
        .map((l, m, r) -> join(l, m, seg, m, r))
      );
  }

  public void splice(int from, int to, Collection<? extends E> elems) {
    if (elems.isEmpty()) {
      remove(from, to);
    } else if (from == to) {
      insertAll(from, elems);
    } else {
      spliceSegments(from, to, Collections.singletonList(new PresentSegment<>(elems)));
    }
  }

  public void spliceByVoid(int from, int to, int length) {
    if (length == 0) {
      remove(from, to);
    } else if (length < 0) {
      throw new IllegalArgumentException("length cannot be negative: " + length);
    } else if (from == to) {
      insertVoid(from, length);
    } else {
      spliceSegments(from, to, Collections.singletonList(new AbsentSegment<>(length)));
    }
  }

  void spliceSegments(int from, int to, List<Segment<E>> middle) {
    Lists.checkRange(from, to, tree.getSummary(Stats.ZERO).getSize());
    tree = tree.caseEmpty()
      .mapLeft(emptyTree -> join(emptyTree, middle, emptyTree))
      .toLeft(nonEmptyTree -> nonEmptyTree
         .split(Stats::getSize, from)
         .map((left, lSuffix, r) -> {
            return nonEmptyTree.split(Stats::getSize, to).map((l, rPrefix, right) -> {
              return join(left, lSuffix, middle, rPrefix, right);
            });
          })
       );
  }

  FingerTree<Segment<E>, Stats> join(FingerTree<Segment<E>, Stats> left, _2t<Segment<E>, Integer> lSuffix, Segment<E> middle, _2t<Segment<E>, Integer> rPrefix, FingerTree<Segment<E>, Stats> right) {
    return join(left, lSuffix, Collections.singletonList(middle), rPrefix, right);
  }

  FingerTree<Segment<E>, Stats> join(FingerTree<Segment<E>, Stats> left, _2t<Segment<E>, Integer> lSuffix, List<Segment<E>> middle, _2t<Segment<E>, Integer> rPrefix, FingerTree<Segment<E>, Stats> right) {
    var lSeg = lSuffix.a();
    var lMax = lSuffix.b();
    if (lMax > 0) {
      left = left.append(lSeg.subSegment(0, lMax));
    }
    var rSeg = rPrefix.a();
    var rMin = rPrefix.b();
    if (rMin < rSeg.getLength()) {
      right = right.prepend(rSeg.subSegment(rMin, rSeg.getLength()));
    }
    return join(left, middle, right);
  }

  FingerTree<Segment<E>, Stats> join(FingerTree<Segment<E>, Stats> left, List<Segment<E>> middle, FingerTree<Segment<E>, Stats> right) {
    for (var seg : middle) {
      left = append(left, seg);
    }
    return join(left, right);
  }

  FingerTree<Segment<E>, Stats> join(FingerTree<Segment<E>, Stats> left, FingerTree<Segment<E>, Stats> right) {
    if (left.isEmpty()) {
      return right;
    } else if (right.isEmpty()) {
      return left;
    } else {
      var lastLeft = left.getLeaf(left.getLeafCount() - 1);
      var firstRight = right.getLeaf(0);
      if (lastLeft.possiblyDestructiveAppend(firstRight)) {
        left = left.updateLeaf(left.getLeafCount() - 1, lastLeft);
        right = right.split(1).b();
      }
      return left.join(right);
    }
  }

  FingerTree<Segment<E>, Stats> append(FingerTree<Segment<E>, Stats> left, Segment<E> right) {
    if (left.isEmpty()) {
      return left.append(right);
    } else {
      var lastLeft = left.getLeaf(left.getLeafCount() - 1);
      return (lastLeft.possiblyDestructiveAppend(right))
        ? left.updateLeaf(left.getLeafCount() - 1, lastLeft)
        : left.append(right);
    }
  }

}