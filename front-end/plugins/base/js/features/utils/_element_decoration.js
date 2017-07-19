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
define(["require", "exports", 'lodash', '../../common/models/Translator', 'weft/models/Element'], function (require, exports, _, Translator, Element) {
    var originalDefaults = Element.prototype.defaults;
    //Element.prototype.idAttribute = 'weaverId';
    Element.prototype.defaults = function () {
        var def = originalDefaults.call(this);
        return _.extend(def, {
            /**
             * Translator reference, Allows to show better names than
             * the one outputed by loom. Currently essentially used for countries.
             */
            translator: new Translator()
        });
    };
    Element.prototype.getTranslated = function () {
        var res = Element.prototype.get.apply(this, arguments);
        if (_.isString(res)) {
            return this.get('translator').translate(res);
        }
        return res;
    };
    var decorate = function () {
    };
    return decorate;
});
//# sourceMappingURL=_element_decoration.js.map