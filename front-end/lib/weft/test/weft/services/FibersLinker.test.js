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
  var FibersLinker = require('weft/services/FibersLinker');
  var uuid = require('uuid');

  describe('weft/services/FibersLinker.js', function () {

    var mockItemType = {
      id: 't-12'
    };

    before(function () {
      var fibers = _.times(5, function () {
        return new Item({
          'l.logicalId': uuid.v1(),

        }, {
          itemType: mockItemType
        });
      });

      this.fibers = new Backbone.Collection(fibers);

      this.fiberId = function (index) {
        return this.fibers.at(index).get('l.logicalId');
      }.bind(this);
    });

    beforeEach(function () {

      this.fibersLinker = new FibersLinker({
        fibers: this.fibers
      });

      this.extraFiber = new Item({
        'l.logicalId': uuid.v1(),
        'l.relations': [1, 3].map(this.fiberId)
      }, {
        itemType: mockItemType
      });
    });

    it('Creates appropriate relations when a Fiber gets added to the list it monitors', function (done) {

      var expectedIds = [1, 3].map(this.fiberId);

      this.extraFiber.on('add:relations', function doValidate() {
        if (_.keys(this.get('relations')).length === 2) {
          expect(_.pluck(this.get('relations'), 'id')).to.deep.equal(expectedIds);
          this.off('add:relations', doValidate);
          done();
        }
      });

      this.fibers.add(this.extraFiber);
    });

    it('Breaks appropriate relations when a Fiber gets removed from the list it monitors', function (done) {

      var removedFiber = this.fibers.first();
      var relatedFibers = [this.fibers.at(1), this.fibers.at(3)];

      removedFiber.updateRelations(relatedFibers);

      removedFiber.on('remove:relations', function doValidate() {
        if (_.keys(this.get('relations')).length === 0) {
          relatedFibers.forEach(function (relatedFiber) {
            expect(relatedFiber.isRelatedTo(removedFiber)).to.be.false;
          });
          this.off('remove:relations', doValidate);
          done();
        }
      });

      this.fibers.remove(removedFiber);
    });

    it('Updates relations when a Fiber gets a new list of relations', function (done) {

      var updatedFiber = this.fibers.first();
      var initialRelatedFibers = [this.fibers.at(1), this.fibers.at(3)];

      updatedFiber.updateRelations(initialRelatedFibers);

      var updatedRelatedFibersIds = [2, 3, 4].map(this.fiberId);

      // Prevents done to be called multiple times
      var validated = false;
      updatedFiber.on('add:relations', function doValidate() {

        if (!validated && _.keys(this.get('relations')).length === 3) {
          validated = true;
          expect(_.pluck(this.get('relations'), 'id')).to.have.members(updatedRelatedFibersIds);
          updatedFiber.off('add:relations', doValidate);
          done();
        }
      });

      updatedFiber.set('l.relations', updatedRelatedFibersIds);
    });
  });

});
