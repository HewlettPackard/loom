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

  var $ = require('jquery');
  var _ = require('lodash');
  var PropertySelector = require('weaver/views/PropertySelector');
  var Metric = require('weft/models/Metric');

  describe('weaver/views/PropertySelector.js', function () {

    before(function () {

      this.properties = {
        'aProperty': 'Its human readable version',
        'anotherProperty': 'Easy to read for a human',
        'yetAnotherProperty': 'With its own name too',
        'property/withaSlash': 'Property with a slash'
      };

      this.newProperties = {
        'aNewProperty': 'A new name',
        'anotherNewProperty': 'Another name'
      };

      this.metrics = {
        'aMetric': new Metric({
          'id': 'aMetric',
          'name': 'Human readable name for the metric'
        }),
        'anotherMetric': new Metric({
          'id': 'anotherMetric',
          'name': 'Another human readable name'
        })
      };
    });

    describe('model', function () {

      it('Should display the properties provided as the model', function () {

        var propertySelector = new PropertySelector({
          model: this.properties
        });

        var $properties = propertySelector.$el.find('.mas-property');
        expect($properties.length).to.equal(_.size(this.properties));
        var index = 0;
        _.forEach(this.properties, function (propertyName) {

          var $element = $properties.eq(index);
          expect($element.html()).to.equal(propertyName);
          index ++;
        });
      });

      it('Should use the `name` property if model contains object', function () {

        var propertySelector = new PropertySelector({
          model: this.metrics
        });

        _.forEach(this.metrics, function (metric, metricID) {

          var $element = propertySelector.$('.mas-propertySelector--' + metricID);
          expect($element.html()).to.equal(metric.get('name'));
        });
      });
    });

    describe('options.title', function () {

      it('Should display given title when set', function () {

        var propertySelector = new PropertySelector({
          model: this.properties,
          title: 'First title'
        });

        var title = propertySelector.$('.mas-propertySelector--title');
        expect(title).to.have.text('First title');

        propertySelector.setTitle('New title');
        expect(title).to.have.text('New title');

        propertySelector.setTitle(undefined);
        expect(title).to.have.text('');
      });
    });

    describe('options.blankLabel', function () {

      it('Should display given label when set', function () {

        var label = 'None';

        var propertySelector = new PropertySelector({
          blankLabel: label,
          model: this.properties
        });

        var $blankElement = propertySelector.$('.mas-property-blank');
        expect($blankElement).to.exist;
        expect($blankElement).to.have.class('mas-property-selected');
        expect($blankElement.html()).to.equal(label);
      });

      it('Should cancel selection when clicked', function () {

        var propertySelector = new PropertySelector({
          blankLabel: 'None',
          model: this.properties
        });

        propertySelector.select('aProperty');

        var $blankElement = propertySelector.$('.mas-property-blank');
        $blankElement.click();
        expect($blankElement).to.have.class('mas-property-selected');
        expect(propertySelector.getSelection()).to.be.undefined;
      });
    });

    describe('setProperties()', function () {

      beforeEach(function () {
        this.propertySelector = new PropertySelector({
          model: this.properties
        });
      });

      it('Should display the new list of properties', function () {

        var $oldPropertiesElements = this.propertySelector.$('.mas-property');

        this.propertySelector.setProperties(this.newProperties);

        // Elements corresponding to old properties should have been removed
        _.forEach($oldPropertiesElements, function (oldPropertyElement) {

          expect(oldPropertyElement.parentElement).not.to.be.ok;
        }, this);

        var $properties = this.propertySelector.$('.mas-property');

        expect($properties.length).to.equal(_.size(this.newProperties));
        var index = 0;
        _.forEach(this.newProperties, function (propertyName) {

          var $element = $properties.eq(index);
          expect($element.html()).to.equal(propertyName);
          index ++;
        });
      });

      it('Should keep current selection when properties get updated', function () {

        this.propertySelector.select('aProperty');

        this.propertySelector.setProperties(_.omit(this.properties, 'anotherProperty'));

        expect(this.propertySelector.options.selection).to.equal('aProperty');
        expect(this.propertySelector.$('.mas-propertySelector--aProperty')).to.have.class('mas-property-selected');
      });

      it('Should remove property from selection if it is not in the updated properties', function () {

        this.propertySelector.select('aProperty');

        this.propertySelector.setProperties(_.omit(this.properties, 'aProperty'));

        expect(this.propertySelector.options.selection).to.be.undefined;
      });

      it('Should remove properties from selection when they\'re not in the updated properties', function () {

        var propertySelector = new PropertySelector({
          model: this.properties,
          multiple: true
        });

        propertySelector.select('aProperty');
        propertySelector.select('anotherProperty');
        propertySelector.select('yetAnotherProperty');

        propertySelector.setProperties(_.omit(this.properties, 'aProperty', 'yetAnotherProperty'));

        expect(propertySelector.options.selection).to.have.length(1);
        expect(propertySelector.options.selection).to.contain('anotherProperty');
      });
    });

    describe('select()', function () {

      beforeEach(function () {

        this.propertySelector = new PropertySelector({
          model: this.properties
        });
      });

      it('Should trigger a `change:selection` event', function (done) {

        var propertyID = 'a-property';

        this.propertySelector.on('change:selection', function (selection) {

          expect(selection).to.equal(propertyID);
          done();
        }, this);

        this.propertySelector.select('a-property');
      });
    });

    describe('`Events : `click`', function () {

      before(function () {

        this.propertySelector = new PropertySelector({
          model: this.properties
        });

        document.body.appendChild(this.propertySelector.el);

        this.propertyID = 'aProperty';
        this.$propertyElement = this.propertySelector.$('.mas-propertySelector--' + this.propertyID);

        this.anotherPropertyID = 'anotherProperty';
        this.$anotherPropertyElement = this.propertySelector.$('.mas-propertySelector--' + this.anotherPropertyID);

        this.propertyWithSlashID = 'property/withaSlash';
        this.$propertyWithSlash = this.propertySelector.$('.mas-property').last();
      });

      after(function () {

        document.body.removeChild(this.propertySelector.el);
      });

      it('Should select the property that was clicked', function () {

        this.$propertyElement.click();

        expect(this.propertySelector.getSelection()).to.equal(this.propertyID);
        expect(this.$propertyElement).to.have.class('mas-property-selected');
      });

      it('Should select the new property that was clicked', function () {

        this.$anotherPropertyElement.click();
        expect(this.propertySelector.getSelection()).to.equal(this.anotherPropertyID);
        expect(this.$propertyElement).not.to.have.class('mas-property-selected');
        expect(this.$anotherPropertyElement).to.have.class('mas-property-selected');
      });

      it('Should cancel selection if it is selected property that was clicked', function () {

        this.$anotherPropertyElement.click();
        expect(this.propertySelector.getSelection()).to.be.undefined;
        expect(this.$anotherPropertyElement).not.to.have.class('mas-property-selected');
      });

      it('Should allow the selection of properties with `/` in their ID', function () {

        this.$propertyWithSlash.click();

        expect(this.propertySelector.getSelection()).to.equal(this.propertyWithSlashID);
        expect(this.$propertyWithSlash).to.have.class('mas-property-selected');
      });
    });

    describe('option.multiple', function () {

      before(function () {

        this.properties = {
          'aProperty': 'Its human readable version',
          'anotherProperty': 'Easy to read for a human',
          'yetAnotherProperty': 'With its own name too'
        };

        this.propertySelector = new PropertySelector({
          model: this.properties,
          blankLabel: 'None',
          multiple: true
        });

        document.body.appendChild(this.propertySelector.el);

        this.propertyID = 'aProperty';
        this.$propertyElement = this.propertySelector.$('.mas-propertySelector--' + this.propertyID);

        this.anotherPropertyID = 'anotherProperty';
        this.$anotherPropertyElement = this.propertySelector.$('.mas-propertySelector--' + this.anotherPropertyID);
      });

      after(function () {

        document.body.removeChild(this.propertySelector.el);
      });

      describe('Events: `click`', function () {

        before(function () {

          this.selectedProperties = ['aProperty', 'anotherProperty'];

          this.$propertyElement.click();
          this.$anotherPropertyElement.click();
        });

        it('Should clear highlighting on the blank label once a element is selected', function () {

          var $blankLabel = this.propertySelector.$('.mas-property-blank');
          expect($blankLabel).not.to.have.class('mas-property-selected');
        });

        it('Should add elements to the selection rather than replace it', function () {

          var selection = this.propertySelector.getSelection();

          expect(selection).to.have.length(2);
          _.forEach(this.selectedProperties, function (property) {

            expect(selection).to.contain(property);
            expect(this.propertySelector.$('.mas-propertySelector--' + property)).to.have.class('mas-property-selected');
          }, this);
        });

        it('Should remove element from selection when element is already selected', function () {

          this.$propertyElement.click();

          var selection = this.propertySelector.getSelection();

          expect(selection).to.have.length(1);
          expect(selection[0]).to.equal(this.anotherPropertyID);
          expect(this.$propertyElement).not.to.have.class('mas-property-selected');
        });

        it('Should highlight the blank label once all elements get deselected', function () {

          this.$anotherPropertyElement.click();

          var $blankLabel = this.propertySelector.$('.mas-property-blank');
          expect($blankLabel).to.have.class('mas-property-selected');
        });

        it('Should clear selection if clicking the blank option', function () {

          this.$propertyElement.click();
          this.$anotherPropertyElement.click();

          var $blankLabel = this.propertySelector.$('.mas-property-blank');
          $blankLabel.click();
          expect(this.$propertyElement).not.to.have.class('mas-property-selected');
          expect(this.$anotherPropertyElement).not.to.have.class('mas-property-selected');
        });
      });

      describe('unselect()', function () {

        beforeEach(function () {
          var selectedProperties = ['aProperty', 'property/withaSlash'];

          _.forEach(selectedProperties, function (property) {

            this.propertySelector.select(property);
          }, this);
        });

        it('Should remove elements from the selection rather than clearing it', function () {

          var property = 'property/withaSlash';
          this.propertySelector.unselect(property);

          var selection = this.propertySelector.getSelection();
          expect(selection).to.have.length(1);

          expect(selection[0]).to.equal('aProperty');
          expect($(this.propertySelector.propertiesElements[property])).not.to.have.class('mas-property-selected');
        });
      });
    });
  });
});
