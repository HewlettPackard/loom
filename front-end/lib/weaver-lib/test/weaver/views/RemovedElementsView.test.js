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
define([
  'weft/models/RemovedElementsCounter',
  'weaver/views/ThreadView/RemovedElementsView'
], function (RemovedElementsCounter, RemovedElementsView) {

  'use strict';

  describe('weaver/views/ThreadView/RemovedElementsView.js', function () {

    it('Should keep up to date with the counter value', sinon.test(function () {

      var duration = 4;
      var counter = new RemovedElementsCounter();
      counter.increment(8, duration);

      var view = new RemovedElementsView({
        model: counter
      });

      expect(view.$el).to.have.text('8');

      this.clock.tick(duration / 2);

      counter.increment(8, duration);

      expect(view.$el).to.have.text('16');

      this.clock.tick(duration / 2);
      expect(view.$el).to.have.text('8');

      counter.reset();
      expect(view.$el).to.have.text('0');
    }));
  });
});
