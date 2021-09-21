package fx.layout.flow;

/**
 * Uses the Visitor Pattern, so {@link Navigator} does not need to check the type of the {@link TargetPosition}
 * before using it to determine how to fill the viewport.
 */
interface TargetPositionVisitor {

  void visit(StartOffStart targetPosition);
  void visit(EndOffEnd targetPosition);
  void visit(MinDistanceTo targetPosition);

}