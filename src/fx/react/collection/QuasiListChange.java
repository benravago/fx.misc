package fx.react.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class QuasiListChange<E> extends ArrayList<QuasiModification<? extends E>> implements QuasiChange<E> {

  QuasiListChange() {
    super();
  }

  QuasiListChange(int initialCapacity) {
    super(initialCapacity);
  }

  QuasiListChange(QuasiChange<E> change) {
    super(change.getModifications());
  }

  @Override
  public List<QuasiModification<? extends E>> getModifications() {
    return Collections.unmodifiableList(this);
  }

  private static final long serialVersionUID = 1L;
}