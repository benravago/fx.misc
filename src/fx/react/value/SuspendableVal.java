package fx.react.value;

import fx.react.Suspendable;

/**
 * Observable value whose invalidation and change notifications can be
 * temporarily suspended. Multiple invalidations encountered while this
 * observable value was suspended will result in a single invalidation and
 * at most one change notification when resumed.
 */
public interface SuspendableVal<T> extends Val<T>, Suspendable {
  //
}
