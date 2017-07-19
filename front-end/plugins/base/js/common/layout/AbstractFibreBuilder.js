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
define(["require", "exports", './AbstractBuilder'], function (require, exports, AbstractBuilder) {
    var AbstractFibreBuilder = (function (_super) {
        __extends(AbstractFibreBuilder, _super);
        function AbstractFibreBuilder() {
            _super.apply(this, arguments);
        }
        AbstractFibreBuilder.prototype.buildFibre = function (context) {
            throw new Error("Unimplemented error");
        };
        return AbstractFibreBuilder;
    })(AbstractBuilder);
    return AbstractFibreBuilder;
});
//# sourceMappingURL=AbstractFibreBuilder.js.map