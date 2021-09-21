package fx.react.value;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.beans.property.SimpleIntegerProperty;

import fx.react.EventStreams;

class VarFromValTest {

  @Test
  void test() {
    var src = new SimpleIntegerProperty(0);
    var twice = src.multiply(2);
    var twiceVar = Var.fromVal(twice, n -> src.set(n.intValue() / 2));

    var values = new ArrayList<Number>();
    EventStreams.valuesOf(twiceVar).subscribe(values::add);

    src.set(1);
    twiceVar.setValue(4);
    twiceVar.setValue(5); // no effect
    twiceVar.setValue(7); // will become 6

    assertEquals(Arrays.asList(0, 2, 4, 6), values);
  }

}
