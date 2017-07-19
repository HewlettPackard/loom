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

  var Item = require('weft/models/Item');
  var ElementDetailsView = require('weaver/views/Element/ElementDetailsView');

  describe('weaver/views/ElementDetailsView.js', function () {

    beforeEach(function () {

      this.model = new Item({
        'name': 'The name',
        "string.Property": 'String property',
        "numberProperty": 10,
        "booleanProperty": false,
        "urlProperty": 'http://hello.world',
        'aProperty': 'someValue',
        'anotherProperty': null,
        'aPropertyNotDisplayed': 'aValueThatWontAppear',
        attributes: this.itemProperties
      }, {
        itemType: {
          attributes: {
            "name": {
              name: 'Name',
              visible: true
            },
            "string.Property": {
              name: 'String property',
              visible: true
            },
            "numberProperty": {
              name: 'Number property',
              visible: true
            },
            "booleanProperty": {
              name: 'Boolean property',
              visible: true
            },
            "urlProperty": {
              name: 'Url property',
              visible: true
            }
          },
          actions: {
            item: [],
            aggregation: []
          }
        }
      });


      this.view = new ElementDetailsView({
        model: this.model
      });
    });

    it('Should display the name as the title', function () {
      expect(this.view.$el.find('.mas-elementDetails--title').find('p').html()).to.equal(this.model.get('name'));
    });

    it('Should display the attributes declared as visible by the Element ItemType', function () {

      var expectedDisplayedAttributes = [{
        "value": "The name",
        "label": "Name"
      }, {
        "value": "String property",
        "label": "String property"
      }, {
        "value": '10',
        "label": "Number property"
      }, {
        "value": 'false',
        "label": "Boolean property"
      }, {
        "value": "http://hello.world",
        "label": "Url property"
      }];

      var $propertyLabels = this.view.$el.find('.mas-sideMenuListItemName');
      var $propertyValues = this.view.$el.find('.mas-sideMenuListItemValue');

      expect($propertyLabels.length).to.equal(expectedDisplayedAttributes.length);
      expect($propertyValues.length).to.equal(expectedDisplayedAttributes.length);

      expectedDisplayedAttributes.forEach(function (expectation, index) {
        expect($propertyLabels.eq(index).text()).to.equal(expectation.label);
        expect($propertyValues.eq(index).text()).to.equal(expectation.value);
      });
    });

    describe('Enabling/disabling actions', function () {

      it('Should disable actions when an element is outdated and enable them when the element is no longer outdated', function () {

        var element = new Item({
          outdated: true
        });

        var view = new ElementDetailsView({
          model: element
        });

        expect(view.$('.mas-action')).to.have.attr('disabled');

        element.set('outdated', false);

        expect(view.$('.mas-action')).not.to.have.attr('disabled');
      });

      it('Should disable actions when an element gets outdated', function () {

        var element = new Item({
          outdated: false
        });

        var view = new ElementDetailsView({
          model: element
        });

        expect(view.$('.mas-action')).not.to.have.attr('disabled');

        element.set('outdated', true);

        expect(view.$('.mas-action')).to.have.attr('disabled');
      });
    });

    describe('Reaction to model updates', function () {

      it('Should update displayed properties when model properties change', function () {

        this.model.set('string.Property', 'aNewValue');
        var $propertyValue = this.view.$el.find("[data-attribute='string.Property'] .mas-sideMenuListItemValue");
        expect($propertyValue).to.have.text('aNewValue');
      });
    });

    describe('Displaying alerts', function () {

      it('Should have a specific class when there is no alerts', function () {

        expect(this.view.$el).to.have.class('mas-elementDetails-noAlert');

        this.model.alert.set('level', 25);

        expect(this.view.$el).not.to.have.class('mas-elementDetails-noAlert');

        this.model.alert.set('level', 0);

        expect(this.view.$el).to.have.class('mas-elementDetails-noAlert');
      });
    });
  });
});
