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
define(["require", "exports", './ATableBuilder', 'plugins/common/layout/builder_interfaces'], function (require, exports, ATableBuilder, helper) {
    var FibreBuilder = (function (_super) {
        __extends(FibreBuilder, _super);
        function FibreBuilder(selector) {
            _super.call(this);
            this.selector = selector;
        }
        FibreBuilder.prototype.buildFibre = function (ctxt, attributes_changed) {
            var keys = _.pluck(ctxt.model.getItemType().getVisibleAttributes(), 'id');
            var classes = [];
            for (var i = 0; i < keys.length; ++i) {
                var css_class = ctxt.attribute_class[i];
                classes.push({ classes: css_class ? [css_class] : [], col: i });
            }
            _.forEach(attributes_changed, function (attribute) {
                classes[keys.indexOf(attribute)].classes.push('mas-has-changed');
            });
            var el = this.build_line(_.map(keys, function (val) {
                return helper.as_line_value(ctxt.model.get(val));
            }), {
                row: ctxt.row_css.concat(['mas-table-row']),
                generic_cell: 'mas-table-cell',
                separator: 'mas-table-separator',
                cols: classes
            });
            var wrapper_relative = this.buildTag({
                tag: 'div',
                style: 'position:relative; overflow: visible; height: 100%;'
            });
            wrapper_relative.appendChild(this.buildTag({
                tag: 'div',
                classes: 'mas-table-contextMenu',
            }));
            el.firstChild.insertBefore(wrapper_relative, el.firstChild.firstChild);
            return el;
        };
        FibreBuilder.prototype.col_css = function (i) {
            return this.selector.col_css(i);
        };
        return FibreBuilder;
    })(ATableBuilder);
    return FibreBuilder;
});
