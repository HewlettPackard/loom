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

  var Backbone = require('backbone');

  /**
   * PatternInTapestryMarker monitors a list of Pattern and a Tapestry
   * to mark the Patterns whose Threads are currently in the Tapestry
   * @class PatternInTapestryMarker
   * @namespace views.ThreadViewHeader
   * @module weaver
   * @submodule views.ThreadViewHeader
   * @constructor
   * @extends Backbone.Model
   */
  var PatternInTapestryMarker = Backbone.Model.extend({

    /**
     * @method initialize
     */
    initialize: function () {
      this.listenTo(this.get('tapestry').get('threads'), 'add remove', this.refresh);
      this.refresh();
    },

    /**
     * Refreshes the flags on patterns
     * @method refresh
     * @return {[type]} [description]
     */
    refresh: function () {
      var threads = this.get('tapestry').get('threads');
      this.get('patterns').forEach(function (pattern) {
        pattern.set('isInTapestry', pattern.hasAllThreadsIn(threads));
      });
    }
  });

  return PatternInTapestryMarker;
});
