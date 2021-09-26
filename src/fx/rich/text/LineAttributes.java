package fx.rich.text;

import java.util.Arrays;
import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Paint;

class LineAttributes {

  final double width;
  final Paint color;
  final Double[] dashArray;

  final boolean isNullValue() {
    return color == null || width == -1;
  }

  /**
   * Java Quirk! Using {@code t.get[border/underline]DashArray()} throws a ClassCastException
   * "Double cannot be cast to Number". However, using {@code t.getDashArrayProperty().get()}
   * works without issue
   */
  LineAttributes(Paint color, Number width, ObjectProperty<Number[]> dashArrayProp) {
    this.color = color;
    if (color == null || width == null || width.doubleValue() <= 0) {
      // null value
      this.width = -1;
      dashArray = null;
    } else {
      // real value
      this.width = width.doubleValue();

      // get the dash array - JavaFX CSS parser seems to return either a Number[] array
      // or a single value, depending on whether only one or more than one value has been
      // specified in the CSS
      Object dashArrayProperty = dashArrayProp.get();
      if (dashArrayProperty != null) {
        if (dashArrayProperty.getClass().isArray()) {
          var numberArray = (Number[]) dashArrayProperty;
          dashArray = new Double[numberArray.length];
          int idx = 0;
          for (var d : numberArray) {
            dashArray[idx++] = (Double) d;
          }
        } else {
          dashArray = new Double[1];
          dashArray[0] = ((Double) dashArrayProperty).doubleValue();
        }
      } else {
        dashArray = null;
      }
    }
  }

  /**
   * Same as {@link #equals(Object)} but no need to check the object for its class
   */
  public boolean equalsFaster(LineAttributes attr) {
    return Objects.equals(width, attr.width) && Objects.equals(color, attr.color) && Arrays.equals(dashArray, attr.dashArray);
  }
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof LineAttributes attr) ? equalsFaster(attr) : false;
  }
  protected final String getSubString() {
    return String.format("width=%s color=%s dashArray=%s", width, color, Arrays.toString(dashArray));
  }

}