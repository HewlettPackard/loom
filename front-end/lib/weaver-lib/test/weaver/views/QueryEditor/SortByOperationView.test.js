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

  var Thread = require('weft/models/Thread');
  var SortByOperationView = require('weaver/views/QueryEditor/SortByOperationView');

  describe('weaver/views/QueryEditor/SortByOperationView.js', function () {

    function simulateClick(element) {

      var event = document.createEvent('MouseEvent');
      event.initEvent('click', true, true);
      element.dispatchEvent(event);
    }

    var thread = new Thread({
        itemType: {
          id: 'some-item-type',
          attributes: {
            'attribute-a': {
              id: 'attribute-a',
              name: 'Attribute A'
            },
            'attribute-b': {
              id: 'attribute-b',
              name: 'Attribute B'
            },
            'attribute-c': {
              id: 'attribute-c',
              name: 'Attribute C'
            }
          },
          operations: {
            SORT_BY: ['attribute-a', 'attribute-b', 'attribute-c']
          }
        }
      });

    var view = new SortByOperationView({
      model: {
        parameters: {}
      },
      operationDescription: {
        id: 'SORT_BY',
        name: 'Sort by',
        params: [{
          id: 'property',
          type: 'ATTRIBUTE_LIST'
        }, {
          id: 'order',
          type: 'ENUM',
          range: ['ASC', 'DSC']
        }],
        icon: 'fa-exchange',
        displayParameters: ['property', 'order']
      },
      thread: thread
    });


    before(function () {
      document.body.appendChild(view.el);
    });

    after(function () {

      view.remove();
    });

    it('Lists the attributes available for sorting', function () {

      view.edit();
      expect(view.$el).to.have.descendants('.mas-propertySelector--property:contains(Attribute A)');
      expect(view.$el).to.have.descendants('.mas-propertySelector--property:contains(Attribute B)');
      expect(view.$el).to.have.descendants('.mas-propertySelector--property:contains(Attribute C)');
    });

    it('Updates the operation with selected values', function () {

      // Click twice to select descending order
      var $attribute = view.$('.mas-propertySelector--property:contains(Attribute B)');
      simulateClick($attribute[0]);
      simulateClick($attribute[0]);
      simulateClick(view.$('.mas-operationEditor--save')[0]);

      expect(view.model.parameters.property).to.equal('attribute-b');
      expect(view.model.parameters.order).to.equal('DSC');
    });

    it('Displays the order in which the value is sorted', function () {

      var $display = view.$('.mas-operation--display');
      expect($display).to.contain('Attribute B, DSC');
    });
  });
});
