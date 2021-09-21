package fx.layout.flow;

import static javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED;

import fx.react.value.Val;
import fx.react.value.Var;
import javafx.application.Platform;
import javafx.beans.DefaultProperty;
import javafx.beans.NamedArg;
import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;

@DefaultProperty("content")
public class VirtualizedScrollPane<V extends Node & Virtualized> extends Region implements Virtualized {

  static final PseudoClass CONTENT_FOCUSED = PseudoClass.getPseudoClass("content-focused");

  final ScrollBar hbar;
  final ScrollBar vbar;
  final V content;
  final ChangeListener<Boolean> contentFocusedListener;
  final ChangeListener<Double> hbarValueListener;
  final ChangeListener<Double> hPosEstimateListener;
  final ChangeListener<Double> vbarValueListener;
  final ChangeListener<Double> vPosEstimateListener;

  Var<Double> hbarValue;
  Var<Double> vbarValue;
  Var<Double> hPosEstimate;
  Var<Double> vPosEstimate;

  /** The Policy for the Horizontal ScrollBar */
  final Var<ScrollPane.ScrollBarPolicy> hbarPolicy;

  public final ScrollPane.ScrollBarPolicy getHbarPolicy() {
    return hbarPolicy.getValue();
  }

  public final void setHbarPolicy(ScrollPane.ScrollBarPolicy value) {
    hbarPolicy.setValue(value);
  }

  public final Var<ScrollPane.ScrollBarPolicy> hbarPolicyProperty() {
    return hbarPolicy;
  }

  /** The Policy for the Vertical ScrollBar */
  final Var<ScrollPane.ScrollBarPolicy> vbarPolicy;

  public final ScrollPane.ScrollBarPolicy getVbarPolicy() {
    return vbarPolicy.getValue();
  }

  public final void setVbarPolicy(ScrollPane.ScrollBarPolicy value) {
    vbarPolicy.setValue(value);
  }

  public final Var<ScrollPane.ScrollBarPolicy> vbarPolicyProperty() {
    return vbarPolicy;
  }

  /**
   * Constructs a VirtualizedScrollPane with the given content and policies
   */
  public VirtualizedScrollPane(@NamedArg("content") V content, @NamedArg("hPolicy") ScrollPane.ScrollBarPolicy hPolicy, @NamedArg("vPolicy") ScrollPane.ScrollBarPolicy vPolicy) {
    this.getStyleClass().add("virtualized-scroll-pane");
    this.content = content;

    // create scrollbars
    hbar = new ScrollBar();
    vbar = new ScrollBar();
    hbar.setOrientation(Orientation.HORIZONTAL);
    vbar.setOrientation(Orientation.VERTICAL);

    // scrollbar ranges
    hbar.setMin(0);
    vbar.setMin(0);
    hbar.maxProperty().bind(content.totalWidthEstimateProperty());
    vbar.maxProperty().bind(content.totalHeightEstimateProperty());

    // scrollbar increments
    setupUnitIncrement(hbar);
    setupUnitIncrement(vbar);
    hbar.blockIncrementProperty().bind(hbar.visibleAmountProperty());
    vbar.blockIncrementProperty().bind(vbar.visibleAmountProperty());

    // scrollbar positions
    hPosEstimate =
      Val.combine(
        content.estimatedScrollXProperty(),
        Val.map(content.layoutBoundsProperty(), Bounds::getWidth),
        content.totalWidthEstimateProperty(),
        VirtualizedScrollPane::offsetToScrollbarPosition
      )
      .asVar(this::setHPosition);

    vPosEstimate =
      Val.combine(
        content.estimatedScrollYProperty(),
        Val.map(content.layoutBoundsProperty(), Bounds::getHeight),
        content.totalHeightEstimateProperty(),
        VirtualizedScrollPane::offsetToScrollbarPosition
      )
      .orElseConst(0.0)
      .asVar(this::setVPosition);

    hbarValue = Var.doubleVar(hbar.valueProperty());
    vbarValue = Var.doubleVar(vbar.valueProperty());

    // The use of a pair of mirrored ChangeListener instead of a more natural bidirectional binding
    // here is a workaround following a change in JavaFX [1] which broke the behaviour of the scroll bar [2].
    // [1] https://bugs.openjdk.java.net/browse/JDK-8264770
    // [2] https://github.com/FXMisc/Flowless/issues/97
    hbarValueListener = (observable, oldValue, newValue) -> hPosEstimate.setValue(newValue);
    hbarValue.addListener(hbarValueListener);
    hPosEstimateListener = (observable, oldValue, newValue) -> hbarValue.setValue(newValue);
    hPosEstimate.addListener(hPosEstimateListener);
    vbarValueListener = (observable, oldValue, newValue) -> vPosEstimate.setValue(newValue);
    vbarValue.addListener(vbarValueListener);
    vPosEstimateListener = (observable, oldValue, newValue) -> vbarValue.setValue(newValue);
    vPosEstimate.addListener(vPosEstimateListener);

    // scrollbar visibility
    hbarPolicy = Var.newSimpleVar(hPolicy);
    vbarPolicy = Var.newSimpleVar(vPolicy);

    var layoutWidth = Val.map(layoutBoundsProperty(), Bounds::getWidth);
    var layoutHeight = Val.map(layoutBoundsProperty(), Bounds::getHeight);

    var needsHBar0 = Val.combine(
      content.totalWidthEstimateProperty(),
      layoutWidth,
      (cw, lw) -> cw > lw
    );
    var needsVBar0 = Val.combine(
      content.totalHeightEstimateProperty(),
      layoutHeight,
      (ch, lh) -> ch > lh
    );
    var needsHBar = Val.combine(
      needsHBar0,
      needsVBar0,
      content.totalWidthEstimateProperty(),
      vbar.widthProperty(),
      layoutWidth,
      (needsH, needsV, cw, vbw, lw) -> needsH || needsV && cw + vbw.doubleValue() > lw
    );
    var needsVBar = Val.combine(
      needsVBar0,
      needsHBar0,
      content.totalHeightEstimateProperty(),
      hbar.heightProperty(),
      layoutHeight,
      (needsV, needsH, ch, hbh, lh) -> needsV || needsH && ch + hbh.doubleValue() > lh
    );

    var shouldDisplayHorizontal = Val.flatMap(
      hbarPolicy, policy -> {
        return switch (policy) {
          case NEVER -> Val.constant(false);
          case ALWAYS -> Val.constant(true);
          default -> needsHBar;  // AS_NEEDED
        };
      });

    var shouldDisplayVertical = Val.flatMap(
      vbarPolicy, policy -> {
        return switch (policy) {
          case NEVER -> Val.constant(false);
          case ALWAYS -> Val.constant(true);
          default -> needsVBar;  // AS_NEEDED
        };
      });

    // request layout later, because if currently in layout, the request is ignored
    shouldDisplayHorizontal.addListener(obs -> Platform.runLater(this::requestLayout));
    shouldDisplayVertical.addListener(obs -> Platform.runLater(this::requestLayout));

    hbar.visibleProperty().bind(shouldDisplayHorizontal);
    vbar.visibleProperty().bind(shouldDisplayVertical);

    contentFocusedListener = (obs, ov, nv) -> pseudoClassStateChanged(CONTENT_FOCUSED, nv);
    content.focusedProperty().addListener(contentFocusedListener);
    getChildren().addAll(content, hbar, vbar);
    getChildren().addListener((Observable obs) -> dispose());
  }

  /**
   * Constructs a VirtualizedScrollPane that only displays its horizontal and vertical scroll bars as needed
   */
  public VirtualizedScrollPane(@NamedArg("content") V content) {
    this(content, AS_NEEDED, AS_NEEDED);
  }

  /**
   * Does not unbind scrolling from Content before returning Content.
   * @return - the content
   */
  public V getContent() {
    return content;
  }

  /**
   * Unbinds scrolling from Content before returning Content.
   * @return - the content
   */
  public V removeContent() {
    getChildren().clear();
    return content;
  }

  void dispose() {
    content.focusedProperty().removeListener(contentFocusedListener);
    hbarValue.removeListener(hbarValueListener);
    hPosEstimate.removeListener(hPosEstimateListener);
    vbarValue.removeListener(vbarValueListener);
    vPosEstimate.removeListener(vPosEstimateListener);
    unbindScrollBar(hbar);
    unbindScrollBar(vbar);
  }

  void unbindScrollBar(ScrollBar bar) {
    bar.maxProperty().unbind();
    bar.unitIncrementProperty().unbind();
    bar.blockIncrementProperty().unbind();
    bar.visibleProperty().unbind();
  }

  @Override
  public Val<Double> totalWidthEstimateProperty() {
    return content.totalWidthEstimateProperty();
  }

  @Override
  public Val<Double> totalHeightEstimateProperty() {
    return content.totalHeightEstimateProperty();
  }

  @Override
  public Var<Double> estimatedScrollXProperty() {
    return content.estimatedScrollXProperty();
  }

  @Override
  public Var<Double> estimatedScrollYProperty() {
    return content.estimatedScrollYProperty();
  }

  @Override
  public void scrollXBy(double deltaX) {
    content.scrollXBy(deltaX);
  }

  @Override
  public void scrollYBy(double deltaY) {
    content.scrollYBy(deltaY);
  }

  @Override
  public void scrollXToPixel(double pixel) {
    content.scrollXToPixel(pixel);
  }

  @Override
  public void scrollYToPixel(double pixel) {
    content.scrollYToPixel(pixel);
  }

  @Override
  protected double computePrefWidth(double height) {
    return content.prefWidth(height);
  }

  @Override
  protected double computePrefHeight(double width) {
    return content.prefHeight(width);
  }

  @Override
  protected double computeMinWidth(double height) {
    return vbar.minWidth(-1);
  }

  @Override
  protected double computeMinHeight(double width) {
    return hbar.minHeight(-1);
  }

  @Override
  protected double computeMaxWidth(double height) {
    return content.maxWidth(height);
  }

  @Override
  protected double computeMaxHeight(double width) {
    return content.maxHeight(width);
  }

  @Override
  protected void layoutChildren() {
    var layoutWidth = snapSizeX(getLayoutBounds().getWidth());
    var layoutHeight = snapSizeY(getLayoutBounds().getHeight());
    var vbarVisible = vbar.isVisible();
    var hbarVisible = hbar.isVisible();
    var vbarWidth = snapSizeY(vbarVisible ? vbar.prefWidth(-1) : 0);
    var hbarHeight = snapSizeX(hbarVisible ? hbar.prefHeight(-1) : 0);
    var w = layoutWidth - vbarWidth;
    var h = layoutHeight - hbarHeight;
    content.resize(w, h);
    hbar.setVisibleAmount(w);
    vbar.setVisibleAmount(h);
    if (vbarVisible) {
      vbar.resizeRelocate(layoutWidth - vbarWidth, 0, vbarWidth, h);
    }
    if (hbarVisible) {
      hbar.resizeRelocate(0, layoutHeight - hbarHeight, w, hbarHeight);
    }
  }

  void setHPosition(double pos) {
    var offset = scrollbarPositionToOffset(pos,
      content.getLayoutBounds().getWidth(),
      content.totalWidthEstimateProperty().getValue()
    );
    content.estimatedScrollXProperty().setValue(offset);
  }

  void setVPosition(double pos) {
    var offset = scrollbarPositionToOffset(pos,
      content.getLayoutBounds().getHeight(),
      content.totalHeightEstimateProperty().getValue()
    );
    content.estimatedScrollYProperty().setValue(offset);
  }

  static void setupUnitIncrement(ScrollBar bar) {
    bar.unitIncrementProperty().bind(new DoubleBinding() {
      /*<init>*/ {
        bind(bar.maxProperty(), bar.visibleAmountProperty());
      }
      @Override
      protected double computeValue() {
        var max = bar.getMax();
        var visible = bar.getVisibleAmount();
        return max > visible ? 16 / (max - visible) * max : 0;
      }
    });
  }

  static double offsetToScrollbarPosition(double contentOffset, double viewportSize, double contentSize) {
    return contentSize > viewportSize ? contentOffset / (contentSize - viewportSize) * contentSize : 0;
  }

  static double scrollbarPositionToOffset(double scrollbarPos, double viewportSize, double contentSize) {
    return contentSize > viewportSize ? scrollbarPos / contentSize * (contentSize - viewportSize) : 0;
  }

}
