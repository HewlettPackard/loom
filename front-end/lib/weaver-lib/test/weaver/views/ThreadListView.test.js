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
  var Item = require('weft/models/Item');
  var Thread = require('weft/models/Thread');
  var ItemType = require('weft/models/ItemType');
  var ThreadListView = require('weaver/views/ThreadList/ThreadListView');
  var AggregatorClient = require('weft/services/AggregatorClient');
  var ProviderLegendService = require('weft/services/ProvidersLegendService');
  var ThreadMonitorService = require('weft/services/ThreadMonitorService');
  var FilterService = require('weft/services/FilterService');
  var BraidingController = require('weft/models/Tapestry/BraidingController');

  // HELPER FUNCTIONS
  function selectElementScenario() {
    this.selection = this.elements[0][1];
    this.view.selectElement(this.selection);
    expect(this.selection.get('selected')).to.be.true;
  }

  describe('weaver/views/ThreadList/ThreadListView.js', function () {

    // To test the ThreadListView, we need...
    beforeEach(function () {

      this.itemType = new ItemType({
        attributes: {},
        operations: {}
      });

      // ... a couple of threads that will be displayed in the view...
      this.threads = [new Thread({
        id: 'thread-1',
        itemType: this.itemType
      }), new Thread({
        id: 'thread-2',
        itemType: this.itemType
      })];

      // ... and a view...
      this.view = new ThreadListView({
        model: new Backbone.Collection(),
        filterService: new FilterService(),
        providerLegendService: new ProviderLegendService(),
        braidingController: new BraidingController()
      });

      // ... as well as a couple of elements for each thread
      var elements = this.elements = [];
      _.times(2, function (threadIndex) {

        var threadElement = elements[threadIndex] = [];
        _.times(6, function (elementIndex) {

          threadElement.push(new Item({
            id: 'item-' + threadIndex + '-' + elementIndex,
            itemType: {
              attributes: {},
              operations: {}
            }
          }));
        });
      }, this);
    });

    describe('displayThreads()', function () {

      beforeEach(function () {

        this.monitor = new ThreadMonitorService({
          aggregator: new AggregatorClient()
        });
        this.view.displayThreads(this.threads, this.monitor);
      });

      it('Should display provided list of threads', function () {

        var $threads = this.view.$('.mas-threadView');

        expect($threads.length).to.equal(2);
        expect($threads.eq(0).data('view').model.id).to.equal('thread-1');
        expect($threads.eq(1).data('view').model.id).to.equal('thread-2');
      });

      it('Should remove legacy threads and add the new threads in the appropriate position', function () {

        var newThreads = [new Thread({
          id: 'thread-3',
          itemType: this.itemType
        }),
        this.threads[0]];

        this.view.displayThreads(newThreads, this.monitor);

        var $threads = this.view.$('.mas-threadView');
        expect($threads.length).to.equal(2);
        expect($threads.eq(0).data('view').model.id).to.equal('thread-3');
        expect($threads.eq(1).data('view').model.id).to.equal('thread-1');
      });
    });

    describe('clear()', function () {

      beforeEach(function () {
        this.view.displayThreads(this.threads);
      });

      it('Should remove all Threads displayed in the ThreadListView', function () {

        this.view.clear();
        expect(this.view.model.size()).to.equal(0);
        expect(this.view.$('.mas-threadView').length).to.equal(0);
      });
    });

    describe('Model events handling', function () {

      it('Should display a Thread when a thread is added to the collection', function () {

        this.view.model.add(this.threads[0]);

        expect(this.view.$el).to.have.descendants('.mas-thread');
      });
    });

    describe('selectElement()', function () {

      it('Should mark provided element as selected', selectElementScenario);

      it('Should unmark previously provided element and set new element as seleted', function () {

        selectElementScenario.apply(this);

        this.newSelection = this.elements[0][2];

        this.view.selectElement(this.newSelection);

        expect(this.selection.get('selected')).to.be.false;
        expect(this.newSelection.get('selected')).to.be.true;
      });
    });

    describe('unselectElement()', function () {

      beforeEach(function () {

        this.selection = this.elements[0][1];
        this.view.selectElement(this.selection);
        expect(this.selection.get('selected')).to.be.true;
      });

      it('Should unmark provided element as seleted', function () {

        this.view.unselectElement(this.selection);
        expect(this.selection.get('selected')).to.be.false;
      });
    });

    describe('filter events', function () {

      it('Should add filter class to the treadListView', function () {

        this.view.addFilterElement(this.elements[0][1]);
        expect(this.view.$el).to.have.class('mas-filter-on');

      });


      it('Should remove filter class to the treadListView', function () {

        this.view.options.filterService.get('filters').add(this.elements[0][1]);
        this.view.removeFilterElement(this.elements[0][1]);
        expect(this.view.$el).to.not.have.class('mas-filter-on');

      });


      it('Should add filter to filterService', function () {

        this.view.addFilterElement(this.elements[0][1]);

        expect(this.view.options.filterService.get('filters').length).to.equal(1);

      });

      it('Should remove filter from filterService', function () {

        this.view.options.filterService.get('filters').add(this.elements[0][1]);

        this.view.removeFilterElement(this.elements[0][1]);

        expect(this.view.options.filterService.get('filters').length).to.equal(0);

      });


    });

  });


});
