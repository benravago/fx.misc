package fx.rich.text.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import static fx.rich.text.model.TwoDimensional.Bias.*;

class TwoLevelNavigatorTest {

  // navigator with 5 elements, each of length 10
  final TwoLevelNavigator navigator = new TwoLevelNavigator(() -> 5, i -> 10);

  @Test
  void testPositiveOffsetWithBackwardBias() {
    var pos = navigator.offsetToPosition(10, Backward);
    assertEquals(0, pos.getMajor());
    assertEquals(10, pos.getMinor());
  }

  @Test
  void testPositiveOffsetWithForwardBias() {
    var pos = navigator.offsetToPosition(10, Forward);
    assertEquals(1, pos.getMajor());
    assertEquals(0, pos.getMinor());
  }

  @Test
  void testNegativeOffsetWithBackwardBias() {
    var pos = navigator.position(4, 10);
    pos = pos.offsetBy(-10, Backward);
    assertEquals(3, pos.getMajor());
    assertEquals(10, pos.getMinor());
  }

  @Test
  void testNegativeOffsetWithForwardBias() {
    var pos = navigator.position(4, 10);
    pos = pos.offsetBy(-10, Forward);
    assertEquals(4, pos.getMajor());
    assertEquals(0, pos.getMinor());
  }

  @Test
  void testZeroOffsetWithBackwardBias() {
    var pos = navigator.position(3, 0);
    pos = pos.offsetBy(0, Backward);
    assertEquals(2, pos.getMajor());
    assertEquals(10, pos.getMinor());

    // additional zero backward offset should have no effect
    assertEquals(pos, pos.offsetBy(0, Backward));
  }

  @Test
  void testZeroOffsetWithForwardBias() {
    var pos = navigator.position(2, 10);
    pos = pos.offsetBy(0, Forward);
    assertEquals(3, pos.getMajor());
    assertEquals(0, pos.getMinor());

    // additional zero forward offset should have no effect
    assertEquals(pos, pos.offsetBy(0, Forward));
  }

  @Test
  void testRightBoundary() {
    var pos = navigator.offsetToPosition(100, Forward);
    assertEquals(4, pos.getMajor());
    assertEquals(60, pos.getMinor());

    pos = pos.clamp();
    assertEquals(4, pos.getMajor());
    assertEquals(9, pos.getMinor());
  }

  @Test
  void testLeftBoundary() {
    var pos = navigator.offsetToPosition(25, Forward).offsetBy(-50, Forward);
    assertEquals(0, pos.getMajor());
    assertEquals(0, pos.getMinor());
  }

}
