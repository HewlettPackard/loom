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

  'use strict';

  var _ = require('lodash');
  var Aggregation = require('weft/models/Aggregation');
  var ClusterLabellingStrategy = require('weaver/views/ThreadView/Elements/ClusterLabellingStrategy');
  var ElementView = require('weaver/views/Element/ElementView');

  // Some automated tests
  describe('weaver/views/ThreadViewElements/ClusterLabellingStrategy.js', function () {

    function createViews(numberOfElements, itemsPerElements, container) {

      var element;
      var views = [];
      var startIndex = 0;

      _.times(numberOfElements, function (index) {

        element = new Aggregation({
          'l.logicalId': 'thread-' + index,
          name: 'Thread #' + index,
          'l.tags': 'braid',
          minIndex: startIndex,
          maxIndex: startIndex + itemsPerElements - 1
        });

        var view = new ElementView({
          model: element
        });

        startIndex += itemsPerElements;

        if (container) {

          container.appendChild(view.el);
        }

        views[index] = view;
      });

      return views;
    }

    describe('updateLabels()', function () {

      beforeEach(function () {

        this.labellingStrategy = new ClusterLabellingStrategy({
          maxNumberOfLabels: 6
        });

        this.container = document.createElement('div');
        document.body.appendChild(this.container);
      });

      afterEach(function () {

        document.body.removeChild(this.container);
      });

      it('Should change the labels to numbers', function () {

        var views = createViews(3, 2, this.container);
        this.labellingStrategy.updateLabels(views);

        expect(views[0].$('.mas-fiberOverview--label')).to.have.text('0');
        expect(views[1].$('.mas-fiberOverview--label')).to.have.text('2');
        expect(views[2].$('.mas-fiberOverview--label')).to.have.text('4');
      });

      it('Should format the labels to human readable values (k, M, G...)', function () {

        var views = createViews(5, 2000, this.container);
        this.labellingStrategy.updateLabels(views);

        expect(views[0].$('.mas-fiberOverview--label')).to.have.text('0');
        expect(views[1].$('.mas-fiberOverview--label')).to.have.text('2.5k');
        expect(views[2].$('.mas-fiberOverview--label')).to.have.text('5k');
        expect(views[3].$('.mas-fiberOverview--label')).to.have.text('7.5k');
      });

      it('Should shift the labels according to position of the value in the range of the Thread', function () {

        this.labellingStrategy.options.maxNumberOfLabels = 2;
        var views = createViews(5, 20, this.container);

        this.labellingStrategy.updateLabels(views);

        var position = parseFloat(views[2].$('.mas-fiberOverview--label')[0].style.left);
        expect(position).to.be.closeTo(52.63, 0.01);
      });
    });
  });
});
