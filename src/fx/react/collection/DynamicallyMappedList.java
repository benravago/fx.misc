package fx.react.collection;

import java.util.function.Function;

import fx.react.Subscription;
import fx.react.value.Val;
import fx.util.Lists;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

class DynamicallyMappedList<E, F> extends LiveListBase<F> implements UnmodifiableByDefaultLiveList<F> {

  final ObservableList<? extends E> source;
  final Val<? extends Function<? super E, ? extends F>> mapper;

  DynamicallyMappedList(ObservableList<? extends E> source, ObservableValue<? extends Function<? super E, ? extends F>> mapper) {
    this.source = source;
    this.mapper = Val.wrap(mapper);
  }

  @Override
  public F get(int index) {
    return mapper.getValue().apply(source.get(index));
  }

  @Override
  public int size() {
    return source.size();
  }

  @Override
  protected Subscription observeInputs() {
    return Subscription.multi(
      LiveList.<E>observeQuasiChanges(source, this::sourceChanged),
      mapper.observeInvalidations(this::mapperInvalidated));
  }

  void sourceChanged(QuasiChange<? extends E> change) {
    notifyObservers(MappedList.mappedChangeView(change, mapper.getValue()));
  }

  void mapperInvalidated(Function<? super E, ? extends F> oldMapper) {
    fireContentReplacement(Lists.mappedView(source, oldMapper));
  }

}