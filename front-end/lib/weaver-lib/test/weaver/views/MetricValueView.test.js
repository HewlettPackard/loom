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
/* global describe, it, expect, beforeEach */
/* jshint expr: true */
define(function (require) {

  'use strict';

  var Thread = require('weft/models/Thread');
  var Element = require('weft/models/Element');
  var Metric = require('weft/models/Metric');
  var MetricValueView = require('weaver/views/MetricValueView');

  beforeEach(function () {

    this.metric = new Metric({
      id: 'some-metric',
      name: 'Some metric',
      unit: 'Mb',
      min: 0,
      max: 10
    });

    this.element = new Element({
      parent: new Thread(),
      'some-metric': 3
    });

    this.element.itemType = {
      getVisibleAttributes: function () {
        return [];
      }
    };

    this.view = new MetricValueView({
      model: this.element,
      metric: this.metric
    });
  });

  describe('weaver/views/MetricValueView.js', function () {

    it('Should scale the histogram bar according to the normalized value of the metric', function () {

      var $graphBar = this.view.$('.mas-graph--value');
      expect($graphBar).to.have.attr('height', '30%');
    });

    it('Should scale the histogram to 1 when value of metric is 0', function () {
      this.element.set('some-metric', 0);
      var $graphBar = this.view.$('.mas-graph--value');
      expect($graphBar).to.have.attr('height', '1px');
    });

    it('Should update the scale of the histogram when the metric changes on the model', function () {

      this.element.set('some-metric', 8);

      var $graphBar = this.view.$('.mas-graph--value');
      expect($graphBar).to.have.attr('height', '80%');
    });

    it('Should have a minimum value if the metric value is below a given threshold', function () {

      this.element.set('some-metric', 0.0045);

      var $graphBar = this.view.$('.mas-graph--value');
      expect($graphBar).to.have.attr('height', '1px');
    });

    it('Should display the actual value of the metric, with its unit as a label', function () {

      expect(this.view.$('.mas-metricValue--label').text()).to.equal('3 Mb');
    });

    it('Should update the label when the metric changes on the model', function () {

      this.element.set('some-metric', 8);

      expect(this.view.$('.mas-metricValue--label').text()).to.equal('8 Mb');
    });

    it('Should display `???` when the metric had no value', function () {

      this.element.set('some-metric', undefined);
      expect(this.view.$('.mas-metricValue--label').html()).to.equal('???');
    });

    it('Should set a special class when the metric has no value', function () {

      this.element.set('some-metric', undefined);
      expect(this.view.$el).to.have.class('mas-metricValue-noValue');

      this.element.set('some-metric', 8);
      expect(this.view.$el).not.to.have.class('mas-metricValue-noValue');
    });

    // Will need to adjust that to match the new behaviour of properties :s
    describe.skip('Metric history', function () {

      beforeEach(function () {
        this.view.metricHistoryFeature = true;
      });

      it('Should display the metric history when the element is selected', function () {

        var $graphBar = this.view.$('.mas-graph--value');
        expect($graphBar.length).to.equal(1);

        this.element.set('selected', true);

        this.element.set('some-metric', 8);

        $graphBar = this.view.$el.find('.mas-graph--value');
        expect($graphBar.length).to.equal(2);
        expect($graphBar.eq(0)).to.have.attr('height', '30%');
        expect($graphBar.eq(0)).to.have.attr('width', '10%');
        expect($graphBar.eq(0)).to.have.attr('x', '80%');
        expect($graphBar.eq(1)).to.have.attr('height', '80%');
        expect($graphBar.eq(0)).to.have.attr('width', '10%');
        expect($graphBar.eq(0)).to.have.attr('x', '90%');

        this.element.set('selected', false);

        $graphBar = this.view.$('.mas-graph--value');
        expect($graphBar.length).to.equal(1);
        expect($graphBar).to.have.attr('height', '80%');
        expect($graphBar).to.have.attr('width', '100%');
      });
    });
  });


});