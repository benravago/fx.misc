package fx.util.sparse;

class Stats {

  static final Stats ZERO = new Stats(0, 0);

  final int size;
  final int presentCount;

  Stats(int size, int presentCount) {
    assert size >= presentCount && presentCount >= 0;
    this.size = size;
    this.presentCount = presentCount;
  }

  int getSize() {
    return size;
  }

  int getPresentCount() {
    return presentCount;
  }

}