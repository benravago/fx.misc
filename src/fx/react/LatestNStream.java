package fx.react;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fx.react.util.Lists;

/**
 * See {@link EventStream#latestN(int)}
 */
class LatestNStream<T> extends EventStreamBase<List<T>> {

  final EventStream<T> source;
  final int n;

  List<T> first = null;
  List<T> second = null;
  List<T> concatView = null;

  LatestNStream(EventStream<T> source, int n) {
    if (n <= 0) {
      throw new IllegalArgumentException("n must be positive. Was " + n);
    }
    this.source = source;
    this.n = n;
  }

  @Override
  protected Subscription observeInputs() {
    first = Collections.emptyList();
    second = new ArrayList<>(n);
    concatView = Lists.concatView(first, second);
    return source.subscribe(this::onEvent);
  }

  void onEvent(T event) {
    if (second.size() == n) {
      first = second;
      second = new ArrayList<>(n);
      concatView = Lists.concatView(first, second);
    }
    second.add(event);
    var total = concatView.size();
    emit(concatView.subList(Math.max(0, total - n), total));
  }

}
