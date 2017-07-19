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
  'use strict';

  var Menu = require('weaver/views/Menu');
  var DisplayMode = require('plugins/common/utils/DisplayMode');

  var MapMenu = Menu.extend({

    className: Menu.prototype.className + ' mas-mapMenu is-collapsed',

    bindings: {
      ':el': {
        classes: {
          'is-active': {
            observe: 'distorted',
            onGet: function (distorted) {
              return distorted;
            }
          }
        }
      }
    },

    initialize: function () {
      Menu.prototype.initialize.apply(this, arguments);
      this.render();
    },

    render: function () {
      Menu.prototype.render.apply(this, arguments);

      this.stickit();

      if (this.model.get('displayMode') !== DisplayMode.MAP) {
        this.$el.addClass('mas-is-visibility-hidden');
      }
    },

    toggle: function () {

      if (!this.model.get('distorted')) {

        this.model.set('distorted', true);
        this._dispatchEvent('action-distort');

      } else {

        this.model.set('distorted', false);
        this._dispatchEvent('action-distort');
      }
    },


  });

  return MapMenu;
});
