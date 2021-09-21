package fx.react;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

class SizeOfTest {

  @Test
  void test() {
    ObservableList<Integer> list = FXCollections.observableArrayList();
    EventStream<Integer> size = EventStreams.sizeOf(list);
    List<Integer> sizes = new ArrayList<>();
    Subscription sub = size.subscribe(sizes::add);
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
