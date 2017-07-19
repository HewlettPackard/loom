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
define(["require", "exports", 'lodash', './features/utils/thread_attributes_to_clone', './common/models/dictionary', './common/utils/DefaultQueryCleaner', './features/utils/display_mode', './features/displayMode', './features/translator', './features/utils/_thread_view_decoration', './features/utils/_query_editor_decoration', './features/utils/_thread_list_view_decoration', './features/utils/_braiding_controller_decoration'], function (require, exports, _, thread_attributes_to_clone, dictionary, DefaultQueryCleaner, display_mode, displayModeFeature, translatorFeature, thread_view_decoration, query_editor_decoration, thread_list_view_decoration, braiding_controller_decoration) {
    var displayModeAvailables = display_mode.displayModeAvailables;
    displayModeFeature();
    translatorFeature();
    thread_view_decoration();
    query_editor_decoration();
    thread_list_view_decoration();
    braiding_controller_decoration.getValue();
    require('../less/style.less');
    function addTranslations(str, traductions) {
        _.assign(dictionary[str], traductions);
    }
    exports.addTranslations = addTranslations;
    exports.always = function () { return true; };
    function registerDisplayMode(register) {
        displayModeAvailables[register.name] = {
            readableName: register.humanReadableName,
            available: register.availability,
            threadViewClass: register.threadViewClass,
            queryCleaner: register.queryCleaner ? register.queryCleaner : new DefaultQueryCleaner()
        };
    }
    exports.registerDisplayMode = registerDisplayMode;
    function registerThreadAttributes() {
        var attributes = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            attributes[_i - 0] = arguments[_i];
        }
        var test = _.uniq(thread_attributes_to_clone.concat(attributes));
        while (thread_attributes_to_clone.pop())
            ;
        thread_attributes_to_clone.push.apply(thread_attributes_to_clone, test);
    }
    exports.registerThreadAttributes = registerThreadAttributes;
});
//# sourceMappingURL=pluginAPI.js.map