package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class UntilLaterTest {
  
  static ExecutorService executor;

  @BeforeAll
  static void setupExecutor() {
    executor = Executors.newSingleThreadExecutor();
  }

  @AfterAll
  static void shutdownExecutor() {
    executor.shutdown();
  }

  @Test
  void retainLatestUntilLaterTest() throws InterruptedException, ExecutionException {
    var future = new CompletableFuture<List<Integer>>();
    var source = new EventSource<Integer>();
    var stream = source.retainLatestUntilLater(executor);
    executor.execute(() -> {
      var emitted = new ArrayList<Integer>();
      stream.subscribe(emitted::add);
      source.push(1);
      executor.execute(() -> {
        source.push(2);
        source.push(3);
        source.push(4);
        executor.execute(() -> {
          source.push(5);
          source.push(6);
          executor.execute(() -> future.complete(emitted));
        });
        source.push(7);
        source.push(8);
      });
      source.push(9);
    });
    List<Integer> emitted = future.get();
    assertEquals(Arrays.asList(9, 8, 6), emitted);
  }

  @Test
  void queueUntilLaterTest() throws InterruptedException, ExecutionException {
    var future = new CompletableFuture<List<Integer>>();
    var source = new EventSource<Integer>();
    var stream = source.queueUntilLater(executor);
    executor.execute(() -> {
      var emitted = new ArrayList<Integer>();
      stream.subscribe(emitted::add);
      source.push(1);
      executor.execute(() -> {
        source.push(2);
        source.push(3);
        source.push(4);
        executor.execute(() -> {
          source.push(5);
          source.push(6);
          executor.execute(() -> future.complete(emitted));
        });
        source.push(7);
        source.push(8);
      });
      source.push(9);
    });
    var emitted = future.get();
    assertEquals(Arrays.asList(1, 9, 2, 3, 4, 7, 8, 5, 6), emitted);
  }

}
