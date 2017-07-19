
import ITagOptions = require("plugins/common/layout/ITagOptions");
import builder_interfaces = require("plugins/common/layout/builder_interfaces");
import AbstractBuilder = require("plugins/common/layout/AbstractBuilder");
import _ = require("lodash");

import Classes = builder_interfaces.Classes;
import LineValue = builder_interfaces.LineValue;

class ATableBuilder extends AbstractBuilder {

  protected col_css(i: number) {
    return '';
  }

  build_line<V extends LineValue>(values: Array<V>, classes: Classes<string>): HTMLElement;
  build_line<V extends LineValue>(values: Array<V>, classes: Classes<Array<string>>): HTMLElement;
  build_line<V extends LineValue>(values: Array<V>, classes: Classes<any>): HTMLElement {

    if (classes.cols) {
      classes.cols.sort((a, b) => {
        if (a.col < b.col) { return -1; }
        if (a.col > b.col) { return 1; }
        return 0;
      });
    } else {
      classes.cols = [];
    }

    var row = this.buildRow(classes.row);

    row.appendChild(this.buildTag({
      tag: 'td',
      classes: this.firstCellClass()
    }));

    for (var j = 0, i = 0; i < values.length; ++i) {
      var val = [classes.generic_cell, this.col_css(i)];
      // Assume the coli array is sorted
      if (j < classes.cols.length && classes.cols[j].col === i) {
        if (typeof classes.cols[j].classes === 'string') { val.push(classes.cols[j].classes); }
        if (typeof classes.cols[j].classes === 'object') { val = val.concat(classes.cols[j].classes); }
        j++;
      }

      var cell = this.buildCell(i, val, values[i].as_str());
      row.appendChild(cell);

      // Separator
      if (i !== values.length - 1) {
        var separator = this.buildTag({
          tag: cell.tagName,
          classes: classes.separator,
        });
        row.appendChild(separator);
      }
    }

    return row;
  }

  protected firstCellClass(): Array<string> {
    return ['mas-fiberState'];
  }

  protected buildRow(classes: Array<string>): HTMLElement {
    return this.buildTag({
      tag: 'tr',
      classes: classes,
    });
  }

  protected buildCell(i: number, classes: Array<string>, value: number): HTMLElement;
  protected buildCell(i: number, classes: Array<string>, value: string): HTMLElement;
  protected buildCell(i: number, classes: Array<string>, value): HTMLElement {
    var cell = this.buildTag({
      tag: 'td',
      classes: classes,
    });
    var nested = this.buildNested(i, value);
    cell.appendChild(nested);
    return cell;
  }

  protected buildNested(i: number, value: number): HTMLElement;
  protected buildNested(i: number, value: string): HTMLElement;
  protected buildNested(i: number, value) : HTMLElement {
    var nested = this.buildTag({ tag: 'div', classes: 'nested' });
    nested.appendChild(this.buildText(value));
    return nested;
  }

}

export = ATableBuilder;
