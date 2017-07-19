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

  var uuid = require('uuid');
  var Operation = require('weft/models/Operation');
  var Item = require('weft/models/Item');
  var ElementView = require('weaver/views/Element/ElementView');
  var FilterRelatedOperationView = require('weaver/views/QueryEditor/FilterRelatedOperationView');

  function simulateClick(element) {
    var event = document.createEvent('MouseEvent');
    event.initEvent('click', true, true);
    element.dispatchEvent(event);
  }

  var operationDescription = {
    name: 'Filter related',
    params: [{
      id: 'id',
      type: 'STRING'
    }]
  };

  describe('weaver/views/QueryEditor/FilterRelatedOperationView.js', function () {

    beforeEach(function () {
      this.fiberId = uuid.v4();
      this.fiber = new Item({
        'l.logicalId': this.fiberId,
        name: 'Fiber name'
      });
      this.elementView = new ElementView({
        model: this.fiber
      });
      this.elementView.$el.appendTo(document.body);
    });

    afterEach(function () {
      this.elementView.remove();
    });

    describe('Display state', function () {
      it('Displays the name of the fiber if present on screen', function () {
        var operation = {
          operator: Operation.FILTER_RELATED_ID,
          parameters: {
            id: this.fiberId
          }
        };
        var view = new FilterRelatedOperationView({
          model: operation,
          operationDescription: operationDescription
        });
        expect(view.$('.mas-operation--title')).to.contain('Filter related (Fiber name)');
      });
    });

    describe('Edition state', function () {
      beforeEach(function () {
        this.operationView = new FilterRelatedOperationView({
          model: {
            operator: Operation.FILTER_RELATED_ID,
            parameters: {}
          },
          operationDescription: operationDescription
        });
        this.operationView.$el.appendTo(document.body);
      });
      afterEach(function () {
        this.operationView.remove();
      });
      it('Should disable use selection option when no fiber is selected', function () {
        this.operationView.edit();
        expect(this.operationView.$('[data-action="use-selection"]')).to.be.disabled;
      });

      describe('Use selection', function () {
        beforeEach(function () {
          // We just need the class there, no need to go down to the model layer
          this.elementView.$el.addClass('is-selected');
          this.operationView.edit();
        });
        it('Configures this operation with currently selected fiber', function () {
          simulateClick(this.operationView.$('[data-action="use-selection"]')[0]);
          expect(this.operationView.$('.mas-operation--title')).to.contain('Filter related (Fiber name)');
          expect(this.operationView.$el).not.to.have.class('is-editing');
        });
      });

      // possibly deprecated at this moment
      describe.skip('Pick displayed fiber', function () {
        beforeEach(function () {
          this.operationView.edit();
          simulateClick(this.operationView.$('[data-action="select-fiber"]')[0]);
        });
        it('Masks the operation form to let users select the fiber', function () {
          expect(this.operationView.$el).to.have.class('is-selecting');
        });
        it('Allows users to select a fiber to use as operand for the operation', function () {
          simulateClick(this.elementView.el);
          expect(this.operationView.$('.mas-operation--title')).to.contain('Filter related (Fiber name)');
          expect(this.operationView.$el).not.to.have.class('is-selecting');
          expect(this.operationView.$el).not.to.have.class('is-editing');
        });
        it('Allows user to cancel their action', function () {
          simulateClick(this.operationView.$('[data-action=cancel-selection]')[0]);
          expect(this.operationView.$el).not.to.have.class('is-selecting');
          expect(this.operationView.$el).to.have.class('is-editing');
        });
      });
    });
  });
});
