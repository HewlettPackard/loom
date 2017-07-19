import Backbone = require('backbone');
import _ = require('lodash');
import AbstractThreadElementsView = require('plugins/common/views/AbstractThreadElementsView');
import ViewFibreOptions = require('plugins/interfaces/ViewFibreOptions');

import FibreBuilder = require('../layout/FibreBuilder');
import FibreConfig = require('../layout/FibreConfig');
import Builder = require('../layout/Builder');
import FibreView = require('./FibreView');
import unit = require('../layout/unit');
import treemap = require('../layout/treemap');
import rect = require('../layout/Rect');

import ViewOptions = Backbone.ViewOptions;
import Rect = rect.Rect;
import Unit = unit.Unit;
import Treemap = treemap.Treemap;
import TreeNode = treemap.TreeNode;

class DepthLevelView extends AbstractThreadElementsView {

  treemap: Treemap;
  model: Thread;
  rect: Rect;
  unit: Unit;
  views: Array<FibreView>;
  drillDownViews: Array<DepthLevelView>;
  fibreConfig: FibreConfig;

  constructor(args: {model: Thread; rect: Rect; unit: Unit; fibreConfig?: FibreConfig}) {
    super(args);
    this.treemap = new Treemap();
    this.drillDownViews = [];
    this.rect = args.rect;
    this.fibreConfig = args.fibreConfig;
    this.unit = args.unit;
  }

  protected renderElements(fibers: Array<Element2>): void {
    // Convert to an Array of TreeNode.
    var elements: Array<TreeNode> = _.map(fibers, (element: any) => {
      var nb = element.get('numberOfItems');
      return {
        model: element,
        area: nb ? nb : 1,
        box: new Rect(0),
        children: function (): Array<TreeNode> { return []; }
      };
    });
    elements.sort((a, b) => b.area - a.area);
    this.treemap.squarify(elements, this.rect);
    var fibreBuilder = new FibreBuilder(this.unit, this.fibreConfig);
    var builder = new Builder(elements, this.rect, fibreBuilder);

    // Clear step.
    this.clear();

    // Build step:
    var res = builder.build('div', this.className, (options: ViewOptions<any>) => {
      return FibreView.factory(<ViewFibreOptions<FibreBuilder>>_.assign(options, { thread: this.model, builder: fibreBuilder }));
    });
    this.views = res.views;
    this.$el.append(res.el);
  }

  protected onResetElements(collection: Backbone.Collection<Element2>, obj: any): void {
    this.render();
  }

  drillDownOnEveryChildren(dispatcher: (thread: Thread) => void): void {

    this.clearDrillDownViews();

    var thread = this.model;
    var fibersViews = this.views;
    var views = this.drillDownViews;
    var index = 0;

    function drilldown_on_child() {
      if (index < fibersViews.length) {
        var nestedThread: Thread = thread.createNestedThread(fibersViews[index].model);
        nestedThread.pushOperation({
          operator: 'GROUP_BY',
          parameters: {
            property: 'tenant'
          }
        });
        var container = fibersViews[index].$('.mas-treemap-fiber--block');
        var w = container.innerWidth();
        var h = container.innerHeight();
        var rect = new Rect(0, 0, w, h);
        var view = new DepthLevelView({
          model: nestedThread,
          rect: rect,
          unit: Unit.Px,
          fibreConfig: {
            ignore_label: true
          }
        });
        view.render();

        container.append(view.el);

        views.push(view);
        dispatcher(nestedThread);

        index += 1;
        setImmediate(drilldown_on_child);
      }
    }

    setImmediate(drilldown_on_child);
  }

  clearDrillDownViews(): void {
    var view;
    while (view = this.drillDownViews.pop()) {
      view.remove();
    }
  }

  clear(): void {
    var view;
    this.clearDrillDownViews();

    if (this.views) {
      while (view = this.views.pop()) {
        view.remove();
      }
    }
    this.$el.empty();
  }

}

export = DepthLevelView;
