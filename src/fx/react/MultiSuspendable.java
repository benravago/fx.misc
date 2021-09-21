package fx.react;

import java.util.Arrays;

class MultiSuspendable implements Suspendable {

  final Suspendable[] suspendables;

  MultiSuspendable(Suspendable... suspendables) {
    this.suspendables = suspendables;
  }

  @Override
  public Guard suspend() {
    Guard[] guards = Arrays.stream(suspendables).map(g -> g.suspend()).toArray(n -> new Guard[n]);
    return new MultiGuard(guards);
  }

}