package fx.react.collection;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import fx.react.value.Var;

class ValAsListTest {

  @Test
  void testNullToValChange() {
    var src = Var.<String>newSimpleVar(null);
    var list = src.asList();
    assertEquals(0, list.size());

    var mods = new ArrayList<ModifiedList<? extends String>>();
    list.observeModifications(mods::add);

    src.setValue("foo");
    assertEquals(1, mods.size());
    var mod = mods.get(0);
    assertEquals(0, mod.getRemovedSize());
    assertEquals(Collections.singletonList("foo"), mod.getAddedSubList());
    assertEquals(1, list.size());
  }

  @Test
  void testValToNullChange() {
    var src = Var.<String>newSimpleVar("foo");
    var list = src.asList();
    assertEquals(1, list.size());

    var mods = new ArrayList<ModifiedList<? extends String>>();
    list.observeModifications(mods::add);

    src.setValue(null);
    assertEquals(1, mods.size());
    var mod = mods.get(0);
    assertEquals(Collections.singletonList("foo"), mod.getRemoved());
    assertEquals(0, mod.getAddedSize());
    assertEquals(0, list.size());
  }

  @Test
  void testValToValChange() {
    var src = Var.newSimpleVar("foo");
    var list = src.asList();
    assertEquals(1, list.size());

    var mods = new ArrayList<ModifiedList<? extends String>>();
    list.observeModifications(mods::add);

    src.setValue("bar");
    assertEquals(1, mods.size());
    var mod = mods.get(0);
    assertEquals(Collections.singletonList("foo"), mod.getRemoved());
    assertEquals(Collections.singletonList("bar"), mod.getAddedSubList());
    assertEquals(1, list.size());
  }

}
