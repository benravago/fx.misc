package fx.undo.manager;

class RevisionedChange<C> {

  final C change;
  final long revision;

  RevisionedChange(C change, long revision) {
    this.change = change;
    this.revision = revision;
  }

  C getChange() {
    return change;
  }

  long getRevision() {
    return revision;
  }

}
