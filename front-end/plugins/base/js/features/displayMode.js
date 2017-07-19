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
define(["require", "exports", './utils/_thread_decoration', './utils/_thread_settings_menu_decoration', './utils/display_mode', '../common/utils/DefaultQueryCleaner', 'weaver/views/ThreadView'], function (require, exports, thread_decoration, thread_settings_menu_decoration, display_mode, DefaultQueryCleaner, ThreadView) {
    thread_decoration();
    thread_settings_menu_decoration();
    var displayModeAvailables = display_mode.displayModeAvailables;
    var always = function () { return true; };
    displayModeAvailables['classic'] = {
        readableName: 'Classic',
        available: always,
        threadViewClass: ThreadView,
        queryCleaner: new DefaultQueryCleaner()
    };
    var feature = function () {
    };
    return feature;
});
//# sourceMappingURL=displayMode.js.map