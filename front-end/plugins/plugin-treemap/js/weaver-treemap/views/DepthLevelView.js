/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
define(["require", "exports", 'lodash', 'plugins/common/views/AbstractThreadElementsView', '../layout/FibreBuilder', '../layout/Builder', './FibreView', '../layout/unit', '../layout/treemap', '../layout/Rect'], function (require, exports, _, AbstractThreadElementsView, FibreBuilder, Builder, FibreView, unit, treemap, rect) {
    var Rect = rect.Rect;
    var Unit = unit.Unit;
    var Treemap = treemap.Treemap;
    var DepthLevelView = (function (_super) {
        __extends(DepthLevelView, _super);
        function DepthLevelView(args) {
            _super.call(this, args);
            this.treemap = new Treemap();
            this.drillDownViews = [];
            this.rect = args.rect;
            this.fibreConfig = args.fibreConfig;
            this.unit = args.unit;
        }
        DepthLevelView.prototype.renderElements = function (fibers) {
            var _this = this;
            var elements = _.map(fibers, function (element) {
                var nb = element.get('numberOfItems');
                return {
                    model: element,
                    area: nb ? nb : 1,
                    box: new Rect(0),
                    children: function () { return []; }
                };
            });
            elements.sort(function (a, b) { return b.area - a.area; });
            this.treemap.squarify(elements, this.rect);
            var fibreBuilder = new FibreBuilder(this.unit, this.fibreConfig);
            var builder = new Builder(elements, this.rect, fibreBuilder);
            this.clear();
            var res = builder.build('div', this.className, function (options) {
                return FibreView.factory(_.assign(options, { thread: _this.model, builder: fibreBuilder }));
            });
            this.views = res.views;
            this.$el.append(res.el);
        };
        DepthLevelView.prototype.onResetElements = function (collection, obj) {
            this.render();
        };
        DepthLevelView.prototype.drillDownOnEveryChildren = function (dispatcher) {
            this.clearDrillDownViews();
            var thread = this.model;
            var fibersViews = this.views;
            var views = this.drillDownViews;
            var index = 0;
            function drilldown_on_child() {
                if (index < fibersViews.length) {
                    var nestedThread = thread.createNestedThread(fibersViews[index].model);
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
        };
        DepthLevelView.prototype.clearDrillDownViews = function () {
            var view;
            while (view = this.drillDownViews.pop()) {
                view.remove();
            }
        };
        DepthLevelView.prototype.clear = function () {
            var view;
            this.clearDrillDownViews();
            if (this.views) {
                while (view = this.views.pop()) {
                    view.remove();
                }
            }
            this.$el.empty();
        };
        return DepthLevelView;
    })(AbstractThreadElementsView);
    return DepthLevelView;
});
