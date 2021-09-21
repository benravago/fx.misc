package fx.undo.manager;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ZeroSizeChangeQueueTest {

  @Test
  void testPositionValidityOnOverflow() {
    var queue = new ZeroSizeChangeQueue<Integer>();
    var pos0 = queue.getCurrentPosition();
    assertTrue(pos0.isValid());
    queue.push(1);
    assertFalse(pos0.isValid());
    assertTrue(queue.getCurrentPosition().isValid());
  }

}
