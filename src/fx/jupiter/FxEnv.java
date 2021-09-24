package fx.jupiter;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;

public class FxEnv {

  static final AtomicBoolean ready = new AtomicBoolean(false);

  public static void startup() {
    if (!ready.get()) {
      Platform.startup(() -> {
        ready.set(true);
        System.out.println("JavaFX Platform ready");
      });
    }
  }

  public static void shutdown() {
    if (ready.get()) {
      Platform.exit();
    }
  }

  public static FxRobot robot() {
    FxEnv.startup();
    return call(() -> new FxRobot());
  }

  public static <V> V call(Callable<V> action) {
    assert action != null;
    if (Platform.isFxApplicationThread()) {
      try {
        return action.call();
      } catch (Throwable t) {
        return uncheck(t);
      }
    } else {
      return callAndWait(action);
    }
  }

  @SuppressWarnings("unchecked")
  public static <V> V callAndWait(Callable<V> action) {
    var result = new AtomicReference<Object>();
    var semaphore = new Semaphore(0);
    Platform.runLater(() -> {
      try {
        result.set(action.call());
      } catch (Throwable t) {
        result.set(t);
      } finally {
        semaphore.release();
      }
    });
    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      note("callAndWait", e);
    }
    var v = result.get();
    return v instanceof Throwable ? uncheck((Throwable) v) : (V) v;
  }

  public static void run(Runnable action) {
    assert action != null;
    if (Platform.isFxApplicationThread()) {
      action.run();
    } else {
      runAndWait(10, 5, action); // 5 pulses, 10 msec gaps
    }
  }

  public static void runAndWait(long pause, int count, Runnable action) {
    var fault = new AtomicReference<Throwable>();
    var latch = new CountDownLatch(count);
    Platform.runLater(() -> {
      try {
        action.run();
      } catch (Throwable t) {
        fault.set(t);
      } finally {
        countDown(latch, pause);
      }
    });
    try {
      latch.await();
    } catch (InterruptedException e) {
      note("runAndWait", e);
    }
    var t = fault.get();
    if (t != null) {
      uncheck(t);
    }
  }

  @SuppressWarnings("unchecked")
  static <T extends Throwable, V> V uncheck(Throwable t) throws T {
    throw (T) t;
  }

  static void countDown(CountDownLatch latch, long pause) {
    var c = latch.getCount();
    if (c > 0) {
      latch.countDown();
      if (c > 1) {
        try {
          Thread.sleep(pause);
        } catch (InterruptedException e) {
          note("countDown", e);
        }
        Platform.runLater(() -> countDown(latch, pause));
      }
    }
  }

  static void note(String m, Throwable t) {
    System.out.format("FxEnv: %s; %s\n", m, t);
  }

}
