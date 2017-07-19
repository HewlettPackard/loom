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

  var $ = require('jquery');
  var BaseView = require('weaver/views/BaseView');
  var template = require('./PatternList.html');

  /**
   * @class PatternList
   * @module weaver
   * @submodule views.patterns
   * @namespace  views.patterns
   * @constructor
   * @extends BaseView
   */
  var PatternList = BaseView.extend({

    /**
     * @property template
     * @type {String}
     * @final
     */
    template: template,

    /**
     * options.model comes from AggregatorClient.availablePatterns {Backbone.Collection}
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.render();
      this.listenTo(this.model, 'add remove change', this.render);
    },

    /**
     * @method render
     */
    render: function () {
      this.$('.mas-patternList--list').empty().append(this.model.map(this.renderPattern, this));
    },

    /**
     * @method renderPattern
     * @param pattern
     * @returns {JQuery|jQuery|HTMLElement}
     */
    renderPattern: function (pattern) {
      var $li = $('<li class="mas-sideMenuListItem mas-pattern">');
      $li.addClass('mas-pattern-' + pattern.id);
      $li.text(pattern.get('name') || pattern.id);
      $li.data('pattern', pattern);
      $li.toggleClass('is-inTapestry', !!pattern.get('isInTapestry'));
      if (!!pattern.get('isInTapestry')) {
        $li.append('<div class="mas-toggle mas-toggle-enabled"><div class="mas-toggle-switch">&nbsp;</div></div>');
      } else {
        $li.append('<div class="mas-toggle"><div class="mas-toggle-switch">&nbsp;</div></div>');
      }
      return $li;
    }
  });

  return PatternList;
});
