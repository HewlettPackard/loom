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

  var Q = require('q');
  var _ = require('lodash');
  var $ = require('jquery');

  /** @type BaseView */
  var BaseView = require('weaver/views/BaseView');
  var ElementView = require('./../Element/ElementView');
  var MetricValueView = require('./../MetricValueView');
  var SortedLabellingStrategy = require('./../SortedLabellingStrategy');
  var ClusterLabellingStrategy = require('./Elements/ClusterLabellingStrategy');
  var DefaultLabellingStrategy = require('./../DefaultLabellingStrategy');
  var emptyElementTemplate = require('./../ThreadViewElements/EmptyElement.html');

  /**
   * The view displaying the elements of a Thread.
   * @class ThreadViewElements
   * @namespace views
   * @module  weaver
   * @submodule views
   * @extends BaseView
   * @constructor
   */
  var ThreadViewElements = BaseView.extend({

    /**
     * @property constructorName
     * @type {String}
     * @default LOOM_ActionDialogView
     * @final
     */
    constructorName: 'LOOM_ThreadViewElements',

    /**
     * @property tagName
     * @type {String}
     */
    tagName: 'ul',

    /**
     * @property className
     * @type {String}
     */
    className: 'mas-elements',

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'didSelectElement': function () {
        this._updateHasSelectionClass(true);
        this._scheduleLabelRefresh();
      },
      'didUnselectElement': function () {
        this._updateHasSelectionClass(false);
        this._scheduleLabelRefresh();
      }
    },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      // Index of the views so we can quickly add/remove them... ahh, if only Backbone.Collections
      // could store views without turning them into models :()
      this.elementViews = {};
      this.sortedLabellingStrategy = new SortedLabellingStrategy();
      this.clusterLabellingStrategy = new ClusterLabellingStrategy();
      this.defaultLabellingStrategy = new DefaultLabellingStrategy();
      this.listenTo(this.model, 'change:sortedBy', this._scheduleLabelRefresh);
      this.listenTo(this.model.get('elements'), 'change:numberOfItems', this._scheduleLabelRefresh);
      this.listenTo(this.model.get('result'), 'change:pending', this._updateEmptyElement);
      this.listenTo(this.model, 'reset:elements', function (elements, options) {
        this._renderElements(options.delta, true);
      });
      this._boundLabelRefreshScheduling = _.bind(this._scheduleLabelRefresh, this);
      this._boundLabelRefresh = _.bind(this.refreshElementsLabels, this);
      $(window).on('resize', this._boundLabelRefreshScheduling);
      this.listenTo(this.model, 'sort:elements', function (elements, options) {
        this._renderElements(options.delta, true);
      });
      this.render();
    },

    /**
     * @method remove
     */
    remove: function () {
      this._removeElements();
      $(window).off('resize', this._boundLabelRefreshScheduling);
      BaseView.prototype.remove.apply(this);
    },

    /**
     * @method render
     */
    render: function () {
      this.$emptyElement = $(emptyElementTemplate);
      if (!this.model.get('elements').isEmpty()) {
        this._renderElements({
          added: this.model.get('elements').models,
          removed: []
        });
      }
    },

    /**
     * @method _renderElements
     * @param delta
     * @private
     */
    _renderElements: function (delta) {
      this._createElements()
        .then(_.bind(this._getOffsets, this))
        .then(_.bind(this._animateElements, this))
        .then(_.bind(this._applyDOMModifications, this, delta))
        .then(_.bind(this._scheduleLabelRefresh, this))
        .then(_.bind(this._notifyRenderingComplete, this))
        .done();
    },

    /**
     * @method _notifyRenderingComplete
     * @private
     */
    _notifyRenderingComplete: function () {
      var event = document.createEvent('Event');
      event.initEvent('didRender', true, true);
      this.el.dispatchEvent(event);
    },

    /**
     * @method _createElements
     * @returns {(function(any=): JQueryPromise<T>)|*|promise.promise|jQuery.promise|(function(string=, Object=): JQueryPromise<any>)|promise}
     * @private
     */
    _createElements: function () {
      var deferred = Q.defer();
      var elements = this.model.get('elements').models;
      if (this.model.hasExcludedItems()) {
        elements = elements.concat(this.model.get('result').get('excludedItems'));
      }
      var numberOfElements = elements.length;
      var i = 0;
      var views = [];
      var self = this;
      function createElement() {
        if (i < numberOfElements) {
          views.push(self._getElementView(elements[i]));
          i++;
          setImmediate(createElement);
        } else {
          deferred.resolve(views);
        }
      }
      createElement();
      return deferred.promise;
    },

    /**
     * @method _applyDOMModifications
     * @param delta
     * @private
     */
    _applyDOMModifications: function (delta) {
      // Start by removing elements that are no longer displayed
      // to make adding the elements simpler
      _.forEach(delta.removed, function (element) {
        this._removeElement(element);
      }, this);
      // Re-adds elements that were already there, but the overhead of computing
      // which moved and which stayed at its place feels bigger than the
      this.model.get('elements').forEach(function (element, index) {
        var view = this._renderElement(element, index);
        view.el.style.left = '';
      }, this);
      this._renderExcludedItems();
      this._updateEmptyElement();
    },

    /**
     * @method _animateElements
     * @param offsets
     * @returns {*}
     * @private
     */
    _animateElements: function (offsets) {
      var promises = this.model.get('elements').map(function (element, index) {
        if (offsets[index] && offsets[index] !== -1) {
          var deferred = Q.defer();
          var HTMLElement = this._getElementView(element).el;
          HTMLElement.style.transition = 'left 1s';
          HTMLElement.style.left = offsets[index] + 'px';
          // More reliable than listening to transitioned
          setTimeout(function () {
            HTMLElement.style.transition = '';
            deferred.resolve(HTMLElement);
          }, 1000);
          return deferred.promise;
        }
      }, this);
      return Q.all(promises);
    },

    /**
     * @method _getOffsets
     * @returns {(function(any=): JQueryPromise<T>)|*|promise.promise|jQuery.promise|(function(string=, Object=): JQueryPromise<any>)|promise}
     * @private
     */
    _getOffsets: function () {
      var clone = this._createClone();
      this._reorderClonedElements(clone);
      document.body.appendChild(clone);
      var deferred = Q.defer();
      window.requestAnimationFrame(_.bind(function () {
        var positions = this._measureElementsOffsets(clone);
        document.body.removeChild(clone);
        deferred.resolve(positions);
      }, this));
      return deferred.promise;
    },

    /**
     * @method _measureElementsOffsets
     * @param clone
     * @returns {*}
     * @private
     */
    _measureElementsOffsets: function (clone) {
      return this.model.get('elements').map(function (element) {
        var clonedElement = clone.querySelector('.mas-element-' + element.cid);
        if (clonedElement) {
          var updatedPosition = clonedElement.offsetLeft;
          var currentPosition = this._getElementView(element).el.offsetLeft;
          if (Math.abs(updatedPosition - currentPosition) < clonedElement.offsetWidth * 0.80) {
            return 0;
          } else {
            return updatedPosition - currentPosition;
          }
        }
      }, this);
    },

    /**
     * @method _reorderClonedElements
     * @param clone
     * @private
     */
    _reorderClonedElements: function (clone) {
      this.model.get('elements').forEach(function (element, index) {
        var clonedElement = clone.querySelector('.mas-element-' + element.cid);
        if (clonedElement) {
          this._updateFlexboxOrder(clonedElement, index);
        }
      }, this);
    },

    /**
     * @method _createClone
     * @returns {Node}
     * @private
     */
    _createClone: function () {
      // Shallow clone as we're just interested in the elements
      var clone = this.el.cloneNode(false);
      clone.style.width = this.el.offsetWidth + 'px';
      clone.style.height = this.el.offsetHeight + 'px';
      // IMPROVE: Use a class on the element (eg. when the elements are compressed
      // to leave space for the menus)
      var currentStyle = window.getComputedStyle(this.el);
      clone.style.paddingLeft = currentStyle.paddingLeft;
      clone.style.paddingRight = currentStyle.paddingRight;
      clone.classList.add('mas-animationClone');
      var currentElements = this.el.querySelectorAll('.mas-element');
      _.forEach(currentElements, function (currentElement) {
        clone.appendChild(currentElement.cloneNode(false));
      });
      return clone;
    },

    /**
     * @method _updateEmptyElement
     * @private
     */
    _updateEmptyElement: function () {
      if (this.model.get('elements').isEmpty()) {
        this.$emptyElement.appendTo(this.el);
      } else {
        this.$emptyElement.remove();
      }
    },

    /**
     * @method _renderExcludedItems
     * @private
     */
    _renderExcludedItems: function () {
      if (this.model.get('result')) {
        var excludedItems =  this.model.get('result').get('excludedItems');
        if (this.excludedItemsView && this.excludedItemsView.model !== excludedItems) {
          this._removeElement(this.excludedItemsView.model);
          this.excludedItemsView = undefined;
        }
        if (this.model.hasExcludedItems()) {
          this.excludedItemsView = this._renderElement(excludedItems, 999999);
          this.excludedItemsView.$el.addClass('mas-element-excludedItems');
          this.EventBus.trigger('thread:fiber:filter:enable');
        }
      }
    },

    /**
     * @todo refactor this
     * @method refreshElementsLabels
     */
    refreshElementsLabels: function () {
      if (!this.el.parentNode) {
        return;
      }
      this.labelRefreshScheduled = false;
      var width = this.el.offsetWidth;
      var selectedThreadWidth = width * ((100 - this.selectedElementWidth) / 100);
      var unselectedElementsWidth = selectedThreadWidth / (this.model.get('elements').size() - 1);
      // We want the views in the order the elements they display are
      // in the Thread, which is not necessarily the one they are in the DOM
      // because layout is done via Flexbox
      var views = this._getViewsList(this.model.get('elements'));
      if (this.model.get('sort')) {
        this.sortedLabellingStrategy.unselectedElementsWidth = unselectedElementsWidth;
        this.sortedLabellingStrategy.threadWidth = width;
        this.sortedLabellingStrategy.attribute = this.model.get('sort').id;
        this.sortedLabellingStrategy.isContainingClusters = this.model.isContainingClusters();
        this.sortedLabellingStrategy.updateLabels(views, unselectedElementsWidth, width);
      } else if (this.model.isContainingClusters()) {
        this.$el.addClass('mas-elements-displayingClusters');
        this.clusterLabellingStrategy.updateLabels(views, unselectedElementsWidth);
      } else {
        /* if not a clustered thread, show labels if there is space */
        this.$el.removeClass('mas-elements-displayingClusters');
        this.defaultLabellingStrategy.unselectedElementsWidth = unselectedElementsWidth;
        this.defaultLabellingStrategy.threadWidth = width;
        this.defaultLabellingStrategy.updateLabels(views, unselectedElementsWidth, width);
      }
    },

    /**
     * @method _renderElement
     * @param element
     * @param index
     * @returns {*}
     * @private
     */
    _renderElement: function (element, index) {
      var view = this._getElementView(element);
      if (!view.el.parentNode) {
        this.elementViews[element.id] = view;
        this.el.appendChild(view.el);
      }
      this._updateFlexboxOrder(view.el, index);
      return view;
    },

    /**
     * @method _updateFlexboxOrder
     * @param element
     * @param order
     * @private
     */
    _updateFlexboxOrder: function (element, order) {
      element.style['-webkit-box-ordinal-group'] = order;
      element.style['-webkit-flex-order'] = order;
      element.style['-ms-flex-order'] = order;
      element.style.order = order;
    },

    /**
     * @method _getViewsList
     * @param elements
     * @returns {Array}
     * @private
     */
    _getViewsList: function (elements) {
      var result = [];
      elements.forEach(function (element) {
        var view = this.elementViews[element.id];
        if (view) {
          result.push(view);
        }
      }, this);
      return result;
    },

    /**
     * @method _getElementView
     * @param element
     * @returns {*}
     * @private
     */
    _getElementView: function (element) {
      return this.elementViews[element.id] || this._createElementView(element);
    },

    /**
     * @method _createElementView
     * @param element
     * @returns {*}
     * @private
     */
    _createElementView: function (element) {
      var view;
      if (this.options.metric) {
        view = new MetricValueView({
          model: element,
          metric: this.options.metric
        });
      } else {
        view = new ElementView({
          model: element,
          metrics: this.model.get('metrics')
        });
      }
      this.elementViews[element.id] = view;
      return view;
    },

    /**
     * @method _removeElement
     * @param element
     * @private
     */
    _removeElement: function (element) {
      var view = this.elementViews[element.id];
      //var view = element.get('view');
      if (view) {
        view.remove();
      }
      delete this.elementViews[element.id];
    },

    /**
     * @method _removeElements
     * @private
     */
    _removeElements: function () {
      _.forEach(this.elementViews, function (view, id) {
        view.remove();
        delete this.elementViews[id];
      }, this);
    },

    /**
     * @method _scheduleLabelRefresh
     * @private
     */
    _scheduleLabelRefresh: function () {
      if (!this.labelRefreshScheduled) {
        this.labelRefreshScheduled = true;
        window.requestAnimationFrame(function () {
          window.requestAnimationFrame(this._boundLabelRefresh);
        }.bind(this));
      }
    },

    /**
     * Sets/Removes the 'has-selection' class on the element
     * @method _updateHasSelectionClass
     * @param {Boolean} hasSelection
     * @private
     */
    _updateHasSelectionClass: function (hasSelection) {
      if (hasSelection) {
        this.$el.addClass('has-selection');
      } else {
        this.$el.removeClass('has-selection');
      }
    }
  });

  return ThreadViewElements;
});
