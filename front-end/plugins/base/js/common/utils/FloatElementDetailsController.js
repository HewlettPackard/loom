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
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", 'backbone', 'lodash', '../views/FloatElementDetailsView'], function (require, exports, Backbone, _, FloatElementDetailsView) {
    var FloatElementDetailsController = (function (_super) {
        __extends(FloatElementDetailsController, _super);
        function FloatElementDetailsController(options) {
            this.target = options.target;
            this.attributesForView = {};
            this.events = {
                'element:show-details': function (event) {
                    this._showDetails(event.originalEvent.args && event.originalEvent.model, event.originalEvent.thread);
                }
            };
            _super.call(this, options);
        }
        FloatElementDetailsController.prototype.remove = function () {
            this.stopListening();
            return this;
        };
        FloatElementDetailsController.prototype.hideView = function () {
            if (this.view) {
                this.view.remove();
                this.view = undefined;
            }
        };
        FloatElementDetailsController.prototype._showDetails = function (modelToPresent, thread) {
            var _this = this;
            if (this.view) {
                this.view.remove();
                this.view = undefined;
            }
            if (modelToPresent) {
                this.view = new FloatElementDetailsView({
                    model: modelToPresent,
                    thread: thread
                });
                this.view.$el.appendTo(this.target);
                _.forEach(this.attributesForView, function (attributes, selector) {
                    _.forEach(attributes, function (value, attrName) {
                        _this.view.$(selector).attr(attrName, value);
                    });
                });
            }
        };
        FloatElementDetailsController.prototype.attr = function (selector, attrName, value) {
            this.attributesForView[selector] = this.attributesForView[selector] || {};
            this.attributesForView[selector][attrName] = value;
        };
        return FloatElementDetailsController;
    })(Backbone.View);
    return FloatElementDetailsController;
});
//# sourceMappingURL=FloatElementDetailsController.js.map