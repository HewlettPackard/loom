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
  var _ = require('lodash');
  require('lodash.extensions');

  var ThreadView = require('weaver/views/ThreadView');

  var ThreadMapShadowView;
  require(["./ThreadMapShadowView"], function (V) {
    // Delayed loading
    ThreadMapShadowView = V;
  });
  var DrillDownMenu = require('./DrillDownMenu');
  var DisplayMode = require('plugins/common/utils/DisplayMode');

  var ThreadViewAdapted = ThreadView.extend({

    events: _.defaults({

      'didRemoveThread': _.fwdEventOnPreventDefault(function () {

        // If the subthread has been removed.
        if (!this.subThread) {
          this.model.set('focus', this.model.get('focus') || this.subThreadFocus);
        }
      }),

      'click .mas-action--view': _.fwdEventOnPreventDefault(function (event) {
        this._showMenu($(event.target).data('aggregation'), event);
      }),

      'drilldown:map': _.fwdEventOnPreventDefault(function (event) {
        this._removeMenu();
      }),

      'drilldown:classic': _.fwdEventOnPreventDefault(function (event) {
        this._removeMenu();
      }),
    }, ThreadView.prototype.events),

    initialize: function (args) {

      this.parentThread = args.parent;

      if (args.map) {

        this.map = args.map;
        this.initializeWhenMapIsReady();
      }

      ThreadView.prototype.initialize.apply(this, arguments);

      this._attachEvents();
    },

    initializeWhenMapIsReady: _.noop,

    _removeBeforeModelClear: function () {

      // Give focus back to parent.
      if (this.parentThread) {
        this.parentThread.subThreadFocus = this.model.get('focus');
      }

      if (this.model.get('aggregation')) {
        // Remove display status from aggregation.
        this.model.get('aggregation').set('displayMode', undefined);
      }
    },

    _attachEvents: function () {

      if (this.model) {
        this.stopListening(this.model);
        this._initializeModelEvents();
        this.stickit();
      }

      this.listenTo(this.model, 'drilldown:map', function (aggregation) {
        this._drillDownOnMap(aggregation);
      });

      this.listenTo(this.model, 'drilldown:classic', function (aggregation) {
        this._drillDownOnSubThread(aggregation);
      });

      this.listenTo(this.model, 'change:focus', function (model, hasfocus) {
        if (hasfocus) {
          this._dispatchEventToParent('focus:up');
          this._dispatchEventToChild('focus:down');
        }
      });

      this.listenTo(this, 'focus:up', function () {
        this.model.set('focus', false);
        this._dispatchEventToParent('focus:up');
      });

      this.listenTo(this, 'focus:down', function () {
        this.model.set('focus', false);
        this._dispatchEventToChild('focus:down');
      });

      this.listenTo(this.model, 'didPressActionView', this._showMenu);
      this.listenTo(this.model, 'didRemoveDetailsView', this._removeMenu);
    },

    _dispatchEventToChild: function (event, args) {
      if (this.subThread) {
        this.subThread.trigger(event, args);
      }
    },

    _dispatchEventToParent: function (event, args) {
      if (this.parentThread) {
        this.parentThread.trigger(event, args);
      }
    },

    _showMenu: function (aggregation, event) {
      if (this.drillDownMenu) {
        this._removeMenu();
      } else {
        var menu = new DrillDownMenu({
          model: aggregation,
        });
        var target = $(event.target);
        var outerHeight = target.outerHeight();
        var offset = target.offset();
        menu.attachToElement(this.$el);
        menu.show(offset.left, offset.top + outerHeight);

        this.drillDownMenu = menu;
        this.drillDownMenu.$el.focus();
        var self = this;
        var cid = this.drillDownMenu.cid;

        // Note: The fail here is that the handler is only
        // removed when you don't try to keep clicking on
        // the same element
        $('html').on('click.' + cid, function (evt) {
          if (!$(evt.target).is('.mas-action--view')) {
            $('html').off('click.' + cid);
            self._removeMenu();
          }
        });
      }
    },

    _createSubThreadView: function (thread) {

      switch (thread.get('displayMode')) {
      case DisplayMode.MAP:
        return new ThreadMapShadowView({
          parent: this,
          model: thread,
          map: this.map,
          providerLegendService: this.options.providerLegendService
        });
      case DisplayMode.CLASSIC:
        return new ThreadViewAdapted({
          parent: this,
          model: thread,
          map: this.map,
          providerLegendService: this.options.providerLegendService
        });
      }

      return ThreadView.prototype._createSubThreadView.apply(this, arguments);
    },

    _removeMenu: function () {
      if (this.drillDownMenu) {
        this.drillDownMenu.remove();
        this.drillDownMenu = undefined;
      }
    },

    _drillDownOnMap: function (aggregation) {
      this._toggleThreadDisplay(aggregation, DisplayMode.MAP);
    },

    _drillDownOnSubThread: function (aggregation) {
      this._toggleThreadDisplay(aggregation, DisplayMode.CLASSIC);
    },
  });

  return ThreadViewAdapted;
});
