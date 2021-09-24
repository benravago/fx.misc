package fx.layout.flow;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import fx.Counter;

@Disabled
class BigCellCreationAndLayoutEfficiencyTest { // extends FlowlessTestBase {

  @BeforeAll
  static void startUp() {
    fx.jupiter.FxEnv.startup(); // initializes JavaFX toolkit
  }

  @BeforeEach
  void setup() {
    Platform.runLater(() -> start(new Stage()));
  }

  ObservableList<String> items;
  Counter cellCreations = new Counter();
  Viewport<String, ?> flow;

  void start(Stage stage) {
    // set up items
    items = FXCollections.observableArrayList();
    items.addAll("red", "green", "blue", "purple");

    // set up virtual flow
    flow = Viewport.createVertical(items, color -> {
      cellCreations.inc();
      var reg = new Region();
      reg.setStyle("-fx-background-color: " + color);
      if (color.equals("purple"))
        reg.setPrefHeight(500.0);
      else
        reg.setPrefHeight(100.0);
      return Cell.wrapNode(reg);
    });

    var stackPane = new StackPane(flow);
    stage.setScene(new Scene(stackPane, 200, 400));
    stage.show();
  }

  @Test // Relates to issue #70
  void having_a_very_tall_item_in_viewport_only_creates_and_lays_out_cell_once() {
    // if this fails then it's probably because the very big purple cell is being created multiple times
    assertEquals(4, cellCreations.get());
  }

}