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
define(["require", "exports"], function (require, exports) {
    var CollapseController = (function () {
        function CollapseController(attributes, collapse) {
            if (collapse) {
                this.collapsed_attributes = collapse.collapsed_attributes;
            }
            else {
                this.collapsed_attributes = {};
            }
            this.attributes = attributes;
        }
        CollapseController.prototype.is_attribute_collapsed = function (attributeName) {
            return this.collapsed_attributes[attributeName];
        };
        CollapseController.prototype.is_column_collapsed = function (col) {
            return this.is_attribute_collapsed(this.attributes[col]);
        };
        CollapseController.prototype.collapse_column = function (col) {
            this.collapsed_attributes[this.attributes[col]] = true;
        };
        CollapseController.prototype.expand_column = function (col) {
            this.collapsed_attributes[this.attributes[col]] = false;
        };
        return CollapseController;
    })();
    return CollapseController;
});
