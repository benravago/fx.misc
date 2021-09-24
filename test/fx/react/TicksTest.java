package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javafx.application.Platform;

import fx.react.util.FxTimer;
import fx.Counter;

class TicksTest {

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
  void fxTicksTest() throws InterruptedException, ExecutionException {
    var nTicks = new CompletableFuture<Integer>();
    Platform.runLater(() -> {
      var counter = new Counter();
      var sub = EventStreams.ticks(Duration.ofMillis(100)).subscribe(counter::accept);
      FxTimer.runLater(Duration.ofMillis(350), sub::unsubscribe); // stop after 3 ticks
      // wait a little more to test that no more than 3 ticks arrive anyway
      FxTimer.runLater(Duration.ofMillis(550), () -> nTicks.complete(counter.get()));
    });
    assertEquals(3, nTicks.get().intValue());
  }

  @Test
  void fxTicks0Test() throws InterruptedException, ExecutionException {
    var nTicks = new CompletableFuture<Integer>();
    Platform.runLater(() -> {
      var counter = new Counter();
      var sub = EventStreams.ticks0(Duration.ofMillis(100)).subscribe(counter::accept);
      // 000 (tick 1) -> 100 (tick 2) -> 200 (tick 3) -> 300 (tick 4) -> 350 (interrupted) = 4 ticks
      FxTimer.runLater(Duration.ofMillis(350), sub::unsubscribe); // stop after 4 ticks
      // wait a little more to test that no more than 4 ticks arrive anyway
      FxTimer.runLater(Duration.ofMillis(550), () -> nTicks.complete(counter.get()));
    });
    assertEquals(4, nTicks.get().intValue());
  }

  @Test
  void fxRestartableTicksTest() throws InterruptedException, ExecutionException {
    var nTicks = new CompletableFuture<Integer>();
    Platform.runLater(() -> {
      var counter = new Counter();
      var impulse = new EventSource<Void>();
      var sub = EventStreams.restartableTicks(Duration.ofMillis(100), impulse).subscribe(counter::accept);
      FxTimer.runLater(Duration.ofMillis(400), sub::unsubscribe);
      FxTimer.runLater(Duration.ofMillis(80), () -> impulse.push(null));
      FxTimer.runLater(Duration.ofMillis(260), () -> impulse.push(null));
      // 000: Start -> 80 (restart)
      // 080: Start
      // 180: End (tick)
      // 180: Start -> 80 (restart)
      // 260: Start
      // 360: End (tick)
      // 400: unsubscribed: 2 ticks
      // wait a little more to test that no more ticks arrive anyway
      FxTimer.runLater(Duration.ofMillis(550), () -> nTicks.complete(counter.get()));
    });
    assertEquals(2, nTicks.get().intValue());
  }

  @Test
  void fxRestartableTicks0Test() throws InterruptedException, ExecutionException {
    var nTicks = new CompletableFuture<Integer>();
    Platform.runLater(() -> {
      var counter = new Counter();
      var impulse = new EventSource<Void>();
      var sub = EventStreams.restartableTicks0(Duration.ofMillis(100), impulse).subscribe(counter::accept);
      FxTimer.runLater(Duration.ofMillis(400), sub::unsubscribe);
      FxTimer.runLater(Duration.ofMillis(80), () -> impulse.push(null));
      FxTimer.runLater(Duration.ofMillis(260), () -> impulse.push(null));
      // 000: 0 (tick) -> 80 (restart)
      // 080: 0 (tick)
      // 180: 0 (tick) -> 80 (restart)
      // 260: 0 (tick)
      // 360: 0 (tick)
      // 400: unsubscribed: 5 ticks
      // wait a little more to test that no more ticks arrive anyway
      FxTimer.runLater(Duration.ofMillis(550), () -> nTicks.complete(counter.get()));
    });
    assertEquals(5, nTicks.get().intValue());
  }

  @Test
  void executorTest() throws InterruptedException, ExecutionException {
    var executor = Executors.newSingleThreadExecutor();

    var nTicks = new CompletableFuture<Integer>();
    executor.execute(() -> {
      var counter = new Counter();
      var sub = EventStreams.ticks(Duration.ofMillis(100), scheduler, executor).subscribe(counter::accept);
      ScheduledExecutorServiceTimer.create(Duration.ofMillis(350), sub::unsubscribe, scheduler, executor).restart(); // stop after 3 ticks
      // wait a little more to test that no more than 3 ticks arrive anyway
      ScheduledExecutorServiceTimer.create(Duration.ofMillis(550), () -> nTicks.complete(counter.get()), scheduler, executor).restart();
    });
    assertEquals(3, nTicks.get().intValue());

    executor.shutdown();
  }

}
