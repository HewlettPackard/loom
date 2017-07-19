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
define(["require", "exports", 'plugins/common/layout/AbstractFibreBuilder', './unit'], function (require, exports, AbstractFibreBuilder, mod_unit) {
    var FibreBuilder = (function (_super) {
        __extends(FibreBuilder, _super);
        function FibreBuilder(unit, fibreConfig) {
            _super.call(this);
            this.unit_str = mod_unit.to_css(unit) + ';';
            this.ignore_label = fibreConfig ? fibreConfig.ignore_label : false;
        }
        FibreBuilder.prototype.buildFibre = function (el) {
            var div = this.buildTag({
                tag: 'div',
                classes: 'mas-treemap-element',
                style: 'width: ' + el.box.width + this.unit_str +
                    'height: ' + el.box.height + this.unit_str +
                    'left: ' + el.box.x + this.unit_str +
                    'bottom: ' + el.box.y + this.unit_str,
            });
            if (!this.ignore_label) {
                div.appendChild(this.buildTag({
                    tag: 'div',
                    classes: [
                        'mas-fiberOverview--label',
                        'mas-treemap-element-padding'
                    ]
                }));
            }
            div.appendChild(this.buildTag({
                tag: 'div',
                classes: [
                    'mas-treemap-fiber--block',
                    'mas-treemap-inside-element',
                    'mas-treemap-element-padding',
                    this.ignore_label ? 'mas-ignore-label' : ''
                ],
            }));
            return div;
        };
        return FibreBuilder;
    })(AbstractFibreBuilder);
    return FibreBuilder;
});
