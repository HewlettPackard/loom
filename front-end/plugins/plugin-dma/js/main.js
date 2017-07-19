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
// Update rendering of FiberAttributes to handle display of desired state
"use strict";
require('../less/index.less');

var ElementDetailsView = require('weaver/views/ElementDetailsView');
var DMAFiberAttributeView = require('weaver-dma/views/DMAFiberAttributeView');

// Enhance ElementDetailsView to display DMA specific information in the attributes
ElementDetailsView.prototype.getView = function (attribute) {
  return new DMAFiberAttributeView({
    model: this.model,
    attribute: attribute
  });
};


var ToolbarView = require('weaver/views/ToolbarView');
var TapestryScreen = require('weaver/screens/TapestryScreen');

var GroupByDMAStatusController = require('weaver-dma/views/GroupByDMAStatusController');
var GroupByDMAStatusToggleTemplate = require('weaver-dma/views/GroupByDMAStatusToggleTemplate.html');

/**
 * Utility to run given `advice` function after given `methodName` on give `object`
 * @var after
 * @type {Function}
 * @param {Object} object The objet whose method to advise
 * @param {String} methodName The name of the method to advise
 * @param {Function} advice The function that will be run after the orignal method
 */
function after(object, methodName, advice) {
  var original = object[methodName];
  object[methodName] = function () {
    original.apply(this, arguments);
    advice.apply(this, arguments);
  };
}


// Add a button in the toolbar to display
after(ToolbarView.prototype, 'initialize', function () {
  this.$('.mas-filter').after(GroupByDMAStatusToggleTemplate);
});

// And the controller that will react to interactions with this button
after(TapestryScreen.prototype, 'initialize', function () {
  this.groupByDMAStatusController = new GroupByDMAStatusController({
    el: this.el,
    model: this.threadListView.model
  });
});

after(TapestryScreen.prototype, '_destroyServices', function () {
  this.groupByDMAStatusController.stopListening();
});
