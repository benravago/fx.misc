package fx.input.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

class Branch<K, V> extends NonEmpty<K, V> {

  final K prefix;
  final List<NonEmpty<K, V>> subTrees;

  Branch(Ops<K, V> ops, K prefix, List<NonEmpty<K, V>> subTrees) {
    super(ops);
    assert Objects.equals(prefix, subTrees.stream().map(NonEmpty::getPrefix).reduce(ops::commonPrefix).get());
    assert subTrees.stream().noneMatch(tree -> Objects.equals(tree.getPrefix(), prefix));
    this.prefix = prefix;
    this.subTrees = subTrees;
  }

  Branch(Ops<K, V> ops, NonEmpty<K, V> t1, NonEmpty<K, V> t2) {
    this(ops, ops.commonPrefix(t1.getPrefix(), t2.getPrefix()), Arrays.asList(t1, t2));
  }

  @Override
  K getPrefix() {
    return prefix;
  }

  @Override
  public Stream<Entry<K, V>> entries() {
    return subTrees.stream().flatMap(tree -> tree.entries());
  }

  @Override
  Data<K, V> collapse() {
    return subTrees.stream().map(tree -> tree.collapse().promote(prefix)).reduce(Data::squash).get();
  }

  @Override
  NonEmpty<K, V> insertInside(K key, V value, BiFunction<? super V, ? super V, ? extends V> combine) {
    assert ops.isPrefixOf(prefix, key);
    if (Objects.equals(key, prefix)) {
      return new Data<>(ops, key, value).insertInside(collapse(), flip(combine));
    }
    // try to find a sub-tree that has common prefix with key longer than this branch's prefix
    for (var i = 0; i < subTrees.size(); ++i) {
      var st = subTrees.get(i);
      var commonPrefix = ops.commonPrefix(key, st.getPrefix());
      if (!Objects.equals(commonPrefix, prefix)) {
        if (Objects.equals(commonPrefix, st.getPrefix())) {
          // st contains key, insert inside st
          return replaceBranch(i, st.insertInside(key, value, combine));
        } else if (Objects.equals(commonPrefix, key)) {
          // st is under key, insert st inside Data(key, value)
          return replaceBranch(i, new Data<>(ops, key, value).insertInside(st.collapse(), flip(combine)));
        } else {
          return replaceBranch(i, new Branch<>(ops, st, new Data<>(ops, key, value)));
        }
      }
    }

    // no branch intersects key, adjoin Data(key, value) to this branch
    var branches = new ArrayList<NonEmpty<K, V>>(subTrees.size() + 1);
    branches.addAll(subTrees);
    branches.add(new Data<>(ops, key, value));
    return new Branch<>(ops, prefix, branches);
  }

  Branch<K, V> replaceBranch(int i, NonEmpty<K, V> replacement) {
    assert ops.isPrefixOf(prefix, replacement.getPrefix());
    assert ops.isPrefixOf(replacement.getPrefix(), subTrees.get(i).getPrefix());
    var branches = new ArrayList<NonEmpty<K, V>>(subTrees);
    branches.set(i, replacement);
    return new Branch<>(ops, prefix, branches);
  }

  @Override
  public <W> NonEmpty<K, W> map(Function<? super V, ? extends W> f, Ops<K, W> ops) {
    var mapped = new ArrayList<NonEmpty<K, W>>(subTrees.size());
    for (var tree : subTrees) {
      mapped.add(tree.map(f, ops));
    }
    return new Branch<>(ops, prefix, mapped);
  }

}