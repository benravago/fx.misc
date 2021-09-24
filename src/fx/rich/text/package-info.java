/**
 * Defines the view-related classes for rendering and editing an
 * {@link fx.rich.text.model.EditableStyledDocument EditableStyledDocument}.
 *
 * <p>
 *     The base area is {@link fx.rich.text.GenericStyledArea}. Those unfamiliar with this
 *     project should read through its javadoc. This class should be used for custom segments (e.g. text and images
 *     in the same area). {@link fx.rich.text.StyledTextArea} uses {@link java.lang.String}-only segments,
 *     and styling them are already supported in the two most common ways via
 *     {@link fx.rich.text.StyleClassedTextArea} and {@link fx.rich.text.InlineCssTextArea}.
 *     For those looking to use a base for a code editor, see {@link fx.rich.text.CodeArea}.
 * </p>
 * <p>
 *     For text fields there is {@link fx.rich.text.StyledTextField} using {@link java.lang.String}-only segments,
 *     and styling them are also already supported in the two most common ways via
 *     {@link fx.rich.text.StyleClassedTextField} and {@link fx.rich.text.InlineCssTextField}.
 * </p>
 *
 * @see fx.rich.text.model.EditableStyledDocument
 * @see fx.rich.text.model.TwoDimensional
 * @see fx.rich.text.model.TwoDimensional.Bias
 * @see fx.rich.text.GenericStyledArea
 * @see fx.rich.text.TextEditingArea
 * @see fx.rich.text.Caret
 * @see fx.rich.text.Selection
 */
package fx.rich.text;