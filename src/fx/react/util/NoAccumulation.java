package fx.react.util;

interface NoAccumulation<T> extends IllegalAccumulation<T, T>, HomotypicAccumulation<T> {
  //
}