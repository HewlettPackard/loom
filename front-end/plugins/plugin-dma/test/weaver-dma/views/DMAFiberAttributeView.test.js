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
define(function (require){

  "use strict";

  var DMAFiberAttributeView = require('weaver-dma/views/DMAFiberAttributeView');

  var Item = require('weft/models/Item');

  describe('weaver-dma/views/DMAFiberAttributeView.js', function (){

    function simulateClick(element) {

      var event = document.createEvent('MouseEvent');
      event.initEvent('click', true, true);
      element.dispatchEvent(event);
    }

    beforeEach(function () {

      this.item = new Item({
        'core.dmaStatus': 'DISCREPANT',
        'core.dmaDiscrepancyCount': 1,
        'core.power': 'ON',
        'core.version': '13.3.7',
        'l.discrepancies': {
          'core.power': 'OFF'
        }
      }, {
        itemType: {
          attributes: {
            'core.power': {
              name: 'Power'
            },
            'core.version': {
              name: 'Version'
            }
          }
        }
      });
    });

    it('Shows an attribute is OK and updates if attribute becomes discrepant', function () {

      var view = new DMAFiberAttributeView({
        model: this.item,
        attribute: {
          'id': 'core.version',
          'name': 'Version'
        }
      });

      expect(view.$el).not.to.have.class('mas-dmaFiberAttribute-hasDiscrepancy');

      this.item.set({
        'l.discrepancies': {
          'core.version': '10.2.4'
        }
      });

      expect(view.$el).to.have.class('mas-dmaFiberAttribute-hasDiscrepancy');
    });

    it('Shows an attribute has discrepancy', function () {

      var view = new DMAFiberAttributeView({
        model: this.item,
        attribute: {
          'id': 'core.power',
          'name': 'Power'
        }
      });

      expect(view.$el).to.have.class('mas-dmaFiberAttribute-hasDiscrepancy');

      this.item.set({
        'l.discrepancies': undefined
      });

      expect(view.$el).not.to.have.class('mas-dmaFiberAttribute-hasDiscrepancy');
    });

    it('Shows expected value when clicking on a discrepant attribute', function () {

      var view = new DMAFiberAttributeView({
        model: this.item,
        attribute: {
          'id': 'core.power',
          'name': 'Power'
        }
      });

      simulateClick(view.el);

      expect(view.$el).to.have.descendants('.mas-dmaFiberAttributeExpectedValue:contains(OFF)');

      simulateClick(view.el);

      expect(view.$el).not.to.have.descendants('.mas-dmaFiberAttributeExpectedValue');
    });

    it('Removes expected value if it is displayed and the attribute stops being discrepant', function () {

      var view = new DMAFiberAttributeView({
        model: this.item,
        attribute: {
          'id': 'core.power',
          'name': 'Power'
        }
      });

      simulateClick(view.el);

      this.item.set({
        'l.discrepancies': undefined
      });

      expect(view.$el).not.to.have.descendants('.mas-dmaFiberAttributeExpectedValue');
    });

  });

});
