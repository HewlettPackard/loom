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

  var Q = require('q');
  var Backbone = require('backbone');
  var _ = require('lodash');

  /**
   * FibersLinker ensures a set of Elements are correctly linked
   * to each other according to their 'l.relations' property
   * @class FibersLinker
   * @namespace services
   * @module weft
   * @submodule services
   * @extends Backbone.Model
   */
  var FibersLinker = Backbone.Model.extend({

    initialize: function () {
      this.listenTo(this.get('fibers'), 'add', this._buildRelations);
      this.listenTo(this.get('fibers'), 'change:l.relations', this._buildRelations);
      this.listenTo(this.get('fibers'), 'remove', this._removeRelations);
    },

    /**
     * @method _buildRelations
     * @param fiber
     * @private
     */
    _buildRelations: function (fiber) {
      this._lookUpRelations(fiber.get('l.relations'))
        .then(function (relatedFibers) {
          fiber.updateRelations(relatedFibers);
        })
        .done();
    },

    /**
     * @method _lookUpRelations
     * @param relatedFibersIds
     * @returns {*|jQuery.promise|promise.promise|(function(string=, Object=): JQueryPromise<any>)|(function(any=): JQueryPromise<T>)|r.promise}
     * @private
     */
    _lookUpRelations: function (relatedFibersIds) {
      var deferred = Q.defer();
      var relatedFibers = [];
      var fibers = this.get('fibers');
      _.forEach(relatedFibersIds, function (fiberId) {
        var relatedFiber;// = fibers.get(fiberId);
        _.forEach(fibers.models, function (value) {
          if (value.get('l.logicalId') === fiberId) {
            relatedFiber = value;
          }
        });
        if (relatedFiber) {
          relatedFibers.push(relatedFiber);
        }
      });
      deferred.resolve(relatedFibers);
      return deferred.promise;
    },

    /**
     * @method _removeRelations
     * @param fiber
     * @private
     */
    _removeRelations: function (fiber) {
      fiber.breakRelations();
    }
  });

  return FibersLinker;
});
