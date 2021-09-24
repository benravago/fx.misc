package fx.rich.text.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

import fx.react.EventStream;
import fx.react.Subscription;
import fx.react.value.Val;

class SubscribeableContentsObsSetTest {

  class BoxedProperty implements Comparable<BoxedProperty> {

    final int badHash;

    BoxedProperty() {
      this(0);
    }

    BoxedProperty(int hash) {
      badHash = hash;
    }

    final SimpleIntegerProperty property = new SimpleIntegerProperty(0);

    final EventStream<Integer> intValues =
      // create a stream of int values for property
      Val.wrap(property).values().map(Number::intValue)
      // ignore the first 0 value
      .filter(n -> n != 0);

    void addOne() {
      property.set(property.get() + 1);
    }

    @Override
    public int compareTo(BoxedProperty o) {
      return Integer.compare(badHash, o.badHash);
    }
    @Override
    public String toString() {
      return "BoxedProperty@" + hashCode();
    }
    @Override
    public int hashCode() {
      return badHash;
    }
  }

  @Test
  void adding_subscriber_after_content_is_added_will_subscribe_to_changes() {
    var contentSet = new SubscribeableContentsObsSet<BoxedProperty>();

    var box = new BoxedProperty();
    contentSet.add(box);

    var storageList = new ArrayList<Integer>();

    // when property is set to a new value, store the new value in storageList
    contentSet.addSubscriber(b -> b.intValues.subscribe(storageList::add));

    var numberOfTimes = 3;
    forEach(numberOfTimes, box::addOne);
    assertEquals(numberOfTimes, storageList.size());
  }

  @Test
  void adding_subscriber_before_content_is_added_will_subscribe_to_changes_when_item_is_added() {
    var contentSet = new SubscribeableContentsObsSet<BoxedProperty>();

    var storageList = new ArrayList<Integer>();

    // when property is set to a new value, store the new value in storageList
    contentSet.addSubscriber(b -> b.intValues.subscribe(storageList::add));

    var box = new BoxedProperty();
    contentSet.add(box);

    var numberOfTimes = 3;
    forEach(numberOfTimes, box::addOne);

    assertEquals(numberOfTimes, storageList.size());
  }

  @Test
  void removing_item_from_list_will_stop_subscription() {
    var contentSet = new SubscribeableContentsObsSet<BoxedProperty>();

    var storageList = new LinkedList<Integer>();

    // when property is set to a new value, store the new value in storageList
    contentSet.addSubscriber(b -> b.intValues.subscribe(storageList::add));

    var box = new BoxedProperty();
    contentSet.add(box);

    var numberOfTimes = 3;
    forEach(numberOfTimes, box::addOne);

    contentSet.remove(box);

    forEach(2, box::addOne);

    assertEquals(3, storageList.size());
  }

  @Test
  void adding_subscriber_and_removing_it_will_not_throw_exception() {
    var set = new SubscribeableContentsObsSet<Integer>();
    var removeSubscriber = set.addSubscriber(i -> Subscription.EMPTY);
    removeSubscriber.unsubscribe();
  }

  @Test
  void adding_subscriber_and_later_removing_it_will_unsubscribe_from_all_elements() {
    var contentSet = new SubscribeableContentsObsSet<BoxedProperty>();

    var storageList = new LinkedList<Integer>();

    // when property is set to a new value, store the new value in storageList
    var removeSubscriber = contentSet.addSubscriber(b -> b.intValues.subscribe(storageList::add));

    var box1 = new BoxedProperty(1);
    var box2 = new BoxedProperty(2);
    contentSet.add(box1);
    contentSet.add(box2);

    box1.addOne();
    box2.addOne();
    assertEquals(2, storageList.size());

    storageList.clear();
    removeSubscriber.unsubscribe();

    box1.addOne();
    box2.addOne();

    assertEquals(0, storageList.size());
  }

  @Test
  void adding_new_subscriber_when_list_has_contents_does_not_fire_change_event() {
    var contentSet = new SubscribeableContentsObsSet<Integer>();

    contentSet.add(1);
    contentSet.add(2);
    contentSet.add(3);

    var changeWasFired = new SimpleBooleanProperty(false);
    var removeChangeListener = contentSet.addChangeListener(change -> changeWasFired.set(true));

    contentSet.addSubscriber(b -> Subscription.EMPTY);

    assertFalse(changeWasFired.get());

    // cleanup
    removeChangeListener.unsubscribe();
  }

  @Test
  void adding_new_subscriber_when_list_has_contents_does_not_fire_invalidation_event() {
    var contentSet = new SubscribeableContentsObsSet<Integer>();

    contentSet.add(1);
    contentSet.add(2);
    contentSet.add(3);

    // when a change occurs add the additions/removals in another list
    var changeWasFired = new SimpleBooleanProperty(false);

    var removeInvalidationListener = contentSet.addInvalidationListener(change -> changeWasFired.set(true));

    // when property is set to a new value, store the new value in storageList
    contentSet.addSubscriber(ignore -> Subscription.EMPTY);

    assertFalse(changeWasFired.get());

    // cleanup
    removeInvalidationListener.unsubscribe();
  }

  @Test
  void adding_and_removing_element_fires_change_event() {
    var set = new SubscribeableContentsObsSet<Integer>();

    var added = new SimpleBooleanProperty(false);
    var removed = new SimpleBooleanProperty(false);

    set.addChangeListener(change -> {
      if (change.wasAdded()) {
        added.set(true);
      } else if (change.wasRemoved()) {
        removed.set(true);
      }
    });

    var value = 3;

    set.add(value);
    assertTrue(added.get());
    assertFalse(removed.get());

    added.set(false);

    set.remove(value);
    assertFalse(added.get());
    assertTrue(removed.get());
  }

  @Test
  void adding_and_removing_element_fires_invalidation_event() {
    var set = new SubscribeableContentsObsSet<Integer>();

    var box = new BoxedProperty();

    set.addInvalidationListener(change -> box.addOne());

    var value = 5;

    set.add(value);
    assertEquals(1, box.property.get());

    set.remove(value);
    assertEquals(2, box.property.get());
  }

  static void forEach(int times, Runnable action) {
    for (var i = 0; i < times; i++) {
      action.run();
    }
  }

}
