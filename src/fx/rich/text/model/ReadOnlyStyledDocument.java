package fx.rich.text.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import fx.react.collection.MaterializedModification;
import fx.util.tree.Index;
import fx.util.Either;
import fx.util.tree.FingerTree;
import fx.util.tree.NonEmpty;
import fx.react.util.Lists;
import fx.util.tree.ToSemigroup;
import static fx.util.Either.*;

import fx.react.state.Tuple2;
import fx.react.state.Tuple3;
import fx.react.state.Tuples;
import static fx.react.state.Tuples.*;


/**
 * An immutable implementation of {@link StyledDocument} that does not allow editing. For a {@link StyledDocument}
 * that can be edited, see {@link EditableStyledDocument}. To create one, use its static factory
 * "from"-prefixed methods or {@link ReadOnlyStyledDocumentBuilder}.
 *
 * @param <PS> The type of the paragraph style.
 * @param <SEG> The type of the segments in the paragraph (e.g. {@link String}).
 * @param <S> The type of the style of individual segments.
 */
public final class ReadOnlyStyledDocument<PS, SEG, S> implements StyledDocument<PS, SEG, S> {

  /**
   * Private class used for calculating {@link TwoDimensional.Position}s within this document.
   */
  static class Summary {
    final int paragraphCount;
    final int charCount;

    Summary(int paragraphCount, int charCount) {
      assert paragraphCount > 0;
      assert charCount >= 0;
      this.paragraphCount = paragraphCount;
      this.charCount = charCount;
    }
    int length() {
      return charCount + paragraphCount - 1;
    }
  }

  /**
   * Private method for quickly calculating the length of a portion (subdocument) of this document.
   */
  static <PS, SEG, S> ToSemigroup<Paragraph<PS, SEG, S>, Summary> summaryProvider() {
    return new ToSemigroup<Paragraph<PS, SEG, S>, Summary>() {
      @Override
      public Summary apply(Paragraph<PS, SEG, S> p) {
        return new Summary(1, p.length());
      }
      @Override
      public Summary reduce(Summary left, Summary right) {
        return new Summary(left.paragraphCount + right.paragraphCount, left.charCount + right.charCount);
      }
    };
  }

  static final Pattern LINE_TERMINATOR = Pattern.compile("\r\n|\r|\n");

  static final BiFunction<Summary, Integer, Either<Integer, Integer>> NAVIGATE =
    (s, i) -> i <= s.length() ? left(i) : right(i - (s.length() + 1));

  /**
   * Creates a {@link ReadOnlyStyledDocument} from the given string.
   *
   * @param str the text to use to create the segments
   * @param paragraphStyle the paragraph style to use for each paragraph in the returned document
   * @param style the style to use for each segment in the document
   * @param segmentOps the operations object that can create a segment froma given text
   * @param <PS> The type of the paragraph style.
   * @param <SEG> The type of the segments in the paragraph (e.g. {@link String}).
   * @param <S> The type of the style of individual segments.
   */
  public static <PS, SEG, S> ReadOnlyStyledDocument<PS, SEG, S> fromString(String str, PS paragraphStyle, S style, TextOps<SEG, S> segmentOps) {
    var m = LINE_TERMINATOR.matcher(str);
    var n = 1;
    while (m.find()) {
      ++n;
    }
    var res = new ArrayList<Paragraph<PS, SEG, S>>(n);

    var start = 0;
    m.reset();
    while (m.find()) {
      var s = str.substring(start, m.start());
      res.add(new Paragraph<>(paragraphStyle, segmentOps, segmentOps.create(s), style));
      start = m.end();
    }
    var last = str.substring(start);
    res.add(new Paragraph<>(paragraphStyle, segmentOps, segmentOps.create(last), style));

    return new ReadOnlyStyledDocument<>(res);
  }

  /**
   * Creates a {@link ReadOnlyStyledDocument} from the given segment.
   *
   * @param segment the only segment in the only paragraph in the document
   * @param paragraphStyle the paragraph style to use for each paragraph in the returned document
   * @param style the style to use for each segment in the document
   * @param segmentOps the operations object that can create a segment froma given text
   * @param <PS> The type of the paragraph style.
   * @param <SEG> The type of the segments in the paragraph (e.g. {@link String}).
   * @param <S> The type of the style of individual segments.
   */
  public static <PS, SEG, S> ReadOnlyStyledDocument<PS, SEG, S> fromSegment(SEG segment, PS paragraphStyle, S style, SegmentOps<SEG, S> segmentOps) {
    if (segment instanceof String && segmentOps instanceof TextOps) {
      return fromString((String) segment, paragraphStyle, style, (TextOps<SEG, S>) segmentOps);
    }
    var content = new Paragraph<PS, SEG, S>(paragraphStyle, segmentOps, segment, style);
    var res = Collections.singletonList(content);
    return new ReadOnlyStyledDocument<>(res);
  }

  /**
   * Creates a {@link ReadOnlyStyledDocument} from the given {@link StyledDocument}.
   *
   * @param <PS> The type of the paragraph style.
   * @param <SEG> The type of the segments in the paragraph (e.g. {@link String}).
   * @param <S> The type of the style of individual segments.
   */
  public static <PS, SEG, S> ReadOnlyStyledDocument<PS, SEG, S> from(StyledDocument<PS, SEG, S> doc) {
    return (doc instanceof ReadOnlyStyledDocument<PS, SEG, S> rosd) ? rosd : new ReadOnlyStyledDocument<>(doc.getParagraphs());
  }

  /**
   * Defines a codec for serializing a {@link ReadOnlyStyledDocument}.
   *
   * @param pCodec the codec for serializing a {@link Paragraph}
   * @param segCodec the codec for serializing a {@link StyledSegment}
   * @param segmentOps the operations object for operating on segments
   *
   * @param <PS> The type of the paragraph style.
   * @param <SEG> The type of the segments in the paragraph (e.g. {@link String}).
   * @param <S> The type of the style of individual segments.
   */
  public static <PS, SEG, S> Codec<StyledDocument<PS, SEG, S>> codec(Codec<PS> pCodec, Codec<StyledSegment<SEG, S>> segCodec, SegmentOps<SEG, S> segmentOps) {
    return new Codec<StyledDocument<PS, SEG, S>>() {
      final Codec<List<Paragraph<PS, SEG, S>>> codec =
        Codec.listCodec(paragraphCodec(pCodec, segCodec, segmentOps));

      @Override
      public String getName() {
        return "application/richtextfx-styled-document<" + pCodec.getName() + ";" + segCodec.getName() + ">";
      }
      @Override
      public void encode(DataOutputStream os, StyledDocument<PS, SEG, S> doc) throws IOException {
        codec.encode(os, doc.getParagraphs());
      }
      @Override
      public StyledDocument<PS, SEG, S> decode(DataInputStream is) throws IOException {
        return new ReadOnlyStyledDocument<>(codec.decode(is));
      }
    };
  }

  static <PS, SEG, S> Codec<Paragraph<PS, SEG, S>> paragraphCodec(Codec<PS> pCodec, Codec<StyledSegment<SEG, S>> segCodec, SegmentOps<SEG, S> segmentOps) {
    return new Codec<Paragraph<PS, SEG, S>>() {
      final Codec<List<StyledSegment<SEG, S>>> segmentsCodec = Codec.listCodec(segCodec);

      @Override
      public String getName() {
        return "paragraph<" + pCodec.getName() + ";" + segCodec.getName() + ">";
      }
      @Override
      public void encode(DataOutputStream os, Paragraph<PS, SEG, S> p) throws IOException {
        pCodec.encode(os, p.getParagraphStyle());
        segmentsCodec.encode(os, p.getStyledSegments());
      }
      @Override
      public Paragraph<PS, SEG, S> decode(DataInputStream is) throws IOException {
        var paragraphStyle = pCodec.decode(is);
        var segments = segmentsCodec.decode(is);
        return new Paragraph<>(paragraphStyle, segmentOps, segments);
      }
    };
  }

  final NonEmpty<Paragraph<PS, SEG, S>, Summary> tree;

  String text = null;
  List<Paragraph<PS, SEG, S>> paragraphs = null;

  ReadOnlyStyledDocument(NonEmpty<Paragraph<PS, SEG, S>, Summary> tree) {
    this.tree = tree;
  }

  ReadOnlyStyledDocument(List<Paragraph<PS, SEG, S>> paragraphs) {
    this.tree = FingerTree
      .mkTree(paragraphs, summaryProvider())
      .caseEmpty().unify(
         emptyTree -> { throw new AssertionError("Unreachable code"); },
         neTree -> neTree
       );
  }

  @Override
  public int length() {
    return tree.getSummary().length();
  }

  @Override
  public String getText() {
    if (text == null) {
      var strings = getParagraphs().stream().map(Paragraph::getText).toArray(n -> new String[n]);
      text = String.join("\n", strings);
    }
    return text;
  }

  public int getParagraphCount() {
    return tree.getLeafCount();
  }

  public Paragraph<PS, SEG, S> getParagraph(int index) {
    return tree.getLeaf(index);
  }

  @Override
  public List<Paragraph<PS, SEG, S>> getParagraphs() {
    if (paragraphs == null) {
      paragraphs = tree.asList();
    }
    return paragraphs;
  }

  @Override
  public Position position(int major, int minor) {
    return new Pos(major, minor);
  }

  @Override
  public Position offsetToPosition(int offset, Bias bias) {
    return position(0, 0).offsetBy(offset, bias);
  }

  /**
   * Splits this document into two at the given position and returns both halves.
   */
  public Tuple2<ReadOnlyStyledDocument<PS, SEG, S>, ReadOnlyStyledDocument<PS, SEG, S>> split(int position) {
    return tree.locate(NAVIGATE, position).map(this::split);
  }

  /**
   * Splits this document into two at the given paragraph's column position and returns both halves.
   */
  public Tuple2<ReadOnlyStyledDocument<PS, SEG, S>, ReadOnlyStyledDocument<PS, SEG, S>> split(int paragraphIndex, int columnPosition) {
    return tree
      .splitAt(paragraphIndex)
      .map((l, p, r) -> {
        var p1 = p.trim(columnPosition);
        var p2 = p.subSequence(columnPosition);
        var doc1 = new ReadOnlyStyledDocument<>(l.append(p1));
        var doc2 = new ReadOnlyStyledDocument<>(r.prepend(p2));
        return t(doc1, doc2);
      }
    );
  }

  @Override
  public ReadOnlyStyledDocument<PS, SEG, S> concat(StyledDocument<PS, SEG, S> other) {
    return concat0(other, Paragraph::concat);
  }

  private ReadOnlyStyledDocument<PS, SEG, S> concatR(StyledDocument<PS, SEG, S> other) {
    return concat0(other, Paragraph::concatR);
  }

  private ReadOnlyStyledDocument<PS, SEG, S> concat0(StyledDocument<PS, SEG, S> other, BinaryOperator<Paragraph<PS, SEG, S>> parConcat) {
    var n = tree.getLeafCount() - 1;
    var p0 = tree.getLeaf(n);
    var p1 = other.getParagraphs().get(0);
    var p = parConcat.apply(p0, p1);
    var tree1 = tree.updateLeaf(n, p);
    var tree2 = (other instanceof ReadOnlyStyledDocument<PS, SEG, S> rosd)
        ? rosd.tree.split(1).b()
        : FingerTree.mkTree(other.getParagraphs().subList(1, other.getParagraphs().size()), summaryProvider());
    return new ReadOnlyStyledDocument<>(tree1.join(tree2));
  }

  @Override
  public StyledDocument<PS, SEG, S> subSequence(int start, int end) {
    return split(end)._1.split(start)._2;
  }

  /**
   * Replaces multiple portions of this document in an efficient manner and returns
   * <ol>
   *     <li>
   *         the updated version of this document that includes all of the replacements,
   *     </li>
   *     <li>
   *         the List of {@link RichTextChange} that represent all the changes from this document
   *         to the returned one, and
   *     </li>
   *     <li>
   *         the List of modifications used to update an area's list of paragraphs for each change.
   *     </li>
   * </ol>
   */
  public Tuple3<ReadOnlyStyledDocument<PS, SEG, S>, List<RichTextChange<PS, SEG, S>>, List<MaterializedModification<Paragraph<PS, SEG, S>>>> replaceMulti(List<Replacement<PS, SEG, S>> replacements) {
    var updatedDoc = this;
    var richChangeList = new ArrayList<RichTextChange<PS, SEG, S>>(replacements.size());
    var parChangeList = new ArrayList<MaterializedModification<Paragraph<PS, SEG, S>>>(replacements.size());
    for (var r : replacements) {
      // Tuple3<ReadOnlyStyledDocument<PS, SEG, S>, RichTextChange<PS, SEG, S>, MaterializedModification<Paragraph<PS, SEG, S>>>
      var postReplacement = updatedDoc.replace(r);
      updatedDoc = postReplacement.get1();
      richChangeList.add(postReplacement.get2());
      parChangeList.add(postReplacement.get3());
    }
    return Tuples.t(updatedDoc, richChangeList, parChangeList);
  }

  /**
   * Convenience method for calling {@link #replace(int, int, ReadOnlyStyledDocument)} with a {@link Replacement}
   * argument.
   */
  public Tuple3<ReadOnlyStyledDocument<PS, SEG, S>, RichTextChange<PS, SEG, S>, MaterializedModification<Paragraph<PS, SEG, S>>> replace(Replacement<PS, SEG, S> replacement) {
    return replace(replacement.getStart(), replacement.getEnd(), replacement.getDocument());
  }

  /**
   * Replaces the given portion {@code "from..to"} with the given replacement and returns
   * <ol>
   *     <li>
   *         the updated version of this document that includes the replacement,
   *     </li>
   *     <li>
   *         the {@link RichTextChange} that represents the change from this document to the returned one, and
   *     </li>
   *     <li>
   *         the modification used to update an area's list of paragraphs.
   *     </li>
   * </ol>
   */
  public Tuple3<ReadOnlyStyledDocument<PS, SEG, S>, RichTextChange<PS, SEG, S>, MaterializedModification<Paragraph<PS, SEG, S>>> replace(int from, int to, ReadOnlyStyledDocument<PS, SEG, S> replacement) {
    return replace(from, to, x -> replacement);
  }

  /**
   * Replaces the given portion {@code "from..to"} in the document by getting that portion of this document,
   * passing it into the mapping function, and using the result as the replacement. Returns
   * <ol>
   *     <li>
   *         the updated version of this document that includes the replacement,
   *     </li>
   *     <li>
   *         the {@link RichTextChange} that represents the change from this document to the returned one, and
   *     </li>
   *     <li>
   *         the modification used to update an area's list of paragraphs.
   *     </li>
   * </ol>
   */
  public Tuple3<ReadOnlyStyledDocument<PS, SEG, S>, RichTextChange<PS, SEG, S>, MaterializedModification<Paragraph<PS, SEG, S>>> replace(int from, int to, UnaryOperator<ReadOnlyStyledDocument<PS, SEG, S>> mapper) {
    ensureValidRange(from, to);
    var start = tree.locate(NAVIGATE, from);
    var end = tree.locate(NAVIGATE, to);
    return replace(start, end, mapper);
  }

  public Tuple3<ReadOnlyStyledDocument<PS, SEG, S>, RichTextChange<PS, SEG, S>, MaterializedModification<Paragraph<PS, SEG, S>>> replace(int paragraphIndex, int fromCol, int toCol, UnaryOperator<ReadOnlyStyledDocument<PS, SEG, S>> f) {
    ensureValidParagraphRange(paragraphIndex, fromCol, toCol);
    return replace(new Index(paragraphIndex, fromCol), new Index(paragraphIndex, toCol), f);
  }

  // Note: there must be a "ensureValid_()" call preceding the call of this method
  private Tuple3<ReadOnlyStyledDocument<PS, SEG, S>, RichTextChange<PS, SEG, S>, MaterializedModification<Paragraph<PS, SEG, S>>> replace(Index start, Index end, UnaryOperator<ReadOnlyStyledDocument<PS, SEG, S>> f) {
    var pos = tree.getSummaryBetween(0, start.major).map(s -> s.length() + 1).orElse(0) + start.minor;
    var removedPars = getParagraphs().subList(start.major, end.major + 1);
    return end.map(this::split).map((l0, r) -> {
      return start.map(l0::split).map((l, removed) -> {
        var replacement = f.apply(removed);
        var doc = l.concatR(replacement).concat(r);
        // Next we use doc.subSequence instead of replacement because Paragraph.concat's returned paragraph style can vary.
        var change = new RichTextChange<PS, SEG, S>(pos, removed, doc.subSequence(pos, pos + replacement.length()));
        var addedPars = doc.getParagraphs().subList(start.major, start.major + replacement.getParagraphCount());
        var parChange = MaterializedModification.create(start.major, removedPars, addedPars);
        return t(doc, change, parChange);
      });
    });
  }

  /**
   * Maps the paragraph at the given index by calling {@link #replace(int, int, UnaryOperator)}. Returns
   * <ol>
   *     <li>
   *         the updated version of this document that includes the replacement,
   *     </li>
   *     <li>
   *         the {@link RichTextChange} that represents the change from this document to the returned one, and
   *     </li>
   *     <li>
   *         the modification used to update an area's list of paragraphs.
   *     </li>
   * </ol>
   */
  public Tuple3<ReadOnlyStyledDocument<PS, SEG, S>, RichTextChange<PS, SEG, S>, MaterializedModification<Paragraph<PS, SEG, S>>> replaceParagraph(int parIdx, UnaryOperator<Paragraph<PS, SEG, S>> mapper) {
    ensureValidParagraphIndex(parIdx);
    return replace(new Index(parIdx, 0), new Index(parIdx, tree.getLeaf(parIdx).length()), doc -> doc.mapParagraphs(mapper));
  }

  /**
   * Maps all of this document's paragraphs using the given mapper and returns them in a new
   * {@link ReadOnlyStyledDocument}.
   */
  public ReadOnlyStyledDocument<PS, SEG, S> mapParagraphs(UnaryOperator<Paragraph<PS, SEG, S>> mapper) {
    var n = tree.getLeafCount();
    var pars = new ArrayList<Paragraph<PS, SEG, S>>(n);
    for (var i = 0; i < n; ++i) {
      pars.add(mapper.apply(tree.getLeaf(i)));
    }
    return new ReadOnlyStyledDocument<>(pars);
  }

  @Override
  public String toString() {
    return getParagraphs().stream().map(Paragraph::toString).reduce((p1, p2) -> p1 + "\n" + p2).orElse("");
  }

  @Override
  public final boolean equals(Object other) {
    return (other instanceof StyledDocument<?, ?, ?> that)
      ? Objects.equals(this.getParagraphs(), that.getParagraphs()) : false;
  }

  @Override
  public final int hashCode() {
    return getParagraphs().hashCode();
  }

  class Pos implements Position {
    Pos(int major, int minor) {
      this.major = major;
      this.minor = minor;
    }

    final int major;
    final int minor;

    @Override
    public String toString() {
      return "(" + major + ", " + minor + ")";
    }
    @Override
    public boolean sameAs(Position other) {
      return getTargetObject() == other.getTargetObject() && major == other.getMajor() && minor == other.getMinor();
    }
    @Override
    public TwoDimensional getTargetObject() {
      return ReadOnlyStyledDocument.this;
    }
    @Override
    public int getMajor() {
      return major;
    }
    @Override
    public int getMinor() {
      return minor;
    }
    @Override
    public Position clamp() {
      if (major == tree.getLeafCount() - 1) {
        var elemLen = tree.getLeaf(major).length();
        return (minor < elemLen) ? this : new Pos(major, elemLen - 1);
      } else {
        return this;
      }
    }

    @Override
    public Position offsetBy(int amount, Bias bias) {
      return tree.locateProgressively(s -> s.charCount + s.paragraphCount, toOffset() + amount).map(Pos::new);
    }

    @Override
    public int toOffset() {
      return (major == 0) ? minor : tree.getSummaryBetween(0, major).get().length() + 1 + minor;
    }
  }

  void ensureValidParagraphIndex(int parIdx) {
    Lists.checkIndex(parIdx, getParagraphCount());
  }

  void ensureValidRange(int start, int end) {
    Lists.checkRange(start, end, length());
  }

  void ensureValidParagraphRange(int par, int start, int end) {
    ensureValidParagraphIndex(par);
    Lists.checkRange(start, end, fullLength(par));
  }

  int fullLength(int par) {
    var n = getParagraphCount();
    return getParagraph(par).length() + (par == n - 1 ? 0 : 1);
  }

}
