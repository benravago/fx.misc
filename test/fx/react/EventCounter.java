package fx.react;

import java.util.function.Consumer;

class EventCounter implements Consumer<Object> {
  private int count = 0;

  @Override
  public void accept(Object o) {
    ++count;
  }

  int get() {
    return count;
  }

  int getAndReset() {
    int res = count;
    count = 0;
    return res;
  }
}
