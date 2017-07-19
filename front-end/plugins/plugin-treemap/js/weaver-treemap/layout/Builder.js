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
define(["require", "exports", 'plugins/common/layout/AbstractBuilder', 'lodash'], function (require, exports, AbstractBuilder, _) {
    var Builder = (function (_super) {
        __extends(Builder, _super);
        function Builder(elements, box, fibreBuilder) {
            _super.call(this);
            this.elements = elements;
            this.box = box;
            this.fibrebuilder = fibreBuilder;
        }
        Builder.prototype.build = function (tag, css_class, viewfactory) {
            var _this = this;
            var views = [];
            var res = this.buildTag({
                tag: tag,
                classes: [css_class],
                style: ' width: ' + this.box.width + this.fibrebuilder.unit_str +
                    ' height: ' + this.box.height + this.fibrebuilder.unit_str
            });
            _.forEach(this.elements, function (node) {
                var el = _this.fibrebuilder.buildFibre(node);
                views.push(viewfactory({
                    el: el,
                    model: node.model
                }));
                res.appendChild(el);
            });
            return {
                el: res,
                views: views
            };
        };
        return Builder;
    })(AbstractBuilder);
    return Builder;
});
