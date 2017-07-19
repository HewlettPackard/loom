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
define(["require", "exports", 'jquery', 'weaver/views/ThreadSettingsMenu', '../../common/utils/DisplayModeMenu'], function (require, exports, $, ThreadSettingsMenu, DisplayModeMenu) {
    var originalInitialize = ThreadSettingsMenu.prototype.initialize;
    function addButton(css, name, style) {
        style = style || "";
        return $('<div class="mas-threadSettingsMenu--item mas-menu ' + css + ' mas-menu-expandsDown mas-menu-rightAligned">' + '<div class="mas-menu--toggle mas-button-leftIcon" ' + style + '>' + name + '</div>' + '</div>');
    }
    ;
    ThreadSettingsMenu.prototype.initialize = function () {
        originalInitialize.apply(this, arguments);
        this.$('.mas-menu--content:first-child').prepend(addButton('mas-displayMenu', 'Display Mode', 'style="display: flex"'));
        /*this._addButton('mas-mapMenu', 'Distortion');
      
        this.mapMenu = new MapMenu({
          model: this.model,
          el: this.$('.mas-mapMenu'),
        });*/
        this.displayModeMenu = new DisplayModeMenu({
            model: this.model,
            el: this.$('.mas-displayMenu')
        });
    };
    var decorate = function () {
    };
    return decorate;
});
//# sourceMappingURL=_thread_settings_menu_decoration.js.map