package fx.react.collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import fx.react.value.Var;

class ValAsListTest {

  @Test
  void testNullToValChange() {
    Var<String> src = Var.newSimpleVar(null);
    LiveList<String> list = src.asList();
    assertEquals(0, list.size());

    List<ModifiedList<? extends String>> mods = new ArrayList<>();
    list.observeModifications(mods::add);

    src.setValue("foo");
    assertEquals(1, mods.size());
    ModifiedList<? extends String> mod = mods.get(0);
    assertEquals(0, mod.getRemovedSize());
    assertEquals(Collections.singletonList("foo"), mod.getAddedSubList());
    assertEquals(1, list.size());
  }

  @Test
  void testValToNullChange() {
    Var<String> src = Var.newSimpleVar("foo");
    LiveList<String> list = src.asList();
    assertEquals(1, list.size());

    List<ModifiedList<? extends String>> mods = new ArrayList<>();
    list.observeModifications(mods::add);

    src.setValue(null);
    assertEquals(1, mods.size());
    ModifiedList<? extends String> mod = mods.get(0);
    assertEquals(Collections.singletonList("foo"), mod.getRemoved());
    assertEquals(0, mod.getAddedSize());
    assertEquals(0, list.size());
  }

  @Test
  void testValToValChange() {
    Var<String> src = Var.newSimpleVar("foo");
    LiveList<String> list = src.asList();
    assertEquals(1, list.size());

    List<ModifiedList<? extends String>> mods = new ArrayList<>();
    list.observeModifications(mods::add);

    src.setValue("bar");
    assertEquals(1, mods.size());
    ModifiedList<? extends String> mod = mods.get(0);
    assertEquals(Collections.singletonList("foo"), mod.getRemoved());
    assertEquals(Collections.singletonList("bar"), mod.getAddedSubList());
    assertEquals(1, list.size());
  }

}
