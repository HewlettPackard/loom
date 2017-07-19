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

  var _ = require('lodash');
  var FiberAttributeView = require('weaver/views/FiberAttributeView');

  var DMAFiberAttributeView = FiberAttributeView.extend({

    events: _.defaults({
      'click': function (event) {
        event.preventDefault();
        this.toggleExpectedValue();
      }
    }, FiberAttributeView.prototype.events),

    initialize: function () {
      FiberAttributeView.prototype.initialize.apply(this, arguments);
      this.listenTo(this.model, 'change:l.discrepancies', this.updateDiscrepancyState);
    },

    render: function () {
      FiberAttributeView.prototype.render.apply(this, arguments);
      this.$el.addClass('mas-dmaFiberAttribute');
      this.updateDiscrepancyState();
    },

    toggleExpectedValue: function () {
      if (this.hasDiscrepancy) {

        this.showsExpectedValue = !this.showsExpectedValue;
        var expectedValue = this.model.get('l.discrepancies')[this.options.attribute.id];

        if(this.showsExpectedValue) {
          this.$el.append('<div class="mas-dmaFiberAttributeExpectedValue">Expected value: ' + expectedValue + '  </div>');
        } else {
          this.$('.mas-dmaFiberAttributeExpectedValue').remove();
        }
      }
    },

    updateDiscrepancyState: function () {

      var discrepancies = this.model.get('l.discrepancies');
      if (discrepancies && discrepancies[this.options.attribute.id]) {
        this.hasDiscrepancy = true;
        this.$el.addClass('mas-dmaFiberAttribute-hasDiscrepancy');
      } else {

        this.showsExpectedValue = false;
        this.hasDiscrepancy = false;
        this.$el.removeClass('mas-dmaFiberAttribute-hasDiscrepancy');
        this.$('.mas-dmaFiberAttributeExpectedValue').remove();
      }
    }
  });

  return DMAFiberAttributeView;
});
