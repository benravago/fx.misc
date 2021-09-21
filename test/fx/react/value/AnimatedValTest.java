package fx.react.value;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import fx.react.EventStreams;
import fx.react.util.Interpolator;

class AnimatedValTest {

  static class WaitUntilListener<T> implements ChangeListener<T> {
    
    final Predicate<T> pred;
    final CompletableFuture<?> toComplete;

    WaitUntilListener(Predicate<T> pred, CompletableFuture<?> toComplete) {
      this.pred = pred;
      this.toComplete = toComplete;
    }

    @Override
    public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
      if (pred.test(newValue)) {
        observable.removeListener(this);
        toComplete.complete(null);
      }
    }
  }

  static <T> void waitUntil(ObservableValue<T> obs, Predicate<T> pred, int timeoutMillis) throws InterruptedException, ExecutionException, TimeoutException {
    var future = new CompletableFuture<Void>();
    Platform.runLater(() -> {
      if (pred.test(obs.getValue())) {
        future.complete(null);
      } else {
        obs.addListener(new WaitUntilListener<>(pred, future));
      }
    });
    future.get(timeoutMillis, TimeUnit.MILLISECONDS);
  }

  @BeforeAll
  static void setup() {
    fx.jupiter.FxRunner.startup(); // initializes JavaFX toolkit
  }

  /**
   * Tests that the target value is reached, eventually.
   */
  @Test
  void sanityTest() throws InterruptedException, ExecutionException, TimeoutException {
    var src = Var.newSimpleVar(0.0);
    var anim = src.animate(Duration.ofMillis(30), Interpolator.EASE_BOTH_DOUBLE);

    Platform.runLater(() -> {
      src.setValue(33.0);
    });

    waitUntil(anim, x -> x == 33.0, 100);
  }

  @Test
  void testEqualNumberOfFramesForFixedDuration() throws InterruptedException, ExecutionException, TimeoutException {
    var src1 = Var.newSimpleVar(0.0);
    var src2 = Var.newSimpleVar(0.0);
    var anim1 = src1.animate(Duration.ofMillis(500), Interpolator.LINEAR_DOUBLE);
    var anim2 = src2.animate(Duration.ofMillis(500), Interpolator.LINEAR_DOUBLE);
    var vals1 = new ArrayList<Double>();
    var vals2 = new ArrayList<Double>();

    Platform.runLater(() -> {
      EventStreams.valuesOf(anim1).subscribe(vals1::add);
      EventStreams.valuesOf(anim2).subscribe(vals2::add);

      src1.setValue(10.0);
      src2.setValue(20.0);
    });

    waitUntil(anim1, x -> x == 10.0, 1000);
    waitUntil(anim2, x -> x == 20.0, 100);

    assertEquals(vals1.size(), vals2.size());
  }

  @Test
  void testProportionalNumberOfFramesForFixedSpeed() throws InterruptedException, ExecutionException, TimeoutException {
    var src1 = Var.newSimpleVar(0);
    var src2 = Var.newSimpleVar(0);
    var anim1 = src1.animate((a, b) -> Duration.ofMillis(b - a), Interpolator.LINEAR_INTEGER);
    var anim2 = src2.animate((a, b) -> Duration.ofMillis(b - a), Interpolator.LINEAR_INTEGER);
    var vals1 = new ArrayList<Integer>();
    var vals2 = new ArrayList<Integer>();

    Platform.runLater(() -> {
      EventStreams.valuesOf(anim1).subscribe(vals1::add);
      EventStreams.valuesOf(anim2).subscribe(vals2::add);

      src1.setValue(100);
      src2.setValue(300);
    });

    waitUntil(anim2, x -> x == 300, 1000);

    assertEquals(100, anim1.getValue().intValue());

    // test that the number of frames for 0 -> 300 is at least
    // twice the number of frames for 0 -> 100 (i.e. a conservative test)
    assertTrue(vals2.size() > (2 * vals1.size()));
    // assertThat(vals2.size(), greaterThan(2 * vals1.size()));
  }

  @Test
  void midAnimationChangeTest() throws InterruptedException, ExecutionException, TimeoutException {
    var src = Var.newSimpleVar(100.0);
    var anim = src.animate(Duration.ofMillis(200), Interpolator.EASE_BOTH_DOUBLE);
    var vals = new ArrayList<Double>();

    Platform.runLater(() -> {
      EventStreams.valuesOf(anim).subscribe(vals::add);

      // when animated value reaches 200.0, set src to -1.0
      anim.addListener((obs, old, newVal) -> {
        if (newVal >= 200.0) {
          src.setValue(-1.0);
        }
      });

      src.setValue(300.0);
    });

    waitUntil(anim, x -> x == -1.0, 1000);

    assertTrue(vals.stream().noneMatch(x -> x == 300.0), "Value 300.0 never reached");
  }

}
