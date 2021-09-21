package fx.react;

class BiSuspendable implements Suspendable {

  final Suspendable s1;
  final Suspendable s2;

  BiSuspendable(Suspendable s1, Suspendable s2) {
    this.s1 = s1;
    this.s2 = s2;
  }

  @Override
  public Guard suspend() {
    return new BiGuard(s1.suspend(), s2.suspend());
  }

}