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
define(["require", "exports", 'plugins/common/views/AbstractFibreListener', 'weaver/views/AlertNotificationView'], function (require, exports, AbstractFibreListener, AlertNotificationView) {
    var FibreView = (function (_super) {
        __extends(FibreView, _super);
        function FibreView(options) {
            _super.call(this, options);
            this.thread = options.thread;
            this.alertNotificationView = new AlertNotificationView({
                model: this.model ? this.model.alert : undefined
            });
            this.alertNotificationView.$el.addClass('mas-fiberOverview--alert')
                .appendTo(this.$el);
            this.$('.mas-fiberOverview--label').append(this.model.getTranslated('name'));
        }
        FibreView.factory = function (options) {
            return new FibreView(options);
        };
        FibreView.prototype._updateElementDetails = function (selected) {
            if (selected) {
                this.dispatchCustomEvent('element:show-details', {
                    args: true,
                    model: this.model,
                    thread: this.thread,
                });
            }
            else {
                this.dispatchCustomEvent('element:show-details', {
                    args: false,
                });
            }
        };
        FibreView.prototype._updateChangedAttributesFullList = function (attributes) {
        };
        return FibreView;
    })(AbstractFibreListener);
    return FibreView;
});
