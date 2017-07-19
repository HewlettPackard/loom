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

  var ActionDefinition = require('weft/models/ActionDefinition');
  var ActionDialogView = require('weaver/views/Element/ActionDialogView');
  var Element = require('weft/models/Element');
  var AggregatorClient = require('weft/services/AggregatorClient');
  var confirm = require('weaver/utils/confirm');


  describe('weaver/views/Element/ActionDialogView.js', function () {

    beforeEach(function () {
      sinon.stub(confirm, 'confirm').returns({
        then: function (callback) {
          callback(confirm.confirm.value);
        }
      });
    });

    afterEach(function () {
      confirm.confirm.restore();
    });

    beforeEach(function () {

      this.aggregator = new AggregatorClient({
        url: '/warp'
      });

      this.element = new Element({
        'l.logicalId': 'element-id'
      });

      var stub = sinon.stub(this.element, 'getAggregator');
      stub.returns(this.aggregator);

      this.action = new ActionDefinition({
        id: 'actionId',
        name: "Action Name",
        description: "Description of the action",
        params: [{
          id: 'string_parameter',
          type: 'STRING',
          name: 'String parameter',
          range: {}
        }, {
          id: "enumerated_parameter",
          type: "ENUMERATED",
          range: {
            'option_a': "Option A",
            'option_b': "Option B"
          }
        }]
      });

      this.view = new ActionDialogView({
        model: this.action,
        element: this.element
      });

      document.body.appendChild(this.view.el);
    });

    afterEach(function () {

      this.view.remove();
    });

    it('Should display the name and description of the action', function () {

      expect(this.view.$('.mas-dialog--title')).to.have.text(this.action.get('name'));
      expect(this.view.$('.mas-dialog--body')).to.have.text(this.action.get('description'));
    });

    it('Should display the necessary inputs for the actions parameters', function () {

      expect(this.view.$('.mas-labeledInput-top .mas-formLabel')).to.have.text('String parameter');

      var $radios = this.view.$('.mas-fancyRadio');
      expect($radios.eq(0).find('.mas-fancyRadio--radio')).to.have.text('Option A');
      expect($radios.eq(1).find('.mas-fancyRadio--radio')).to.have.text('Option B');
    });

    it('Should execute the action with the filled in parameters', sinon.test(function () {

      // Need some stubbing to prevent async execution of promises
      this.stub(this.view, 'promptForConfirmation').returns({
        then: function (callback) {
          callback(true);
        }
      });

      this.view.$('.mas-labeledInput-top input').val('Filled');
      this.view.$('.mas-fancyRadio:last-child .mas-fancyRadio--input')[0].checked = true;

      this.view.$('.mas-dialog--submit').click();

      // Verifies that it sends the appropriate request, including:
      //  - prepending the aggregator URL
      //  - inserting the element's ID
      //  - appending the query string corresponding to the parameters
      var expectedURL = '/warp/actions';
      var expectedBody = {
        "id": "actionId",
        "targets": ['element-id'],
        "params": [{
          "id": "string_parameter",
          "value": "Filled"
        }, {
          "id": "enumerated_parameter",
          "value": "option_b"
        }]
      };

      expect(this.requests[0].url).to.equal(expectedURL);
      expect(JSON.parse(this.requests[0].requestBody)).to.deep.equal(expectedBody);

      expect(confirm.confirm).not.to.have.been.called;
    }));

    it('Should prompt user if the ActionDefinition contains a `confirm` field', sinon.test(function () {

      confirm.confirm.value = true;
      this.action.set('confirm', 'Are you sure you want to run this action?');

      this.view.$('.mas-labeledInput-top input').val('Filled');
      this.view.$('.mas-fancyRadio:last-child .mas-fancyRadio--input')[0].checked = true;

      this.view.$('.mas-dialog--submit').click();

      expect(confirm.confirm).to.have.been.called;
      expect(this.requests.length).to.equal(1, 'The request should have been sent');
    }));

    it('Should not execute action if user does not confirm action', sinon.test(function () {

      confirm.confirm.value = false;
      this.action.set('confirm', 'Are you sure you want to run this action?');

      this.view.$('.mas-labeledInput-top input').val('Filled');
      this.view.$('.mas-fancyRadio:last-child .mas-fancyRadio--input')[0].checked = true;

      this.view.$('.mas-dialog--submit').click();

      expect(confirm.confirm).to.have.been.called;
      expect(this.requests.length).to.equal(0, 'No request should have been sent');
    }));

  });
});
