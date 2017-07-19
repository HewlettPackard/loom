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
  'weft/models/Metric',
  'weft/models/Sort',
  'weaver/views/SortView'
], function (Metric, Sort, SortView) {

  "use strict";

  describe('weaver/views/SortView.js', function () {

    function simulateClick(element) {

      var event = document.createEvent('MouseEvent');
      event.initEvent('click', true, true);
      element.dispatchEvent(event);
    }

    beforeEach(function () {

      var metric = new Metric({
        id: 'cpuload',
        name: 'CPU load'
      });

      this.sort = new Sort({
        property: metric,
        order: Sort.ORDER_ASCENDING
      });

      this.view = new SortView({
        model: this.sort
      });
    });

    it('Should display the name of the property used for sorting', function () {

      expect(this.view.$el).to.contain('CPU load');
      expect(this.view.$el).to.have.class('mas-sortView-ascending');
    });

    it('Should reverse sorting order when clicked', function () {

      simulateClick(this.view.el);
      expect(this.sort.get('order')).to.equal(Sort.ORDER_DESCENDING);
      expect(this.view.$el).to.have.class('mas-sortView-descending');
    });
  });
});
