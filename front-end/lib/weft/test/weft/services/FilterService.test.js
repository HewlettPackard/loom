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
  var Item = require('weft/models/Item');
  var AggregatorClient  = require('weft/services/AggregatorClient');
  var FilterService = require('weft/services/FilterService');
  var Tapestry = require('weft/models/Tapestry');
  var Thread = require('weft/models/Thread');
  var uuid = require('uuid');

  function setup() {

    this.elements = _.times(8, function () {
      return new Item({
        // Avoid sharing indices
        'l.logicalId': uuid.v1() + ''
      }, {});
    });

    // Let's make these elements related
    // 0 — 1 — 2
    // |   |   |
    // 3 — 4 — 5
    this.elements[0].addRelation(this.elements[1]);
    this.elements[2].addRelation(this.elements[1]);
    this.elements[4].addRelation(this.elements[1]);
    this.elements[4].addRelation(this.elements[3]);
    this.elements[4].addRelation(this.elements[5]);
    this.elements[0].addRelation(this.elements[3]);
    this.elements[2].addRelation(this.elements[5]);

    this.service = new FilterService();

    this.expectMarkingFilterFlag = function (indices, marked) {

      _.forEach(indices, function (index) {

        if (marked) {
          expect(this.elements[index].get('isMatchingFilter')).to.equal(true, index + '');
        } else {
          expect(this.elements[index].get('isMatchingFilter')).to.equal(false, index);
        }
      }, this);
    };
  }

  describe('weft/services/FilterService.js', function () {

    describe('Filtering on changes', function () {
      beforeEach(setup);

      it('Should mark elements related to current filters', function () {

        this.service.addFilter(this.elements[1]);

        expect(this.elements[1].get('isPartOfFilter')).to.be.true;

        // Related elements should be flagged
        this.expectMarkingFilterFlag([0, 2, 4], true);
        // Element acting as filter should be flagged too
        this.expectMarkingFilterFlag([1], true);
        // Other elements shouldn't
        this.expectMarkingFilterFlag([3, 5], false);

        this.service.addFilter(this.elements[5]);
        this.service.addFilter(this.elements[3]);

        this.expectMarkingFilterFlag([4], true);
        this.expectMarkingFilterFlag([1, 3, 5], true);
        this.expectMarkingFilterFlag([0, 2], false);

        this.service.removeFilter(this.elements[5]);

        this.expectMarkingFilterFlag([0, 4], true);
        this.expectMarkingFilterFlag([1, 3], true);
        this.expectMarkingFilterFlag([2, 5], false);

        this.service.removeFilter(this.elements[3]);
        this.service.removeFilter(this.elements[1]);

        this.expectMarkingFilterFlag([0, 1, 2, 3, 4, 5], false);
      });


      it('Should update marking when an element gets related to one of the elements in the filter', function () {

        this.service.addFilter(this.elements[1]);

        this.elements[1].addRelation(this.elements[3]);

        this.expectMarkingFilterFlag([3], true);

        this.elements[5].addRelation(this.elements[1]);

        this.expectMarkingFilterFlag([5], true);
      });

      it('Should update marking when an element stops being related to one of the elements in the filter', function () {

        this.service.addFilter(this.elements[1]);

        this.elements[1].removeRelation(this.elements[2]);

        this.expectMarkingFilterFlag([2], false);

        this.elements[4].removeRelation(this.elements[1]);

        this.expectMarkingFilterFlag([4], false);
      });

      it('Should update marking properly after an element gets added to the filter', function () {

        this.service.addFilter(this.elements[1]);

        this.elements[1].addRelation(this.elements[3]);

        this.service.setFilter([this.elements[5]]);

        this.expectMarkingFilterFlag([3], false);
      });
    });

    describe('activate()/deactivate()', function () {

      before(setup);

      it('Should clear marking when deactivated', function () {

        this.service.addFilter(this.elements[1]);
        this.expectMarkingFilterFlag([0, 2, 4], true);
        this.service.deactivate();

        // Removes marking when deactivated
        this.expectMarkingFilterFlag([0, 1, 2, 3, 4, 5], false);
      });

      it('Should ignore changes while deactivated', function () {

        // Ignores mark changes when deactivated
        this.service.addFilter(this.elements[5]);
        this.expectMarkingFilterFlag([0, 1, 2, 3, 4, 5], false);
      });

      it('Should refresh marking upon activation', function () {

        // Marks elements when activated again
        this.service.activate();
        this.expectMarkingFilterFlag([1, 2, 5, 4], true);
        this.expectMarkingFilterFlag([0, 3], false);
      });
    });

    describe('relationType', function () {

      beforeEach(function () {

        setup.apply(this);

        var tapestry = new Tapestry([], {
          aggregator: new AggregatorClient()
        });
        var index = tapestry.get('fibersIndex').get('index');

        _.forEach(this.elements, function (el) {
          el.itemType = {
            getVisibleAttributes: function () { return []; },
            attributes: {}
          };
          index.add(el);
          el.set('parent', new Thread());
          tapestry.add(el.get('parent'));
          el.get('parent').set('itemType', el.itemType);
        });

        // Let's throw in different type of relations
        // 0 - 1 - 2
        // |   !   |
        // 3 = 4 - 5
        this.elements[4].itemType.attributes = {
          '3:4:=': {},
          '1:4:!': {},
        };
        this.elements[4].get('l.relationTypes')['3:4:='] = [this.elements[3].get('l.logicalId')];
        this.elements[4].get('l.relationTypes')['1:4:!'] = [this.elements[1].get('l.logicalId')];

        this.service.set('relationType', '=');
        this.service.addFilter(this.elements[4]);
      });

      it('Should only mark the elements of given relation type', function () {

        this.expectMarkingFilterFlag([0, 1, 2, 5], false);
        this.expectMarkingFilterFlag([3, 4], true);
      });

      it('Should update marked elements when relationType changes', function () {

        this.service.set('relationType', '!');

        this.expectMarkingFilterFlag([0, 2, 3, 5], false);
        this.expectMarkingFilterFlag([1, 4], true);
      });

      it('Should update when elements of filter get updated relation types', function () {

        this.elements[4].set('l.relationTypes', {
          '3:4:=': [
            this.elements[1].get('l.logicalId'),
            this.elements[5].get('l.logicalId')
          ]
        });
        this.expectMarkingFilterFlag([0, 2, 3], false);
        this.expectMarkingFilterFlag([1, 4, 5], true);
      });

      it('Should update when a relation is added to the filter', function () {

        // Add two new relations, one which has a specific type
        // the other which hasn't
        this.elements[4].set('l.relationTypes', {
          '3:4:=': [
            this.elements[3].get('l.logicalId'),
            this.elements[6].get('l.logicalId')
          ]
        });

        this.elements[4].addRelation(this.elements[6]);
        this.elements[4].addRelation(this.elements[7]);

        this.expectMarkingFilterFlag([0, 1, 2, 5, 7], false);
        this.expectMarkingFilterFlag([3, 4, 6], true);
      });
    });
  });

});
