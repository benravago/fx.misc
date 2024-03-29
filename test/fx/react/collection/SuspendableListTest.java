package fx.react.collection;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;

class SuspendableListTest {

  @Test
  void test() {
    var base = FXCollections.observableArrayList(10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
    var wrapped = LiveList.suspendable(base);
    var mirror = new ArrayList<Integer>(wrapped);
    wrapped.addListener((Change<? extends Integer> change) -> {
      while (change.next()) {
        if (change.wasPermutated()) {
          var newMirror = new ArrayList<Integer>(mirror);
          for (var i = 0; i < mirror.size(); ++i) {
            newMirror.set(change.getPermutation(i), mirror.get(i));
          }
          mirror.clear();
          mirror.addAll(newMirror);
        } else {
          var sub = mirror.subList(change.getFrom(), change.getFrom() + change.getRemovedSize());
          sub.clear();
          sub.addAll(change.getAddedSubList());
        }
      }
    });

    wrapped.suspendWhile(() -> {
      base.addAll(2, Arrays.asList(12, 11, 13));
      base.remove(7, 9);
      base.subList(8, 10).replaceAll(i -> i + 20);
      base.subList(4, 9).clear();
      base.addAll(4, Arrays.asList(16, 18, 25));
      base.sort(null);
      assertEquals(Arrays.asList(10, 9, 8, 7, 6, 5, 4, 3, 2, 1), mirror);
    });

    assertEquals(Arrays.asList(1, 9, 10, 11, 12, 16, 18, 22, 25), mirror);
  }

}
