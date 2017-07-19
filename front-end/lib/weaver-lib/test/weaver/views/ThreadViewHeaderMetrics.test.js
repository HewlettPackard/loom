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
/* global describe, it, expect, beforeEach*/
/* jshint expr: true, strict: false */
define(function (require)  {

  "use strict";

  var _ = require('lodash');
  var Thread = require('weft/models/Thread');
  var Metric = require('weft/models/Metric');
  var ThreadViewHeaderMetrics = require('weaver/views/ThreadViewHeaderMetrics');

  describe('weaver/views/ThreadViewHeaderMetrics.js', function () {
    beforeEach(function () {
      this.metrics = _.times(3, function (index) {
        return new Metric({
          id: index,
          name: 'Metric #' + index
        });
      });
      this.thread = new Thread({});
      this.thread.get('metrics').add(this.metrics[1]);
    });
    it('Should list the metrics displayed by the Thread', function () {
      var view = new ThreadViewHeaderMetrics({
        model: this.thread
      });
      var $HTMLElements = view.$('.mas-threadMetrics--metric');
      expect($HTMLElements.length).to.equal(1);
      expect($HTMLElements).to.contain('Metric #1');
    });
    it('Should add a new metric when a metric is added to the Thread', function () {
      var view = new ThreadViewHeaderMetrics({
        model: this.thread
      });
      this.thread.get('metrics').add(this.metrics[0]);
      var $HTMLElements = view.$('.mas-threadMetrics--metric');
      expect($HTMLElements.length).to.equal(2);
      expect($HTMLElements).to.contain('Metric #1');
      expect($HTMLElements).to.contain('Metric #0');
    });
  });
});