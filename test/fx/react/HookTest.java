package fx.react;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class HookTest {

  /**
   * Tests that the side effect is not allowed to cause recursive event
   * emission.
   */
  @Test
  void recursionPreventionTest() {
    assertThrows(IllegalStateException.class, () -> {
      EventSource<Integer> source = new EventSource<>();
      source.hook(i -> source.push(i - 1)).pin();
      source.push(5);
    });
  }

}
