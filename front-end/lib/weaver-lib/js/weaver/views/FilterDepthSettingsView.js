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

  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');
  var template = require('./FilterDepthSettingsView.html');

  /**
   * @class FilterDepthSettingsView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var FilterDepthSettingsView = BaseView.extend({

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'change': function (event) {
        this.model.set('depth', parseInt(event.target.value, 10) || undefined);
      },
      'submit': function (event) {
        event.preventDefault();
      }
    },

    /**
     * @property {FilterService} model The FilterService whose depth is controlled by this view
     */

    /**
     * The template used to help structuring the view
     * @property template
     * @type {String}
     */
    template: template,

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.render();
    },

    /**
     * @method render
     */
    render: function () {
      this._renderTitle();
      this._renderDepth(this.model && this.model.get('depth'));
    },

    /**
     * @method _renderTitle
     * @private
     */
    _renderTitle: function () {
      this.$('.mas-filterDepthSettings--title').text(this.options.title);
    },

    /**
     * @method _renderDepth
     * @param depth
     * @private
     */
    _renderDepth: function (depth) {
      if (depth) {
        this.$('[value=' + depth + ']').prop('checked', true);
      } else {
        this.$('[value=all]').prop('checked', true);
      }
    }
  });

  return FilterDepthSettingsView;
});
