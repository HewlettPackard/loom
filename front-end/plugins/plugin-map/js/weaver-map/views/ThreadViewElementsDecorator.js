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
define(function (require) {
  "use strict";

  var ThreadViewElements = require('weaver/views/ThreadViewElements');

  var originalformatLabel = ThreadViewElements.prototype._formatLabel;

  ThreadViewElements.prototype._formatLabel = function (value, unit) {
    var translator = this.model.get('translator');

    return originalformatLabel.call(this, translator.translate(value), unit);
  };
});