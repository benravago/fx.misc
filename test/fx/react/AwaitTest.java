package fx.react;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fx.util.Try;
import javafx.application.Platform;
import javafx.concurrent.Task;

class AwaitTest {

  private ExecutorService executor;

  @BeforeAll
  static void startUp() {
    fx.jupiter.FxRunner.startup(); // initializes JavaFX toolkit
  }

  @BeforeEach
  void setUp() throws Exception {
    executor = Executors.newSingleThreadExecutor();
  }

  @AfterEach
  void tearDown() throws Exception {
    executor.shutdown();
  }

  @Test
  void testAwaitCompletionStage() throws InterruptedException, ExecutionException {
    CompletableFuture<List<Object>> emitted = new CompletableFuture<>();
    EventSource<Integer> src = new EventSource<>();
    AwaitingEventStream<Try<Integer>> squares = src.mapToCompletionStage(x -> async(x * x)).await(executor);
    executor.execute(() -> {
      List<Object> res = aggregate(
          EventStreams.merge(squares.map(Try::get), EventStreams.valuesOf(squares.pendingProperty())));
      src.push(1);
      src.push(2);
      src.push(3);
      src.push(4);
      executor.execute(() -> executor.execute(() -> emitted.complete(res)));
    });
    assertEquals(Arrays.asList(false, true, 1, 4, 9, 16, false), emitted.get());
  }

  @Test
  void testAwaitTask() throws InterruptedException, ExecutionException {
    CompletableFuture<List<Object>> emitted = new CompletableFuture<>();
    EventSource<Integer> src = new EventSource<>();
    AwaitingEventStream<Try<Integer>> squares = src.mapToTask(x -> background(x * x)).await();
    Platform.runLater(() -> {
      List<Object> res = aggregate(
          EventStreams.merge(squares.map(Try::get), EventStreams.valuesOf(squares.pendingProperty())));
      src.push(1);
      src.push(2);
      src.push(3);
      src.push(4);
      executor.execute(() -> Platform.runLater(() -> emitted.complete(res)));
    });
    assertEquals(Arrays.asList(false, true, 1, 4, 9, 16, false), emitted.get());
  }

  @Test
  void testAwaitLatestCompletionStage() throws InterruptedException, ExecutionException {
    CompletableFuture<List<Object>> emitted = new CompletableFuture<>();
    EventSource<Integer> src = new EventSource<>();
    AwaitingEventStream<Try<Integer>> squares = src.mapToCompletionStage(x -> async(x * x)).awaitLatest(executor);
    executor.execute(() -> {
      List<Object> res = aggregate(
          EventStreams.merge(squares.map(Try::get), EventStreams.valuesOf(squares.pendingProperty())));
      src.push(1);
      src.push(2);
      executor.execute(() -> executor.execute(() -> {
        src.push(3);
        src.push(4);
        executor.execute(() -> executor.execute(() -> emitted.complete(res)));
      }));
    });
    assertEquals(Arrays.asList(false, true, 4, false, true, 16, false), emitted.get());
  }

  @Test
  void testAwaitLatestTask() throws InterruptedException, ExecutionException {
    CompletableFuture<List<Object>> emitted = new CompletableFuture<>();
    EventSource<Integer> src = new EventSource<>();
    AwaitingEventStream<Try<Integer>> squares = src.mapToTask(x -> background(x * x)).awaitLatest();
    executor.execute(() -> {
      List<Object> res = aggregate(
          EventStreams.merge(squares.map(Try::get), EventStreams.valuesOf(squares.pendingProperty())));
      src.push(1);
      src.push(2);
      executor.execute(() -> Platform.runLater(() -> {
        src.push(3);
        src.push(4);
        executor.execute(() -> Platform.runLater(() -> emitted.complete(res)));
      }));
    });
    assertEquals(Arrays.asList(false, true, 4, false, true, 16, false), emitted.get());
  }

  @Test
  void testAwaitLatestCompletionStageWithCanceller() throws InterruptedException, ExecutionException {
    CompletableFuture<List<Object>> emitted = new CompletableFuture<>();
    EventSource<Integer> src = new EventSource<>();
    EventSource<Void> canceller = new EventSource<>();
    AwaitingEventStream<Try<Integer>> squares = src.mapToCompletionStage(x -> async(x * x)).awaitLatest(canceller,
        executor);
    executor.execute(() -> {
      List<Object> res = aggregate(
          EventStreams.merge(squares.map(Try::get), EventStreams.valuesOf(squares.pendingProperty())));
      src.push(1);
      src.push(2);
      canceller.push(null);
      executor.execute(() -> executor.execute(() -> {
        src.push(3);
        src.push(4);
        executor.execute(() -> executor.execute(() -> {
          src.push(5);
          src.push(6);
          canceller.push(null);
          executor.execute(() -> executor.execute(() -> emitted.complete(res)));
        }));
      }));
    });
    assertEquals(Arrays.asList(false, true, false, true, 16, false, true, false), emitted.get());
  }

  @Test
  void testAwaitLatestTaskWithCanceller() throws InterruptedException, ExecutionException {
    CompletableFuture<List<Object>> emitted = new CompletableFuture<>();
    EventSource<Integer> src = new EventSource<>();
    EventSource<Void> canceller = new EventSource<>();
    AwaitingEventStream<Try<Integer>> squares = src.mapToTask(x -> background(x * x)).awaitLatest(canceller);
    Platform.runLater(() -> {
      List<Object> res = aggregate(
          EventStreams.merge(squares.map(Try::get), EventStreams.valuesOf(squares.pendingProperty())));
      src.push(1);
      src.push(2);
      canceller.push(null);
      executor.execute(() -> Platform.runLater(() -> {
        src.push(3);
        src.push(4);
        executor.execute(() -> Platform.runLater(() -> {
          src.push(5);
          src.push(6);
          canceller.push(null);
          executor.execute(() -> Platform.runLater(() -> emitted.complete(res)));
        }));
      }));
    });
    assertEquals(Arrays.asList(false, true, false, true, 16, false, true, false), emitted.get());
  }

  private CompletionStage<Integer> async(int x) {
    CompletableFuture<Integer> res = new CompletableFuture<>();
    executor.execute(() -> res.complete(x));
    return res;
  }

  private <T> Task<T> background(T t) {
    Task<T> task = new Task<T>() {
      @Override
      protected T call() throws Exception {
        return t;
      }
    };
    executor.execute(task);
    return task;
  }

  private static <T> List<T> aggregate(EventStream<T> stream) {
    List<T> res = new ArrayList<>();
    stream.subscribe(x -> res.add(x));
    return res;
  }
}
