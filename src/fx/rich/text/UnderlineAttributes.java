package fx.rich.text;

import java.util.Objects;

import javafx.scene.shape.StrokeLineCap;

class UnderlineAttributes extends LineAttributes {

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