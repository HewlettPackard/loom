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
  var BaseView = require('./../BaseView');
  var Operation = require('weft/models/Operation');

  /**
   * View displaying the title of a Thread using the appropriate format
   * @class  ThreadTitleView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ThreadTitleView = BaseView.extend({

    /**
     * The thread whose title this ThreadTitleView displays
     * @property {models.Thread} model
     */

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ThreadTitleView
     * @final
     */
    constructorName: 'LOOM_ThreadTitleView',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-threadTitle mas-titleInContext',

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      if (this.model) {
        this.render();
      }
    },

    /**
     * @method render
     */
    render: function () {
      this.el.innerHTML = this._getTitle();
    },

    /**
     * @method _getTitle
     * @returns {*}
     * @private
     */
    _getTitle: function () {
      if (this.model.get('aggregation') && this.model.get('aggregation').get('l.tags') === Operation.GROUP_BY_ID) {
        return this._getGroupTitle();
      }
      return '<span class="mas-titleInContext--title">' + this._getName() + '</span>';
    },

    /**
     * @method _getGroupTitle
     * @returns {string}
     * @private
     */
    _getGroupTitle: function () {
      var parent = this.model.get('parent');
      if (parent) {
        var attribute = this.model.getLastParentGrouping();
        var propertyName = attribute.name;
        var unit = attribute.unit;
        // TODO: Create helper formatting method
        if (unit) {
          unit = '&hairsp;' + unit;
        }
        return '<span class="mas-titleInContext--context">' + (propertyName || attribute.id) + '</span><span class="mas-titleInContex--separator">:&nbsp;</span><span class="mas-titleInContext--title">' + this._getName() + (unit || '') + '</span>';
      }
    },

    /**
     * @method _getName
     * @returns {*}
     * @private
     */
    _getName: function () {
      return this.model.get('name');
    }
    
  });

  return ThreadTitleView;

});
