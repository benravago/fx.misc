package fx.react.collection;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.collections.FXCollections;

import fx.react.EventStreams;

class ListSizeTest {

  @Test
  void test() {
    var list = FXCollections.<Integer>observableArrayList();
    var size = LiveList.sizeOf(list);
    var sizes = new ArrayList<Integer>();
    var sub = EventStreams.valuesOf(size).subscribe(sizes::add);
    list.add(1);
    list.addAll(2, 3, 4);
    assertEquals(Arrays.asList(0, 1, 4), sizes);

    sub.unsubscribe();
    sizes.clear();
    list.addAll(5, 6);
    assertEquals(Arrays.asList(), sizes);

    EventStreams.valuesOf(size).subscribe(sizes::add);
    list.addAll(7, 8);
    assertEquals(Arrays.asList(6, 8), sizes);
  }

}
