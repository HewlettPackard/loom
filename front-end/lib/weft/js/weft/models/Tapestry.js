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

  require('setimmediate');
  var _ = require('lodash');
  var Backbone = require('backbone');
  var FibersIndex = require('./Tapestry/FibersIndex');
  var RelationTypeList = require('./Tapestry/RelationTypeList');
  var SharedQueryResultsManager = require('./Tapestry/SharedQueryResultsManager');
  var FibersLinker = require('../services/FibersLinker');

  /**
   * @class Tapestry
   * @module weft
   * @namespace models
   * @submodule models
   * @extends Backbone.Model
   * @constructor
   */
  var Tapestry = Backbone.Model.extend({

    constructorName: 'LOOM_Tapestry',

    urlRoot: '/tapestries',

    // TODO (in later iteration) Add logic to remove nested Threads when parent Threads get removed

    defaults: function () {
      return {
        /**
         * The list of Threads displayed by the Tapestry
         * @attribute threads {Backbone.Collection}
         */
        threads: new Backbone.Collection()
      };
    },

    initialize: function (models, options) {
      this.threadId = 0;
      options = options || {};
      if (options.aggregator) {
        this.aggregator = options.aggregator;
      }

      /**
       * @attribute relationTypeList
       * @type {RelationTypeList}
       */
      this.set('relationTypeList', new RelationTypeList([], {
        tapestry: this
      }));

      /**
       * @attribute sharedQueryResults
       * @type {SharedQueryResultsManager}
       */
      this.set('sharedQueryResults', new SharedQueryResultsManager({
        threads: this.get('threads'),
        aggregator: this.aggregator
      }));

      /**
       * @attribute fibersIndex
       * @type {FibersIndex}
       */
      var fibersIndex = new FibersIndex({
        queryResults: this.get('sharedQueryResults').get('results')
      });

      this.set('fibersIndex', fibersIndex);

      /**
       * @attribute fibersLinker
       * @type {FibersLinker}
       */
      this.set('fibersLinker', new FibersLinker({
        fibers: fibersIndex.get('index')
      }));

      this.listenTo(this.get('threads'), 'add change:query', this._updateQueryability, this);
    },

    // PUBLIC API

    /**
     * Adds the Threads of given pattern to the Tapestry
     * @param  {models.Pattern} pattern
     * @chainable
     */
    load: function (pattern) {

      this.add(_.map(pattern.get('threads'), function (thread) {
        return thread.clone();
      }));

      return this;
    },

    /**
     * Adds one or multiple Threads to the Tapestry
     * @param {models.Thread} threads
     * @chainable
     */
    add: function (threads) {

      this._assignIds(threads);
      this.get('threads').add(threads);
      return this;
    },

    /**
     * Removes one or multiple Threads from the Tapestry
     * @param  {models.Thread} threads
     * @chainable
     */
    remove: function (threads) {

      this.get('threads').remove(threads);
      return this;
    },

    // Overrides the save function to use the aggregator the tapestry is linked to
    save: function () {

      this.isSyncScheduled = false;
      if (this.aggregator) {
        return this.aggregator.syncTapestry(this)
          .then(this._markThreadsAsQueryable)
          .then(this._notifySync)
          .done();
      }
    },

    url: function () {

      return this.urlRoot + '/' + this.id;
    },

    toJSON: function () {

      var result = {
        threads: this.get('threads').toJSON()
      };

      if (this.id) {
        result.id = this.id;
      }

      return result;
    },

    attachAutoSyncOnEvents: function () {
      this.listenTo(this.get('threads'), 'add remove change:query', this._autoSync, this);
    },

    // PRIVATE HELPERS
    _autoSync: function () {

      if (!this.isSyncScheduled) {
        this.isSyncScheduled = true;
        setImmediate(_.bind(this.save, this));
      }
    },

    _updateQueryability: function (thread) {
      thread.get('result').set('queryable', false);
    },

    _markThreadsAsQueryable: function (tapestry) {

      tapestry.get('threads').forEach(function (thread) {

        var result = thread.get('result');
        result.set('queryable', true);//, {silent: true});
        //result.trigger('change:queryable', result, true);
      });

      return tapestry;
    },

    _notifySync: function (tapestry) {

      tapestry.trigger('syncComplete', tapestry);
      return tapestry;
    },

    // Simple numeric IDs should be sufficient before we get a collision
    _assignIds: function (threads) {

      if (_.isArray(threads)) {
        _.forEach(threads, this._assignIds, this);
      } else {
        if (threads.get('tapestry') !== this) {
          threads.set({
            'id': this.threadId++,
            tapestry: this
          });
        }
      }
    }
  });

  return Tapestry;
});
