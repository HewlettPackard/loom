// Dependencies
import FloatElementDetailsController = require('plugins/common/utils/FloatElementDetailsController');
import Backbone = require('backbone');
import _ = require('lodash');

import mod_treemap = require('../layout/treemap');
import mod_rect = require('../layout/Rect');
import BaseView = require('weaver/views/BaseView');
import AbstractThreadElementsView = require('plugins/common/views/AbstractThreadElementsView');
import DepthLevelView = require('./DepthLevelView');
import unit = require('../layout/unit');

import Rect = mod_rect.Rect;

import Treemap = mod_treemap.Treemap;
import TreeNode = mod_treemap.TreeNode;
import to_css = unit.to_css;
import Unit = unit.Unit;

class TreeMapView extends BaseView {

  rootLevelView: DepthLevelView;
  elementDetailsView: FloatElementDetailsController;

  constructor(args: {className: string; model: Thread}) {
    super(args);
    this.elementDetailsView = new FloatElementDetailsController({
      el: this.el,
      target: this.el
    });
    this.render();
    // this.listenTo(this.model, 'reset:elements', () => {
    //   this.rootLevelView.drillDownOnEveryChildren((nestedThread) => {
    //     this.dispatchCustomEvent('didDisplayThread', {
    //       thread: nestedThread
    //     });
    //   });
    // });
    this.listenTo(this.model, 'change:query', () => {
      this.elementDetailsView.hideView();
    });
  }

  render(): TreeMapView {
    // Temporary
    this.clear();

    var width = 100;
    var height = 100;
    var u = Unit.Pct;
    this.rootLevelView = new DepthLevelView({
      model: this.model,
      rect: new Rect(0, 0, width, height),
      unit: u,
    });

    this.$el.attr('style',
      'max-height: 415px;' +
      'height: calc( 40vw + 15px);');
    this.rootLevelView.$el.attr('style',
      'max-height: 400px;' +
      'height: calc( 40vw );' +
      'width: calc( 50vw );' +
      'max-width: 500px;');
    this.elementDetailsView.attr('.mas-mapElementDetailsSubView', 'style',
      'height: calc(40vw - 26px);');

    this.rootLevelView.className = 'mas-treemap-view';
    this.rootLevelView.render();

    this.$el.append(this.rootLevelView.el);
    return this;
  }

  clear(): void {
    if (this.rootLevelView) {
      this.rootLevelView.clear();
    }
    this.$el.empty();
  }

}

export = TreeMapView;
