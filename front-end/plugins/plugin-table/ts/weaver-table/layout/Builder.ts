import ATableBuilder = require("./ATableBuilder");
import Element2 = require('weft/models/Element');
import ViewFactory = require('plugins/common/layout/ViewFactory');
import builder_interfaces = require('plugins/common/layout/builder_interfaces');
import Backbone = require('backbone');

import FibreBuilder = require('./FibreBuilder');
import TableHeaderListener = require('../controller/TableHeaderListener');
import TableHeaderSortListener = require('../controller/TableHeaderSortListener');
import SelectorController = require('../controller/SelectorController');
import table_header = require('./table_header');
import row_css = require('../util/row_css');

import Model = Backbone.Model;
import TableHeader = table_header.TableHeader;
import ColCssClasses = builder_interfaces.ColCSSClasses;

interface HeaderCell {
  class_col: string;
  attribute_pos: number;
}

class HeaderBuilder extends ATableBuilder {

  private selector: SelectorController;
  headerCells: {[index: number]: HeaderCell };

  constructor(selector: SelectorController) {
    super();
    this.selector = selector;
    this.headerCells = {};
  }

  protected buildNested(i: number, value): HTMLElement {
    var nested = super.buildNested(i, value);
    var node = this.buildTag({
      tag: 'div',
      classes: 'mas-sorting-indicator',
    });

    nested.insertBefore(node, nested.firstChild);

    return nested;
  }

  protected col_css(i: number) {
    return this.selector.col_css(i);
  }

  protected firstCellClass(): Array<string> {
    return [];
  }

  protected buildCell(i: number, classes: Array<string>, value): HTMLElement {
    var cell = this.buildTag({
      tag: 'th',
      classes: classes,
    });
    var nested = this.buildNested(i, value);
    cell.appendChild(nested);
    var tooltip = this.buildTag({
      tag: 'div',
      classes: ['mas-fiberOverview--tooltip', 'mas-tooltip', 'mas-tooltip-bottomRightArrow'],
    });
    this.headerCells[i] = {
      class_col: this.col_css(i),
      attribute_pos: i,
    }
    tooltip.appendChild(this.buildText(value));
    cell.appendChild(tooltip);
    return cell;
  }
}

class Builder extends ATableBuilder {

  private selector: SelectorController;
  private headerCells: {[index: number]: HeaderCell };

  constructor() {
    super();
    this.headerCells = {};
  }

  build<V>(
    header: TableHeader<string>,
    columnsCss: Array<string>,
    elements: Array<Element2>,
    viewfactory: ViewFactory<V, Model>,
    thread: Thread,
    builder:  FibreBuilder)
    : {
        el: HTMLElement;
        views: Array<V>;
        collapseController: TableHeaderListener;
        sortController: TableHeaderSortListener;
      };

  build<V>(
    header: TableHeader<Array<string>>,
    columnsCss: Array<string>,
    elements: Array<Element2>,
    viewfactory: ViewFactory<V, Model>,
    thread: Thread,
    builder:  FibreBuilder)
    : {
        el: HTMLElement;
        views: Array<V>;
        collapseController: TableHeaderListener;
        sortController: TableHeaderSortListener;
      };

  build<V>(
    header: TableHeader<any>,
    columnsCss: Array<string>,
    elements: Array<Element2>,
    viewfactory: ViewFactory<V, Model>,
    thread: Thread,
    builder:  FibreBuilder)
    : {
        el: HTMLElement;
        views: Array<V>;
        collapseController: TableHeaderListener;
        sortController: TableHeaderSortListener;
      }
  {
    var views: Array<V> = [];
    var table = this.buildTag({
      tag: 'table',
      classes: ['mas-table']
    });

    // Set id to match builder id, to have equivalent col_css classes.
    // This really feels hacky right now. Should be clean later.
    this.selector = builder.selector;

    var i = 0;
    var cols = columnsCss.map((css) => { return { classes: [css], col: i++ }; })

    // Header containing attributes names and
    // handling sorting state.
    var sortController = this.createAttributeHeader(header, cols);

    // Second line of header containing collapse actions
    var collapseColumnController = this.createCollapseHeader(cols);

    var tableHeader = this.buildTag({ tag: 'thead'});
    var tableBody = this.buildTag({ tag: 'tbody'});

    table.appendChild(tableHeader);
    table.appendChild(tableBody);

    tableHeader.appendChild(sortController.el);
    tableHeader.appendChild(collapseColumnController.el);

    var row = 0;
    _.forEach(elements, (element: Element2) => {
      var rcss = row_css(row);
      row++;
      var el = builder.buildFibre({ model: element, attribute_class: columnsCss, row_css: rcss });

      views.push(viewfactory({
        el: el,
        model: element,
        builder: builder,
        thread: thread,
      }));
      tableBody.appendChild(el);
    });

    return {
      el: table,
      views: views,
      collapseController: collapseColumnController,
      sortController: sortController,
    };
  }

  /**
   * This method create the first line of the table. Each cell will contains
   * an attribute name.
   * The TableHeaderSortListener will handle click on each of those cells.
   */
  private createAttributeHeader(header: TableHeader<any>, cols: Array<ColCssClasses<Array<string>>>)
    : TableHeaderSortListener
  {
    var headerBuilder = new HeaderBuilder(this.selector);

    var sortColumnController =  new TableHeaderSortListener(headerBuilder.build_line(header.values, {
      row: ['mas-table-header', 'mas-table-row'],
      separator: 'mas-table-separator',
      generic_cell: 'mas-table-cell',
      cols: cols
    }));

    _.forEach(headerBuilder.headerCells, (c: HeaderCell) => {
      sortColumnController.listenToColumn(c.class_col, c.attribute_pos);
    });

    return sortColumnController;
  }


  /**
   * This method create the second line of the table serving the collapse action.
   * The builder use by this method is currently `this`.
   * TODO: Move it into a separate builder.
   */
  private createCollapseHeader(cols: Array<ColCssClasses<Array<string>>>)
    : TableHeaderListener
  {

    var collapse_values = _.times(cols.length, () => builder_interfaces.as_line_value(''));

    var collapseColumnController = new TableHeaderListener(this.build_line(collapse_values, {
      row: ['mas-table-row', 'mas-table-collapse-actions'],
      separator: 'mas-table-separator',
      generic_cell: 'mas-table-cell',
      cols: cols
    }));

    _.forEach(this.headerCells, (c: HeaderCell) => {
      collapseColumnController.listenToColumn(c.class_col, c.attribute_pos);
    });

    return collapseColumnController;
  }

  protected col_css(i: number) {
    return this.selector.col_css(i);
  }

  protected firstCellClass(): Array<string> {
    return [];
  }

  protected buildNested(i:number, value): HTMLElement {
    var nested = super.buildNested(i, value);
    $(nested).addClass('mas-collapse-action');
    return nested;
  }

  protected buildCell(i: number, classes: Array<string>, value): HTMLElement {
    var el = super.buildCell(i, classes, value);
    this.headerCells[i] = {
      class_col: this.col_css(i),
      attribute_pos: i,
    }
    return el;
  }

}

export = Builder;
