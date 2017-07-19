import AbstractFibreBuilder = require('plugins/common/layout/AbstractFibreBuilder');
import FibreConfig = require('./FibreConfig');
import treemap = require('./treemap');
import mod_unit = require('./unit');

import Unit = mod_unit.Unit;
import TreeNode = treemap.TreeNode;

class FibreBuilder extends AbstractFibreBuilder {

  unit_str: string;
  ignore_label: boolean;

  constructor(unit: Unit, fibreConfig: FibreConfig) {
    super();
    this.unit_str = mod_unit.to_css(unit) + ';';
    this.ignore_label = fibreConfig ? fibreConfig.ignore_label: false;
  }

  buildFibre(el: TreeNode): HTMLElement {
    var div = this.buildTag({
      tag: 'div',
      classes: 'mas-treemap-element',
      style:  'width: ' + el.box.width + this.unit_str +
              'height: ' + el.box.height + this.unit_str +
              'left: ' + el.box.x + this.unit_str +
              'bottom: ' + el.box.y + this.unit_str,
    });
    if (!this.ignore_label) {
      div.appendChild(this.buildTag({
        tag: 'div',
        classes: [
          'mas-fiberOverview--label',
          'mas-treemap-element-padding'
        ]
      }));
    }
    div.appendChild(this.buildTag({
      tag: 'div',
      classes: [
        'mas-treemap-fiber--block',
        'mas-treemap-inside-element',
        'mas-treemap-element-padding',
        this.ignore_label ? 'mas-ignore-label': ''
      ],
    }));
    return div;
  }

}

export = FibreBuilder;
