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
  var Q = require('q');
  var confirm = require('weaver/utils/confirm');
  require('backbone.stickit');

  var SelectionService = require('weft/services/SelectionService');
  /** @type BaseView */
  var AggregationItemType = require('weft/models/AggregationItemType');
  var BaseView = require('weaver/views/BaseView');
  var template = require('weaver/views/ThreadView/ThreadView.html');
  var ThreadViewHeader = require('weaver/views/ThreadViewHeader/ThreadViewHeader');
  var ThreadViewElements = require('weaver/views/ThreadView/ThreadViewElements');
  var ProviderLegendView = require('weaver/views/ProviderLegendView');
  var QueryEditor = require('weaver/views/QueryEditor');
  var RemovedElementsView = require('weaver/views/ThreadView/RemovedElementsView');

  /**
   * A ThreadView displays the content of a given Thread as well as its nested Threads and Metrics
   * @class ThreadView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ThreadView = BaseView.extend({

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-threadView',

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ActionDialogView
     * @final
     */
    constructorName: 'LOOM_ThreadView',

    /**
     * The template used to help structuring the view
     * @property template
     * @type {String}
     */
    template: template,

    /**
     * @property THREAD_METRIC_HEIGHT
     * @type {Number}
     * @default 40
     * @final
     */
    THREAD_METRIC_HEIGHT: 40,

    /**
     * @property legendView
     * @type views.ProviderLegendView
     */
    legendView: undefined,

    /**
     * @property bindings
     * @type {Object}
     */
    bindings: {
      ':el': {
        classes: {
          'is-unavailable': 'unavailable',
          'mas-threadView-beingUpdated': 'outdated'
        }
      }
    },

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'action:closeChildThread': '_closeChildThread',
      'click .mas-action-toggleSettings': function (event) {
        if (!event.isDefaultPrevented()) {
          //this.toggleSettingsMenu();
          event.preventDefault();
          this.EventBus.trigger('thread:click', {
            thread: this.model,
            threadView: this
          });
        }
      },
      'click .mas-action-toggleQuery': function (event) {
        if (!event.isDefaultPrevented()) {
          this.toggleQueryEditor();
          event.preventDefault();
        }
      },
      'click .mas-action--close': function (event) {
        // If this is not a main thread then close it
        if (!event.isDefaultPrevented() && $(event.target).parents('.' + this.className)[0] !== this.$el[0]) {
          event.preventDefault();
          this.EventBus.trigger('thread:close', {threadView: this});
        }
      }
    },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.$mainThread = this.$('.mas-thread');
      this.$subThreads = this.$('.mas-threadView--subthreads');
      this.childThreads = new SelectionService({
        flag: 'displayed'
      });
      this.queryEditor = this._createQueryEditor();
      this.childThreads.on('change:selection', this._renderChildThread, this);
      if (this.model) {
        this._initializeModelEvents();
        this._initializeRemovedElementsCounter();
      }
      this.render();
      if (this.model) {
        this.listenTo(this.model, 'reset:elements', function () {
          var excludedItems = this.model.get('result').get('excludedItems');
          if (excludedItems && excludedItems.get('numberOfItems')) {
            this.$('.mas-thread').addClass('has-excludedItems');
          } else {
            this.$('.mas-thread').removeClass('has-excludedItems');
          }
        });
      }
      this.listenTo(this.EventBus, 'thread:fiber:action:display' , function(event) {
        if (event.threadView === this && event.fiber.itemType instanceof AggregationItemType) {
          this._toggleThreadDisplay(event.fiber);
        }
      });

      this.listenTo(this.EventBus, 'thread:selected' , function(event) {
        if (event.threadView === this) {
          this.$el.find('.mas-thread--settingsToggle').first().addClass('is-selected');
        }
      });
      this.listenTo(this.EventBus, 'thread:unselected' , function(event) {
        if (event.threadView === this) {
          this.$el.find('.mas-thread--settingsToggle').first().removeClass('is-selected');
        }
      });
      this.listenTo(this.EventBus, 'thread:close:request' , function(event) {
        if (event.threadView === this) {
          // if this is a root thread, then ask a question
          if (this.$el.parents('.' + this.className)[0] === undefined) {
            var message = 'You are about to remove the "' + this.model.get('name') + '" root thread. Are you sure you want to proceed?';
            confirm.confirm(message)
            .then(_.bind(function (confirmed) {
              if (confirmed) {
                this.EventBus.trigger('thread:list:remove', {threadView: this});
              }
            }, this))
            .done();
          } else {
            this.EventBus.trigger('thread:close', {threadView: this});
          }
        }
      });
      this.listenTo(this.EventBus, 'thread:close' , function(event) {
        if (event.threadView === this) {
          this.remove();
        }
      });
    },

    /**
     * @method _initializeRemovedElementsCounter
     * @private
     */
    _initializeRemovedElementsCounter: function () {
      this.removedElementsCounter = this.model.getRemovedElementsCounter();
      this.removedElementsView = new RemovedElementsView({
        model: this.removedElementsCounter
      });
      this.listenTo(this.removedElementsCounter, 'change:numberOfRemovedElements', this._updateHasRemovedElements);
    },

    /**
     * @method _updateHasRemovedElements
     * @param counter
     * @private
     */
    _updateHasRemovedElements: function (counter) {
      if (counter.get('numberOfRemovedElements')) {
        this.$mainThread.addClass('has-removedElements');
      } else {
        this.$mainThread.removeClass('has-removedElements');
      }
    },

    /**
     * @method _createQueryEditor
     * @private
     */
    _createQueryEditor: function () {
      return new QueryEditor({
        model: this.model,
        collapsed: true,
        el: this.$('.mas-thread--queryEditor')
      });
    },

    /**
     * @method _initializeModelEvents
     * @private
     */
    _initializeModelEvents: function () {
      this.listenTo(this.model, 'reset:elements', this._removeLegacyElements);
      this.listenTo(this.model, 'reset:elements', this._maybeRemoveNestedThread);
      this.listenTo(this.model, 'change:outdated', this._lockDuringUpdate);
    },

    /**
     * @method toggleQueryEditor
     */
    toggleQueryEditor: function () {
      this.queryEditor.toggle();
      if (this.queryEditor.$el.hasClass('is-collapsed')) {
        this.EventBus.trigger('query:editor:hidden');
      } else {
        this.EventBus.trigger('query:editor:shown');
      }
    },

    /**
     * Displays a nested Thread querying given aggregation
     * @method displayNestedThread
     * @param thread {models.Thread}
     * @return {views.ThreadView} The ThreadView created to display the nested Thread
     */
    displayNestedThread: function (aggregation) {
      this.removeNestedThread();
      var promiseSubThread = this._doDisplayNestedThread(aggregation);
      aggregation.set('displayed', true);
      this.dispatchCustomEvent('didUpdateNestedThread');
      return promiseSubThread;
    },

    /**
     * Removes current nested Thread
     * @method removeNestedThread
     * @param thread {models.Thread}
     */
    removeNestedThread: function () {
      if (this.subThread) {
        var thread = this.subThread.model;
        thread.get('aggregation').set('displayed', false);
        this.subThread.remove();
        this.subThread = undefined;
        this.dispatchCustomEvent('didRemoveThread', {thread: thread});
      }
      this.dispatchCustomEvent('didUpdateNestedThread');
    },

    /**
     * Removes the view from the DOM
     * @method remove
     * @see http://backbonejs.org/#View-remove
     */
    remove: function () {
      this.removeNestedThread();
      this._removeBeforeModelClear();
      this.removedElementsView.remove();
      // Thread cleanup should be handled at model level rather than in the view, to allow an app using only Weft to
      // handle removals properly
      this._clearElements(this.model.get('elements'));
      this.model.clear();
      this.headerView.remove();
      this.elementsView.remove();
      this.queryEditor.remove();
      BaseView.prototype.remove.apply(this);
    },

    /**
     * @method disableActionsOnElements
     */
    disableActionsOnElements: function () {
      _.forEach(this.elementsView.elementViews, function (elementView) {
        elementView.disableActions();
      });
    },

    /**
     * @method enableActionsOnElements
     */
    enableActionsOnElements: function () {
      _.forEach(this.elementsView.elementViews, function (elementView) {
        elementView.enableActions();
      });
    },

    /**
     * Checks if the Thread's elements or any of its metrics
     * are covered by an element with given dimensions
     * @todo deprecate and remove?
     * @method hasElementsOrMetricsCoveredBy
     * @param  {ClientRect}  elementDimensions The dimensions of the element
     * @return {Boolean}
     */
    // hasElementsOrMetricsCoveredBy: function (elementDimensions) {

    //   var minimumOverlap = {
    //     top: 10,
    //     bottom: 20
    //   };

    //   var threadElementsDimension = this.elementsView.el.getBoundingClientRect();
    //   return !(threadElementsDimension.bottom - minimumOverlap.bottom < elementDimensions.top ||
    //     threadElementsDimension.top + minimumOverlap.top > elementDimensions.bottom);
    // },

    // PRIVATE HELPERS
    // TODO: Should be part of the tapestry so the model stays coherent :D
    /**
     * @method _clearElements
     * @param elements
     * @private
     */
    _clearElements: function (elements) {
      elements.forEach(function (element) {
        if (element.get('selected')) {
          this._clearSelectedElement(element);
        }
        if (element.get('isPartOfFilter')) {
          this._removeElementFromFilter(element);
        }
      }, this);
    },

    /**
     * @method _clearSelectedElement
     * @param element
     * @private
     */
    _clearSelectedElement: function (element) {
      var evt = document.createEvent('Event');
      evt.initEvent('action:unselectElement', true, true);
      evt.thread = element;
      this.el.dispatchEvent(evt);
    },

    /**
     * @method _removeElementFromFilter
     * @param element
     * @private
     */
    _removeElementFromFilter: function (element) {
      var evt = document.createEvent('Event');
      evt.initEvent('action:removeFilterElement', true, true);
      evt.thread = element;
      this.el.dispatchEvent(evt);
    },

    /**
     * Temporarily disables the thread and prevents interaction when regrouped
     * @method _lockDuringUpdate
     * @private
     */
    _lockDuringUpdate: function (thread, outdated) {
      if (outdated) {
        this.disableActionsOnElements();
        this.removeNestedThread();
      } else {
        this.enableActionsOnElements();
      }
    },

    // @todo: Why isn't it in ThreadViewElements!
    /**
     * @method _removeLegacyElements
     * @param elements
     * @param options
     * @private
     */
    _removeLegacyElements: function (elements, options) {
      this._clearElements(options.delta.removed);
    },

    /**
     * @method _maybeRemoveNestedThread
     * @param elements
     * @param options
     * @private
     */
    _maybeRemoveNestedThread: function (elements, options) {
      var removedElements = options.delta.removed;
      var currentAggregation = this.subThread ? this.subThread.model.get('aggregation') : undefined;
      var childThreadHasBeenRemoved = _.contains(removedElements, currentAggregation);
      if (childThreadHasBeenRemoved) {
        this.removeNestedThread();
      }
    },

    /**
     * Toggles display of thread for the given aggregation
     * @method _toggleThreadDisplay
     * @param aggregation {Aggregation} The aggregation we want to toggle the display.
     * @private
     */
    _toggleThreadDisplay: function (aggregation) {
      if (this.subThread && this.subThread.model.get('aggregation') === aggregation) {
        this.removeNestedThread();
      } else {
        this.displayNestedThread(aggregation);
      }
    },

    /**
     * Close child thread contained in given event
     * @method _closeChildThread
     * @param event {Event} The event notifying a thread should be closed
     * @private
     */
    _closeChildThread: function (event) {
      if (!event.isDefaultPrevented()) {
        event.preventDefault();
        var thread = event.originalEvent.thread;
        this.removeNestedThread(thread);
      }
    },

    /**
     * Removes the views the ThreadView delegates parts of its rendering to
     * @method _removeNestedViews
     * @private
     */
    _removeNestedViews: function () {
      this._removeChildThreadView();
    },

    /**
     * @method render
     */
    render: function () {
      if (this.model) {
        this._renderElements();
        this._renderHeader();
        this._renderLegend();
        this.stickit();
        this.removedElementsView.$el
          .addClass('mas-thread--removedElements')
          .appendTo(this.$('.mas-thread--contents'));
        this._updateHasRemovedElements(this.removedElementsCounter);
      }
      this._updateNestedState();
    },

    /**
     * @method _renderHeader
     * @private
     */
    _renderHeader: function () {
      // Save old view for later remove
      var oldHeaderView;
      if (this.headerView) {
        oldHeaderView = this.headerView;
      }
      this.headerView = this._createViewHeader();
      this.$('.mas-thread--header').replaceWith(this.headerView.el);
      if (oldHeaderView) {
        oldHeaderView.remove();
      }
    },

    /**
     * @method _createViewHeader
     * @private
     */
    _createViewHeader: function () {
      return new ThreadViewHeader({
        className: ThreadViewHeader.prototype.className + ' mas-thread--header',
        model: this.model
      });
    },

    /**
     * @method _renderElements
     * @private
     */
    _renderElements: function () {
      // Save old view for later remove
      var oldElementsView;
      if (this.elementsView) {
        oldElementsView = this.elementsView;
      }
      this.elementsView = this._createViewElements();
      this.elementsView.$el.on('didRender', _.bind(function () {
        this.headerView.refreshSummary();
      }, this));
      this.$('.mas-thread--fibers').replaceWith(this.elementsView.el);
      // Remove old view when it has been replaced
      if (oldElementsView) {
        oldElementsView.remove();
      }
      this.elementsView.$el.append('<div class="mas-threadView--veil></div>');
    },

    /**
     * @method _renderLegend
     * @private
     */
    _renderLegend: function () {
      this.legendView = new ProviderLegendView({
        el: this.$('.mas-thread--legend'),
        model: this.model,
        providerLegendService: this.options.providerLegendService
      });
    },

    /**
     * @method _createViewElements
     * @private
     */
    _createViewElements: function () {
      return new ThreadViewElements({
        className: ThreadViewElements.prototype.className + ' mas-thread--fibers',
        model: this.model
      });
    },

    /**
     * @method _updateNestedState
     * @private
     */
    _updateNestedState: function () {
      if (this.model && this.model.get('parent')) {
        this.$el.addClass('mas-threadView-nested');
      } else {
        this.$el.addClass('mas-threadView-main');
      }
    },

    /**
     * @method _doDisplayNestedThread
     * @param aggregation
     * @returns {(function(any=): JQueryPromise<T>)|*|promise.promise|jQuery.promise|(function(string=, Object=): JQueryPromise<any>)|promise}
     * @private
     */
    _doDisplayNestedThread: function (aggregation) {
      var deferred = Q.defer();
      if (aggregation) {
        window.requestAnimationFrame(_.bind(function () {
          var nestedThread = this.model.createNestedThread(aggregation);
          this.subThread = this._createSubThreadView(nestedThread);
          this.$subThreads.append(this.subThread.el);
          this.subThread.dispatchCustomEvent('didDisplayThread', {
            thread: nestedThread
          });
          deferred.resolve(this.subThread);
        }, this));
      } else {
        deferred.reject(new Error('no aggregation given.'));
      }
      return deferred.promise;
    },

    /**
     * @method _createSubThreadView
     * @param thread
     * @returns {*}
     * @private
     */
    _createSubThreadView: function (thread) {
      return new ThreadView({
        model: thread,
        providerLegendService: this.options.providerLegendService
      });
    },

    _removeBeforeModelClear: _.noop
  });

  return ThreadView;
});
