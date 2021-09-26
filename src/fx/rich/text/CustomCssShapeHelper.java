package fx.rich.text;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.scene.control.IndexRange;
import javafx.scene.shape.Path;

class CustomCssShapeHelper<T> {

  final List<Range<T, IndexRange>> ranges = new LinkedList<>();
  final List<Path> shapes = new LinkedList<>();

  final Supplier<Path> createShape;
  final BiConsumer<Path, Range<T, IndexRange>> configureShape;
  final Consumer<Path> addToChildren;
  final Consumer<Collection<Path>> clearUnusedShapes;

  CustomCssShapeHelper(Supplier<Path> createShape, BiConsumer<Path, Range<T, IndexRange>> configureShape, Consumer<Path> addToChildren, Consumer<Collection<Path>> clearUnusedShapes) {
    this.createShape = createShape;
    this.configureShape = configureShape;
    this.addToChildren = addToChildren;
    this.clearUnusedShapes = clearUnusedShapes;
  }

  record Range<T,I>(T value, I index){}

  /**
   * Calculates the range of a value (background color, underline, etc.) that is shared between multiple
   * consecutive {@link TextExt} nodes
   */
  void updateSharedShapeRange(T value, int start, int end, BiFunction<T, T, Boolean> equals) {
    Runnable addNewValueRange = () -> ranges.add(new Range<>(value, new IndexRange(start, end)));

    if (ranges.isEmpty()) {
      addNewValueRange.run();
    } else {
      var lastIndex = ranges.size() - 1;
      var lastShapeValueRange = ranges.get(lastIndex);
      var lastShapeValue = lastShapeValueRange.value;

      // calculate smallest possible position which is consecutive to the given start position
      var prevEndNext = lastShapeValueRange.index.getEnd();
      if (start == prevEndNext && // Consecutive?
          equals.apply(lastShapeValue, value)) // Same style?
      {
        var lastRange = lastShapeValueRange.index;
        var extendedRange = new IndexRange(lastRange.getStart(), end);
        ranges.set(lastIndex, new Range<>(lastShapeValue, extendedRange));
      } else {
        addNewValueRange.run();
      }
    }
  }

  /**
   * Updates the shapes calculated in {@link #updateSharedShapeRange(Object, int, int, BiFunction)} and
   * configures them via {@code configureShape}.
   */
  void updateSharedShapes() {
    // remove or add shapes, depending on what's needed
    var neededNumber = ranges.size();
    var availableNumber = shapes.size();

    if (neededNumber < availableNumber) {
      var unusedShapes = shapes.subList(neededNumber, availableNumber);
      clearUnusedShapes.accept(unusedShapes);
      unusedShapes.clear();
    } else if (availableNumber < neededNumber) {
      for (var i = 0; i < neededNumber - availableNumber; i++) {
        var shape = createShape.get();
        shapes.add(shape);
        addToChildren.accept(shape);
      }
    }

    // update the shape's color and elements
    for (var i = 0; i < ranges.size(); i++) {
      configureShape.accept(shapes.get(i), ranges.get(i));
    }

    // clear, since it's no longer needed
    ranges.clear();
  }
}
