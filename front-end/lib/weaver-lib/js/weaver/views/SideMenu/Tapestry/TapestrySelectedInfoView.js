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
define(function () {

  "use strict";

  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');
  var template = require('weaver/views/SideMenu/SideMenuSelectedInfoView.html');

  /**
   * TapestrySelectedInfoView displays information about the (un)/selected fiber in the side menu
   * @class TapestrySelectedInfoView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var TapestrySelectedInfoView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_TapestrySelectedInfoView
     * @final
     */
    constructorName: 'LOOM_TapestrySelectedInfoView',

    /**
     * @property tagName
     * @type {String}
     */
    template: template,

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-tapestrySelectedInfoView',

    /**
     * model: list of providers from the aggregator
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.listenTo(this.model, 'change', function() {
        this.updateConnectedProvidersInfo();
      });
      this.updateConnectedProvidersInfo();
      this.render();
    },

    updateConnectedProvidersInfo: function() {
      var loggedInCount = this.model && this.model.where({'loggedIn': true}). length;
      if (!loggedInCount || loggedInCount === 0) {
        this.setTitle('Connected to');
        this.setDetail('None');
      } else {
        this.setTitle('Connected to');
        this.setDetail(loggedInCount + (loggedInCount < 2 ? ' Provider' : ' Providers') );
      }
    },

    /**
     * Set the title of the element
     * @method setTitle
     * @param title
     */
    setTitle: function(title) {
      this.$('.mas-sideMenuSelectedInfoView--title').text(title);
    },

    /**
     * Set the detail of the element
     * @method setDetail
     * @param detail
     */
    setDetail: function(detail) {
      this.$('.mas-sideMenuSelectedInfoView--detail').text(detail);
    }

  });

  return TapestrySelectedInfoView;
});
