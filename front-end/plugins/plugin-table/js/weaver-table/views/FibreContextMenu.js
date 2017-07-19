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
define(["require", "exports", 'backbone', 'lodash', 'jquery', 'weaver/views/ElementDetailsView/ElementActionsView', 'weaver/views/ElementDetailsView/ActionDialogController'], function (require, exports, Backbone, _, $, ElementActionsView, ActionDialogController) {
    var FibreContextMenu = (function (_super) {
        __extends(FibreContextMenu, _super);
        function FibreContextMenu(args) {
            var _this = this;
            _super.call(this, args);
            this.title = 'Actions';
            this.className = FibreContextMenu.className;
            this.thread = args.thread;
            this.events = {
                'click .mas-action--view': function (event) {
                    event.preventDefault();
                    _this.thread.trigger('didPressActionView', _this.model, event);
                },
                'click .mas-action--filter': function (event) {
                    event.preventDefault();
                    if (!_this.model.get('disabled')) {
                        _this.model.set('isPartOfFilter', !_this.model.get('isPartOfFilter'));
                    }
                },
                'click': function (event) {
                    event.originalEvent.ignoreSelect = true;
                }
            };
            this.delegateEvents();
        }
        FibreContextMenu.prototype.render = function () {
            var $buttons = $('<div>', {
                class: 'mas-table-contextMenu--actions'
            });
            this.elementActionView = new ElementActionsView({
                el: $buttons,
                model: this.model,
            });
            this.actionDialogController = new ActionDialogControllerSpec({
                model: this.model,
                el: this.el
            });
            var $header = $('<div>', {
                class: 'mas-table-contextMenu--title'
            }).append(this.title);
            this.$el.append($header);
            this.$el.append($buttons);
            return this;
        };
        FibreContextMenu.prototype.show = function () {
            this.$el.addClass('mas-display-block');
        };
        FibreContextMenu.prototype.hide = function () {
            this.$el.removeClass('mas-display-block');
            this.actionDialogController.hideDialog();
        };
        FibreContextMenu.className = 'mas-table-contextMenu';
        return FibreContextMenu;
    })(Backbone.View);
    var ActionDialogControllerSpec = (function (_super) {
        __extends(ActionDialogControllerSpec, _super);
        function ActionDialogControllerSpec(options) {
            var _this = this;
            _.assign(this.events, ActionDialogController.prototype.events, {
                'click .mas-action-hideDialog': function (event) {
                    event.preventDefault();
                    _this.hideDialog();
                }
            });
            _super.call(this, options);
        }
        ActionDialogControllerSpec.prototype._manuallyResetHeightOfSiblings = function () {
        };
        return ActionDialogControllerSpec;
    })(ActionDialogController);
    return FibreContextMenu;
});
