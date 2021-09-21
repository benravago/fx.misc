package fx.react.value;

import java.time.Duration;
import java.util.function.BiFunction;

import javafx.animation.Transition;
import javafx.beans.value.ObservableValue;

import fx.react.Subscription;
import fx.react.util.Interpolator;

class AnimatedVal<T> extends ValBase<T> {

  class FractionTransition extends Transition {
    @Override
    protected void interpolate(double frac) {
      fraction = frac;
      invalidate();
    }
    void setDuration(Duration d) {
      setCycleDuration(javafx.util.Duration.millis(d.toMillis()));
    }
  }

  final ObservableValue<T> src;
  final BiFunction<? super T, ? super T, Duration> duration;
  final Interpolator<T> interpolator;
  final FractionTransition transition = new FractionTransition();

  double fraction = 1.0;
  T oldValue = null;

  AnimatedVal(ObservableValue<T> src, BiFunction<? super T, ? super T, Duration> duration, Interpolator<T> interpolator) {
    this.src = src;
    this.duration = duration;
    this.interpolator = interpolator;
  }

  @Override
  protected Subscription connect() {
    oldValue = src.getValue();
    return Val.observeChanges(src, (obs, oldVal, newVal) -> {
      oldValue = getValue();
      var d = duration.apply(oldValue, newVal);
      transition.setDuration(d);
      transition.playFromStart();
    });
  }

  @Override
  protected T computeValue() {
    return fraction == 1.0 ? src.getValue() : interpolator.interpolate(oldValue, src.getValue(), fraction);
  }

}
