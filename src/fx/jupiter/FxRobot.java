package fx.jupiter;

import java.util.ArrayDeque;
import java.util.function.Consumer;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import javafx.scene.robot.Robot;

public class FxRobot {

  final Robot robot;
  Parent root;

  FxRobot() {
    robot = new Robot();
  }

  public FxRobot stage(Parent main, Consumer<Stage> start) {
    assert main != null && start != null;
    root = main;
    FxEnv.run(() -> {
      var stage = new Stage();
      var scene = new Scene(root);
      stage.setScene(scene);
      start.accept(stage);
      stage.show();
    });
    return this;
  }

  public FxRobot interact(Runnable action) {
    FxEnv.run(action);
    return this;
  }

  public FxRobot sleep(long millis) {
    try { Thread.sleep(millis); }
    catch (InterruptedException ie) { note("sleep",ie); }
    return this;
  }

  public FxRobot then(Runnable action) {
    try { action.run(); }
    catch (Throwable t) { note("then",t); }
    return this;
  }

  public FxRobot root() {
    return root(Pos.TOP_LEFT);
  }

  public FxRobot root(Pos position) {
    return from(root,position);
  }

  public FxRobot from(Node node) {
    return from(node,Pos.TOP_LEFT);
  }

  public FxRobot from(Node node, Pos position) {
    return moveTo(locate(node,position));
  }

  public FxRobot moveTo(Point2D location, double x, double y) {
    return moveTo(location.add(x,y));
  }

  public FxRobot moveTo(Point2D location) {
    interact(() -> robot.mouseMove(location) );
    return this;
  }

  public FxRobot moveTo(double x, double y) {
    interact(() -> robot.mouseMove(x,y) );
    return this;
  }

  public FxRobot moveTo(Bounds b) {
    var p = new Point2D(b.getCenterX(),b.getCenterY());
    return moveTo(p);
  }

  public FxRobot click(MouseButton button) {
    interact(() -> robot.mouseClick(button) );
    return this;
  }

  public FxRobot click(MouseButton button, Runnable action) {
    interact(() -> {
      try { robot.mousePress(button); action.run(); }
      finally { robot.mouseRelease(button); }
    });
    return this;
  }

  public FxRobot click(MouseButton button, int repeat) {
    interact(() -> { for (var i = 0; i < repeat; i++) robot.mouseClick(button);});
    return this;
  }

  public FxRobot press(MouseButton button) {
    interact(() -> robot.mousePress(button) );
    return this;
  }

  public FxRobot release(MouseButton button) {
    interact(() -> robot.mouseRelease(button) );
    return this;
  }

  public FxRobot spin(int wheelAmt) {
    interact(() -> robot.mouseWheel(wheelAmt) );
    return this;
  }

  public FxRobot type(KeyCode key) {
    interact(() -> robot.keyType(key) );
    return this;
  }

  public FxRobot type(KeyCode key, Runnable action) {
    interact(() -> {
      try { robot.keyPress(key); action.run(); }
      finally { robot.keyRelease(key); }
    });
    return this;
  }

  public FxRobot chord(KeyCode... keys) {
    return chord(keys, ()->{});
  }

  public FxRobot chord(KeyCode[] keys, Runnable action) {
    interact(() -> keySequence(keys,action) );
    return this;
  }

  public FxRobot press(KeyCode key) {
    interact(() -> robot.keyPress(key) );
    return this;
  }

  public FxRobot release(KeyCode key) {
    interact(() -> robot.keyRelease(key) );
    return this;
  }

  public FxRobot hold(KeyCode key) {
    return hold(key,25);
  }

  public FxRobot hold(KeyCode key, long millis) {
    interact(() -> { robot.keyType(key); sleep(millis); } );
    return this;
  }

  public FxRobot write(String s) {
    interact(() -> {
      for (var c:s.toCharArray()) robot.keyType(key(c));
    });
    return this;
  }

  static void note(String m, Throwable t) {
    System.out.format("FxRobot: %s; %s\n", m, t);
  }

  void keySequence(KeyCode[] keys, Runnable action) {
    var i = 0;
    try {
      while (i < keys.length) {
        robot.keyPress(keys[i]);
        i++;
      }
      action.run();
    }
    finally {
      while (i-- > 0) {
        robot.keyRelease(keys[i]);
      }
    }
  }

  static KeyCode[] keys(KeyCode... keys) {
    return keys; // just a helper
  }

  public static KeyCode key(char c) {
   return switch (c) {
    default -> KeyCode.UNDEFINED;

    case 'a' -> KeyCode.A;
    case 'b' -> KeyCode.B;
    case 'c' -> KeyCode.C;
    case 'd' -> KeyCode.D;
    case 'e' -> KeyCode.E;
    case 'f' -> KeyCode.F;
    case 'g' -> KeyCode.G;
    case 'h' -> KeyCode.H;
    case 'i' -> KeyCode.I;
    case 'j' -> KeyCode.J;
    case 'k' -> KeyCode.K;
    case 'l' -> KeyCode.L;
    case 'm' -> KeyCode.M;
    case 'n' -> KeyCode.N;
    case 'o' -> KeyCode.O;
    case 'p' -> KeyCode.P;
    case 'q' -> KeyCode.Q;
    case 'r' -> KeyCode.R;
    case 's' -> KeyCode.S;
    case 't' -> KeyCode.T;
    case 'u' -> KeyCode.U;
    case 'v' -> KeyCode.V;
    case 'w' -> KeyCode.W;
    case 'x' -> KeyCode.X;
    case 'y' -> KeyCode.Y;
    case 'z' -> KeyCode.Z;

    case '0' -> KeyCode.DIGIT0;
    case '1' -> KeyCode.DIGIT1;
    case '2' -> KeyCode.DIGIT2;
    case '3' -> KeyCode.DIGIT3;
    case '4' -> KeyCode.DIGIT4;
    case '5' -> KeyCode.DIGIT5;
    case '6' -> KeyCode.DIGIT6;
    case '7' -> KeyCode.DIGIT7;
    case '8' -> KeyCode.DIGIT8;
    case '9' -> KeyCode.DIGIT9;

    case '\t' -> KeyCode.TAB;
    case '\b' -> KeyCode.BACK_SPACE;
    case '\n' -> KeyCode.ENTER;
    case '\'' -> KeyCode.QUOTE;
    case '\\' -> KeyCode.BACK_SLASH;

    case ' ' -> KeyCode.SPACE;
    case '!' -> KeyCode.EXCLAMATION_MARK;
    case '"' -> KeyCode.QUOTEDBL;
    case '#' -> KeyCode.NUMBER_SIGN;
    case '$' -> KeyCode.DOLLAR;
    case '(' -> KeyCode.LEFT_PARENTHESIS;
    case ')' -> KeyCode.RIGHT_PARENTHESIS;
    case '+' -> KeyCode.PLUS;
    case ',' -> KeyCode.COMMA;
    case '-' -> KeyCode.MINUS;
    case '.' -> KeyCode.PERIOD;
    case '/' -> KeyCode.SLASH;
    case ':' -> KeyCode.COLON;
    case ';' -> KeyCode.SEMICOLON;
    case '=' -> KeyCode.EQUALS;
    case '@' -> KeyCode.AT;
    case '[' -> KeyCode.OPEN_BRACKET;
    case ']' -> KeyCode.CLOSE_BRACKET;
    case '^' -> KeyCode.CIRCUMFLEX;
    case '_' -> KeyCode.UNDERSCORE;
    case '`' -> KeyCode.BACK_QUOTE;
   };
  }

  public static Point2D locate(Node node) {
    if (node == null) return Point2D.ZERO;
    var ns = node.getScene();
    var window = new Point2D(ns.getWindow().getX(), ns.getWindow().getY());
    var scene = new Point2D(ns.getX(), ns.getY());
    var local = node.localToScene(0.0, 0.0);
    var x = Math.round(window.getX() + scene.getX() + local.getX());
    var y = Math.round(window.getY() + scene.getY() + local.getY());
    return new Point2D(x,y);
  }

  public static Point2D locate(Node node, Pos position) {
    var bounds = node.localToScreen(node.getBoundsInLocal());
    return point(bounds,position);
  }

  public static Point2D point(Bounds b, Pos p) {
    var x = b.getMinX() + (b.getWidth() * hPos(p));
    var y = b.getMinY() + (b.getHeight() * vPos(p));
    return new Point2D(x,y);
  }

  public static double hPos(Pos p) {
    return switch (p.getHpos()) {
      case LEFT -> 0.0;
      case CENTER -> 0.5;
      case RIGHT -> 1.0;
      default -> throw new IllegalArgumentException("HPos: "+p);
    };
  }

  public static double vPos(Pos p) {
    return switch (p.getVpos()) {
      case TOP -> 0.0;
      case CENTER -> 0.5;
      case BOTTOM -> 1.0;
      default -> throw new IllegalArgumentException("VPos: "+p);
    };
  }

}
/*

==  void  keyPress(KeyCode keyCode)
==  void  keyRelease(KeyCode keyCode)
==  void  keyType(KeyCode keyCode)

==  void  mouseMove(double x, double y)
==  void  mouseMove(Point2D location)

==  void  mouseClick(MouseButton... buttons)
==  void  mousePress(MouseButton... buttons)
==  void  mouseRelease(MouseButton... buttons)
==  void  mouseWheel(int wheelAmt)

    Point2D getMousePosition()
    double  getMouseX()
    double  getMouseY()

    Color   getPixelColor(double x, double y)
    Color   getPixelColor(Point2D location)

    WritableImage   getScreenCapture(WritableImage image, double x, double y, double width, double height)
    WritableImage   getScreenCapture(WritableImage image, double x, double y, double width, double height, boolean scaleToFit)
    WritableImage   getScreenCapture(WritableImage image, Rectangle2D region)
    WritableImage   getScreenCapture(WritableImage image, Rectangle2D region, boolean scaleToFit)

*/
