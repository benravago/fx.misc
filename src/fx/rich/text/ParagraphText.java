package fx.rich.text;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;

import fx.react.state.Tuple2;
import fx.react.state.Tuples;
import fx.react.value.Val;
import fx.rich.text.model.Paragraph;
import fx.rich.text.model.StyledSegment;

/**
 * The class responsible for rendering the segments in an paragraph. It also renders additional RichTextFX-specific
 * CSS found in {@link TextExt} as well as the selection and caret shapes.
 *
 * @param <PS> paragraph style type
 * @param <SEG> segment type
 * @param <S> segment style type
 */
class ParagraphText<PS, SEG, S> extends TextFlowExt {

  final ObservableSet<CaretNode> carets = FXCollections.observableSet(new HashSet<>(1));

  public final ObservableSet<CaretNode> caretsProperty() {
    return carets;
  }

  final ObservableMap<Selection<PS, SEG, S>, SelectionPath> selections = FXCollections.observableMap(new HashMap<>(1));

  public final ObservableMap<Selection<PS, SEG, S>, SelectionPath> selectionsProperty() {
    return selections;
  }

  final MapChangeListener<? super Selection<PS, SEG, S>, ? super SelectionPath> selectionPathListener;
  final SetChangeListener<? super CaretNode> caretNodeListener;

  // FIXME: changing it currently has not effect, because
  // Text.impl_selectionFillProperty().set(newFill) doesn't work
  // properly for Text node inside a TextFlow (as of JDK8-b100).
  final ObjectProperty<Paint> highlightTextFill = new SimpleObjectProperty<>(Color.WHITE);

  public ObjectProperty<Paint> highlightTextFillProperty() {
    return highlightTextFill;
  }

  Paragraph<PS, SEG, S> paragraph;

  final CustomCssShapeHelper<Paint> backgroundShapeHelper;
  final CustomCssShapeHelper<BorderAttributes> borderShapeHelper;
  final CustomCssShapeHelper<UnderlineAttributes> underlineShapeHelper;

  // Note: order of children matters because later children cover up earlier children:
  // towards children's 0 index:
  //      background shapes
  //      selection shapes - always add to selectionShapeStartIndex
  //      border shapes
  //      text
  //      underline shapes
  //      caret shapes
  // towards getChildren().size() - 1 index
  int selectionShapeStartIndex = 0;

  ParagraphText(Paragraph<PS, SEG, S> par, Function<StyledSegment<SEG, S>, Node> nodeFactory) {
    this.paragraph = par;

    getStyleClass().add("paragraph-text");

    var leftInset = Val.map(insetsProperty(), Insets::getLeft);
    var topInset = Val.map(insetsProperty(), Insets::getTop);

    ChangeListener<IndexRange> selectionRangeListener = (obs, ov, nv) -> requestLayout();
    selectionPathListener = change -> {
      if (change.wasRemoved()) {
        var p = change.getValueRemoved();
        p.rangeProperty().removeListener(selectionRangeListener);
        p.layoutXProperty().unbind();
        p.layoutYProperty().unbind();

        getChildren().remove(p);
      }
      if (change.wasAdded()) {
        var p = change.getValueAdded();
        p.rangeProperty().addListener(selectionRangeListener);
        p.layoutXProperty().bind(leftInset);
        p.layoutYProperty().bind(topInset);

        getChildren().add(selectionShapeStartIndex, p);
        updateSingleSelection(p);
      }
    };
    selections.addListener(selectionPathListener);

    ChangeListener<Integer> caretPositionListener = (obs, ov, nv) -> requestLayout();
    caretNodeListener = change -> {
      if (change.wasRemoved()) {
        var caret = change.getElementRemoved();
        caret.columnPositionProperty().removeListener(caretPositionListener);
        caret.layoutXProperty().unbind();
        caret.layoutYProperty().unbind();
        getChildren().remove(caret);
      }
      if (change.wasAdded()) {
        var caret = change.getElementAdded();
        caret.columnPositionProperty().addListener(caretPositionListener);
        caret.layoutXProperty().bind(leftInset);
        caret.layoutYProperty().bind(topInset);
        getChildren().add(caret);
        updateSingleCaret(caret);
      }
    };
    carets.addListener(caretNodeListener);

    // XXX: see the note at highlightTextFill
    //        highlightTextFill.addListener(new ChangeListener<Paint>() {
    //            @Override
    //            public void changed(ObservableValue<? extends Paint> observable,
    //                    Paint oldFill, Paint newFill) {
    //                for(PumpedUpText text: textNodes())
    //                    text.impl_selectionFillProperty().set(newFill);
    //            }
    //        });

    // populate with text nodes
    par.getStyledSegments().stream().map(nodeFactory).forEach(n -> {
      if (n instanceof TextExt t) {
        // XXX: binding selectionFill to textFill,
        // see the note at highlightTextFill
        t.selectionFillProperty().bind(t.fillProperty());
      }
      getChildren().add(n);
    });

    // set up custom css shape helpers
    UnaryOperator<Path> configurePath = shape -> {
      shape.setManaged(false);
      shape.layoutXProperty().bind(leftInset);
      shape.layoutYProperty().bind(topInset);
      return shape;
    };
    Supplier<Path> createBackgroundShape = () -> configurePath.apply(new BackgroundPath());
    Supplier<Path> createBorderShape = () -> configurePath.apply(new BorderPath());
    Supplier<Path> createUnderlineShape = () -> configurePath.apply(new UnderlinePath());

    Consumer<Collection<Path>> clearUnusedShapes = paths -> getChildren().removeAll(paths);
    Consumer<Path> addToBackground = path -> getChildren().add(0, path);
    Consumer<Path> addToBackgroundAndIncrementSelectionIndex = addToBackground.andThen(ignore -> selectionShapeStartIndex++);
    Consumer<Path> addToForeground = path -> getChildren().add(path);
    backgroundShapeHelper = new CustomCssShapeHelper<>(
      createBackgroundShape,
      (backgroundShape, tuple) -> {
        backgroundShape.setStrokeWidth(0);
        backgroundShape.setFill(tuple._1);
        backgroundShape.getElements().setAll(getRangeShape(tuple._2));
      },
      addToBackgroundAndIncrementSelectionIndex,
      clearUnusedShapes
    );
    borderShapeHelper = new CustomCssShapeHelper<>(
      createBorderShape,
      (borderShape, tuple) -> {
        var attributes = tuple._1;
        borderShape.setStrokeWidth(attributes.width);
        borderShape.setStroke(attributes.color);
        if (attributes.type != null) {
          borderShape.setStrokeType(attributes.type);
        }
        if (attributes.dashArray != null) {
          borderShape.getStrokeDashArray().setAll(attributes.dashArray);
        }
        borderShape.getElements().setAll(getRangeShape(tuple._2));
      },
      addToBackground,
      clearUnusedShapes
    );
    underlineShapeHelper = new CustomCssShapeHelper<>(
      createUnderlineShape,
      (underlineShape, tuple) -> {
        var attributes = tuple._1;
        underlineShape.setStroke(attributes.color);
        underlineShape.setStrokeWidth(attributes.width);
        underlineShape.setStrokeLineCap(attributes.cap);
        if (attributes.dashArray != null) {
          underlineShape.getStrokeDashArray().setAll(attributes.dashArray);
        }
        underlineShape.getElements().setAll(getUnderlineShape(tuple._2));
      },
      addToForeground,
      clearUnusedShapes
    );
  }

  void dispose() {
    carets.clear();
    selections.clear();
    // The above must be before the below to prevent any memory leaks.
    // Then remove listeners to also avoid memory leaks.
    selections.removeListener(selectionPathListener);
    carets.removeListener(caretNodeListener);

    getChildren().stream()
      .filter(n -> n instanceof TextExt)
      .map(n -> (TextExt) n)
      .forEach(t -> t.selectionFillProperty().unbind());

    getChildren().clear();
  }

  public Paragraph<PS, SEG, S> getParagraph() {
    return paragraph;
  }

  public <T extends Node & Caret> double getCaretOffsetX(T caret) {
    layout(); // ensure layout, is a no-op if not dirty
    if (isVisible() /* notFolded */ ) {
      checkWithinParagraph(caret);
    }
    var bounds = caret.getLayoutBounds();
    return (bounds.getMinX() + bounds.getMaxX()) / 2;
  }

  public <T extends Node & Caret> Bounds getCaretBounds(T caret) {
    layout(); // ensure layout, is a no-op if not dirty
    checkWithinParagraph(caret);
    return caret.getBoundsInParent();
  }

  public <T extends Node & Caret> Bounds getCaretBoundsOnScreen(T caret) {
    layout(); // ensure layout, is a no-op if not dirty
    checkWithinParagraph(caret);
    var localBounds = caret.getBoundsInLocal();
    return caret.localToScreen(localBounds);
  }

  public Bounds getRangeBoundsOnScreen(int from, int to) {
    layout(); // ensure layout, is a no-op if not dirty
    var rangeShape = getRangeShapeSafely(from, to);

    var p = new Path();
    p.setManaged(false);
    p.setLayoutX(getInsets().getLeft());
    p.setLayoutY(getInsets().getTop());

    getChildren().add(p);

    p.getElements().setAll(rangeShape);
    var localBounds = p.getBoundsInLocal();
    var rangeBoundsOnScreen = p.localToScreen(localBounds);

    getChildren().remove(p);

    return rangeBoundsOnScreen;
  }

  public Optional<Bounds> getSelectionBoundsOnScreen(Selection<PS, SEG, S> selection) {
    if (selection.getLength() == 0) {
      return Optional.empty();
    } else {
      layout(); // ensure layout, is a no-op if not dirty
      var selectionShape = selections.get(selection);
      checkWithinParagraph(selectionShape);
      var localBounds = selectionShape.getBoundsInLocal();
      return Optional.ofNullable(selectionShape.localToScreen(localBounds));
    }
  }

  public int getCurrentLineStartPosition(Caret caret) {
    return getLineStartPosition(getClampedCaretPosition(caret));
  }

  public int getCurrentLineEndPosition(Caret caret) {
    return getLineEndPosition(getClampedCaretPosition(caret));
  }

  public int currentLineIndex(Caret caret) {
    return getLineOfCharacter(getClampedCaretPosition(caret));
  }

  public int currentLineIndex(int position) {
    return getLineOfCharacter(position);
  }

  <T extends Node> void checkWithinParagraph(T shape) {
    if (shape.getParent() != this) {
      throw new IllegalArgumentException(String.format(
        "This ParagraphText is not the parent of the given shape (%s):\nExpected: %s\nActual:   %s",
        shape, this, shape.getParent()));
    }
  }

  int getClampedCaretPosition(Caret caret) {
    return Math.min(caret.getColumnPosition(), paragraph.length());
  }

  void updateAllCaretShapes() {
    carets.forEach(this::updateSingleCaret);
  }

  void updateSingleCaret(CaretNode caretNode) {
    var shape = getCaretShape(getClampedCaretPosition(caretNode), true);
    caretNode.getElements().setAll(shape);
  }

  void updateAllSelectionShapes() {
    selections.values().forEach(this::updateSingleSelection);
  }

  void updateSingleSelection(SelectionPath path) {
    path.getElements().setAll(getRangeShapeSafely(path.rangeProperty().getValue()));
  }

  PathElement[] getRangeShapeSafely(IndexRange range) {
    return getRangeShapeSafely(range.getStart(), range.getEnd());
  }

  /**
   * Gets the range shape for the given positions within the text, including the newline character, if range
   * defined by the start/end arguments include it.
   *
   * @param start the start position of the range shape
   * @param end the end position of the range shape. If {@code end == paragraph.length() + 1}, the newline character
   *            will be included in the selection by selecting the rest of the line
   */
  PathElement[] getRangeShapeSafely(int start, int end) {
    PathElement[] shape;
    if (end <= paragraph.length()) {
      // selection w/o newline char
      shape = getRangeShape(start, end);
    } else {
      // Selection includes a newline character.
      if (paragraph.length() == 0) {
        // empty paragraph
        shape = createRectangle(0, 0, getWidth(), getHeight());
      } else if (start == paragraph.length()) {
        // selecting only the newline char

        // calculate the bounds of the last character
        shape = getRangeShape(start - 1, start);
        var lineToTopRight = (LineTo) shape[shape.length - 4];
        shape = createRectangle(lineToTopRight.getX(), lineToTopRight.getY(), getWidth(), getHeight());
      } else {
        shape = getRangeShape(start, paragraph.length());
        // Since this might be a wrapped multi-line paragraph,
        // there may be multiple groups of (1 MoveTo, 4 LineTo objects) for each line:
        // MoveTo(topLeft), LineTo(topRight), LineTo(bottomRight), LineTo(bottomLeft)

        // We only need to adjust the top right and bottom right corners to extend to the
        // width/height of the line, simulating a full line selection.
        var length = shape.length;
        if (length > 3) { // Prevent IndexOutOfBoundsException accessing shape[] issue #689
          var bottomRightIndex = length - 3;
          var topRightIndex = bottomRightIndex - 1;
          var lineToTopRight = (LineTo) shape[topRightIndex];
          shape[topRightIndex] = new LineTo(getWidth(), lineToTopRight.getY());
          shape[bottomRightIndex] = new LineTo(getWidth(), getHeight());
        }
      }
    }

    if (getLineSpacing() > 0) {
      var half = getLineSpacing() / 2.0;
      for (var g = 0; g < shape.length; g += 5) {
        var tl = (MoveTo) shape[g];
        tl.setY(tl.getY() - half);
        var tr = (LineTo) shape[g + 1];
        tr.setY(tl.getY());
        var br = (LineTo) shape[g + 2];
        br.setY(br.getY() + half);
        var bl = (LineTo) shape[g + 3];
        bl.setY(br.getY());
        var t2 = (LineTo) shape[g + 4];
        t2.setY(tl.getY());
      }
    }

    if (getLineCount() > 1) {
      // adjust right corners of wrapped lines
      var wrappedAtEndPos = (end > 0 && getLineOfCharacter(end) > getLineOfCharacter(end - 1));
      var adjustLength = shape.length - (wrappedAtEndPos ? 0 : 5);
      for (var i = 0; i < adjustLength; i++) {
        if (shape[i] instanceof MoveTo) {
          ((LineTo) shape[i + 1]).setX(getWidth());
          ((LineTo) shape[i + 2]).setX(getWidth());
        }
      }
    }

    return shape;
  }

  PathElement[] createRectangle(double topLeftX, double topLeftY, double bottomRightX, double bottomRightY) {
    return new PathElement[] {
      new MoveTo(topLeftX, topLeftY),
      new LineTo(bottomRightX, topLeftY),
      new LineTo(bottomRightX, bottomRightY),
      new LineTo(topLeftX, bottomRightY),
      new LineTo(topLeftX, topLeftY)
    };
  }

  void updateBackgroundShapes() {
    int start = 0;

    // calculate shared values among consecutive nodes
    for (var node : getManagedChildren()) {
      if (!(node instanceof TextExt)) {
        // node is a custom objects (e.g. image)
        // - custom objects do not support background color, border and underline
        // - text length of custom objects is always 1
        start += 1;
        continue;
      }

      var text = (TextExt) node;
      var end = start + text.getText().length();

      var backgroundColor = text.getBackgroundColor();
      if (backgroundColor != null) {
        backgroundShapeHelper.updateSharedShapeRange(backgroundColor, start, end, Paint::equals);
      }

      var border = new BorderAttributes(text);
      if (!border.isNullValue()) {
        borderShapeHelper.updateSharedShapeRange(border, start, end, BorderAttributes::equalsFaster);
      }

      var underline = new UnderlineAttributes(text);
      if (!underline.isNullValue()) {
        underlineShapeHelper.updateSharedShapeRange(underline, start, end, UnderlineAttributes::equalsFaster);
      }

      start = end;
    }

    borderShapeHelper.updateSharedShapes();
    backgroundShapeHelper.updateSharedShapes();
    underlineShapeHelper.updateSharedShapes();
  }

  @Override
  public String toString() {
    return String.format("ParagraphText@%s(paragraph=%s)", hashCode(), paragraph);
  }

  @Override
  protected void layoutChildren() {
    super.layoutChildren();
    updateAllCaretShapes();
    updateAllSelectionShapes();
    updateBackgroundShapes();
  }

  static class CustomCssShapeHelper<T> {

    final List<Tuple2<T, IndexRange>> ranges = new LinkedList<>();
    final List<Path> shapes = new LinkedList<>();

    final Supplier<Path> createShape;
    final BiConsumer<Path, Tuple2<T, IndexRange>> configureShape;
    final Consumer<Path> addToChildren;
    final Consumer<Collection<Path>> clearUnusedShapes;

    CustomCssShapeHelper(Supplier<Path> createShape, BiConsumer<Path, Tuple2<T, IndexRange>> configureShape, Consumer<Path> addToChildren, Consumer<Collection<Path>> clearUnusedShapes) {
      this.createShape = createShape;
      this.configureShape = configureShape;
      this.addToChildren = addToChildren;
      this.clearUnusedShapes = clearUnusedShapes;
    }

    /**
     * Calculates the range of a value (background color, underline, etc.) that is shared between multiple
     * consecutive {@link TextExt} nodes
     */
    void updateSharedShapeRange(T value, int start, int end, BiFunction<T, T, Boolean> equals) {
      Runnable addNewValueRange = () -> ranges.add(Tuples.t(value, new IndexRange(start, end)));

      if (ranges.isEmpty()) {
        addNewValueRange.run();
      } else {
        var lastIndex = ranges.size() - 1;
        var lastShapeValueRange = ranges.get(lastIndex);
        var lastShapeValue = lastShapeValueRange._1;

        // calculate smallest possible position which is consecutive to the given start position
        var prevEndNext = lastShapeValueRange.get2().getEnd();
        if (start == prevEndNext && // Consecutive?
            equals.apply(lastShapeValue, value)) // Same style?
        {
          var lastRange = lastShapeValueRange._2;
          var extendedRange = new IndexRange(lastRange.getStart(), end);
          ranges.set(lastIndex, Tuples.t(lastShapeValue, extendedRange));
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

  static class BorderAttributes extends LineAttributesBase {

    final StrokeType type;

    BorderAttributes(TextExt text) {
      super(text.getBorderStrokeColor(), text.getBorderStrokeWidth(), text.borderStrokeDashArrayProperty());
      type = text.getBorderStrokeType();
    }
    /**
     * Same as {@link #equals(Object)} but no need to check the object for its class
     */
    public boolean equalsFaster(BorderAttributes attr) {
      return super.equalsFaster(attr) && Objects.equals(type, attr.type);
    }
    @Override
    public boolean equals(Object obj) {
      return (obj instanceof BorderAttributes attributes) ? equalsFaster(attributes) : false;
    }
    @Override
    public String toString() {
      return String.format("BorderAttributes[type=%s %s]", type, getSubString());
    }
  }

  static class UnderlineAttributes extends LineAttributesBase {

    final StrokeLineCap cap;

    UnderlineAttributes(TextExt text) {
      super(text.getUnderlineColor(), text.getUnderlineWidth(), text.underlineDashArrayProperty());
      cap = text.getUnderlineCap();
    }

    /**
     * Same as {@link #equals(Object)} but no need to check the object for its class
     */
    public boolean equalsFaster(UnderlineAttributes attr) {
      return super.equalsFaster(attr) && Objects.equals(cap, attr.cap);
    }
    @Override
    public boolean equals(Object obj) {
      return (obj instanceof UnderlineAttributes attr) ? equalsFaster(attr) : false;
    }
    @Override
    public String toString() {
      return String.format("UnderlineAttributes[cap=%s %s]", cap, getSubString());
    }
  }

  static class LineAttributesBase {

    final double width;
    final Paint color;
    final Double[] dashArray;

    public final boolean isNullValue() {
      return color == null || width == -1;
    }

    /**
     * Java Quirk! Using {@code t.get[border/underline]DashArray()} throws a ClassCastException
     * "Double cannot be cast to Number". However, using {@code t.getDashArrayProperty().get()}
     * works without issue
     */
    LineAttributesBase(Paint color, Number width, ObjectProperty<Number[]> dashArrayProp) {
      this.color = color;
      if (color == null || width == null || width.doubleValue() <= 0) {
        // null value
        this.width = -1;
        dashArray = null;
      } else {
        // real value
        this.width = width.doubleValue();

        // get the dash array - JavaFX CSS parser seems to return either a Number[] array
        // or a single value, depending on whether only one or more than one value has been
        // specified in the CSS
        Object dashArrayProperty = dashArrayProp.get();
        if (dashArrayProperty != null) {
          if (dashArrayProperty.getClass().isArray()) {
            var numberArray = (Number[]) dashArrayProperty;
            dashArray = new Double[numberArray.length];
            int idx = 0;
            for (var d : numberArray) {
              dashArray[idx++] = (Double) d;
            }
          } else {
            dashArray = new Double[1];
            dashArray[0] = ((Double) dashArrayProperty).doubleValue();
          }
        } else {
          dashArray = null;
        }
      }
    }

    /**
     * Same as {@link #equals(Object)} but no need to check the object for its class
     */
    public boolean equalsFaster(LineAttributesBase attr) {
      return Objects.equals(width, attr.width) && Objects.equals(color, attr.color) && Arrays.equals(dashArray, attr.dashArray);
    }
    @Override
    public boolean equals(Object obj) {
      return (obj instanceof LineAttributesBase attr) ? equalsFaster(attr) : false;
    }
    protected final String getSubString() {
      return String.format("width=%s color=%s dashArray=%s", width, color, Arrays.toString(dashArray));
    }
  }

}
