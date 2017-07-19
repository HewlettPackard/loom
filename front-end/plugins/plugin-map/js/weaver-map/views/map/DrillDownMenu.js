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

  var d3 = require('d3');
  var $ = require('jquery');
  var CssClassHelper = require('./CssClassHelper');
  var CustomEventDispatcher = require('weaver/views/CustomEventDispatcher');

  var DisplayMode = require('plugins/common/utils/DisplayMode');

  var template = require('./DrillDownMenu.html');

  var DrillDownMenu = CustomEventDispatcher.extend({

    events: {
      'click .fa-globe': function (event) {
        this.model.get('parent').trigger('drilldown:map', this.model);
        this.__dispatchEvent(event, "drilldown:map");
      },
      'click .fa-th-list': function (event) {
        this.model.get('parent').trigger('drilldown:classic', this.model);
        this.__dispatchEvent(event, "drilldown:classic");
      }
    },

    initialize: function () {
      var $dom = $(template);
      this.setElement($dom[0]);

      //this.$el.data(args.data);
      this.$el.data('view', this);

      this._initListClass(d3.select(this.el));
      this._fillWithExistingClasses();

      switch (this.model.get('displayMode')) {
      case DisplayMode.MAP:
        this.$('.fa-globe').addClass('active');
        break;
      case DisplayMode.CLASSIC:
        this.$('.fa-th-list').addClass('active');
        break;
      default:
      }

      this._addClass('mas-isHidden');
    },

    show: function (globalX, globalY) {
      if (this.attached) {
        this._removeClass('mas-isHidden');
        this.$el.css('left', globalX - this.attached.offsetLeft);
        this.$el.css('top', globalY - this.attached.offsetTop);
      }
    },

    attachToElement: function (jqueryElement) {
      jqueryElement.append(this.$el);
      var offset = jqueryElement.offset();
      this.attached = {
        offsetTop: offset.top,
        offsetLeft: offset.left
      };
    },

  });

  CssClassHelper.assignTo(DrillDownMenu);

  return DrillDownMenu;

});
