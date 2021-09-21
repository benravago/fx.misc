package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.collections.FXCollections;

class SizeOfTest {

  @Test
  void test() {
    var list = FXCollections.<Integer>observableArrayList();
    var size = EventStreams.sizeOf(list);
    var sizes = new ArrayList<Integer>();
    var sub = size.subscribe(sizes::add);
    list.add(1);
    list.addAll(2, 3, 4);
    assertEquals(Arrays.asList(0, 1, 4), sizes);

    sub.unsubscribe();
    sizes.clear();
    list.addAll(5, 6);
    assertEquals(Arrays.asList(), sizes);

    size.subscribe(sizes::add);
    list.addAll(7, 8);
    assertEquals(Arrays.asList(6, 8), sizes);
  }

}
