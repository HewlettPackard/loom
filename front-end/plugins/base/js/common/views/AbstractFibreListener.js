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
define(["require", "exports", 'weaver/views/AbstractElementView', 'weaver/views/ElementView/ElementStateController', '../utils/AttributeHasChangedMonitor'], function (require, exports, AbstractElementView, ElementStateController, AttributeHasChangedMonitor) {
    var AbstractFibreListener = (function (_super) {
        __extends(AbstractFibreListener, _super);
        function AbstractFibreListener(options) {
            var _this = this;
            this.bindings = {
                ':el': {
                    classes: {
                        'is-related': 'related',
                        'is-highlighted': 'highlighted',
                        'is-fromHighlightedProvider': 'isFromHighlightedProvider'
                    }
                }
            };
            this.events = {
                'click': function (event) {
                    if (!event.isDefaultPrevented() && !event.originalEvent.ignoreSelect) {
                        if (_this.selected) {
                            _this.unselectElement(event);
                        }
                        else {
                            _this.selectElement(event);
                        }
                    }
                }
            };
            _super.call(this, options);
            this.attributesChanged = [];
            this.elementStateController = new ElementStateController({
                el: this.el,
                model: this.model
            });
            this.monitor = new AttributeHasChangedMonitor(this.model);
            this.listenTo(this.model, 'change:isMatchingFilter', function (model, matchesFilter) {
                _this._updateFilterState(matchesFilter);
            });
            this.listenTo(this.model, 'change:isPartOfFilter', this.setFilter);
            this.listenTo(this.model.alert, 'change:level', function (model, level) { return _this._updateAlertState(model.previous('level'), level); });
            //this.listenTo(this.model.updatedAttributesMonitor, 'change:updatedAttributes', this._updateUpdatedAttributes);
            this.listenTo(this.model, 'attributesHaveChanged', this._updateChangedAttributes);
            //this.listenTo(this.model.alert, 'change:level', () => this._updateChangedAttributes({ newAttributes: ['alertLevel']}));
        }
        AbstractFibreListener.prototype.selectElement = function (event) {
            if (this.selected) {
                return;
            }
            this.dispatchCustomEvent('willSelectElement', {
                view: this
            });
            if (!(event && event.isDefaultPrevented())) {
                this.selected = true;
                this._updateSelectedState(true);
                this.dispatchCustomEvent('didSelectElement', {
                    view: this
                });
            }
        };
        AbstractFibreListener.prototype.unselectElement = function (event) {
            this.selected = false;
            this._updateSelectedState(false);
            this.dispatchCustomEvent('didUnselectElement', undefined);
            if (event) {
                // Prevent default behaviour to avoid selecting the element back
                // Other option would be do enable/disable listeners according to
                // if element is selected or not
                event.preventDefault();
            }
        };
        AbstractFibreListener.prototype.remove = function () {
            this.elementStateController.deactivate();
            _super.prototype.remove.call(this);
            return this;
        };
        AbstractFibreListener.prototype.render = function () {
            this.stickit();
            _super.prototype.render.call(this);
            this.$el.removeClass('mas-element');
            this._updateAlertState(this.model.alert.get('level'), this.model.alert.get('level'));
            return this;
        };
        AbstractFibreListener.prototype._updateElementDetails = function (selected) {
            throw new Error("Unimplemented error");
        };
        AbstractFibreListener.prototype._updateChangedAttributes = function (changes) {
            this.attributesChanged = _.union(this.attributesChanged, changes.newAttributes);
            this.attributesChanged = _.bind(_.without, _, this.attributesChanged).apply(_, changes.notNewAnymoreAttributes);
            this._updateChangedAttributesFullList(this.attributesChanged);
        };
        AbstractFibreListener.prototype._updateChangedAttributesFullList = function (attributes) {
            throw new Error("Unimplemented error");
        };
        AbstractFibreListener.prototype._updateSelectedState = function (selected) {
            if (selected) {
                this.$el.addClass('is-selected');
            }
            else {
                this.$el.removeClass('is-selected');
            }
            this._updateElementDetails(selected);
        };
        AbstractFibreListener.prototype._updateAlertState = function (oldlevel, level) {
            this.$el.removeClass('mas-alertNotification-' + oldlevel);
            this.$el.addClass('mas-alertNotification-' + level);
        };
        AbstractFibreListener.prototype.setFilter = function () {
            if (this.model.get('isPartOfFilter')) {
                this._dispatchEvent('action:addFilterElement', null);
                this.$el.addClass('is-partOfFilter');
            }
            else {
                this._dispatchEvent('action:removeFilterElement', null);
                this.$el.removeClass('is-partOfFilter');
            }
        };
        return AbstractFibreListener;
    })(AbstractElementView);
    return AbstractFibreListener;
});
//# sourceMappingURL=AbstractFibreListener.js.map