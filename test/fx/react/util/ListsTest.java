package fx.react.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class ListsTest {

  @Test
  void testConcatStackSafety() {
    var singleton = Collections.singletonList("");
    var list = singleton;

    for (var i = 0; i < 100_000; ++i) {
      list = Lists.concat(list, singleton);
    }

    list.size();
    list.get(50_000);
  }

  @Test
  void testConcatSublistStackSafety() {
    var singleton = Collections.singletonList("");
    List<String> list = new ArrayList<>(100_000);

    for (var i = 0; i < 50_000; ++i) {
      list.add("");
    }
    for (var i = 0; i < 50_000; ++i) {
      list = Lists.concat(list.subList(1, 50_000), singleton);
    }

    list.size();
    list.get(0);
  }
}
