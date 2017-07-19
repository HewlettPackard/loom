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
  var confirm = require('weaver/utils/confirm');
  var Backbone = require('backbone');
  var SelectionService = require('weft/services/SelectionService');
  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');
  var ThreadView = require('weaver/views/ThreadView/ThreadView');
  var MenuGroupController = require('weaver/views/helpers/MenuGroupController');
  var FilterRelatedOperandHighlighter = require('./ThreadListView/FilterRelatedOperandHighlighter');

  require('weaver/utils/jquery.insertAt');
  require('weaver/utils/jquery.sortElements');

  /**
   * ThreadListView is the main view, displaying the list of top level threads. Expanded groups or clusters, as well
   * as metrics are added to their parent's ThreadView, not to the ThreadListView
   *
   * @class ThreadListView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ThreadListView = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ThreadListView
     * @final
     */
    constructorName: 'LOOM_ThreadListView',

    /**
     * @property tagName
     * @type {String}
     */
    tagName: 'ul',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-threadList',

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'click .mas-action--close': function (event) {
        if (!event.isDefaultPrevented()) {
          var thread = $(event.target).parents('.mas-threadView').data('view').model;
          var message = 'You are about to remove the "' + thread.get('name') + '" root thread. Are you sure you want to proceed?';
          confirm.confirm(message)
            .then(_.bind(function (confirmed) {
              if (confirmed) {
                this.model.remove(thread);
              } else {
                $(event.target).parents('.mas-headerActions').data('view').collapse();
              }
            }, this))
            .done();
        }
      },
      'action:selectElement': function (event) {
        var element = this._getElement(event);
        this.selectElement(element);
      },
      'action:unselectElement': function (event) {
        var element = this._getElement(event);
        event.preventDefault();
        this.unselectElement(element);
      },
      // TODO: Extract in separate controller
      'action:addFilterElement': function (event) {
        var element = this._getElement(event);
        this.addFilterElement(element);
      },
      'action:removeFilterElement': function (event) {
        var element = this._getElement(event);
        this.removeFilterElement(element);
      },
      'click .mas-action--clone': function (event) {
        this._cloneThread(event);
      },
      'click .mas-action--changeDisplayMode': function (event) {
        var thread = $(event.target).parents('.mas-threadView').data('view').model;
        this.displayThread(thread);
      },
      'scroll': function() {
        this.EventBus.trigger('thread-list:scrolled');
      },
      'drop': function(event) {
        this.EventBus.trigger('thread-list:drop', event);
      },
      sort: function () { // event, ui
        this.EventBus.trigger('thread-list:sort');
      },
      sortstart: function () { // event, ui
        this.EventBus.trigger('thread-list:sortstart');
      },
      sortstop: function () { // event, ui
        this.EventBus.trigger('thread-list:sortstop');
      }
    },

    /**
     * @property this.model is a list of threads
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.model = this.model || new Backbone.Collection();
      this.filterRelatedOperandHighlighter = new FilterRelatedOperandHighlighter({
        el: this.el
      });
      new MenuGroupController({
        el: this.el,
        groupClass: 'mas-headerActions'
      });
      new MenuGroupController({
        el: this.el,
        groupClass: 'mas-threadSettingsMenu'
      });
      new MenuGroupController({
        el: this.el,
        groupClass: 'mas-threadSettingsMenu--item'
      });
      this.$el.sortable({
        handle: '.mas-threadHeader--info, .mas-threadPositionControl, .mas-threadHeader'
      });
      this.threadViews = {};
      if (!this.options.selectionService) {
        this.options.selectionService = new SelectionService({
          flag: 'selected'
        });
      }
      this.render();
      this._registerModelListeners();
      this.listenTo(this.EventBus, 'thread:list:remove' , function(event) {
        this.model.remove(event.threadView.model);
      });
      this.listenTo(this.EventBus, 'thread:clone' , function(event) {
        this._cloneThread(event);
      });
    },

    _cloneThread: function(event) {
      var clone = event.thread.clone();
      this.model.add(clone);
      this.options.braidingController.get('threads').add(clone);
    },

    /**
     * @method _addThread
     * @param thread
     * @param options
     * @private
     */
    _addThread: function (thread, options) {
      this.model.add(thread, options);
      this.options.braidingController.get('threads').add(thread);
    },

    /**
     * @method _removeThread
     * @param thread
     * @private
     */
    _removeThread: function (thread) {
      this.model.remove(thread);
      this.options.braidingController.get('threads').remove(thread);
    },

    /**
     * Displays given list of Threads, replacing the Threads  currently displayed
     * (but keeping those already there so scrollbars stay at the right position)
     * @method  displayThreads
     * @param  {Array} threads The list of threads
     */
    displayThreads: function (threads) {
      this._removeLegacyThreads(threads);
      this._doDisplayThreads(threads);
    },

    /**
     * Displays given list of Threads after the Threads already on screen
     * @method appendThreads
     * @param  {Array} threads
     */
    appendThreads: function (threads) {
      _.forEach(threads, this._addThread, this);
    },

    /**
     * @method displayThread
     * @param thread
     */
    displayThread: function (thread) {
      var view = this.createView(thread);
      //insert at the same index in the DOM as as the model's in thread collection
      view.$el.insertAt(this.model.indexOf(thread), this.$el);
      view.render();
      view.dispatchCustomEvent('didDisplayThread', {
        thread: thread
      });
      this.threadViews[thread.id] = view;
    },

    /**
     * @method createView
     * @param thread
     * @returns {ThreadView}
     */
    createView: function (thread) {
      return new ThreadView({
        model: thread,
        providerLegendService: this.options.providerLegendService,
        selectionService: this.options.selectionService
      });
    },

    /**
     * @method removeThread
     * @param thread
     */
    removeThread: function (thread) {
      this.threadViews[thread.id].remove();
      this.dispatchCustomEvent('didRemoveThread', {
        thread: thread
      });
      delete this.threadViews[thread.id];
    },

    /**
     * @method clear
     */
    clear: function () {
      _.forEach(this.threadViews, function (view) {
        this._removeThread(view.model);
      }, this);
    },

    /**
     * Selects given element. If an element was already selected,
     * it gets unselected
     * @method selectElement
     * @param element {models.Element}
     */
    selectElement: function (element) {
      this.options.selectionService.select(element);
    },

    /**
     * Unselect's given element
     * @method unselectElement
     * @param element {models.Element}
     */
    unselectElement: function (element) {
      this.options.selectionService.unselect(element);
    },

    /**
     * Filters on given element
     * @method filterElement
     * @param element {models.Element}
     */
    addFilterElement: function (element) {
      this.options.filterService.addFilter(element);
    },

    /**
     * Removes filter on given element
     * @method removeFilterElement
     * @param element {models.Element}
     */
    removeFilterElement: function (element) {
      this.options.filterService.removeFilter(element);
    },

    /**
     * @method updateFilter
     */
    updateFilter: function () {
      //apply filter if present
      if (this.options.filterService.get('filters').length) {
        this.$el.addClass('mas-filter-on');
      } else {
        this.$el.removeClass('mas-filter-on');
      }
    },

    /**
     * @method _removeLegacyThreads
     * @param newThreads
     * @private
     */
    _removeLegacyThreads: function (newThreads) {
      var legacyThreads = _.difference(this.model.models, newThreads);
      this.model.remove(legacyThreads);
    },

    /**
     * @method _doDisplayThreads
     * @param threads
     * @private
     */
    _doDisplayThreads: function (threads) {
      _.forEach(threads, function (thread, index) {
        this._addThread(thread, {
          at: index
        });
      }, this);
    },

    /**
     * @method remove
     */
    remove: function () {
      BaseView.prototype.remove.apply(this, arguments);
      this.filterRelatedOperandHighlighter.stopListening();
    },

    /**
     * @method _registerModelListeners
     * @private
     */
    _registerModelListeners: function () {
      this.listenTo(this.model, 'add', this.displayThread);
      this.listenTo(this.model, 'remove', this.removeThread);
      this.listenTo(this.options.filterService.get('filters'), 'remove', this.updateFilter);
      this.listenTo(this.options.filterService.get('filters'), 'add', this.updateFilter);
    },

    /**
     * @method _getElement
     * @param event
     * @returns {*}
     * @private
     */
    _getElement: function (event) {
      return event.originalEvent.thread;
    }
    
  });

  return ThreadListView;
});
