package fx.undo;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import fx.react.EventSource;
import fx.react.value.Var;

class UndoManagerTest {

  @SafeVarargs
  static <T> List<T> list(T... items) {
    return Arrays.asList(items);
  }

  @Test
  void testSingleChangeUndoInvertsTheChange() {
    var changes = new EventSource<Integer>();
    var lastAction = Var.<Integer>newSimpleVar(null);
    var um = UndoManagerFactory.unlimitedHistorySingleChange(
      changes,
      i -> -i,
      i -> {
        lastAction.setValue(i);
        changes.push(i);
      }
    );

    changes.push(3);
    changes.push(7);
    assertNull(lastAction.getValue());

    um.undo();
    assertEquals(-7, lastAction.getValue().intValue());

    um.undo();
    assertEquals(-3, lastAction.getValue().intValue());

    um.redo();
    assertEquals(3, lastAction.getValue().intValue());

    um.redo();
    assertEquals(7, lastAction.getValue().intValue());
  }

  @Test
  void testMultiChangeUndoInvertsTheChangesAndReversesTheList() {
    var changes = new EventSource<List<Integer>>();
    var lastChange = Var.<List<Integer>>newSimpleVar(null);
    var um = UndoManagerFactory.unlimitedHistoryMultiChange(
      changes,
      i -> -i,
      i -> {
        lastChange.setValue(i);
        changes.push(i);
      }
    );

    changes.push(list(3, 7));
    assertNull(lastChange.getValue());

    um.undo();
    assertEquals(list(-7, -3), lastChange.getValue());

    um.redo();
    assertEquals(list(3, 7), lastChange.getValue());
  }

  @Test
  void testMark() {
    var changes = new EventSource<Integer>();
    var um = UndoManagerFactory.fixedSizeHistorySingleChange(
      changes,
      c -> c,
      changes::push,
      4
    );

    assertTrue(um.atMarkedPositionProperty().get());
    changes.push(1);
    assertFalse(um.atMarkedPositionProperty().get());
    changes.push(2);
    um.mark();
    assertTrue(um.atMarkedPositionProperty().get());
    changes.push(3);
    changes.push(4);
    assertFalse(um.atMarkedPositionProperty().get());
    um.undo();
    um.undo();
    assertTrue(um.atMarkedPositionProperty().get());
    changes.push(3);
    changes.push(4);
    changes.push(5); // overflow
    changes.push(6);
    assertFalse(um.atMarkedPositionProperty().get());
  }

  @Test
  void testPositionValidAfterAddingAChange() {
    var changes = new EventSource<Integer>();
    var um = UndoManagerFactory.unlimitedHistorySingleChange(
      changes,
      c -> c,
      changes::push
    );

    changes.push(1);
    var pos = um.getCurrentPosition();
    changes.push(1);
    assertTrue(pos.isValid());
  }

  @Test
  void testPositionInvalidAfterSingleChangeMerge() {
    var changes = new EventSource<Integer>();
    var um = UndoManagerFactory.unlimitedHistorySingleChange(
      changes,
      c -> -c,
      changes::push,
      (c1, c2) -> Optional.of(c1 + c2)
    );

    changes.push(1);
    var pos = um.getCurrentPosition();
    changes.push(1);
    assertFalse(pos.isValid());
  }

  @Test
  void testPositionInvalidAfterMultiChangeMerge() {
    var changes = new EventSource<List<Integer>>();
    var um = UndoManagerFactory.unlimitedHistoryMultiChangeUM(
      changes,
      c -> -c,
      changes::push,
      (c1, c2) -> Optional.of(c1 + c2)
    );

    changes.push(list(1));
    var pos = um.getCurrentPosition();
    changes.push(list(1));
    assertFalse(pos.isValid());
  }

  @Test
  void testRedoUnavailableAfterSingleChangeAnnihilation() {
    var changes = new EventSource<Integer>();
    var um = UndoManagerFactory.unlimitedHistorySingleChange(
      changes,
      c -> -c,
      changes::push,
      (c1, c2) -> Optional.of(c1 + c2),
      c -> c == 0
    );

    changes.push(1);
    changes.push(-1);
    assertFalse(um.isRedoAvailable());
  }

  @Test
  void testRedoUnavailableAfterMultiChangeAnnihilation() {
    var changes = new EventSource<List<Integer>>();
    var um = UndoManagerFactory.unlimitedHistoryMultiChange(
      changes,
      c -> -c,
      changes::push,
      (c1, c2) -> Optional.of(c1 + c2),
      c -> c == 0
    );

    changes.push(list(1, 2, 3));
    changes.push(list(-1, -2, -3));
    assertFalse(um.isRedoAvailable());
  }

  @Test
  void zeroHistoryUndoManagerMark() {
    var changes = new EventSource<Integer>();
    var um = UndoManagerFactory.zeroHistorySingleChange(changes);

    assertTrue(um.atMarkedPositionProperty().get());
    changes.push(1);
    assertFalse(um.atMarkedPositionProperty().get());
    changes.push(2);
    um.mark();
    assertTrue(um.atMarkedPositionProperty().get());
    changes.push(3);
    changes.push(4);
    assertFalse(um.atMarkedPositionProperty().get());
  }

  /**
   * Tests that isAtMarkedPosition() forces atMarkedPositionProperty()
   * become valid.
   */
  @Test
  void testAtMarkedPositionRevalidation() {
    var changes = new EventSource<Integer>();
    var um = UndoManagerFactory.zeroHistorySingleChange(changes);

    um.atMarkedPositionProperty().get(); // atMarkedPositionProperty is now valid

    // we are going to expect two invalidations
    var latch = new CountDownLatch(2);
    um.atMarkedPositionProperty().addListener(observable -> latch.countDown());

    changes.push(1); // atMarkedPositionProperty has been invalidated
    assertEquals(1, latch.getCount());

    um.isAtMarkedPosition(); // we want to test whether this caused revalidation of atMarkedPositionProperty

    changes.push(2); // should have caused invalidation of atMarkedPositionProperty
    assertEquals(0, latch.getCount());
  }

  @Test

  void testFailFastWhenExpectedChangeNotReceived() {
    assertThrows(IllegalStateException.class, () -> {
      var changes = new EventSource<Integer>();
      var um = UndoManagerFactory.unlimitedHistorySingleChange(changes, i -> -i, i -> {});
      changes.push(1);
      um.undo(); // should throw because the undone change is not received back
    });
  }

  // Identity Change Tests

  @Test
  void testPushedNonIdentitySingleChangeIsStored() {
    var lastAppliedValue = new SimpleIntegerProperty(0);
    var changes = new EventSource<Integer>();
    var um = UndoManagerFactory.unlimitedHistorySingleChange(
      changes,
      i -> -i, // invert
      i -> {
        lastAppliedValue.set(i);
        changes.push(i);
      }, // apply change and re-emit value so expected change is received
      (a, b) -> Optional.of(a + b), // merge adds two changes together
      i -> i == 0 // identity change = 0
    );

    changes.push(4);
    assertTrue(um.isUndoAvailable());
    um.undo();
    assertEquals(-4, lastAppliedValue.get());
    assertFalse(um.isUndoAvailable());
  }

  @Test
  void testPushedNonIdentityMultiChangeIsStored() {
    var lastAppliedValue = new SimpleObjectProperty<List<Integer>>();
    var changes = new EventSource<List<Integer>>();
    var um = UndoManagerFactory.unlimitedHistoryMultiChange(
      changes,
      i -> -i, // invert
      i -> {
        lastAppliedValue.set(i);
        changes.push(i);
      }, // apply redo and re-emit value so expected change is received
      (a, b) -> Optional.of(a + b), // merge adds two changes together
      i -> i == 0 // identity change = 0
    );

    changes.push(list(4, 5));
    assertTrue(um.isUndoAvailable());
    um.undo();
    assertEquals(list(-5, -4), lastAppliedValue.get());
    assertFalse(um.isUndoAvailable());
  }

  @Test
  void testPushedIdentitySingleChangeIsNotStored() {
    var lastAppliedValue = new SimpleIntegerProperty(0);
    var changes = new EventSource<Integer>();
    var um = UndoManagerFactory.unlimitedHistorySingleChange(
      changes,
      i -> -i, // invert
      i -> {
        lastAppliedValue.set(i);
        changes.push(i);
      }, // apply change and re-emit value so expected change is received
      (a, b) -> Optional.of(a + b), // merge adds two changes together
      i -> i == 0 // identity change = 0
    );

    // force lastAppliedValue to store non-zero value
    changes.push(4);
    um.undo();

    // test that pushed identity change is not stored
    changes.push(0);
    assertFalse(um.isUndoAvailable());
    assertEquals(-4, lastAppliedValue.get());
  }

  @Test
  void testPushedIdentityMultiChangeIsNotStored() {
    var lastAppliedValue = new SimpleObjectProperty<List<Integer>>();
    var changes = new EventSource<List<Integer>>();
    var um = UndoManagerFactory.unlimitedHistoryMultiChange(
      changes,
      i -> -i, // invert
      i -> {
        lastAppliedValue.set(i);
        changes.push(i);
      }, // apply redo and re-emit value so expected change is received
      (a, b) -> Optional.of(a + b), // merge adds two changes together
      i -> i == 0 // identity change = 0
    );

    // force lastAppliedValue to store non-zero value
    changes.push(list(4, 8));
    um.undo();

    // test that pushed identity change is not stored
    changes.push(list(0, 0));
    assertFalse(um.isUndoAvailable());
    assertEquals(list(-8, -4), lastAppliedValue.get());
  }

  @Test
  void testMergeResultingInIdentitySingleChangeAnnihilatesBothAndPreventsNextMerge() {
    var lastAppliedValue = new SimpleIntegerProperty(0);
    var changes = new EventSource<Integer>();
    var um = UndoManagerFactory.unlimitedHistorySingleChange(
      changes,
      i -> -i, // invert
      i -> {
        lastAppliedValue.set(i);
        changes.push(i);
      }, // apply change and re-emit value so expected change is received
      (a, b) -> Optional.of(a + b), // merge adds two changes together
      i -> i == 0 // identity change = 0
    );

    // have at least one change stored
    changes.push(6);
    // prevent next merge from occurring
    um.preventMerge();

    // now push the identity-resulting merge changes
    changes.push(-3); // change A
    changes.push(3); // change B

    // changes should annihilate; neither are stored
    assertTrue(um.isUndoAvailable());
    um.undo();
    assertFalse(um.isUndoAvailable());
    assertEquals(-6, lastAppliedValue.get());

    um.redo(); // redo to test whether merge occurs on next push
    changes.push(3);
    assertTrue(um.isUndoAvailable());
    um.undo();
    assertTrue(um.isUndoAvailable());
    assertEquals(-3, lastAppliedValue.get());
  }

  @Test
  void testMergeResultingInIdentityMultiChangeAnnihilatesBothAndPreventsNextMerge() {
    var lastAppliedValue = new SimpleObjectProperty<List<Integer>>();
    var changes = new EventSource<List<Integer>>();
    var um = UndoManagerFactory.unlimitedHistoryMultiChange(
      changes,
      i -> -i, // invert
      i -> {
        lastAppliedValue.set(i);
        changes.push(i);
      }, // apply redo and re-emit value so expected change is received
      (a, b) -> Optional.of(a + b), // merge adds two changes together
      i -> i == 0 // identity change = 0
    );

    // have at least one change stored
    changes.push(list(6, 9));
    // prevent next merge from occurring
    um.preventMerge();

    // now push the identity-resulting merge changes
    changes.push(list(-3, -4)); // change A
    changes.push(list(3, 4)); // change B

    // changes should annihilate; neither are stored
    assertTrue(um.isUndoAvailable());
    um.undo();
    assertFalse(um.isUndoAvailable());
    assertEquals(list(-9, -6), lastAppliedValue.get());

    um.redo(); // redo to test whether merge occurs on next push
    changes.push(list(3, 4));
    assertTrue(um.isUndoAvailable());
    um.undo();
    assertTrue(um.isUndoAvailable());
    assertEquals(list(-4, -3), lastAppliedValue.get());
  }

  @Test
  void testMergeResultingInNonIdentitySingleChangeStoresMergeAndPreventsNextMerge() {
    var lastAppliedValue = new SimpleIntegerProperty(0);
    var changes = new EventSource<Integer>();
    var um = UndoManagerFactory.unlimitedHistorySingleChange(
      changes,
      i -> -i, // invert
      i -> {
        lastAppliedValue.set(i);
        changes.push(i);
      }, // apply change and re-emit value so expected change is received
      (a, b) -> Optional.of(a + b), // merge adds two changes together
      i -> i == 0 // identity change = 0
    );

    changes.push(1);
    changes.push(2);
    assertTrue(um.isUndoAvailable());
    um.undo();
    assertFalse(um.isUndoAvailable());
    assertEquals(-3, lastAppliedValue.get());

    um.redo(); // redo to test whether merge occurs on next push
    changes.push(5);
    assertTrue(um.isUndoAvailable());
    um.undo();
    assertEquals(-5, lastAppliedValue.get());
  }

  @Test
  void testMergeResultingInNonIdentityMultiChangeStoresMergeAndPreventsNextMerge() {
    var lastAppliedValue = new SimpleObjectProperty<List<Integer>>();
    var changes = new EventSource<List<Integer>>();
    var um = UndoManagerFactory.unlimitedHistoryMultiChange(
      changes,
      i -> -i, // invert
      i -> {
        lastAppliedValue.set(i);
        changes.push(i);
      }, // apply change and re-emit value so expected change is received
      (a, b) -> Optional.of(a + b), // merge adds two changes together
      i -> i == 0 // identity change = 0
    );

    changes.push(list(1, 4));
    changes.push(list(2, 5));
    assertTrue(um.isUndoAvailable());
    um.undo();
    assertFalse(um.isUndoAvailable());
    assertEquals(list(-9, -3), lastAppliedValue.get());

    um.redo(); // redo to test whether merge occurs on next push
    changes.push(list(5, 8));
    assertTrue(um.isUndoAvailable());
    um.undo();
    assertEquals(list(-8, -5), lastAppliedValue.get());
  }

}
