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

  /**
   * Monitors the tapestry to keep the list of relation types up to date
   * @class RelationTypeUpdater
   * @constructor
   * @namespace services
   * @module  weft
   * @submodule services
   * @extends Backbone.Events
   * @param {models.Tapestry} tapestry The tapestry to monitor
   */
  function RelationTypeUpdater(tapestry) {
    this.listenTo(tapestry, 'syncComplete', this._reloadRelations);
  }

  _.extend(RelationTypeUpdater.prototype, Backbone.Events, {

    /**
     * Triggered when the 'syncComplete' event fires on the tapestry
     * @method _reloadRelations
     * @param tapestry
     * @private
     */
    _reloadRelations: function (tapestry) {
      tapestry.get('relationTypeList').fetch();
    }
  });

  return RelationTypeUpdater;
});