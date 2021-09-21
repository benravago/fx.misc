package fx.react;

class CloseableOnceGuard implements Guard {

  Guard delegate;

  CloseableOnceGuard(Guard delegate) {
    this.delegate = delegate;
  }

  @Override
  public void close() {
    if (delegate != null) {
      delegate.close();
      delegate = null;
    }
  }

}