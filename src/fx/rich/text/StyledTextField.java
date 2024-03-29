package fx.rich.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import fx.rich.text.model.EditableStyledDocument;
import javafx.application.Application;
import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

/**
 * A text field whose segment generic has been specified to be a {@link String}. How the text
 * will be styled is not yet specified in this class, but use {@link StyleClassedTextField} for a style class
 * approach to styling the text and {@link InlineCssTextField} for an inline css approach to styling the text.
 *
 * <p>Use CSS Style Class ".styled-text-field" for styling the control.</p>
 * 
 * @param <PS> type of paragraph style
 * @param <S> type of style that can be applied to text.
 * 
 * @author Jurgen
 */
public abstract class StyledTextField<PS, S> extends StyledTextArea<PS, S> {
  
  final static List<CssMetaData<? extends Styleable, ?>> CSS_META_DATA_LIST;

  @SuppressWarnings({ "rawtypes", "unchecked" })
  final static CssMetaData<StyledTextField, TextAlignment> TEXT_ALIGNMENT = new CustomCssMetaData<>(
    "-fx-alignment",
    (StyleConverter<?, TextAlignment>) StyleConverter.getEnumConverter(TextAlignment.class),
    TextAlignment.LEFT,
    s -> (StyleableObjectProperty) s.alignmentProperty()
   );

  @SuppressWarnings({ "rawtypes", "unchecked" })
  final static CssMetaData<StyledTextField, Paint> PROMPT_TEXT_FILL = new CustomCssMetaData<>(
    "-fx-prompt-text-fill",
    (StyleConverter<?, Paint>) StyleConverter.getPaintConverter(),
    Color.GRAY,
    s -> (StyleableObjectProperty) s.promptTextFillProperty()
  );

  final static Pattern VERTICAL_WHITESPACE = Pattern.compile("\\v+");
  final static String STYLE_SHEET;
  final static double HEIGHT;
  
  /*<init>*/ static {
    var styleables = new ArrayList<CssMetaData<? extends Styleable, ?>>(GenericStyledArea.getClassCssMetaData());
    styleables.add(PROMPT_TEXT_FILL);
    styleables.add(TEXT_ALIGNMENT);
    CSS_META_DATA_LIST = Collections.unmodifiableList(styleables);

    var globalCSS = System.getProperty("javafx.userAgentStylesheetUrl"); // JavaFX preference!
    if (globalCSS == null) {
      globalCSS = Application.getUserAgentStylesheet();
    }
    if (globalCSS == null) {
      globalCSS = Application.STYLESHEET_MODENA;
    }
    globalCSS = "styled-text-field-" + globalCSS.toLowerCase() + ".css";
    STYLE_SHEET = StyledTextField.class.getResource(globalCSS).toExternalForm();

    // Ugly hack to get a TextFields default height :(
    // as it differs between Caspian, Modena, etc.
    var tf = new TextField("GetHeight");
    new Scene(tf).snapshot(null);
    HEIGHT = tf.getHeight();
  }

  boolean selectAll = true;
  StyleableObjectProperty<TextAlignment> textAlignment;
  StyleableObjectProperty<Paint> promptFillProp;

  public StyledTextField(
    @NamedArg("initialParagraphStyle") PS initialParagraphStyle,
    @NamedArg("applyParagraphStyle") BiConsumer<TextFlow, PS> applyParagraphStyle,
    @NamedArg("initialTextStyle") S initialTextStyle,
    @NamedArg("applyStyle") BiConsumer<? super TextExt, S> applyStyle,
    @NamedArg("document") EditableStyledDocument<PS, String, S> document
  ) {
    super(initialParagraphStyle, applyParagraphStyle, initialTextStyle, applyStyle, document, true);

    getStylesheets().add(STYLE_SHEET);
    getStyleClass().setAll("styled-text-field");

    setAccessibleRole(AccessibleRole.TEXT_FIELD);
    setPrefSize(135, HEIGHT);

    addEventFilter(KeyEvent.KEY_PRESSED, KE -> {
      if (KE.getCode() == KeyCode.ENTER) {
        fireEvent(new ActionEvent(this, null));
        KE.consume();
      } else if (KE.getCode() == KeyCode.TAB) {
        traverse(this.getParent(), this, KE.isShiftDown() ? -1 : +1);
        KE.consume();
      }
    });

    addEventFilter(MouseEvent.MOUSE_PRESSED, ME -> selectAll = isFocused());

    focusedProperty().addListener((ob, was, focused) -> {
      if (!was && focused && selectAll) {
        selectRange(getLength(), 0);
      } else if (!focused && was) {
        moveTo(0);
        requestFollowCaret();
      }
      selectAll = true;
    });

    super.setWrapText(false);
    wrapTextProperty().addListener((ob, ov, wrap) -> {
      if (wrap) { // veto any changes
        wrapTextProperty().unbind();
        super.setWrapText(false);
      }
    });
  }

  /*
   * There's no public API to move the focus forward or backward
   * without explicitly knowing the node. So here's a basic local
   * implementation to accomplish that.
   */
  Node traverse(Parent p, Node from, int dir) {
    if (p == null) {
      return null;
    }
    var nodeList = p.getChildrenUnmodifiable();
    var len = nodeList.size();
    var neighbor = -1;

    if (from != null) {
      while (++neighbor < len && nodeList.get(neighbor) != from) ; // no-op
    } else if (dir == 1) {
      neighbor = -1;
    } else {
      neighbor = len;
    }
    
    for (neighbor += dir; neighbor > -1 && neighbor < len; neighbor += dir) {
      var target = nodeList.get(neighbor);
      if (target instanceof Pane || target instanceof Group) {
        target = traverse((Parent) target, null, dir); // down
        if (target != null) {
          return target;
        }
      } else if (target.isVisible() && !target.isDisabled() && target.isFocusTraversable()) {
        target.requestFocus();
        return target;
      }
    }

    return traverse(p.getParent(), p, dir); // up
  }

  /**
   * Specifies how the text should be aligned when there is empty space within the TextField.
   * To configure via CSS use {@code -fx-alignment:} and values from {@link javafx.scene.text.TextAlignment}.
   */
  public final ObjectProperty<TextAlignment> alignmentProperty() {
    if (textAlignment == null) {
      textAlignment = new CustomStyleableProperty<>(TextAlignment.LEFT, "textAlignment", this, TEXT_ALIGNMENT);
      textAlignment.addListener((ob, ov, alignment) -> changeAlignment(alignment));
    }
    return textAlignment;
  }

  public final TextAlignment getAlignment() {
    return textAlignment == null ? TextAlignment.LEFT : textAlignment.getValue();
  }

  public final void setAlignment(TextAlignment value) {
    alignmentProperty().setValue(value);
  }

  protected abstract void changeAlignment(TextAlignment txtAlign);

  /**
   * The action handler associated with this text field, or {@code null} if no action handler is assigned.
   * The action handler is normally called when the user types the ENTER key.
   */
  ObjectProperty<EventHandler<ActionEvent>> onAction =
    new ObjectPropertyBase<EventHandler<ActionEvent>>() {
      @Override protected void invalidated() { setEventHandler(ActionEvent.ACTION, get()); }
      @Override public Object getBean() { return StyledTextField.this; }
      @Override public String getName() { return "onAction"; }
    };

  public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
    return onAction;
  }

  public final EventHandler<ActionEvent> getOnAction() {
    return onActionProperty().get();
  }

  public final void setOnAction(EventHandler<ActionEvent> value) {
    onActionProperty().set(value);
  }

  /**
   * The prompt text to display or <tt>null</tt> if no prompt text is to be displayed.
   * <p>The Text will be aligned according to the text fields alignment setting and have a default
   * text fill of GRAY unless you have changed it by any means, e.g. with CSS "-fx-prompt-text-fill" 
   */
  public final void setPromptText(Text value) {
    placeholderProperty().set(value);
  }

  public final ObjectProperty<? super Text> promptTextProperty() {
    return placeholderProperty();
  }

  public final Text getPromptText() {
    return getPlaceholder() instanceof Text ? (Text) getPlaceholder() : null;
  }

  /** setPlaceholder is not supported by StyledTextField, use setPromptText instead */
  @Override
  public void setPlaceholder(Node value, Pos where) {
    throw new UnsupportedOperationException("Use setPromptText instead");
  }

  @Override
  protected void configurePlaceholder(Node placeholder) {
    placeholder.layoutYProperty().bind(Bindings.createDoubleBinding(
      () -> (getHeight() - placeholder.getLayoutBounds().getHeight()) / 2 + Math.abs(placeholder.getLayoutBounds().getMinY()),
      heightProperty(),
      placeholder.layoutBoundsProperty()
    ));

    placeholder.layoutXProperty().bind(Bindings.createDoubleBinding(
      () -> calcHorizontalPos(),
      widthProperty(),
      placeholder.layoutBoundsProperty(),
      paddingProperty(),
      alignmentProperty()
    ));

    if (placeholder instanceof Text text && text.getFill() == Color.BLACK) {
      text.fillProperty().bind(promptTextFillProperty());
    }
  }

  ObjectProperty<Paint> promptTextFillProperty() {
    if (promptFillProp == null) {
      promptFillProp = new CustomStyleableProperty<>(Color.GRAY, "promptFill", this, PROMPT_TEXT_FILL);
    }
    return promptFillProp;
  }

  double calcHorizontalPos() {
    var leftPad = getPadding().getLeft();
    var rightPad = getPadding().getRight();
    var promptWidth = getPlaceholder().getLayoutBounds().getWidth();
    var alignment = getAlignment();
    var alignmentPadding = leftPad;

    if (alignment == TextAlignment.RIGHT) {
      alignmentPadding = rightPad;
    } else if (alignment == TextAlignment.CENTER) {
      alignmentPadding = 0;
    }
    if (promptWidth < (getWidth() - alignmentPadding)) {
      setClip(null);
    } else {
      setClip(new Rectangle(getWidth(), getHeight()));
    }
    
    return switch (alignment) {
      case CENTER -> (getWidth() - promptWidth) / 2;
      case RIGHT -> getWidth() - rightPad - promptWidth;
      default -> leftPad;
    };
  }

  @Override
  public void replaceText(int start, int end, String text) {
    super.replaceText(start, end, VERTICAL_WHITESPACE.matcher(text).replaceAll(" "));
  }

  public void setText(String text) {
    replaceText(text);
  }
  
  @Override
  public void setWrapText(boolean value) {
    // This is not applicable for text fields.
  }

  @Override
  public boolean isWrapText() {
    return false;  // This ALWAYS returns FALSE for styled text fields.
  }

  @Override
  public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
    return CSS_META_DATA_LIST;
  }

  public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
    return CSS_META_DATA_LIST;
  }

}
