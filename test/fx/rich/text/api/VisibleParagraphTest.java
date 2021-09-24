package fx.rich.text.api;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import fx.text.junit.TestCase;

class VisibleParagraphTest extends TestCase {

  @Test
  void get_first_visible_paragraph_index_with_non_blank_lines() {
    var lines = new String[] { "abc", "def", "ghi" };
    r.interact(() -> {
      area.setWrapText(true);
      stage().setWidth(120);
      area.replaceText(String.join("\n", lines));
    });
    assertEquals(0, area.firstVisibleParToAllParIndex());
    assertEquals(2, area.lastVisibleParToAllParIndex());
    assertEquals(1, area.visibleParToAllParIndex(1));
  }

  @Test
  void get_last_visible_paragraph_index_with_non_blank_lines() {
    var lines = new String[] { "abc", "def", "ghi" };
    r.interact(() -> {
      area.setWrapText(true);
      stage().setWidth(120);
      area.replaceText(String.join("\n", lines));
    });
    assertEquals(2, area.lastVisibleParToAllParIndex());
  }

  @Test
  void get_specific_visible_paragraph_index_with_non_blank_lines() {
    var lines = new String[] { "abc", "def", "ghi" };
    r.interact(() -> {
      area.setWrapText(true);
      stage().setWidth(120);
      area.replaceText(String.join("\n", lines));
    });
    assertEquals(2, area.visibleParToAllParIndex(2));
  }

  @Test
  void get_first_visible_paragraph_index_with_all_blank_lines() {
    var lines = new String[] { "", "", "" };
    r.interact(() -> {
      area.setWrapText(true);
      stage().setWidth(120);
      area.replaceText(String.join("\n", lines));
    });
    assertEquals(0, area.firstVisibleParToAllParIndex());
  }

  @Test
  void get_last_visible_paragraph_index_with_all_blank_lines() {
    var lines = new String[] { "", "", "" };
    r.interact(() -> {
      area.setWrapText(true);
      stage().setWidth(120);
      area.replaceText(String.join("\n", lines));
    });
    assertEquals(2, area.lastVisibleParToAllParIndex());
  }

  @Test
  void get_specific_visible_paragraph_index_with_all_blank_lines() {
    var lines = new String[] { "", "", "" };
    r.interact(() -> {
      area.setWrapText(true);
      stage().setWidth(120);
      area.replaceText(String.join("\n", lines));
    });
    assertEquals(2, area.visibleParToAllParIndex(2));
  }

  @Test
  void get_first_visible_paragraph_index_with_some_blank_lines() {
    var lines = new String[] { "abc", "", "", "", "def", "" };
    r.interact(() -> {
      area.setWrapText(true);
      stage().setWidth(120);
      area.replaceText(String.join("\n", lines));
    });
    assertEquals(0, area.firstVisibleParToAllParIndex());
  }

  @Test
  void get_last_visible_paragraph_index_with_some_blank_lines() {
    var lines = new String[] { "abc", "", "", "", "def", "" };
    r.interact(() -> {
      area.setWrapText(true);
      stage().setWidth(120);
      area.replaceText(String.join("\n", lines));
    });
    assertEquals(5, area.lastVisibleParToAllParIndex());
  }

  @Test
  void get_specific_visible_paragraph_index_with_some_blank_lines() {
    var lines = new String[] { "abc", "", "", "", "def", "" };
    r.interact(() -> {
      area.setWrapText(true);
      stage().setWidth(120);
      area.replaceText(String.join("\n", lines));
    });
    assertEquals(3, area.visibleParToAllParIndex(3));
  }

}
