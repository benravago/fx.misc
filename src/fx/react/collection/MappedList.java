package fx.react.collection;

import java.util.List;
import java.util.function.Function;

import javafx.collections.ObservableList;
import fx.react.Subscription;
import fx.util.Lists;

class MappedList<E, F> extends LiveListBase<F> implements UnmodifiableByDefaultLiveList<F> {

  final ObservableList<? extends E> source;
  final Function<? super E, ? extends F> mapper;

  MappedList(ObservableList<? extends E> source, Function<? super E, ? extends F> mapper) {
    this.source = source;
    this.mapper = mapper;
  }

  @Override
  public F get(int index) {
    return mapper.apply(source.get(index));
  }

  @Override
  public int size() {
    return source.size();
  }

  @Override
  protected Subscription observeInputs() {
    return LiveList.<E>observeQuasiChanges(source, this::sourceChanged);
  }

  void sourceChanged(QuasiChange<? extends E> change) {
    notifyObservers(mappedChangeView(change, mapper));
  }

  static <E, F> QuasiChange<F> mappedChangeView(QuasiChange<? extends E> change, Function<? super E, ? extends F> mapper) {
    return new QuasiChange<F>() {
      @Override
      public List<? extends QuasiModification<? extends F>> getModifications() {
        var mods = change.getModifications();
        return Lists.<QuasiModification<? extends E>, QuasiModification<F>>mappedView(
          mods, mod -> new QuasiModification<F>() {
            @Override public int getFrom() { return mod.getFrom(); }
            @Override public int getAddedSize() { return mod.getAddedSize(); }
            @Override public List<? extends F> getRemoved() { return Lists.mappedView(mod.getRemoved(), mapper); }
          });
      }};
  }

}
