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
  var Backbone = require('backbone');
  var Thread = require('weft/models/Thread');

  /**
   * A Pattern is a collection of Threads that should be displayed together
   * @class  Pattern
   * @module  weft
   * @submodule models
   * @namespace  models
   * @constructor
   * @extends Backbone.Model
   */
  var Pattern = Backbone.Model.extend({

    constructorName: 'LOOM_Pattern',

    /**
     * @property {String} id The ID of the pattern
     */

    /**
     * @attribute {String} name A human readable name for the pattern
     */

    /**
     * @attribute {Array} threads The list of Threads composing the pattern
     */

    /**
     * A flag marking if this Pattern is part of the Tapestry
     * @attribute {Boolean} isInTapestry
     * @default false
     */

    /**
     * Returns the list of Threads in this Pattern that are not already
     * in given `listOfThreads`
     * @method  getMissingThreads
     * @param   {Backbone.Collection} listOfThreads
     * @return  {Array}
     */
    getMissingThreads: function (listOfThreads) {
      return _.filter(this.get('threads'), function (threadDefinition) {
        return !listOfThreads.find(threadDefinition.isSameAs, threadDefinition);
      });
    },

    /**
     * Finds Threads that are part of this Pattern in given list of Threads
     * @method  findThreads
     * @param   {Backbone.Collection} listOfThreads
     * @return  {Array}
     */
    findThreads: function (listOfThreads) {
      return _.filter(listOfThreads.models, function (thread) {
        return _.find(this.get('threads'), thread.isSameAs, thread);
      }, this);
    },

    /**
     * Checks if all the Threads of this Pattern are listed
     * in given `listOfThreads`
     * @method hasAllThreadsIn
     * @param  {Backbone.collection}  listOfThreads [description]
     * @return {Boolean}
     */
    hasAllThreadsIn: function (listOfThreads) {
      var visibleThreads = listOfThreads.filter(function (thread) {
        return !thread.get('hidden');
      });
      return _.every(this.get('threads'), function (threadDefinition) {
        return _.find(visibleThreads, threadDefinition.isSameAs, threadDefinition);
      });
    },

    /**
     * [Backbone's Model.parse()](http://backbonejs.org/#Model-parse)
     * @method parse
     * @return {[type]} [description]
     * @since  2.0
     */
    parse: function (json) {
      // Parse ItemType to create DisplayableProperties in Thread
      var result = _.omit(json, 'threads', '_meta');
      result.threads = this._parseThreads(json) || [];
      return result;
    },

    /**
     * Checks if the pattern is offered by any of the providers
     * in given list
     * @method isOfferedBy
     * @param  {Array|models.Provider}  providers One or multiple providers
     * @return {Boolean}
     */
    isOfferedBy: function (providers) {
      if (!_.isArray(providers)) {
        providers = [providers];
      }
      return _.find(providers, function (provider) {
        return provider.isOffering(this);
      }, this);
    },

    /**
     * @method getThreadsItemTypes
     * @returns {string[]}
     */
    getThreadsItemTypes: function () {
      return _.keys(_.reduce(this.get('threads'), function (itemTypes, thread) {
        itemTypes[thread.get('itemType').id] = true;
        return itemTypes;
      }, {}));
    },

    /**
     * @method _parseThreads
     * @param json
     * @returns {TResult[]}
     * @private
     */
    _parseThreads: function (json) {
      var itemTypeDefinitions = {};
      if (json._meta) {
        itemTypeDefinitions = json._meta.itemTypes || {};
      }
      return _.map(json.threads, function (json) {
        return this._parseThread(json, itemTypeDefinitions);
      }, this);
    },

    /**
     * @method _parseThread
     * @param json
     * @param itemTypeDefinitions
     * @returns {Thread}
     * @private
     */
    _parseThread: function (json, itemTypeDefinitions) {
      if (json.itemType && _.isString(json.itemType)) {
        json.itemType = itemTypeDefinitions[json.itemType];
      }
      return new Thread(json, {
        parse: true
      });
    }
  });

  Pattern.PROVIDERS_PATTERN_ID = 'Providers';

  /**
   * @method join
   * @returns {*}
   */
  Pattern.join = function () {
    return new Pattern({
      threads: _.reduce(arguments, function (threadList, pattern) {
        return pattern ? threadList.concat(pattern.get('threads')) : threadList;
      }, [])
    });
  };

  return Pattern;
});
