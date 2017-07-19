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
/*jshint -W064*/
define(function (require) {
  "use strict";

  var _ = require('lodash');
  var Backbone = require('backbone');
  var Thread = require('weft/models/Thread');
  var Query = require('weft/models/Query');
  var QueryAutoUpdaterMapOperations = require('weaver-map/models/QueryAutoUpdaterMapOperations');
  var QueryAutoUpdater = require('plugins/common/models/QueryAutoUpdater');
  var AttributesOperationBuilder = require('weaver-map/models/AttributesOperationBuilder');
  var MapDataManager = require('weaver-map/models/map/MapDataManager');

  var MetaThread = Backbone.Model.extend({

    initialize: function () {

      this._elements = {};
      this._diffs = {};
      this._maskInitialThread();
      this._initGetSetMethods();
      this._initHiddenThread();
      this._initForwardEvents();

      this._elementsMerged = new Backbone.Collection();

      this.getGeoAttributes = _.memoize(Thread.prototype.getGeoAttributes);
    },

    _maskInitialThread: function () {
      this.availableSorts = this.get('thread').availableSorts;
      this.removedElementsCounter = this.get('thread').removedElementsCounter;

      // TODO: Need some testing
      this.cid = this.get('thread').cid;
    },

    _initHiddenThread: function () {

      var thread = this._get('thread');

      this._set('hiddenThread', new Thread({
        viewData: thread.get('viewData'),
        itemType: this.get('itemType'),
      }));

      var hiddenThread = this._get('hiddenThread');

      // We create the query for the hidden thread.
      // But we don't want it to trigger anything because it will be modified
      // a second time by the query updater.
      hiddenThread.set('query', this._createQueryForHiddenThread(thread), {
        silent: true
      });

      // The updater will be set later by startThreadResultMerging.
      this.listenTo(hiddenThread, 'change:queryUpdater', function () {
        this.trigger('didChangeQueryUpdaterForHiddenThread', hiddenThread);
      });
    },

    _createQueryForHiddenThread: function (thread) {

      var filterByRegionAttributes = _.keys(thread.getAttributesForOperation('FILTER_BY_REGION'));
      var polygonClusteringAttributes = _.keys(thread.getAttributesForOperation('POLYGON_CLUSTERING'));

      var complementFilterByRegion = AttributesOperationBuilder({
        operator: 'FILTER_BY_REGION',
        attributes: filterByRegionAttributes,
        parameters: {
          attributes: filterByRegionAttributes
        }
      }).setRawParameter('complement', true)
        .build();

      return new Query({
        inputs: thread.get('query').get('inputs'),
        operationPipeline: [complementFilterByRegion],
        limit: {
          operator: 'POLYGON_CLUSTERING',
          attributes: polygonClusteringAttributes,
          parameters: {
            attributes: polygonClusteringAttributes
          }
        },
      });
    },

    stopListeningToThreads: function () {

      var thread = this._get('thread');
      var hiddenThread = this._get('hiddenThread');

      if (thread) {
        this.stopListening(thread, 'reset:elements');
      }

      if (hiddenThread) {
        this.stopListening(hiddenThread, 'reset:elements');
      }

      // Feel like a hack (see QueryAutoUpdater)
    },


    startThreadResultMerging: function () {

      if (!this._hasStartResultMergingBeenCalled) {

        this.stopListeningToThreads();

        this.listenTo(this._get('thread'), 'reset:elements', function (collection, diffs) {

          this._storeNewData('thread', collection, diffs);

          this._scheduleTriggerResetElements();
        });
        this.listenTo(this._get('hiddenThread'), 'reset:elements', function (collection, diffs) {

          this._storeNewData('hiddenThread', collection, diffs);

          this._scheduleTriggerResetElements();
        });

        // We start the query update if not already started.
        this._shouldSetMapQueryUpdater();

        // Prevent subsequent call to startThreadResultMerging
        this._hasStartResultMergingBeenCalled = true;
        // Allow next call to stopThreadResultMerging
        this._hasStopResultMergingBeenCalled = false;
      }
    },

    stopThreadResultMerging: function () {

      if (!this._hasStopResultMergingBeenCalled) {

        this.stopListeningToThreads();

        /*if (this.mapData) {
          this.stopListening(this.mapData);
        }*/
        this._shouldSetDefaultQueryUpdater();


        this.listenTo(this._get('thread'), 'reset:elements', _.bind(this.trigger, this, 'reset:elements'));

        // Prevent subsequent call to stopThreadResultMerging
        this._hasStopResultMergingBeenCalled = true;
        // Allow next call to startThreadResultMerging
        this._hasStartResultMergingBeenCalled = false;
      }
    },

    isMerging: function () {
      return this._hasStartResultMergingBeenCalled && this._hasMergedOnce;
    },

    setMap: function (map) {
      this.mapData = MapDataManager.get(map);

      if (this._hasFailedSetMapQueryUpdaterOnHiddenThread) {
        this._setMapQueryUpdaterOnHiddenThread();
      }
    },

    _setMapQueryUpdaterOnHiddenThread: function () {

      var hiddenThread = this._get('hiddenThread');

      hiddenThread.set('queryUpdater', new QueryAutoUpdaterMapOperations({
        mapData: this.mapData,
        thread: hiddenThread,
      }));

      hiddenThread.get('queryUpdater').forceUpdate();
    },

    _shouldSetMapQueryUpdater: function () {

      if (this.mapData) {

        this._setMapQueryUpdaterOnHiddenThread();
      } else {

        this._hasFailedSetMapQueryUpdaterOnHiddenThread = true;
      }
    },

    _shouldSetDefaultQueryUpdater: function () {

      var hiddenThread = this._get('hiddenThread');

      hiddenThread.set('queryUpdater', new QueryAutoUpdater({
        thread: hiddenThread
      }));

      hiddenThread.resetElements([]);
      this._hasFailedSetMapQueryUpdaterOnHiddenThread = false;
    },

    _initForwardEvents: function () {
      this.listenTo(this._get('thread'), 'all', function (name) {
        if (name !== 'reset:elements') {
          this.trigger.apply(this, arguments);
        }
      });
    },

    _storeNewData: function (name, collection, diffs) {
      this._elements[name] = collection;
      this._diffs[name] = diffs;
    },

    _scheduleTriggerResetElements: function () {
      clearTimeout(this._triggerResetElementTimeoutId);
      this._triggerResetElementTimeoutId = setTimeout(_.bind(this._triggerResetElements, this), 50);
    },

    _triggerResetElements: function () {
      this._elementsMerged.reset(_.reduce(this._elements, function (result, value) {
        return result.concat(value.models);
      }, []));
      this._diffsMerged = _.reduce(this._diffs, function (result, value) {
        result.delta.added = result.delta.added.concat(value.delta.added);
        result.delta.removed  = result.delta.removed.concat(value.delta.removed);
        result.previousModels = result.previousModels.concat(value.previousModels);
        return result;
      }, { delta: { added: [], removed: [] }, previousModels: []});

      this._hasMergedOnce = true;
      this.trigger('reset:elements', this._elementsMerged, this._diffsMerged);
    },

    _initGetSetMethods: function () {
      this.get = function (value) {
        if (value === 'elements' && this.isMerging()) {
          return this._elementsMerged;
        }
        return Thread.prototype.get.apply(this._get('thread'), arguments);
      };

      this.set = function () {
        return Thread.prototype.set.apply(this._get('thread'), arguments);
      };
    },

    _get: function () {
      return Backbone.Model.prototype.get.apply(this, arguments);
    },

    _set: function () {
      return Backbone.Model.prototype.set.apply(this, arguments);
    },

    getMainThread: function () {
      return this._get('thread');
    },

    getHiddenThread: function () {
      return this._get('hiddenThread');
    },

    createNestedThread: function () {
      return Thread.prototype.createNestedThread.apply(this._get('thread'), arguments);
    },

    getAvailableOperations: function () {
      return Thread.prototype.getAvailableOperations.apply(this._get('thread'), arguments);
    },

    pushOperation: function () {
      return Thread.prototype.pushOperation.apply(this._get('thread'), arguments);
    },

    removeOperations: function () {
      return Thread.prototype.removeOperations.apply(this._get('thread'), arguments);
    },

    limitWith: function () {
      return Thread.prototype.limitWith.apply(this._get('thread'), arguments);
    },

    hasOperation: function () {
      return Thread.prototype.hasOperation.apply(this._get('thread'), arguments);
    },

    getOperation: function () {
      return Thread.prototype.getOperation.apply(this._get('thread'), arguments);
    },

    updateOperations: function () {
      return Thread.prototype.updateOperations.apply(this._get('thread'), arguments);
    },

    hasLimit: function () {
      return Thread.prototype.hasLimit.apply(this._get('thread'), arguments);
    },

    clone: function () {
      return Thread.prototype.clone.apply(this._get('thread'), arguments);
    },

    isSameAs: function () {
      return Thread.prototype.isSameAs.apply(this._get('thread'), arguments);
    },

    parse: function () {
      return Thread.prototype.parse.apply(this._get('thread'), arguments);
    },

    getTranslated: function () {
      return Thread.prototype.getTranslated.apply(this._get('thread'), arguments);
    },

    toJSON: function () {
      return Thread.prototype.toJSON.apply(this._get('thread'), arguments);
    },

    getSummary: function () {

      // TODO: temporary fix: what should we do with summary when only the hidden thread is updated ?
      if (!this.get('focus') && this.oldSummary) {
        return this.oldSummary;
      }

      var summaryHiddenThread;
      var summaryMainThread = this._get('thread').getSummary();

      if (this._get('hiddenThread')) {
        summaryHiddenThread = this._get('hiddenThread').getSummary();
        this.oldSummary = {
          numberOfItems: summaryHiddenThread.numberOfItems + summaryMainThread.numberOfItems,
          numberOfAlerts: summaryHiddenThread.numberOfAlerts + summaryMainThread.numberOfAlerts,
        };
      } else {
        this.oldSummary = summaryMainThread;
      }

      return this.oldSummary;
    },

    clearElementsRelations: function () {
      this._get('thread').clearElementsRelations();
      this._get('hiddenThread').clearElementsRelations();
    },

    clear: function () {
      this._get('thread').clear();
      this._get('hiddenThread').clear();
    },

    isContainingGroups: function () {
      return Thread.prototype.isContainingGroups.apply(this._get('thread'), arguments);
    },

    isContainingClusters: function () {
      return Thread.prototype.isContainingClusters.apply(this._get('thread'), arguments);
    },

    isContainingItems: function () {
      return Thread.prototype.isContainingItems.apply(this._get('thread'), arguments);
    },

    refreshResult: function () {
      return Thread.prototype.refreshResult.apply(this._get('thread'), arguments);
    },

    url: function () {
      return Thread.prototype.url.apply(this._get('thread'), arguments);
    },

    getAttribute: function () {
      return Thread.prototype.getAttribute.apply(this._get('thread'), arguments);
    },

    getAttributesForOperation: function () {
      return Thread.prototype.getAttributesForOperation.apply(this._get('thread'), arguments);
    },

    ///////////////////////////////////////////////////////////////////////////////////
    // No doubts for what follow, it needs to be handled by the original thread only.

    getAvailableMetrics: function () {
      return Thread.prototype.getAvailableMetrics.apply(this._get('thread'), arguments);
    },

    getAvailableMetric: function () {
      return Thread.prototype.getAvailableMetric.apply(this._get('thread'), arguments);
    },

    refreshMetricsRanges: function () {
      return Thread.prototype.refreshMetricsRanges.apply(this._get('thread'), arguments);
    },

    setDisplayedMetrics: function () {
      return Thread.prototype.setDisplayedMetrics.apply(this._get('thread'), arguments);
    },

    removeDisplayedMetric: function () {
      return Thread.prototype.removeDisplayedMetric.apply(this._get('thread'), arguments);
    },

    getStateChangeTimeout: function () {
      return Thread.prototype.getStateChangeTimeout.apply(this._get('thread'), arguments);
    },

    refreshMetricRange: function () {
      return Thread.prototype.refreshMetricRange.apply(this._get('thread'), arguments);
    },

    getRemovedElementsCounter: function () {
      return Thread.prototype.getRemovedElementsCounter.apply(this._get('thread'), arguments);
    },

    sortBy: function () {
      return Thread.prototype.sortBy.apply(this._get('thread'), arguments);
    },

    getSort: function () {
      return Thread.prototype.getSort.apply(this._get('thread'), arguments);
    },

    filter: function () {
      return Thread.prototype.filter.apply(this._get('thread'), arguments);
    },

    isGrouped: function () {
      return Thread.prototype.isGrouped.apply(this._get('thread'), arguments);
    },

    getDisplayableProperties: function () {
      return Thread.prototype.getDisplayableProperties.apply(this._get('thread'), arguments);
    },

    resetElements: function () {
      return Thread.prototype.resetElements.apply(this._get('thread'), arguments);
    }

  });



  return MetaThread;
});
