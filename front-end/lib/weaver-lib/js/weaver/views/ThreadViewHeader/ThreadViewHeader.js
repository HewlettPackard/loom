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
  var BaseView = require('./../BaseView');
  var template = require('./ThreadViewHeader.html');
  var ThreadView = require('./../ThreadView/ThreadView');
  var ThreadTitleView = require('./ThreadTitleView');
  var PollingFeedbackView = require('./../PollingFeedbackView');
  var ThreadSummaryView = require('./../ThreadView/ThreadSummaryView');
  var ThreadViewHeaderMetrics = require('./../ThreadViewHeaderMetrics');

  /**
   * ThreadViewHeader displays a header listing the name of the Thread and the actions available on the
   * Thread (grouping, selecting...) as well as its currently enabled Metrics, A summary and its polling state.
   *
   * @class ThreadViewHeader
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ThreadViewHeader = BaseView.extend({

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-threadHeader',

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ThreadViewHeader
     * @final
     */
    constructorName: 'LOOM_ThreadViewHeader',

    /**
     * The type of property selection currently occurring.
     * Will be undefined if the property selector is not displayed
     * @property propertySelectionDisplayed
     * @type {String}
     * @default undefined
     */

    /**
     * The PropertySelector instance displayed in this header
     * @property propertySelector
     * @type {views.PropertySelector}
     */

    /**
     * The template used to structure the view
     * @property template
     * @type {String}
     */
    template: template,

    /**
     * Used internally to render the metrics list with clickable remove button
     *
     * @property threadViewHeaderMetrics
     * @type views.ThreadViewHeaderMetrics
     */
    threadViewHeaderMetrics: undefined,

    /**
     * @property threadSummary
     * @type views.ThreadSummaryView
     */
    threadSummary: undefined,

    /**
     * @property pollingFeedback
     * @type views.PollingFeedbackView
     */
    pollingFeedback: undefined,

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);

      this.threadViewHeaderMetrics = new ThreadViewHeaderMetrics({
        className: ThreadViewHeaderMetrics.prototype.className + ' mas-threadHeader--metrics',
        model: this.model
      });
      if (this.model) {
        this.listenTo(this.model, 'reset:elements', this._updateActions);
        this.listenTo(this.model, 'reset:elements', this._updateElementTypeDisplay);
        this.listenTo(this.model, 'change:query', this._updateStateOfPipeline);
        // Due to a bug with Chrome, flexbox and `justify-content: space-between`
        // we need to position the list of metrics absolutely and create the necessary
        // space for them to be displayed :(
        this.listenTo(this.model.get('metrics'), 'add remove', this._updatePaddingForMetrics);
        this.render();
      }
    },

    /**
     * @method refreshSummary
     */
    refreshSummary: function () {
      this.threadSummary.render();
    },

    /**
     * @method remove
     */
    remove: function () {
      // Necessary cleanup to remove event listeners to prevent memory leaks
      this.pollingFeedback.remove();
      this.threadSummary.remove();
      this.threadViewHeaderMetrics.remove();
      BaseView.prototype.remove.apply(this, arguments);
    },

    /**
     * @method _dispatchEvent
     * @param name
     * @param data
     * @private
     */
    _dispatchEvent: function (name, data) {
      var evt = document.createEvent('Event');
      evt.initEvent(name, true, true);
      _.extend(evt, data, {
        thread: this.model,
        view: this
      });
      this.el.dispatchEvent(evt);
    },

    /**
     * @method render
     */
    render: function () {
      if (!this.options.metric) {
        this._renderThreadSummary();
        this._renderPollingFeedback();
        this.$el.append(this.threadViewHeaderMetrics.el);
      } else {
        this.$el.addClass('mas-threadHeader-metric');
      }
      this._updateActions();
      this._updateTitle();
      this._updateElementTypeDisplay();
      this._updatePaddingForMetrics();
    },

    /**
     * @method _renderThreadSummary
     * @private
     */
    _renderThreadSummary: function () {
      this.threadSummary = new ThreadSummaryView({
        el: this.$('.mas-threadSummary'),
        model: this.model
      });
    },

    /**
     * @method _renderPollingFeedback
     * @private
     */
    _renderPollingFeedback: function () {
      this.pollingFeedback = new PollingFeedbackView({
        el: this.$('.mas-threadHeader--pollingFeedback'),
        model: this.model
      });
    },

    /**
     * Calculates the correct padding height for the current number of metrics.
     *
     * @method _updatePaddingForMetrics
     * @private
     */
    _updatePaddingForMetrics: function () {
      var numberOfMetrics = this.model.get('metrics').size();
      this.el.style.paddingBottom = numberOfMetrics ? (numberOfMetrics * ThreadView.THREAD_METRIC_HEIGHT) + 'px' : '';
    },

    /**
     * @method _updateTitle
     * @private
     */
    _updateTitle: function () {
      var placeholder = this.el.querySelector('.mas-threadHeader--title');
      new ThreadTitleView({
        el: placeholder,
        model: this.options.metric || this.model
      });
    },

    /**
     * @method _updateElementTypeDisplay
     * @private
     */
    _updateElementTypeDisplay: function () {
      if (this.model.isContainingItems()) {
        this.$el.addClass('mas-threadHeader-displayingItems');
        this.$el.removeClass('mas-threadHeader-displayingGroups');
        this.$el.removeClass('mas-threadHeader-displayingClusters');
        return;
      }
      if (this.model.isContainingClusters()) {
        this.$el.removeClass('mas-threadHeader-displayingItems');
        this.$el.removeClass('mas-threadHeader-displayingGroups');
        this.$el.addClass('mas-threadHeader-displayingClusters');
        return;
      }
      if (this.model.isContainingGroups()) {
        this.$el.removeClass('mas-threadHeader-displayingItems');
        this.$el.addClass('mas-threadHeader-displayingGroups');
        this.$el.removeClass('mas-threadHeader-displayingClusters');
        return;
      }
      this.$el.removeClass('mas-threadHeader-displayingItems');
      this.$el.removeClass('mas-threadHeader-displayingGroups');
      this.$el.removeClass('mas-threadHeader-displayingClusters');
    },

    /**
     * @method _updateActions
     * @private
     */
    _updateActions: function () {
      if (this.model && !this.model.get('parent') && (!this.options.metric)) {
        this.$el.find('.mas-action--closeChildThread').remove();
      }
    },

    /**
     * @method _updateStateOfPipeline
     * @param thread
     * @param query
     * @private
     */
    _updateStateOfPipeline: function (thread, query) {
      if (query.hasOperations()) {
        this.$el.addClass('has-operationsInPipeline');
      } else {
        this.$el.removeClass('has-operationsInPipeline');
      }
    }

  });

  return ThreadViewHeader;
});