
import Aggregation = require('weft/models/Aggregation');
import treemap = require('./treemap');
import rect = require('./Rect');
import ViewFactory = require('plugins/common/layout/ViewFactory');
import AbstractBuilder = require('plugins/common/layout/AbstractBuilder');
import FibreBuilder = require('./FibreBuilder');
import _ = require('lodash');
import mod_unit = require('./unit');
import Backbone = require('backbone');
import Model = Backbone.Model;

import Unit = mod_unit.Unit;
import TreeNode = treemap.TreeNode;
import Rect = rect.Rect;

class Builder extends AbstractBuilder {

  elements: Array<TreeNode>;
  box: Rect;
  fibrebuilder: FibreBuilder;

  constructor(elements: Array<TreeNode>, box: Rect, fibreBuilder: FibreBuilder) {
    super();
    this.elements = elements;
    this.box = box;
    this.fibrebuilder = fibreBuilder;
  }

  build<V>(tag: string, css_class: string, viewfactory: ViewFactory<V, Model>)
    : { el: HTMLElement; views: Array<V>; }
  {
    var views: Array<V> = [];
    var res = this.buildTag({
      tag: tag,
      classes: [css_class],
      style:  ' width: ' + this.box.width + this.fibrebuilder.unit_str +
              ' height: ' + this.box.height + this.fibrebuilder.unit_str
    });
    _.forEach(this.elements, (node: TreeNode) => {
      var el = this.fibrebuilder.buildFibre(node);
      views.push(viewfactory({
        el: el,
        model: node.model
      }));
      res.appendChild(el);
    });

    return {
      el: res,
      views: views
    };
  }
}


export = Builder;
