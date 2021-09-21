package fx.react;

class MultiSubscription implements Subscription {

  final Subscription[] subscriptions;

  MultiSubscription(Subscription... subscriptions) {
    this.subscriptions = subscriptions;
  }

  @Override
  public void unsubscribe() {
    for (var s : subscriptions) {
      s.unsubscribe();
    }
  }

}