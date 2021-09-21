package fx.undo.manager;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class UnlimitedChangeQueueTest {

  @Test
  void testPositionValidityOnUndo() {
    var queue = new UnlimitedChangeQueue<Integer>();
    var pos0 = queue.getCurrentPosition();
    queue.push(1);
    var pos1 = queue.getCurrentPosition();
    queue.push(2);
    var pos2 = queue.getCurrentPosition();
    queue.push(3);
    var pos3 = queue.getCurrentPosition();
    queue.push(4);
    var pos4 = queue.getCurrentPosition();

    assertTrue(pos0.isValid());
    assertTrue(pos1.isValid());
    assertTrue(pos2.isValid());
    assertTrue(pos3.isValid());
    assertTrue(pos4.isValid());

    queue.prev();

    assertTrue(pos0.isValid());
    assertTrue(pos1.isValid());
    assertTrue(pos2.isValid());
    assertTrue(pos3.isValid());
    assertTrue(pos4.isValid());

    queue.push(4);

    assertTrue(pos0.isValid());
    assertTrue(pos1.isValid());
    assertTrue(pos2.isValid());
    assertTrue(pos3.isValid());
    assertFalse(pos4.isValid());

    queue.prev();
    queue.prev();
    queue.prev();
    queue.prev();

    assertTrue(pos0.isValid());
    assertTrue(pos1.isValid());
    assertTrue(pos2.isValid());
    assertTrue(pos3.isValid());
    assertFalse(pos4.isValid());

    queue.push(1);

    assertTrue(pos0.isValid());
    assertFalse(pos1.isValid());
    assertFalse(pos2.isValid());
    assertFalse(pos3.isValid());
    assertFalse(pos4.isValid());
  }

  @Test
  void testPositionValidityOnForgetHistory() {
    var queue = new UnlimitedChangeQueue<Integer>();
    var pos0 = queue.getCurrentPosition();
    queue.push(1);
    var pos1 = queue.getCurrentPosition();
    queue.push(2);
    var pos2 = queue.getCurrentPosition();
    queue.push(3);
    var pos3 = queue.getCurrentPosition();
    queue.push(4);
    var pos4 = queue.getCurrentPosition();

    queue.prev();
    queue.prev();
    queue.forgetHistory();

    assertFalse(pos0.isValid());
    assertFalse(pos1.isValid());
    assertTrue(pos2.isValid());
    assertTrue(pos3.isValid());
    assertTrue(pos4.isValid());
  }

  @Test
  void testPositionEquality() {
    var queue = new UnlimitedChangeQueue<Integer>();
    queue.push(1);
    var pos = queue.getCurrentPosition();
    assertEquals(pos, queue.getCurrentPosition());
    queue.push(2);
    assertNotEquals(pos, queue.getCurrentPosition());
    queue.prev();
    assertEquals(pos, queue.getCurrentPosition());
    queue.prev();
    assertNotEquals(pos, queue.getCurrentPosition());
    queue.next();
    assertEquals(pos, queue.getCurrentPosition());
    queue.prev();
    queue.push(3);
    assertNotEquals(pos, queue.getCurrentPosition());
  }

}
