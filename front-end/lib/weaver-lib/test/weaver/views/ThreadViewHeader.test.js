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

  "use strict";

  var _ = require('lodash');
  var Backbone = require('backbone');
  var Metric = require('weft/models/Metric');
  var Item = require('weft/models/Item');
  var Aggregation = require('weft/models/Aggregation');
  var Thread = require('weft/models/Thread');
  var ThreadViewHeader = require('weaver/views/ThreadViewHeader/ThreadViewHeader');
  var Operation = require('weft/models/Operation');

  describe('weaver/views/ThreadViewHeader.js', function () {

    beforeEach(function () {

      this.metrics = {
        'a-metric': new Metric({
          'id': 'a-metric',
          'name': 'A Metric'
        }),
        'another-metric': new Metric({
          'id': 'another-metric',
          'name': 'Another metric'
        })
      };

      var itemElements = this.itemElements = [];
      _.times(4, function () {
        itemElements.push(new Item());
      });

      var threadElements = this.threadElements = [];
      _.times(4, function () {
        threadElements.push(new Aggregation({
          'l.tags': Operation.GROUP_BY_ID
        }));
      });

      var clusterElements = this.clusterElements = [];
      _.times(4, function () {
        clusterElements.push(new Aggregation({
          'l.tags': Operation.BRAID_ID
        }));
      });

      this.thread = new Thread({
        elements: new Backbone.Collection(itemElements),
        itemGroupBy: {
          'id': 'ID',
          'name': 'Name',
          'a-property': 'A property',
          'another-property': 'Another Property',
          'group-by-only': 'Group by only property'
        },
        itemSortBy: {
          'id': 'ID',
          'name': 'Name',
          'a-property': 'A property',
          'another-property': 'Another Property',
          'sort-by-only': 'Sort by only property'
        },
        itemMetrics: this.metrics
      });

      this.threadWithNoPropertiesList = new Thread({});

      this.threadViewHeader = new ThreadViewHeader({
        model: this.thread
      });
      this.threadViewHeader.$el.appendTo(document.body);
    });

    afterEach(function () {

      this.threadViewHeader.remove();
    });

    describe('Title', function () {

      it('Should display the name of the Thread it is displaying', function () {

        expect(this.threadViewHeader.$('.mas-threadHeader--title')).to.contain(this.thread.get('name'));
      });

      it('Should display the name of the metric if it is set to display a metric', function () {

        var metric = this.thread.get('itemMetrics')['a-metric'];
        var threadViewHeaderWithMetric = new ThreadViewHeader({
          model: this.thread,
          metric: metric
        });

        expect(threadViewHeaderWithMetric.$el).to.have.class('mas-threadHeader-metric');
        expect(threadViewHeaderWithMetric.$('.mas-threadHeader--title')).to.contain(metric.get('name'));
      });
    });

    describe.skip('Displaying the type of elements that are displayed', function () {

      it('Should have a specific class according to the type of content of the Thread', function () {

        this.thread.resetElements(this.itemElements);

        expect(this.threadViewHeader.$el).to.have.class('mas-threadHeader-displayingItems');
        expect(this.threadViewHeader.$el).not.to.have.class('mas-threadHeader-displayingGroups');
        expect(this.threadViewHeader.$el).not.to.have.class('mas-threadHeader-displayingClusters');

        this.thread.resetElements(this.threadElements);

        expect(this.threadViewHeader.$el).not.to.have.class('mas-threadHeader-displayingItems');
        expect(this.threadViewHeader.$el).to.have.class('mas-threadHeader-displayingGroups');
        expect(this.threadViewHeader.$el).not.to.have.class('mas-threadHeader-displayingClusters');

        this.thread.resetElements(this.clusterElements);

        expect(this.threadViewHeader.$el).not.to.have.class('mas-threadHeader-displayingItems');
        expect(this.threadViewHeader.$el).not.to.have.class('mas-threadHeader-displayingGroups');
        expect(this.threadViewHeader.$el).to.have.class('mas-threadHeader-displayingClusters');
      });
    });
  });
});
