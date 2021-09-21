package fx.layout.flow;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.scene.shape.Rectangle;

import fx.jupiter.Fx;

class VirtualFlowTest { // extends FlowlessTestBase

  @Test @Fx
  void idempotentShowTest() {
    // create VirtualFlow with 1 big cell
    var rect = new Rectangle(500, 500);
    var items = FXCollections.singletonObservableList(rect);
    var vf = Viewport.createVertical(items, Cell::wrapNode);
    vf.resize(100, 100); // size of VirtualFlow less than that of the cell
    vf.layout();

    vf.show(110.0);
    vf.show(110.0);
    vf.layout();
    assertEquals(-10.0, rect.getBoundsInParent().getMinY(), 0.01);
  }

  @Test
  void idempotentSetLengthOffsetTest() {
    // create VirtualFlow with 1 big cell
    var rect = new Rectangle(500, 500);
    var items = FXCollections.singletonObservableList(rect);
    var vf = Viewport.createVertical(items, Cell::wrapNode);
    vf.resize(100, 100); // size of VirtualFlow less than that of the cell
    vf.layout();

    vf.setLengthOffset(10.0);
    vf.setLengthOffset(10.0);
    vf.layout();
    assertEquals(-10.0, rect.getBoundsInParent().getMinY(), 0.01);
  }

  @Test
  void fastVisibleIndexTest() {
    var items = FXCollections.<Rectangle>observableArrayList();
    for (var i = 0; i < 100; i++) {
      items.add(new Rectangle(500, 100));
    }
    var vf = Viewport.createVertical(items, Cell::wrapNode);
    vf.resize(100, 450); // size of VirtualFlow enough to show several cells
    vf.layout();

    var visibleCells = vf.visibleCells();

    vf.show(0);
    vf.layout();
    assertSame(visibleCells.get(0), vf.getCell(vf.getFirstVisibleIndex()));
    assertSame(visibleCells.get(visibleCells.size() - 1), vf.getCell(vf.getLastVisibleIndex()));
    assertTrue(vf.getFirstVisibleIndex() <= 0 && 0 <= vf.getLastVisibleIndex());

    vf.show(50);
    vf.layout();
    assertSame(visibleCells.get(0), vf.getCell(vf.getFirstVisibleIndex()));
    assertSame(visibleCells.get(visibleCells.size() - 1), vf.getCell(vf.getLastVisibleIndex()));
    assertTrue(vf.getFirstVisibleIndex() <= 50 && 50 <= vf.getLastVisibleIndex());

    vf.show(99);
    vf.layout();
    assertSame(visibleCells.get(0), vf.getCell(vf.getFirstVisibleIndex()));
    assertSame(visibleCells.get(visibleCells.size() - 1), vf.getCell(vf.getLastVisibleIndex()));
    assertTrue(vf.getFirstVisibleIndex() <= 99 && 99 <= vf.getLastVisibleIndex());
  }

}
