package fx.react.collection;

import java.util.ArrayList;
import java.util.List;

public interface ModifiedList<E> extends ListModificationLike<E> {

  List<? extends E> getAddedSubList();

  ModifiedList<E> trim();

  default MaterializedModification<E> materialize() {
    return MaterializedModification.create(getFrom(), getRemoved(), new ArrayList<>(getAddedSubList()));
  }

}
