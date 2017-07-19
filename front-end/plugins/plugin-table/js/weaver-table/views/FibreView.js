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
define(["require", "exports", 'plugins/common/views/AbstractFibreListener', './FibreContextMenu', 'jquery'], function (require, exports, AbstractFibreListener, FibreContextMenu, $) {
    var FibreView = (function (_super) {
        __extends(FibreView, _super);
        function FibreView(options) {
            _super.call(this, options);
            this.thread = options.thread;
            this.builder = options.builder;
            this.$el.data('view', this);
            this.fibreContextMenu = new FibreContextMenu({
                el: this.$('.' + FibreContextMenu.className),
                model: this.model,
                thread: this.thread,
            });
            this.fibreContextMenu.render();
        }
        FibreView.factory = function (options) {
            return new FibreView(options);
        };
        FibreView.prototype.selectElement = function (event) {
            _super.prototype.selectElement.call(this, event);
            this.fibreContextMenu.show();
        };
        FibreView.prototype.unselectElement = function (event) {
            _super.prototype.unselectElement.call(this, event);
            this.fibreContextMenu.hide();
        };
        FibreView.prototype._updateElementDetails = function (selected) { };
        FibreView.prototype._updateChangedAttributesFullList = function (attributes) {
            var attribute_class = [];
            var children = this.$el.children('.mas-table-cell');
            var row_css = _.filter(this.$el[0].className.split(' '), function (css) { return css === 'mas-last-two-row'; });
            for (var i = 0; i < children.length; ++i) {
                var child = children[i];
                var className = _.without(child.className.split(' '), 'mas-has-changed').join(' ');
                attribute_class.push(className);
            }
            var el = this.builder.buildFibre({ model: this.model, attribute_class: attribute_class, row_css: row_css }, attributes);
            this.$el.html($(el).html());
            this.fibreContextMenu.setElement(this.$('.' + FibreContextMenu.className));
            this.fibreContextMenu.render();
        };
        return FibreView;
    })(AbstractFibreListener);
    return FibreView;
});
