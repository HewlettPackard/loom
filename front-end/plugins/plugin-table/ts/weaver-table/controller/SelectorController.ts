import $ = require('jquery');
import _ = require('lodash');

class SelectorController {

  private id: string;
  private context: (selector: string) => JQuery;

  constructor(context: (selector: string) => JQuery) {
    this.id = _.uniqueId();
    this.context = context ? context: $;
  }

  col_css(i: number): string {
    return 'table-' + this.id + '-col-' + i;
  }

  select_col(i: number): JQuery {
    return this.context('.' + this.col_css(i));
  }

  select_col_header(i: number): JQuery {
    return this.context('.mas-table-header .' + this.col_css(i));
  }

  $root(): JQuery {
    return this.context('.mas-table tbody');
  }
}

export = SelectorController;
