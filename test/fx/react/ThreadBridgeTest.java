package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

class ThreadBridgeTest {

  @Test
  void test() throws InterruptedException, ExecutionException {
    ThreadFactory threadFactory1 = runnable -> new Thread(runnable, "thread 1");
    ThreadFactory threadFactory2 = runnable -> new Thread(runnable, "thread 2");
    var exec1 = Executors.newSingleThreadExecutor(threadFactory1);
    var exec2 = Executors.newSingleThreadExecutor(threadFactory2);
    var src1 = new EventSource<Integer>();
    var stream2 = src1.threadBridge(exec1, exec2);
    var stream1 = stream2.threadBridge(exec2, exec1);

    var emittedFrom2 = new ArrayList<Integer>();
    var emittedFrom1 = new ArrayList<Integer>();
    var emissionThreads2 = new ArrayList<String>();
    var emissionThreads1 = new ArrayList<String>();

    var emittedFrom2Final = new CompletableFuture<List<Integer>>();
    var emittedFrom1Final = new CompletableFuture<List<Integer>>();
    var emissionThreads2Final = new CompletableFuture<List<String>>();
    var emissionThreads1Final = new CompletableFuture<List<String>>();

    var subscribed = new CountDownLatch(2);
    exec2.execute(() -> {
      stream2.subscribe(i -> {
        if (i != null) {
          emittedFrom2.add(i);
          emissionThreads2.add(Thread.currentThread().getName());
        } else {
          emittedFrom2Final.complete(emittedFrom2);
          emissionThreads2Final.complete(emissionThreads2);
        }
      });
      subscribed.countDown();
    });
    exec1.execute(() -> {
      stream1.subscribe(i -> {
        if (i != null) {
          emittedFrom1.add(i);
          emissionThreads1.add(Thread.currentThread().getName());
        } else {
          emittedFrom1Final.complete(emittedFrom1);
          emissionThreads1Final.complete(emissionThreads1);
        }
      });
      subscribed.countDown();
    });

    subscribed.await();
    exec1.execute(() -> {
      src1.push(1);
      src1.push(2);
      src1.push(3);
      src1.push(null);
    });

    assertEquals(Arrays.asList(1, 2, 3), emittedFrom2Final.get());
    assertEquals(Arrays.asList(1, 2, 3), emittedFrom1Final.get());
    assertEquals(Arrays.asList("thread 2", "thread 2", "thread 2"), emissionThreads2Final.get());
    assertEquals(Arrays.asList("thread 1", "thread 1", "thread 1"), emissionThreads1Final.get());

    exec1.shutdown();
    exec2.shutdown();
  }

}
