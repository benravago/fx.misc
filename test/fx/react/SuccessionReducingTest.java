package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

class SuccessionReducingTest {

  @BeforeAll
  static void startUp() {
    fx.jupiter.FxEnv.startup(); // initializes JavaFX toolkit
  }

  ScheduledExecutorService scheduler;

  @BeforeEach
  void setUp() throws Exception {
    scheduler = Executors.newSingleThreadScheduledExecutor();
  }

  @AfterEach
  void tearDown() throws Exception {
    scheduler.shutdown();
  }

  @Test
  void fxTest() throws InterruptedException, ExecutionException {
    var future1 = new CompletableFuture<List<Object>>();
    var future2 = new CompletableFuture<List<Object>>();

    Platform.runLater(() -> {
      var source = new EventSource<Integer>();

      var reducing1 = source.reduceSuccessions((a, b) -> a + b, Duration.ofMillis(200));
      var pending1 = EventStreams.valuesOf(reducing1.pendingProperty());
      var emitted1 = new ArrayList<Object>();
      EventStreams.merge(reducing1, pending1).subscribe(i -> emitted1.add(i));

      var reducing2 = source.reduceSuccessions(() -> 0, (a, b) -> a + b, Duration.ofMillis(200));
      var pending2 = EventStreams.valuesOf(reducing2.pendingProperty());
      var emitted2 = new ArrayList<Object>();
      EventStreams.merge(reducing2, pending2).subscribe(i -> emitted2.add(i));

      source.push(1);
      source.push(2);
      scheduler.schedule(() -> Platform.runLater(() -> source.push(3)), 50, TimeUnit.MILLISECONDS);

      scheduler.schedule(() -> Platform.runLater(() -> source.push(4)), 300, TimeUnit.MILLISECONDS);
      scheduler.schedule(() -> Platform.runLater(() -> source.push(5)), 350, TimeUnit.MILLISECONDS);

      scheduler.schedule(() -> Platform.runLater(() -> future1.complete(emitted1)), 600, TimeUnit.MILLISECONDS);
      scheduler.schedule(() -> Platform.runLater(() -> future2.complete(emitted2)), 600, TimeUnit.MILLISECONDS);
    });

    assertEquals(Arrays.asList(false, true, 6, false, true, 9, false), future1.get());
    assertEquals(Arrays.asList(false, true, 6, false, true, 9, false), future2.get());
  }

  @Test
  void executorTest() throws InterruptedException, ExecutionException {
    var future1 = new CompletableFuture<List<Object>>();
    var future2 = new CompletableFuture<List<Object>>();

    var executor = Executors.newSingleThreadExecutor();

    executor.execute(() -> {
      var source = new EventSource<Integer>();

      var reducing1 = source.reduceSuccessions((a, b) -> a + b, Duration.ofMillis(200), scheduler, executor);
      var pending1 = EventStreams.valuesOf(reducing1.pendingProperty());
      var emitted1 = new ArrayList<Object>();
      EventStreams.merge(reducing1, pending1).subscribe(i -> emitted1.add(i));

      var reducing2 = source.reduceSuccessions(() -> 0, (a, b) -> a + b, Duration.ofMillis(200), scheduler, executor);
      var pending2 = EventStreams.valuesOf(reducing2.pendingProperty());
      var emitted2 = new ArrayList<Object>();
      EventStreams.merge(reducing2, pending2).subscribe(i -> emitted2.add(i));

      source.push(1);
      source.push(2);
      scheduler.schedule(() -> executor.execute(() -> source.push(3)), 50, TimeUnit.MILLISECONDS);

      scheduler.schedule(() -> executor.execute(() -> source.push(4)), 300, TimeUnit.MILLISECONDS);
      scheduler.schedule(() -> executor.execute(() -> source.push(5)), 350, TimeUnit.MILLISECONDS);

      scheduler.schedule(() -> executor.execute(() -> future1.complete(emitted1)), 600, TimeUnit.MILLISECONDS);
      scheduler.schedule(() -> executor.execute(() -> future2.complete(emitted2)), 600, TimeUnit.MILLISECONDS);
    });

    assertEquals(Arrays.asList(false, true, 6, false, true, 9, false), future1.get());
    assertEquals(Arrays.asList(false, true, 6, false, true, 9, false), future2.get());

    executor.shutdown();
  }

}
