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
define(["require", "exports", 'weaver/views/Menu', 'weaver/views/PropertySelector', './DisplayMode'], function (require, exports, Menu, PropertySelector, DisplayMode) {
    var DisplayModeMenu = (function (_super) {
        __extends(DisplayModeMenu, _super);
        function DisplayModeMenu() {
            _super.apply(this, arguments);
        }
        DisplayModeMenu.prototype.initialize = function () {
            Menu.prototype.initialize.apply(this, arguments);
            this.className = Menu.prototype.className + ' mas-displayMenu is-collapsed';
            // this.$el = template.clone();
            this.helper = new DisplayMode({
                thread: this.model
            });
            this.propertySelector = new PropertySelector({
                title: 'Display Options',
                preventDeselectionOnSameClick: true,
                model: this.helper.getPossibleDisplayModeWithReadableName()
            });
            this.propertySelector.select(this.model.get('displayMode'), false);
            this.listenTo(this.propertySelector, 'change:selection', this._updateDisplayMode);
            this.toggleElement = undefined;
            this.render();
        };
        DisplayModeMenu.prototype.render = function () {
            Menu.prototype.render.apply(this, arguments);
            this.stickit();
            this.$el.addClass(this.className);
            this.propertySelector.$el.addClass('mas-menu--content');
            this.el.appendChild(this.propertySelector.el);
            if (!this.helper.hasManyDisplayMode()) {
                this.$el.addClass('mas-is-visibility-hidden');
            }
        };
        DisplayModeMenu.prototype._renderToggle = function () {
            var toggle = this.toggleElement = document.createElement('div');
            var globe = document.createElement('div');
            var list = document.createElement('div');
            var refresh = document.createElement('div');
            globe.classList.add('mas-displayMode--map');
            globe.classList.add('fa');
            globe.classList.add('fa-globe');
            list.classList.add('mas-displayMode--classic');
            list.classList.add('fa');
            list.classList.add('fa-th-list');
            refresh.classList.add('mas-displayMode--switch');
            refresh.classList.add('fa');
            refresh.classList.add('fa-refresh');
            toggle.appendChild(globe);
            toggle.appendChild(list);
            toggle.appendChild(refresh);
            toggle.classList.add('mas-displayMenu--toggle');
            this.$('.mas-menu--toggle').prepend(toggle);
        };
        DisplayModeMenu.prototype._updateDisplayMode = function (displayMode) {
            this.model.set('displayMode', displayMode);
        };
        return DisplayModeMenu;
    })(Menu);
    return DisplayModeMenu;
});
//# sourceMappingURL=DisplayModeMenu.js.map