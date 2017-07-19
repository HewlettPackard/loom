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
    (function (Unit) {
        Unit[Unit["Px"] = 0] = "Px";
        Unit[Unit["Vh"] = 1] = "Vh";
        Unit[Unit["Vw"] = 2] = "Vw";
        Unit[Unit["Pct"] = 3] = "Pct";
    })(exports.Unit || (exports.Unit = {}));
    var Unit = exports.Unit;
    function to_css(unit) {
        switch (unit) {
            case Unit.Px: return 'px';
            case Unit.Vh: return 'vh';
            case Unit.Vw: return 'vw';
            case Unit.Pct: return '%';
        }
    }
    exports.to_css = to_css;
});
