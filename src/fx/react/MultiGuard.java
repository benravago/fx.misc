package fx.react;

class MultiGuard implements Guard {

  final Guard[] guards;

  MultiGuard(Guard... guards) {
    this.guards = guards;
  }

  @Override
  public void close() {
    // close in reverse order
    for (var i = guards.length - 1; i >= 0; --i) {
      guards[i].close();
    }
  }

}