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
  var DefaultOperationView = require('weaver/views/QueryEditor/DefaultOperationView');

  describe('weaver/views/QueryEditor/DefaultOperationView.js', function () {

    var operationDescriptionIndex = {
      'withStringParam': {
        name: 'Operation (String)',
        params: [{
          id: 'stringParam',
          type: 'STRING'
        }]
      },
      'withIntParam': {
        name: 'Operation (Int)',
        params: [{
          id: 'intParam',
          type: 'INT'
        }]
      },
      'withFloatParam': {
        name: 'Operation (Float)',
        params: [{
          id: 'floatParam',
          type: 'FLOAT'
        }]
      },
      'withAttributeListParam': {
        id: 'operation_with_attributeList',
        name: 'Operation (AttributeList)',
        params: [{
          id: 'attributeListParam',
          type: 'ATTRIBUTE_LIST'
        }],
        displayParameters: ['attributeListParam']
      },
      'withOneAttributeParam': {
        id: 'operation_with_one_attribute',
        name: 'Operation (One attribute)',
        params: [{
          id: 'attributeListParam',
          type: 'ATTRIBUTE_LIST'
        }],
        displayParameters: ['attributeListParam']
      },
      'withDisplayedParameters': {
        name: 'Operation (Displayed Parameters)',
        params: [{
          id: 'param-1',
          type: 'STRING'
        }, {
          id: 'param-2',
          type: 'STRING'
        }, {
          id: 'param-3',
          type: 'STRING'
        }],
        displayParameters: ['param-1', 'param-3']
      }
    };

    var thread = new Thread({
      itemType: {
        attributes: {
          'attribute-a': {
            id: 'attribute-a',
            name: 'Attribute A'
          },
          'attribute-b': {
            id: 'attribute-b',
            name: 'Attribute B'
          },'attribute-c': {
            id: 'attribute-c',
            name: 'Attribute C'
          }
        },
        operations: {
          'operation_with_attributeList': ['attribute-a', 'attribute-c'],
          'operation_with_one_attribute': ['attribute-b']
        }
      }
    });

    describe('renderEditionState()', function () {

      it('Displays the operator if no operation description is provided', function () {

        var operation = {
          operator: 'UNKNOWN_OPERATOR'
        };

        var view = new DefaultOperationView({
          model: operation
        });
        view.edit();

        expect(view.$('.mas-operation--edit.mas-operation--title')).to.contain('UNKNOWN_OPERATOR');
      });

      it('Displays a text input if parameter type is string', function () {

        var operation = {
          parameters: {
            stringParam: 'string_value'
          }
        };

        var view = new DefaultOperationView({
          model: operation,
          operationDescription: operationDescriptionIndex.withStringParam
        });
        view.edit();

        expect(view.$('.mas-operation--form')).to.have.descendants('input[type=text][value=string_value][name=stringParam]');
      });

      it('Displays a `number` input if parameter is int', function () {

        var operation = {
          parameters: {
            intParam: 12
          }
        };

        var view = new DefaultOperationView({
          model: operation,
          operationDescription: operationDescriptionIndex.withIntParam
        });
        view.edit();

        expect(view.$('.mas-operation--form')).to.have.descendants('input[type=number][value=12][name=intParam]');
      });

      it('Displays a `number` input if parameter is float', function () {

        var operation = {
          parameters: {
            floatParam: 5.5
          }
        };

        var view = new DefaultOperationView({
          model: operation,
          operationDescription: operationDescriptionIndex.withFloatParam
        });
        view.edit();
        expect(view.$('.mas-operation--form')).to.have.descendants('input[type=number][value="5.5"][name=floatParam]');
      });

      it('Displays a PropertySelector if parameter is an attributeList', function () {

        var operation = {
          parameters: {
            attributeListParam: 'attribute-c'
          }
        };

        var view = new DefaultOperationView({
          model: operation,
          operationDescription: operationDescriptionIndex.withAttributeListParam,
          thread: thread
        });
        view.edit();
        expect(view.$('.mas-operation--form')).to.have.descendants('.mas-propertySelector--property[class*=attribute-a]:contains(Attribute A)');
        expect(view.$('.mas-operation--form')).to.have.descendants('.mas-propertySelector--property[class*=attribute-c]:contains(Attribute C).mas-property-selected');
      });

      it('Displays a frozen attribute if there is only one attribute available for an attributeList', function () {

        var operation = {
          parameters:{}
        };

        var view = new DefaultOperationView({
          model: operation,
          operationDescription: operationDescriptionIndex.withOneAttributeParam,
          thread: thread
        });
        view.edit();
        expect(view.$('.mas-operationEditor--frozenParameter:contains(Attribute B)')).to.have.attr('data-value', 'attribute-b');
      });

      it('Displays the `name` of the operator if present in the operation description', function () {

        var view = new DefaultOperationView({
          model: {},
          operationDescription: operationDescriptionIndex.withStringParam
        });
        view.edit();

        expect(view.$('.mas-operation--edit.mas-operation--title')).to.contain('Operation (String)');
      });
    });

    describe('renderDisplayState()', function () {
      it('Displays the operator if no operation description is provided', function () {

        var operation = {
          operator: 'UNKNOWN_OPERATOR'
        };

        var view = new DefaultOperationView({
          model: operation
        });

        expect(view.$('.mas-operation--display.mas-operation--title')).to.contain('UNKNOWN_OPERATOR');
      });

      it('Displays the value of parameters listed in the operationDescriptions\' `displayParams`', function () {

        var operation = {
          parameters: {
            'param-1': '#1',
            'param-2': '#2',
            'param-3': '#3'
          }
        };

        var view = new DefaultOperationView({
          model: operation,
          operationDescription: operationDescriptionIndex.withDisplayedParameters
        });

        expect(view.$('.mas-operation--display.mas-operation--title')).to.contain('Operation (Displayed Parameters) (#1, #3)');
      });

      it('Displays the attribute name of the parameter if it comes from an attributeList', function () {
        var operation = {
          parameters: {
            attributeListParam: 'attribute-c'
          }
        };

        var view = new DefaultOperationView({
          model: operation,
          operationDescription: operationDescriptionIndex.withAttributeListParam,
          thread: thread
        });

        expect(view.$('.mas-operation--display.mas-operation--title')).to.contain('Operation (AttributeList) (Attribute C)');
      });
    });

    describe('updateOperation()', function () {

      it('Updates the parameter with selected attriubte if the type is `attributeList`', function () {
        var operation = {
          parameters: {
            attributeListParam: 'attribute-c'
          }
        };

        var view = new DefaultOperationView({
          model: operation,
          operationDescription: operationDescriptionIndex.withAttributeListParam,
          thread: thread
        });
        view.edit();

        view.$('.mas-propertySelector--attribute-a').click();
        view.updateOperation();

        expect(view.model.parameters.attributeListParam).to.equal('attribute-a');
      });

      it('Parses the parameter if the type is `float`', function () {

        var view = new DefaultOperationView({
          model: {},
          operationDescription: operationDescriptionIndex.withFloatParam
        });
        view.edit();

        view.$('[name=floatParam]').val(7.2);
        view.updateOperation();

        expect(Math.abs(view.model.parameters.floatParam - 7.2)).to.be.at.most(0.1);
      });

      it('Parses the parameter if the type is `int`', function () {
        var view = new DefaultOperationView({
          model: {},
          operationDescription: operationDescriptionIndex.withIntParam
        });
        view.edit();

        view.$('[name=intParam]').val(25);
        view.updateOperation();

        expect(view.model.parameters.intParam).to.equal(25);
      });

      it('Updates the operation with the parameter from the form', function () {

        var view = new DefaultOperationView({
          model: {},
          operationDescription: operationDescriptionIndex.withStringParam
        });
        view.edit();

        view.$('[name=stringParam]').val('new_value');
        view.updateOperation();

        expect(view.model.parameters.stringParam).to.equal('new_value');
      });
    });
  });
});
