import _ = require('lodash');
import HasModel = require('plugins/interfaces/HasModel');
import rect_module = require('./Rect');

import Rect = rect_module.Rect;
import Direction = rect_module.Direction;
import opposite = rect_module.opposite;

export interface TreeNode extends HasModel {
  area: number;
  box: Rect;
  children(): Array<TreeNode>;
}

export class Treemap {

  total:number;
  cdir: Direction;
  cbox: Rect;
  x: number;
  y: number;
  row_total: number;

  // Assume the array is sorted
  // by descending order.
  squarify(elements: Array<TreeNode>, box: Rect): void
  {
    this.x = box.x;
    this.y = box.y;
    this.cdir = box.shorter_direction();
    this.total = _.reduce(elements, (s, e) => e.area + s, 0);
    this.cbox = new Rect(box);
    var fullbox_area = box.width * box.height;
    var row = [];
    var i = 0;
    this.row_total = 0;
    while (i < elements.length) {
      var child = elements[i];
      // Current row is empty ? Add the element to the row
      if (row.length == 0) {
        row.push(child);
        this.row_total += child.area;
        i++;
      // Check what we can do to speed up the row
      } else {
        if (this.is_ratio_improved(row, child)) {
          row.push(child);
          this.row_total += child.area;
          i++;
        } else {
          this.layout_row(row, fullbox_area);
          row = [];
          this.total -= this.row_total;
          this.row_total = 0;
          this.cdir = this.cbox.shorter_direction();
        }
      }
    }
    if (row.length > 0) {
      this.layout_row(row, fullbox_area);
    }
  }

  private is_ratio_improved(row: Array<TreeNode>, child: TreeNode):boolean {
    var row_total = this.row_total;
    var worst_ratio = _.reduce(row, (s, e) => {
      return Math.max(this.ratio(e, row_total), s);
    }, 0, this);
    row_total += child.area;
    var new_worst_ratio = _.reduce(row, (s, e) => {
      return Math.max(this.ratio(e, row_total), s);
    }, 0, this);
    return worst_ratio > new_worst_ratio;
  }

  private ratio(element: TreeNode, row_total: number):number {
    var a = this.get_direction_size(element, row_total);
    var b = this.get_opposite_dir_size(row_total);
    return Math.max(a / b, b / a);
  }

  private layout_row(row: Array<TreeNode>, fullbox_area: number):void {
    // TODO: increment position and set box x, y values.
    var row_total = this.row_total;
    var b = this.get_opposite_dir_size(row_total);
    var x = this.x;
    var y = this.y;
    var el;

    while (el = row.pop()) {
      var a = this.get_direction_size(el, row_total);
      el.box.set(this.cdir, a);
      el.box.set(opposite(this.cdir), b);
      el.box.x = x;
      el.box.y = y;

      if (this.cdir == Direction.Vertical) {
        y += a;
      } else {
        x += a;
      }
    }

    if (this.cdir == Direction.Vertical) {
      this.x += b;
    } else {
      this.y += b;
    }

    var oldvalue = this.cbox.get(opposite(this.cdir));
    this.cbox.set(opposite(this.cdir), oldvalue - b);
  }

  private get_direction_size(element: TreeNode, row_total: number): number {
    return this.cbox.get(this.cdir) * element.area / row_total;
  }

  private get_opposite_dir_size(row_total: number): number {
    return row_total *
          this.cbox.get(opposite(this.cdir)) / this.total;
  }
}
