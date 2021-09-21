package fx.react;

class BiSubscription implements Subscription {

  final Subscription s1;
  final Subscription s2;

  BiSubscription(Subscription s1, Subscription s2) {
    this.s1 = s1;
    this.s2 = s2;
  }

  @Override
  public void unsubscribe() {
    s1.unsubscribe();
    s2.unsubscribe();
  }

}