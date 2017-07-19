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
  var $ = require('jquery');
  var BaseView = require('../../../BaseView');

  /**
   * PatternController is responsible for displaying whichever pattern gets chosen from the PatternSelectionMenu
   * @backbone no-initialize
   * @class  PatternController
   * @namespace  screens.TapestryScreen
   * @module weaver
   * @submodule screens.TapestryScreen
   * @constructor
   * @extends BaseView
   */
  var PatternController = BaseView.extend({

    /**
     * The collection of Threads to add the Threads to display in
     * @property {Backbone.Collection} model
     */

    events: {
      'click .mas-pattern:not(.is-inTapestry)': function (event) {
        var $target = $(event.currentTarget);
        var pattern = $target.data('pattern');
        if (!pattern) {
          console.warn('pattern not found', event, event.target);
          return;
        }
        this.displayPattern(pattern);
      }
    },

    /**
     * @method displayPattern
     * @param pattern
     */
    displayPattern: function (pattern) {
      _.forEach(pattern.getMissingThreads(this.model), function (thread) {
        var clone = thread.clone();
        this.options.braidingController.get('threads').add(clone);
        this.model.add(clone);
      }, this);
    }
  });

  return PatternController;
});
