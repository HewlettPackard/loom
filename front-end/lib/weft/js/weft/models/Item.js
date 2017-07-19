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
"use strict";

// TODO: Fix this properly by renaming Element to Fiber
/*jshint -W079 */
var Element = require('weft/models/Element');
var ItemType = require('weft/models/ItemType');

/**
 * Items are the unitary elements of a Thread (eg. a particular chassis).
 * @class Item
 * @namespace models
 * @module weft
 * @extends models.Element
 * @constructor
 */
var Item = Element.extend({
  initialize: function (attributes, options) {

    options = options || {};
    options.itemType = options.itemType || {};

    options.itemType = new ItemType(options.itemType);
    Element.prototype.initialize.call(this, attributes, options);
  }
});

module.exports = Item;
