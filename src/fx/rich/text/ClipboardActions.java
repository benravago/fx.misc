package fx.rich.text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Optional;

import javafx.scene.control.IndexRange;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.util.Pair;

import fx.rich.text.model.Codec;
import fx.rich.text.model.ReadOnlyStyledDocument;
import fx.rich.text.model.SegmentOps;
import fx.rich.text.model.StyledDocument;
import fx.rich.text.model.StyledSegment;

/**
 * Clipboard actions for {@link TextEditingArea}.
 */
public interface ClipboardActions<PS, SEG, S> extends EditActions<PS, SEG, S> {

  /**
   * Gets codecs to encode/decode style information to/from binary format.
   * Providing codecs enables clipboard actions to retain the style information.
   */
  Optional<Pair<Codec<PS>, Codec<StyledSegment<SEG, S>>>> getStyleCodecs();

  void setStyleCodecs(Codec<PS> paragraphStyleCodec, Codec<StyledSegment<SEG, S>> textStyleCodec);

  SegmentOps<SEG, S> getSegOps();

  /**
   * Transfers the currently selected text to the clipboard,
   * removing the current selection.
   */
  default void cut() {
    copy();
    IndexRange selection = getSelection();
    deleteText(selection.getStart(), selection.getEnd());
  }

  /**
   * Transfers the currently selected text to the clipboard,
   * leaving the current selection.
   */
  default void copy() {
    var selection = getSelection();
    if (selection.getLength() > 0) {
      var content = new ClipboardContent();

      content.putString(getSelectedText());

      getStyleCodecs().ifPresent(codecs -> {
        var codec = ReadOnlyStyledDocument.codec(codecs.getKey(), codecs.getValue(), getSegOps());
        var format = dataFormat(codec.getName());
        var doc = subDocument(selection.getStart(), selection.getEnd());
        var os = new ByteArrayOutputStream();
        var dos = new DataOutputStream(os);
        try {
          codec.encode(dos, doc);
          content.put(format, os.toByteArray());
        } catch (IOException e) {
          System.err.println("Codec error: Exception in encoding '" + codec.getName() + "':");
          e.printStackTrace(); // TODO: replace with
        }
      });

      Clipboard.getSystemClipboard().setContent(content);
    }
  }

  /**
   * Inserts the content from the clipboard into this text-editing area,
   * replacing the current selection. If there is no selection, the content
   * from the clipboard is inserted at the current caret position.
   */
  default void paste() {
    var clipboard = Clipboard.getSystemClipboard();

    if (getStyleCodecs().isPresent()) {
      var codecs = getStyleCodecs().get();
      // Codec<StyledDocument<PS, SEG, S>>
      var codec = ReadOnlyStyledDocument.codec(codecs.getKey(), codecs.getValue(), getSegOps());
      var format = dataFormat(codec.getName());
      if (clipboard.hasContent(format)) {
        var bytes = (byte[]) clipboard.getContent(format);
        var is = new ByteArrayInputStream(bytes);
        var dis = new DataInputStream(is);
        StyledDocument<PS, SEG, S> doc = null;
        try {
          doc = codec.decode(dis);
        } catch (IOException e) {
          System.err.println("Codec error: Failed to decode '" + codec.getName() + "':");
          e.printStackTrace(); // TODO: replace with System.Logger
        }
        if (doc != null) {
          replaceSelection(doc);
          return;
        }
      }
    }

    if (clipboard.hasString()) {
      var text = clipboard.getString();
      if (text != null) {
        replaceSelection(text);
      }
    }
  }

  // class ClipboardHelper
  static DataFormat dataFormat(String name) {
    var format = DataFormat.lookupMimeType(name);
    if (format != null) {
      return format;
    } else {
      return new DataFormat(name);
    }
  }

}
