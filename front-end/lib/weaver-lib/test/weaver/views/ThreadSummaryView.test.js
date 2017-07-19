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
/* global describe, it, sinon, expect */
/* jshint expr: true, strict: false */
define([
  'weft/models/Thread',
  'weaver/views/ThreadView/ThreadSummaryView'
], function (Thread, ThreadSummaryView) {

  'use strict';

  describe('weaver/views/ThreadSummaryView.js', function () {

    it('Should display the number of items and the number of alerts', function () {

      var thread = new Thread();
      thread.get('result').set('pending', false);
      sinon.stub(thread, 'getSummary').returns({
        numberOfItems: 10,
        numberOfAlerts: 50
      });

      var view = new ThreadSummaryView({
        model: thread
      });

      expect(view.$numberOfItems).to.have.text('10 items');
      expect(view.$numberOfAlerts).to.have.text('50');
    });

    it('Should display when the query is pending', function () {

      var thread = new Thread();
      thread.get('result').set('pending', true);
      sinon.stub(thread, 'getSummary').returns({
        numberOfItems: 1,
        numberOfAlerts: 50
      });
      var view = new ThreadSummaryView({
        model: thread
      });
      expect(view.$el).to.have.class('mas-threadSummary-pending');
    });
  });


});