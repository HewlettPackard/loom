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
  /** @type BaseView */
  var BaseView = require('./BaseView');
  var formattedAttributeValue = require('weaver/views/helpers/formattedAttributeValue');

  /**
   * @class FiberAttributeView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var FiberAttributeView = BaseView.extend({

    /**
     * @property template
     * @type {String}
     */
    template: require('./FiberAttributeView.html'),

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.listenTo(this.model, 'change:' + this.options.attribute.id, _.bind(this.updateValue, this));
      this.render();
    },

    /**
     * @method render
     */
    render: function () {
      BaseView.prototype.render.apply(this, arguments);
      this.updateTitle();
      this.updateValue();
    },

    /**
     * @method updateTitle
     */
    updateTitle: function () {
      this.$('.mas-sideMenuListItemName').html(this.options.attribute.name);
    },

    /**
     * @method updateValue
     */
    updateValue: function () {
      var propertyValue = this.model.get(this.options.attribute.id);
      if (_.isObject(propertyValue)) {
        //app urls hard coded for demo
        if (this.options.attribute.id === 'core.applicationUrls') {
          propertyValue = this._makeUrlList(propertyValue);
        } else {
          propertyValue = JSON.stringify(propertyValue);
        }
      } else {
        propertyValue = formattedAttributeValue(this.model, this.options.attribute.id);
      }
      this.$('.mas-sideMenuListItemValue').html(propertyValue.toString());
    },

    /**
     * @method _makeUrlList
     * @param list
     * @returns {string}
     * @private
     */
    _makeUrlList: function (list) {
      var htmlList = '<ul class="mas-elementDetails--urlList">';
      _.forEach(list, function (item) {
        htmlList += ('<li><a href=' + item.value + ' target="_blank" >' + item.key + '</a></li>');
      });
      htmlList += ('</ul>');
      return htmlList;
    }
  });

  return FiberAttributeView;

});
