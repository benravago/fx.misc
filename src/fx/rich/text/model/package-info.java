/**
 * Defines the support classes and operations related to {@link fx.rich.text.model.EditableStyledDocument},
 * the immutable model of rich-text content that can be rendered and edited.
 *
 * <p>
 *     An {@link fx.rich.text.model.EditableStyledDocument} acts as an immutable model for rich-text content
 *     that will be rendered by an object implementing the {@link fx.rich.text.TextEditingArea} interface.
 *     A {@link fx.rich.text.model.StyledDocument} is composed of a list of
 *     {@link fx.rich.text.model.Paragraph}s. Paragraphs are nothing more than an
 *     object containing a paragraph style (type {@code PS}), a list of a generic segments (type {@code SEG}), and a
 *     list of generic styles (type {@code S}) that can apply to a segment. Most of the time, either
 *     {@link fx.rich.text.model.EditableStyledDocument} or
 *     {@link fx.rich.text.model.ReadOnlyStyledDocument} are being used to implement that interface.
 * </p>
 * <p>
 *     The document can include more than just text; thus, the segment generic
 *     can be specified as regular text ({@link java.lang.String}) or as an {@link fx.react.util.Either} (e.g.
 *     {@code Either<String, Image>} or as a nested Either (e.g.
 *     {@code Either<String, Either<Image, Either<Circle, Square>}) if one wanted to have four different kinds of segments
 *     (ways to specify a segment generic in a way that still makes the code easy to read are not described here).
 * </p>
 * <p>
 *     To allow these generics, one must supply a {@link fx.rich.text.model.SegmentOps} object that can
 *     correctly operate on the generic segments and their generic styles. In addition, a
 *     {@link fx.rich.text.model.TextOps} adds one more method to its base interface by adding a method
 *     that maps a {@link java.lang.String} to a given segment. For text-based custom segments, one should use
 *     {@link fx.rich.text.model.SegmentOpsBase} and for node-based custom segments, one should use
 *     {@link fx.rich.text.model.NodeSegmentOpsBase}.
 * </p>
 * <p>
 *     The document also uses {@link fx.rich.text.model.StyleSpans} to store styles in a memory-efficient way.
 *     To construct one, use {@link fx.rich.text.model.StyleSpans#singleton(fx.rich.text.model.StyleSpan)}
 *     or {@link fx.rich.text.model.StyleSpansBuilder}.
 * </p>
 * <p>
 *     To navigate throughout the document, read through the javadoc of
 *     {@link fx.rich.text.model.TwoDimensional} and {@link fx.rich.text.model.TwoDimensional.Bias}.
 *     Also, read the difference between "position" and "index" in
 *     {@link fx.rich.text.model.StyledDocument#getAbsolutePosition(int, int)}.
 * </p>
 * <p>To serialize things correctly, see {@link fx.rich.text.model.Codec} and its static factory methods.
 * </p>
 * <p>
 *     Lastly, the {@link fx.rich.text.model.EditableStyledDocument} can emit
 *     {@link fx.rich.text.model.PlainTextChange}s or {@link fx.rich.text.model.RichTextChange}s
 *     that can be used to undo/redo various changes.
 * </p>
 *
 * @see fx.rich.text.model.EditableStyledDocument
 * @see fx.rich.text.model.Paragraph
 * @see fx.rich.text.model.SegmentOps
 * @see fx.rich.text.model.TwoDimensional
 * @see fx.rich.text.model.TwoDimensional.Bias
 */
package fx.rich.text.model;