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

  var FilterService = require('weft/services/FilterService');
  var PrimaryFilterService = require('weaver/services/PrimaryFilterService');
  var RelationshipHighlightFilterService = require('weaver/services/RelationshipHighlightFilterService');
  var EventBus = require('weaver/utils/EventBus');

  describe('weaver/services/PrimaryFilterService.js', function () {
    describe('defaults', function () {
      it('Should contain FilterService default properties', function () {
        var service = new PrimaryFilterService();
        for (var item in FilterService.prototype.defaults()) {
          expect(service.attributes).to.have.property(item);
        }
      });
    });
    describe('EventBus', function () {
      describe('RelationshipHighlightFilterService.RelationTypeChangeEvent', function() {
        it('Should change the relationType', function () {
          var newValue = 'SOME_NEW_VALUE';
          this.service = new PrimaryFilterService();
          EventBus.trigger(RelationshipHighlightFilterService.RelationTypeChangeEvent, newValue);
          expect(this.service.get('relationType')).to.equal(newValue);
        });
      });
    });
  });
});
