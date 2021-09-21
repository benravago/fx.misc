package fx;

import java.util.function.Consumer;

public class Counter implements Consumer<Object> {
  int count = 0;
  public void inc() { count += 1; }
  public int get() { return count; }
  public void reset() { count = 0; }
  public int getAndReset() { int res = count; count = 0; return res; }
  @Override public void accept(Object o) { ++count; }
}