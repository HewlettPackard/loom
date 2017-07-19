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
  require('lodash.extensions');

  var ThreadViewAdapted = require('./ThreadViewAdapted');
  var AggregationItemManager = require('./AggregationItemManager');
  var RemovedElementsView = require('weaver/views/RemovedElementsView');
  var QueryAutoUpdaterMapOperations = require('weaver-map/models/QueryAutoUpdaterMapOperations');

  var ThreadMapShadowView = ThreadViewAdapted.extend({

    events: _.defaults({
      
      'action-distort': _.fwdEventOnPreventDefault(function () {
        this._toggle();
      }),

      // 'tapone .mas-thread--fibers': _.fwdEventOnPreventDefault(function () {
      //   this.model.set('focus', true);
      // }),
     
      'didRemoveThread': _.fwdEventOnPreventDefault(function () {
        var fakeEvent =  {
          isDefaultPrevented: function () { return false; },
          preventDefault:     function () {},
        };
        ThreadViewAdapted.prototype.events.didRemoveThread.call(this, fakeEvent);

        if (!this.subThread && !this.parentThread) {
          this._removeFocusView();
        }
      }),

    }, ThreadViewAdapted.prototype.events),

    initialize: function () {

      ThreadViewAdapted.prototype.initialize.apply(this, arguments);

      // Acquire focus
      this.model.set('focus', true);

      this.$el.on('didRender', _.bind(function () {
        this.headerView.refreshSummary();
      }, this));
    },

    initializeWhenMapIsReady: function () {

      // Cf: _renderMap in ThreadMapView.
      this._initItemsManager();
    },

    render: function () {
      ThreadViewAdapted.prototype.render.apply(this, arguments);

      // We need the dom to be ready.
      this._registerDomEvent();
    },

    remove: function () {
      this.itemsManager.remove();

      this.elementsView = {
        remove: _.noop
      };
      // Remove as defined by the ThreadView class.
      ThreadViewAdapted.prototype.remove.apply(this);
    },

    _initItemsManager: function () {
      this.itemsManager = new AggregationItemManager({
        map: this.map,
        model: this.model
      });
    },

    _renderRemovedElementsView: function () {

      if (this.removedElementsView) {
        this.stopListening(this.removedElementsCounter);
        this.removedElementsView.remove();
      }

      this.removedElementsCounter = this.model.getRemovedElementsCounter();

      this.removedElementsView = new RemovedElementsView({
        tagName: 'li',
        model: this.removedElementsCounter
      });

      this.listenTo(this.removedElementsCounter, 'change:numberOfRemovedElements', this._updateHasRemovedElements, this);

      this.removedElementsView.$el.addClass('mas-elements--removedElements')
        .appendTo(this.$('.mas-thread--fibers'));

      this._updateHasRemovedElements(this.removedElementsCounter);

    },

    _updateHasRemovedElements: function (counter) {

      if (counter.get('numberOfRemovedElements')) {
        this.$('.mas-thread--fibers').addClass('has-removedElements');
      } else {
        this.$('.mas-thread--fibers').removeClass('has-removedElements');
      }
    },

    _createQueryUpdater: function () {

      if (this.map) {

        return new QueryAutoUpdaterMapOperations({
          thread: this.model,
          map: this.map,
        });
      }
    },

    _renderElements: function () {
       
      // TODO transfer interface constraint on an other class, currently managed by MapViewElements
      this.elementsView = this.map;

      // Quick and dirty hack for demo:
      this.$('.mas-thread--fibers').replaceWith($(
        '<div class="mas-thread--fibers mas-elements" style="justify-content: space-around;">' +
          '<div style="color: #555f68;">' +
            '<div style="font-size: 25px; line-height: 60px; font-variant: small-caps;">this thread is shown on the map</div>' +
        '</div>')
      );

      this._renderRemovedElementsView();
    },

    _attachEvents: function () {
      ThreadViewAdapted.prototype._attachEvents.apply(this, arguments);

      this.listenTo(this.model, 'change:focus', function (model, hasfocus) {
        if (hasfocus) {
          this._createFocusView();
        } else {
          this._removeFocusView();
        }
      });

      this.listenTo(this.model, 'reset:elements', function (elements, options) {

        this._triggerEventForRemoved(options.delta.removed);
        var promise = this.itemsManager.fillFromModel(elements.models);
        promise = this._actionsAfterRenderNewElements(promise, this);

        promise.done();
      });
    },

    _registerDomEvent: function () {

      // Here because it does not work if placed in events attribute.
      this.$('.mas-thread--fibers').on('tapone', _.bind(function (ignored, ev) {
        if (!ev.originalEvent.isDefaultPrevented()) {
          // Acquire focus
          this.model.set('focus', true);
        }
      }, this));
    },

    _triggerEventForRemoved: function (elements) {
      _(elements).forEach(function (element) {
        element.trigger('hasBeenRemoved');
      });
    },

    _createFocusView: function () {
      if (!this.focusView && (this.subThread || this.parentThread)) {
        this.focusView = $(
          '<div class="mas-isOverlay" style="' +
            //'right: -4px;' +
            'top: 0px;' +
            'left: 0px;' +
            'width: 100%;' + //'width: 5px;' +
            'z-index: 0;' +
            'height: 100%;' +
            'opacity: 0.7;' +
            'border-width: 1px;' +
            'border-style: solid;' +
            'border-color: rgb(0, 65, 28);' +
            //'background-color: rgb(0, 139, 61);' +
            'box-shadow: inset 0 0 0px 3px rgb(0, 139, 61);' +
          '">' +
          '</div>');

        this.focusView.prependTo(this.$mainThread.find('.mas-thread--fibers'));//'.mas-threadHeader'));
      }
    },

    _removeFocusView: function () {
      if (this.focusView) {
        this.focusView.remove();
        this.focusView = undefined;
      }
    },

    _actionsAfterRenderNewElements: function (promise, self) {
      return promise
        .then(function () {
          self._notifyRenderingComplete();
        })
        .fail(function (error) {
          console.log(error);
        });
    },

    _toggle: function () {
      console.log('TODO');
    },

    _notifyRenderingComplete: function () {
      var event = document.createEvent('Event');
      event.initEvent('didRender', true, true);
      this.el.dispatchEvent(event);
    },
  });

  return ThreadMapShadowView;
});