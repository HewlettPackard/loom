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
/* global describe, it, sinon, expect, beforeEach, afterEach */
/* jshint expr: true */
define(function (require) {
  "use strict";

  var _ = require('lodash');
  var Operation = require('weft/models/Operation');
  var ItemType = require('weft/models/ItemType');
  var Thread = require('weft/models/Thread');
  var Query = require('weft/models/Query');
  var QueryValidatorMap = require('weaver-map/models/QueryValidatorMap');
  var MetaThread = require('weaver-map/models/MetaThread');

  describe('QueryValidatorMap', function () {

    beforeEach(function () {

      var operations = {};
      operations[Operation.GRID_CLUSTERING_ID] = ['lon', 'lat'];
      operations[Operation.FILTER_BY_REGION_ID] =  ['lon', 'lat'];
      operations[Operation.KMEANS_ID] = ['lon', 'lat'];
      operations[Operation.POLYGON_CLUSTERING_ID] = ['lon', 'lat'];


      // ... a Thread ...
      this.thread = new Thread({
        id: 'thread',
        name: 'Thread',
        displayMode: 'map',
        itemType: new ItemType({
          attributes: {
            'name': {
              'name': 'Name',
              visible: true
            },
            'lon': {
              'name': 'Coord Longitude',
              visible: true,
              plottable: true
            },
            'lat': {
              'name': 'Coord Latitude',
              visible: true,
              plottable: true
            },
          },
          operations: operations,
          orderedAttributes: [
            'name',
            'lon',
            'lat'
          ],
          geoAttributes: [{
            latitude: 'lat',
            longitude: 'lon',
          }],
        })
      });

      this.fakeMapViewElement = {
        cid: _.uniqueId(),
        getD3Element: function () { return {}; },
        $el: {
          off: _.noop,
          on: _.noop,
        },
      };

      this.thread = new MetaThread({
        thread: this.thread
      });

      this.thread.setMap(this.fakeMapViewElement);


    });

    afterEach(function () {

    });

    describe('initialize()', function () {

/*      it("Should add FILTER_BY_REGION operation and GRID_CLUSTERING operation as they're available", function () {

        this.queryValidator = new QueryValidatorMap({
          map: this.fakeMapViewElement,
          thread: this.thread,
        });

        expect(this.thread.hasLimit(Operation.GRID_CLUSTERING_ID)).to.be.true;
        expect(this.thread.hasOperation(Operation.FILTER_BY_REGION_ID)).to.be.true;
      });*/

      it("Should add GROUP_BY country because there's no operations on the pipeline", function () {

        this.queryValidator = new QueryValidatorMap({
          map: this.fakeMapViewElement,
          thread: this.thread,
        });

        expect(this.thread.hasOperation({
          operator: Operation.GROUP_BY_ID,
          parameters: {
            property: 'country'
          }
        })).to.be.true;
      });

      it("Should do nothing when a pipeline is already present: come from a Pattern or from a Clone action", function () {

        this.thread.pushOperation({
          operator: Operation.GROUP_BY_ID,
          parameters: {
            property: 'country',
          }
        });

        var spy = sinon.spy();

        this.thread.on('change:query', spy);

        this.queryValidator = new QueryValidatorMap({
          map: this.fakeMapViewElement,
          thread: this.thread,
        });

        expect(spy).not.to.have.been.called;
        expect(this.thread.hasLimit(Operation.GRID_CLUSTERING_ID)).to.be.false;
        expect(this.thread.hasOperation(Operation.FILTER_BY_REGION_ID)).to.be.false;
      });
    });

    describe('validateQuery()', function () {

      beforeEach(function () {
        this.thread.pushOperation({
          operator: 'FAKE_OP'
        });
        this.queryValidator = new QueryValidatorMap({
          map: this.fakeMapViewElement,
          thread: this.thread,
        });
      });

      it('Should remove filter by region when grouping by country', function () {

        var query = this.thread.get('query');

        expect(query.hasOperation(Operation.FILTER_BY_REGION_ID)).to.be.true;
        expect(query.hasLimit(Operation.GRID_CLUSTERING_ID)).to.be.true;

        query = query.pushOperation({
          operator: Operation.GROUP_BY_ID,
          parameters: {
            property: 'country'
          }
        });
        query = this.queryValidator.validateQuery(query);

        expect(query.hasOperation(Operation.FILTER_BY_REGION_ID)).to.be.false;
        expect(query.hasLimit(Operation.GRID_CLUSTERING_ID)).to.be.false;
      });

      it("Should update the query as necessary (in this case shouldn't)", function () {

        var query = this.queryValidator.validateQuery(this.thread.get('query'));

        expect(query.attributes).to.deep.equal(this.thread.get('query').attributes);
        expect(query.hasOperation(Operation.FILTER_BY_REGION_ID)).to.be.true;
      });

      it('Should add a braid if the query is empty', function () {

        this.queryValidator.filterByRegion = undefined;
        var query = this.queryValidator.validateQuery(new Query());

        expect(query.hasLimit(Operation.BRAID_ID)).to.be.true;
      });

      it("Should update the query as necessary (in this case should)", function () {

        this.thread.removeOperations(Operation.FILTER_BY_REGION_ID);

        var query = this.queryValidator.validateQuery(this.thread.get('query'));

        expect(query.attributes).not.to.equal(this.thread.get('query').attributes);
        expect(query.hasOperation(Operation.FILTER_BY_REGION_ID)).to.be.true;
      });
    });
  });

});