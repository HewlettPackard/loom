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
define(["require", "exports", 'jquery', 'weaver/views/ElementDetailsView', './ActionDialogController2'], function (require, exports, $, ElementDetailsView, ActionDialogController2) {
    var FloatElementDetailsView = (function (_super) {
        __extends(FloatElementDetailsView, _super);
        function FloatElementDetailsView(options) {
            var _this = this;
            _super.call(this, options);
            this.events = {
                'click .mas-action--view': function (event) {
                    event.preventDefault();
                    _this.thread.trigger('didPressActionView', _this.model, event);
                },
                'click .mas-action--filter': function () {
                    event.preventDefault();
                    if (!_this.model.get('disabled')) {
                        _this.model.set('isPartOfFilter', !_this.model.get('isPartOfFilter'));
                    }
                }
            };
            /// IE hack to fix LOOM-1632
            /// https://connect.microsoft.com/IE/feedbackdetail/view/951267/
            /// horizontal-scrolling-with-flex-grow-max-width
            if (window.screen.width * 0.66 > 800) {
                this.$el.attr('style', 'width: 400px');
            }
            this.delegateEvents();
            this.thread = options.thread;
            // ugly, would have been preferrable to have it in a css class.
            this.$el.removeClass('mas-elementDetails');
            this.$el.addClass('mas-element-' + this.model.cid);
            this.$el.addClass('mas-element');
            this.$el.addClass('mas-element--content');
            this.$el.addClass('mas-mapElementDetails');
            var movedView = this.$el.children().detach();
            var subView = $('<div class="mas-mapElementDetailsSubView mas-elementDetails"></div>');
            subView.append(movedView);
            this.$el.append(subView);
            if (this.model.get('displayMode')) {
                this.$el.addClass('is-displayed');
            }
            this.listenTo(this.model, 'change:displayMode', function (model, displayMode) {
                if (displayMode) {
                    _this.$el.addClass('is-displayed');
                }
                else {
                    _this.$el.removeClass('is-displayed');
                }
            });
            if (this.model.get('isMatchingFilter')) {
                this.$el.addClass('is-matchingFilter');
            }
            this.listenTo(this.model, 'change:isMatchingFilter', function (model, match) {
                if (match) {
                    _this.$el.addClass('is-matchingFilter');
                }
                else {
                    _this.$el.removeClass('is-matchingFilter');
                }
            });
            if (this.model.get('isPartOfFilter')) {
                this.$el.addClass('is-partOfFilter');
            }
            this.listenTo(this.model, 'change:isPartOfFilter', function (model, value) {
                if (value) {
                    _this.$el.addClass('is-partOfFilter');
                }
                else {
                    _this.$el.removeClass('is-partOfFilter');
                }
            });
            this.listenTo(this.model, 'change:displayed', function (model, displayed) {
                if (displayed) {
                    _this.$el.addClass('is-displayed');
                }
                else {
                    _this.$el.removeClass('is-displayed');
                }
            });
            this.$('.mas-elementDetails--properties').css('max-height', 'none');
            this.$('.mas-alertDetails').css('flex-shrink', '0');
            this.actionDialogController.stopListening();
            this.actionDialogController.undelegateEvents();
            this.actionDialogController = new ActionDialogController2({
                model: this.model,
                el: this.el
            });
        }
        FloatElementDetailsView.prototype.remove = function () {
            ElementDetailsView.prototype.remove.apply(this, arguments);
            this.thread.trigger('didRemoveDetailsView');
            return this;
        };
        FloatElementDetailsView.prototype._updateTitle = function () {
            var label = this.model.getTranslated('name');
            var thread = this.thread;
            if (thread && thread.isGrouped()) {
                var groupByOperation = thread.getLastGroupByOperation();
                var unit = thread.getUnit(groupByOperation.parameters.property);
                if (unit) {
                    label += ' ' + unit;
                }
            }
            this.$('.mas-elementDetails--title').html(label);
        };
        return FloatElementDetailsView;
    })(ElementDetailsView);
    return FloatElementDetailsView;
});
//# sourceMappingURL=FloatElementDetailsView.js.map