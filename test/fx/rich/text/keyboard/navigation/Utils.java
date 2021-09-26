package fx.rich.text.keyboard.navigation;

import java.util.Arrays;

import fx.rich.text.GenericStyledArea;

class Utils {

  static int entityStart(int entityIndex, String[] array) {
    return (entityIndex == 0) ? 0
      : Arrays.stream(array)
              .map(String::length)
              .limit(entityIndex)
              .reduce(0, (a, b) -> a + b) + entityIndex; // for delimiter characters
  }

  static int entityEnd(int entityIndex, String[] array, GenericStyledArea<?, ?, ?> area) {
    return (entityIndex == array.length - 1)
      ? area.getLength() : entityStart(entityIndex + 1, array) - 1;
  }

}
