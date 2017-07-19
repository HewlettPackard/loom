
import SelectorController = require('./SelectorController');
import row_css = require('../util/row_css');

enum SortState {
  ASC,
  DSC
}

class SortController {

  private current_sorted_column: number;
  private current_sort_state: SortState;
  private attributes: Array<string>;
  private selector: SelectorController;

  constructor(selector: SelectorController, attributes: Array<string>) {
    this.selector = selector;
    this.attributes = attributes;
    this.current_sorted_column = -1;
    this.current_sort_state = SortState.ASC;
  }

  /**
   * Call this method if you need to refresh the sort.
   * This is usefull when some attribute elements have changed
   * and the sort is not correct anymore.
   */
  refresh(): void {

    if (this.current_sorted_column < 0) return;

    // Inverse current sort state to prevent
    // refresh of inversing the sort.
    if (this.current_sort_state === SortState.ASC) {
      this.current_sort_state = SortState.DSC
    } else {
      this.current_sort_state = SortState.ASC;
    }

    // Sort on current sorted column.
    this.sort_column(this.current_sorted_column);
  }

  /**
   * Sort the rows based on the given column number attribute.
   */
  sort_column(col: number): void {

    if (col < 0) return;

    // Remove previous css state
    this.clear_old_col();

    if (col === this.current_sorted_column &&
        this.current_sort_state === SortState.ASC) {
      this.current_sort_state = SortState.DSC;
    } else {
      this.current_sort_state = SortState.ASC;
    }
    this.current_sorted_column = col;

    // Set the new css state.
    this.set_sort_css_for_col(col);

    var child_selector = '.mas-table-row';

    // Sort the elements
    this.selector.$root()
      .children(child_selector)
      .sortElements((a, b) => {
      var a_data = $(a).data("view");
      var b_data = $(b).data("view");

      // We assign numbers by default.
      a_data = a_data ? a_data.model.get(this.attributes[col]) : -1;
      b_data = b_data ? b_data.model.get(this.attributes[col]) : -1;

      // If the real value is a string then, set the default to the
      // empty string.
      if (typeof a_data === 'string' && typeof b_data === 'number') {
        b_data = '';
      }

      if (typeof a_data === 'number' && typeof b_data === 'string') {
        a_data = '';
      }

      if (a_data === b_data) return 0;

      if (this.current_sort_state === SortState.ASC) {
        return a_data > b_data ? 1 : -1;
      } else {
        return a_data < b_data ? 1 : -1;
      }
    });

    // Clean last-row css:
    this.selector.$root()
      .children('.mas-last-row')
      .removeClass('mas-last-row');

    // Set the new last-row-css:
    this.selector.$root()
      .children(child_selector)
      .each((index, elem) => {
        $(elem).addClass(row_css(index).join(' '));
      });
  }

  private set_sort_css_for_col(col: number) {

    var newHeaderCol = this.selector.select_col_header(col);

    newHeaderCol.addClass('is-sorting-column');
    if (this.current_sort_state === SortState.ASC) {
      newHeaderCol.addClass('is-sorted-asc');
    } else {
      newHeaderCol.addClass('is-sorted-dsc');
    }
  }

  private clear_old_col(): void {
    if (this.current_sorted_column >= 0) {
      var oldHeadercol = this.selector.select_col_header(this.current_sorted_column);
      oldHeadercol.removeClass('is-sorting-column');
      if (this.current_sort_state === SortState.ASC) {
        oldHeadercol.removeClass('is-sorted-asc');
      } else {
        oldHeadercol.removeClass('is-sorted-dsc');
      }
    }
  }
}


export = SortController;
