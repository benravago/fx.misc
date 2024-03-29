package fx.rich.text.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fx.react.EventSource;
import fx.react.EventStream;
import fx.react.Subscription;
import fx.react.Suspendable;
import fx.react.SuspendableEventStream;
import fx.react.SuspendableNo;
import fx.react.collection.ListChangeAccumulator;
import fx.react.collection.LiveList;
import fx.react.collection.LiveListBase;
import fx.react.collection.MaterializedModification;
import fx.react.collection.QuasiModification;
import fx.react.collection.SuspendableList;
import fx.react.collection.UnmodifiableByDefaultLiveList;
import fx.react.value.SuspendableVal;
import fx.react.value.Val;
import static fx.rich.text.model.TwoDimensional.Bias.*;

class GenericEditableStyledDocumentBase<PS, SEG, S> implements EditableStyledDocument<PS, SEG, S> {

  class Paragraphs extends LiveListBase<Paragraph<PS, SEG, S>> implements UnmodifiableByDefaultLiveList<Paragraph<PS, SEG, S>> {

    @Override
    public Paragraph<PS, SEG, S> get(int index) {
      return doc.getParagraph(index);
    }

    @Override
    public int size() {
      return doc.getParagraphCount();
    }

    @Override
    protected Subscription observeInputs() {
      return parChangesList.subscribe(list -> {
        var accumulator = new ListChangeAccumulator<Paragraph<PS, SEG, S>>();
        for (var mod : list) {
          try { mod = mod.trim(); }
          catch (IndexOutOfBoundsException ignore) {}
          // add the quasiListModification itself, not as a quasiListChange, in case some overlap
          accumulator.add(QuasiModification.create(mod.getFrom(), mod.getRemoved(), mod.getAddedSize()));
        }
        notifyObservers(accumulator.asListChange());
      });
    }
  }

  ReadOnlyStyledDocument<PS, SEG, S> doc;

  final EventSource<List<RichTextChange<PS, SEG, S>>> internalRichChangeList = new EventSource<>();
  final SuspendableEventStream<List<RichTextChange<PS, SEG, S>>> richChangeList = internalRichChangeList.pausable();

  @Override
  public EventStream<List<RichTextChange<PS, SEG, S>>> multiRichChanges() {
    return richChangeList;
  }

  final Val<String> internalText = Val.create(() -> doc.getText(), internalRichChangeList);
  final SuspendableVal<String> text = internalText.suspendable();

  @Override
  public String getText() {
    return text.getValue();
  }

  @Override
  public Val<String> textProperty() {
    return text;
  }

  final Val<Integer> internalLength = Val.create(() -> doc.length(), internalRichChangeList);
  final SuspendableVal<Integer> length = internalLength.suspendable();

  @Override
  public int getLength() {
    return length.getValue();
  }

  @Override
  public Val<Integer> lengthProperty() {
    return length;
  }

  @Override
  public int length() {
    return length.getValue();
  }

  final EventSource<List<MaterializedModification<Paragraph<PS, SEG, S>>>> parChangesList = new EventSource<>();

  final SuspendableList<Paragraph<PS, SEG, S>> paragraphs = new Paragraphs().suspendable();

  @Override
  public LiveList<Paragraph<PS, SEG, S>> getParagraphs() {
    return paragraphs;
  }

  @Override
  public ReadOnlyStyledDocument<PS, SEG, S> snapshot() {
    return doc;
  }

  final SuspendableNo beingUpdated = new SuspendableNo();

  @Override
  public final SuspendableNo beingUpdatedProperty() {
    return beingUpdated;
  }

  @Override
  public final boolean isBeingUpdated() {
    return beingUpdated.get();
  }

  /**
   * Creates an {@link EditableStyledDocument} with the given document as its initial content
   */
  GenericEditableStyledDocumentBase(ReadOnlyStyledDocument<PS, SEG, S> initialContent) {
    this.doc = initialContent;
    var omniSuspendable = Suspendable.combine(
      text,
      length,
      // add streams after properties, to be released before them
      richChangeList,
      // paragraphs to be released first
      paragraphs
    );
    omniSuspendable.suspendWhen(beingUpdated);
  }

  /**
   * Creates an {@link EditableStyledDocument} with the given paragraph as its initial content
   */
  GenericEditableStyledDocumentBase(Paragraph<PS, SEG, S> initialParagraph) {
    this(new ReadOnlyStyledDocument<>(Collections.singletonList(initialParagraph)));
  }

  /**
   * Creates an empty {@link EditableStyledDocument}
   */
  GenericEditableStyledDocumentBase(PS initialParagraphStyle, S initialStyle, SegmentOps<SEG, S> segmentOps) {
    this(new Paragraph<>(initialParagraphStyle, segmentOps, segmentOps.createEmptySeg(), initialStyle));
  }

  @Override
  public Position position(int major, int minor) {
    return doc.position(major, minor);
  }

  @Override
  public Position offsetToPosition(int offset, Bias bias) {
    return doc.offsetToPosition(offset, bias);
  }

  @Override
  public void replaceMulti(List<Replacement<PS, SEG, S>> replacements) {
    doc.replaceMulti(replacements).exec(this::updateMulti);
  }

  @Override
  public void replace(int start, int end, StyledDocument<PS, SEG, S> replacement) {
    doc.replace(start, end, ReadOnlyStyledDocument.from(replacement)).exec(this::updateSingle);
  }

  @Override
  public void setStyle(int from, int to, S style) {
    doc.replace(from, to, removed -> removed.mapParagraphs(par -> par.restyle(style))).exec(this::updateSingle);
  }

  @Override
  public void setStyle(int paragraphIndex, S style) {
    doc.replaceParagraph(paragraphIndex, p -> p.restyle(style)).exec(this::updateSingle);
  }

  @Override
  public void setStyle(int paragraphIndex, int fromCol, int toCol, S style) {
    doc.replace(paragraphIndex, fromCol, toCol, d -> d.mapParagraphs(p -> p.restyle(style))).exec(this::updateSingle);
  }

  @Override
  public void setStyleSpans(int from, StyleSpans<? extends S> styleSpans) {
    var len = styleSpans.length();
    doc.replace(from, from + len, d -> {
      var i = styleSpans.position(0, 0);
      var pars = new ArrayList<Paragraph<PS, SEG, S>>(d.getParagraphs().size());
      for (var p : d.getParagraphs()) {
        var j = i.offsetBy(p.length(), Backward);
        var spans = styleSpans.subView(i, j);
        pars.add(p.restyle(0, spans));
        i = j.offsetBy(1, Forward); // skip the newline
      }
      return new ReadOnlyStyledDocument<>(pars);
    }).exec(this::updateSingle);
  }

  @Override
  public void setStyleSpans(int paragraphIndex, int from, StyleSpans<? extends S> styleSpans) {
    setStyleSpans(doc.position(paragraphIndex, from).toOffset(), styleSpans);
  }

  @Override
  public void setParagraphStyle(int paragraphIndex, PS style) {
    doc.replaceParagraph(paragraphIndex, p -> p.setParagraphStyle(style)).exec(this::updateSingle);
  }

  @Override
  public StyledDocument<PS, SEG, S> concat(StyledDocument<PS, SEG, S> that) {
    return doc.concat(that);
  }

  @Override
  public StyledDocument<PS, SEG, S> subSequence(int start, int end) {
    return doc.subSequence(start, end);
  }

  /* and package private methods */

  void updateSingle(ReadOnlyStyledDocument<PS, SEG, S> newValue, RichTextChange<PS, SEG, S> change, MaterializedModification<Paragraph<PS, SEG, S>> parChange) {
    updateMulti(newValue, Collections.singletonList(change), Collections.singletonList(parChange));
  }

  void updateMulti(ReadOnlyStyledDocument<PS, SEG, S> newValue, List<RichTextChange<PS, SEG, S>> richChanges, List<MaterializedModification<Paragraph<PS, SEG, S>>> parChanges) {
    this.doc = newValue;
    beingUpdated.suspendWhile(() -> {
      internalRichChangeList.push(richChanges);
      parChangesList.push(parChanges);
    });
  }

}
