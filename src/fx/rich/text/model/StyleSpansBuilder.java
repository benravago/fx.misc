package fx.rich.text.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

/**
 * A one-time-use builder that Builds a memory efficient {@link StyleSpans} object.
 *
 * @param <S> the segment style type
 */
public class StyleSpansBuilder<S> {

  static class StyleSpansImpl<S> extends StyleSpansBase<S> {
    StyleSpansImpl(List<StyleSpan<S>> spans) {
      this.spans = spans;
    }

    final List<StyleSpan<S>> spans;
    int length = -1;

    @Override
    public Iterator<StyleSpan<S>> iterator() {
      return spans.iterator();
    }
    @Override
    public int length() {
      if (length == -1) {
        length = spans.stream().mapToInt(StyleSpan::getLength).sum();
      }
      return length;
    }
    @Override
    public int getSpanCount() {
      return spans.size();
    }
    @Override
    public StyleSpan<S> getStyleSpan(int index) {
      return spans.get(index);
    }
    @Override
    public String toString() {
      return "StyleSpans(length=" + length() + " spanCount=" + getSpanCount() + " spans=" + spans + ')';
    }
  }

  static <S> StyleSpans<S> overlay(StyleSpans<S> s1, StyleSpans<S> s2, BiFunction<? super S, ? super S, ? extends S> f) {
    var acc = new StyleSpansBuilder<S>(s1.getSpanCount() + s2.getSpanCount());
    var t1 = s1.iterator();
    var t2 = s2.iterator();
    var h1 = t1.next(); // remember that all StyleSpans have at least one StyleSpan
    var h2 = t2.next(); // remember that all StyleSpans have at least one StyleSpan
    for (;;) {
      var len1 = h1.getLength();
      var len2 = h2.getLength();
      if (len1 == len2) {
        acc.add(f.apply(h1.getStyle(), h2.getStyle()), len1);
        if (!t1.hasNext()) {
          return acc.addAll(t2).create();
        } else if (!t2.hasNext()) {
          return acc.addAll(t1).create();
        } else {
          h1 = t1.next();
          h2 = t2.next();
        }
      } else if (len1 < len2) {
        acc.add(f.apply(h1.getStyle(), h2.getStyle()), len1);
        h2 = new StyleSpan<>(h2.getStyle(), len2 - len1);
        if (t1.hasNext()) {
          h1 = t1.next();
        } else {
          return acc.add(h2).addAll(t2).create();
        }
      } else { // len1 > len2
        acc.add(f.apply(h1.getStyle(), h2.getStyle()), len2);
        h1 = new StyleSpan<>(h1.getStyle(), len1 - len2);
        if (t2.hasNext()) {
          h2 = t2.next();
        } else {
          return acc.add(h1).addAll(t1).create();
        }
      }
    }
  }

  boolean created = false;
  final ArrayList<StyleSpan<S>> spans;

  public StyleSpansBuilder(int initialCapacity) {
    this.spans = new ArrayList<>(initialCapacity);
  }

  public StyleSpansBuilder() {
    this.spans = new ArrayList<>();
  }

  public StyleSpansBuilder<S> add(StyleSpan<S> styleSpan) {
    ensureNotCreated();
    addSpan(styleSpan);
    return this;
  }

  public StyleSpansBuilder<S> add(S style, int length) {
    return add(new StyleSpan<>(style, length));
  }

  public StyleSpansBuilder<S> addAll(Collection<? extends StyleSpan<S>> styleSpans) {
    return addAll(styleSpans, styleSpans.size());
  }

  public StyleSpansBuilder<S> addAll(Iterable<? extends StyleSpan<S>> styleSpans, int sizeHint) {
    spans.ensureCapacity(spans.size() + sizeHint);
    return addAll(styleSpans);
  }

  public StyleSpansBuilder<S> addAll(Iterable<? extends StyleSpan<S>> styleSpans) {
    ensureNotCreated();
    for (var span : styleSpans) {
      addSpan(span);
    }
    return this;
  }

  public StyleSpansBuilder<S> addAll(Iterator<? extends StyleSpan<S>> styleSpans) {
    ensureNotCreated();
    while (styleSpans.hasNext()) {
      addSpan(styleSpans.next());
    }
    return this;
  }

  public StyleSpans<S> create() {
    ensureNotCreated();
    if (spans.isEmpty()) {
      throw new IllegalStateException("No spans have been added");
    }

    created = true;
    return new StyleSpansImpl<>(Collections.unmodifiableList(spans));
  }

  void addSpan(StyleSpan<S> span) {
    if (spans.isEmpty()) {
      spans.add(span);
    } else if (span.getLength() > 0) {
      if (spans.size() == 1 && spans.get(0).getLength() == 0) {
        spans.set(0, span);
      } else {
        StyleSpan<S> prev = spans.get(spans.size() - 1);
        if (prev.getStyle().equals(span.getStyle())) {
          spans.set(spans.size() - 1, new StyleSpan<>(span.getStyle(), prev.getLength() + span.getLength()));
        } else {
          spans.add(span);
        }
      }
    } else {
      // do nothing, don't add a zero-length span
    }
  }

  void ensureNotCreated() {
    if (created) {
      throw new IllegalStateException("Cannot reuse StyleRangesBuilder after StyleRanges have been created.");
    }
  }

}
