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
  var MetricValueLabelView = require('weaver/views/MetricValueLabelView');

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

    this.view = new MetricValueLabelView({
      model: this.element,
      metric: this.metric
    });
  });

  describe('weaver/views/MetricValueLabelView.js', function () {

    it('Should be abbreviated by default', function () {
      expect(this.view.label.abbreviated).to.be.true;
    });

    it('Should correctly toggle abbreviation', function () {
      expect(this.view.label.abbreviated).to.be.true;
      this.view.label.toggleAbbreviation();
      expect(this.view.label.abbreviated).to.be.false;
      this.view.label.toggleAbbreviation();
      expect(this.view.label.abbreviated).to.be.true;
    });

  });

});