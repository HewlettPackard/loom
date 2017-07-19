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
  'lodash',
  'weft/models/Thread',
  'weft/models/Metric'
], function (_, Thread, Metric) {

  "use strict";

  describe('weft/models/Thread/AvailableSortsUpdater.js', function () {

    before(function () {

      this.metrics = _.times(4, function (index) {
        return new Metric({
          id: index,
          name: 'Metric #' + index
        });
      });


      this.thread = new Thread({
        itemMetrics: _.indexBy(this.metrics, 'id')
      });
    });

    it.skip('Should add corresponding sort option when a Metric gets displayed for the Thread', function () {

      this.thread.setDisplayedMetrics([
        this.metrics[0],
        this.metrics[2]
      ]);

      var sorts = this.thread.availableSorts;
      expect(sorts.size()).to.equal(2);
      expect(sorts.at(0).id).to.equal(0);
      expect(sorts.at(1).id).to.equal(2);
    });

    it.skip('Should remove corresponding sort options when a Metric gets removed', function () {

      this.thread.setDisplayedMetrics([
        this.metrics[2]
      ]);

      var sorts = this.thread.availableSorts;
      expect(sorts.size()).to.equal(1);
      expect(sorts.at(0).id).to.equal(2);

      this.thread.removeDisplayedMetric({
        id: 2
      });

      expect(sorts.size()).to.equal(0);
    });
  });
});