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
define([
  'weft/models/Metric'
], function (Metric) {

  'use strict';

  describe('weft/models/Metric.js', function () {

    describe('dynamicRange', function () {

      it('Should be true if the maximum bound is `Inf`', function () {

        var metric = new Metric({
          min: 0,
          max: 'Inf'
        });

        expect(metric.get('dynamicRange')).to.equal('history');
      });
    });

    describe('normalise()', function () {

      beforeEach(function () {

        this.metric = new Metric({
          min: 2,
          max: 10
        });
      });

      it('Should return the normalised value, according to the bounds of the metric', function () {

        expect(this.metric.normalise(4)).to.equal(0.25);
      });

      it('Should normalise each value in an array', function () {

        expect(this.metric.normalise([4,6,8])).to.deep.equal([0.25, 0.5, 0.75]);
      });

      it('Should return 1 if value is above the upper bound', function () {

        expect(this.metric.normalise(12)).to.equal(1);
      });

      it('Should return 0 if value is below the lower bound', function () {

        expect(this.metric.normalise(1)).to.equal(0);
      });

      it('Should return 0 if the value is null or undefined', function () {

        expect(this.metric.normalise(undefined)).to.equal(0);
        expect(this.metric.normalise(null)).to.equal(0);
      });

      it('Should return 1 if min and max have the same value, unless they are 0', function () {

        this.metric.set('min', 25);
        this.metric.set('max', 25);

        expect(this.metric.normalise(1)).to.equal(1);
        expect(this.metric.normalise(25)).to.equal(1);
        expect(this.metric.normalise(40)).to.equal(1);

        this.metric.set('min', 0);
        this.metric.set('max', 0);

        expect(this.metric.normalise(0)).to.equal(0);
        expect(this.metric.normalise(1)).to.equal(0);
      });

      it('Should normalise values appropriately when some are negative', function () {

        this.metric.set('min', -6);

        expect(this.metric.normalise(-2)).to.equal(0.25);
        expect(this.metric.normalise(-8)).to.equal(0);
        expect(this.metric.normalise(12)).to.equal(1);
        expect(this.metric.normalise(6)).to.equal(0.75);
      });
    });
  });
});
