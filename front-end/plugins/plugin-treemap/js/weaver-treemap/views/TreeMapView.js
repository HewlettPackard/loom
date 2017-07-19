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
define(["require", "exports", 'plugins/common/utils/FloatElementDetailsController', '../layout/Rect', 'weaver/views/BaseView', './DepthLevelView', '../layout/unit'], function (require, exports, FloatElementDetailsController, mod_rect, BaseView, DepthLevelView, unit) {
    var Rect = mod_rect.Rect;
    var Unit = unit.Unit;
    var TreeMapView = (function (_super) {
        __extends(TreeMapView, _super);
        function TreeMapView(args) {
            var _this = this;
            _super.call(this, args);
            this.elementDetailsView = new FloatElementDetailsController({
                el: this.el,
                target: this.el
            });
            this.render();
            this.listenTo(this.model, 'change:query', function () {
                _this.elementDetailsView.hideView();
            });
        }
        TreeMapView.prototype.render = function () {
            this.clear();
            var width = 100;
            var height = 100;
            var u = Unit.Pct;
            this.rootLevelView = new DepthLevelView({
                model: this.model,
                rect: new Rect(0, 0, width, height),
                unit: u,
            });
            this.$el.attr('style', 'max-height: 415px;' +
                'height: calc( 40vw + 15px);');
            this.rootLevelView.$el.attr('style', 'max-height: 400px;' +
                'height: calc( 40vw );' +
                'width: calc( 50vw );' +
                'max-width: 500px;');
            this.elementDetailsView.attr('.mas-mapElementDetailsSubView', 'style', 'height: calc(40vw - 26px);');
            this.rootLevelView.className = 'mas-treemap-view';
            this.rootLevelView.render();
            this.$el.append(this.rootLevelView.el);
            return this;
        };
        TreeMapView.prototype.clear = function () {
            if (this.rootLevelView) {
                this.rootLevelView.clear();
            }
            this.$el.empty();
        };
        return TreeMapView;
    })(BaseView);
    return TreeMapView;
});
