package fx.react.value;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.beans.property.SimpleIntegerProperty;

import fx.react.collection.Change;

class ValTest {

  @Test
  void changesTest() {
    var src = new SimpleIntegerProperty(0);
    var val = Val.wrap(src);

    var changes = new ArrayList<Change<Number>>();
    val.changes().subscribe(changes::add);

    src.set(1);
    src.set(2);
    src.set(3);

    assertArrayEquals(Arrays.asList(0, 1, 2).toArray(), changes.stream().map(change -> change.getOldValue()).toArray());
    assertArrayEquals(Arrays.asList(1, 2, 3).toArray(), changes.stream().map(change -> change.getNewValue()).toArray());
  }

}
