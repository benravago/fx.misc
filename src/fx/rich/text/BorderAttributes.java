package fx.rich.text;

import java.util.Objects;

import javafx.scene.shape.StrokeType;

class BorderAttributes extends LineAttributes {

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