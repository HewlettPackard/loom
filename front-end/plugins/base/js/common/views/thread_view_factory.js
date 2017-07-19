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
define(["require", "exports", '../utils/DisplayMode', '../../features/utils/display_mode', 'backbone'], function (require, exports, DisplayMode, module_dm, Backbone) {
    var displayModeAvailables = module_dm.displayModeAvailables;
    function main(thread, selectionService, providerLegendService) {
        var displayMode = thread.get('displayMode');
        displayMode = displayMode ? displayMode : DisplayMode.CLASSIC;
        return new displayModeAvailables[displayMode].threadViewClass({
            model: thread,
            selectionService: selectionService,
            providerLegendService: providerLegendService
        });
    }
    exports.main = main;
    function subThread(thread, otherOptions) {
        if (otherOptions === void 0) { otherOptions = {}; }
        var displayMode = thread.get('displayMode');
        displayMode = displayMode ? displayMode : DisplayMode.CLASSIC;
        otherOptions.model = thread;
        return new displayModeAvailables[displayMode].threadViewClass(otherOptions);
    }
    exports.subThread = subThread;
});
//# sourceMappingURL=thread_view_factory.js.map