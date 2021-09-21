package fx.react;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ThenAccumulateForTest {
  private ScheduledExecutorService scheduler;

  @BeforeEach
  void setUp() throws Exception {
    scheduler = Executors.newSingleThreadScheduledExecutor();
  }

  @AfterEach
  void tearDown() throws Exception {
    scheduler.shutdown();
  }

  @Test
  void test() throws InterruptedException, ExecutionException {
    EventSource<Integer> source = new EventSource<>();
    EventStream<Integer> stream = source.thenReduceFor(Duration.ofMillis(100), (a, b) -> a + b, scheduler, scheduler);
    List<Integer> emitted = new ArrayList<>();
    stream.subscribe(x -> {
      while ((x /= 2) > 0)
        source.push(x);
    });
    stream.subscribe(emitted::add);
    scheduler.execute(() -> {
      source.push(3);
      source.push(2);
      source.push(1);
    });
    CompletableFuture<List<Integer>> future = new CompletableFuture<>();
    scheduler.schedule(() -> future.complete(emitted), 350, TimeUnit.MILLISECONDS);
    assertEquals(Arrays.asList(3, 4, 3, 1), future.get());
  }
}
