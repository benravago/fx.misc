package fx.react;

import fx.react.value.Val;
import javafx.beans.value.ObservableValue;

/**
 * Observable boolean that changes value when suspended.
 * Which boolean value is the value of the base state and which is the value
 * of the suspended state depends on the implementation. */
public interface Toggle extends Val<Boolean>, Suspendable {

  /**
   * Creates a {@linkplain Toggle} view of an observable boolean and a
   * {@linkplain Suspendable} whose suspension causes the boolean value
   * to switch.
   * @param obs boolean value that indicates suspension of {@code suspender}.
   * @param suspender Assumed to switch the value of {@code obs} when
   * suspended and switch back when resumed, unless there are other suspenders
   * keeping it in the value corresponding to the suspended state.
   */
  static Toggle from(ObservableValue<Boolean> obs, Suspendable suspender) {
    return new ToggleFromVal(Val.wrap(obs), suspender);
  }

}