package fx.react.value;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import fx.react.collection.Change;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

class ValTest {

  @Test
  void changesTest() {
    IntegerProperty src = new SimpleIntegerProperty(0);
    Val<Number> val = Val.wrap(src);

    List<Change<Number>> changes = new ArrayList<>();
    val.changes().subscribe(changes::add);

    src.set(1);
    src.set(2);
    src.set(3);

    assertArrayEquals(Arrays.asList(0, 1, 2).toArray(), changes.stream().map(change -> change.getOldValue()).toArray());
    assertArrayEquals(Arrays.asList(1, 2, 3).toArray(), changes.stream().map(change -> change.getNewValue()).toArray());
  }

}
