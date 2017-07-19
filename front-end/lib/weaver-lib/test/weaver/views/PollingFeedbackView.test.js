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
/*global describe, it, expect, beforeEach */
/*jshint expr: true, strict: false */
define([
  'weft/models/Thread',
  'weaver/views/PollingFeedbackView'
], function (Thread, PollingFeedbackView) {

  describe('weaver/views/PollingFeedbackView.js', function () {

    beforeEach(function () {

      this.thread = new Thread();
      this.view = new PollingFeedbackView({
        model: this.thread
      });
    });

    it('Should display wether a Thread is polled or not', function () {

      this.thread.set('polled', 'polling');
      expect(this.view.$el).to.have.class('mas-pollingFeedback-polling');

      this.thread.set('polled', 'success');
      expect(this.view.$el).not.to.have.class('mas-pollingFeedback-polling');
    });

    it('Should display if a Thread poll has failed', function () {

      this.thread.set('polled', 'polling');
      expect(this.view.$el).to.have.class('mas-pollingFeedback-polling');

      this.thread.set('polled', 'failed');
      expect(this.view.$el).to.have.class('mas-pollingFeedback-failed');
    });
  });
});