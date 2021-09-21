package fx.react;

class BiGuard implements Guard {

  final Guard g1;
  final Guard g2;

  BiGuard(Guard g1, Guard g2) {
    this.g1 = g1;
    this.g2 = g2;
  }

  @Override
  public void close() {
    // close in reverse order
    g2.close();
    g1.close();
  }

}