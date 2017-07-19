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
  var confirm = require('weaver/utils/confirm');

  /**
   * DefaultPatternLoader monitors given list of Providers to prompt users,
   * when they login to a Provider, if they want to add its default Pattern
   * to given list of Threads
   * @class DefaultPatternLoader
   * @module weaver
   * @submodule screens.TapestryScreen
   * @namespace screens.TapestryScreen
   * @constructor
   * @extends Backbone.Events
   */
  function DefaultPatternLoader(providers, threads, braidingController) {
    this.threads = threads;
    this.listenTo(providers, 'change:loggedIn', this._maybeAddDefaultPattern);
    this.braidingController = braidingController;
  }

  _.merge(DefaultPatternLoader.prototype, Backbone.Events, {

    /**
     * @method _maybeAddDefaultPattern
     * @param provider
     * @private
     */
    _maybeAddDefaultPattern: function (provider) {
      if (provider.get('loggedIn')) {
        this._confirmDefaultPatternAdd(provider);
      }
    },

    /**
     * @method _confirmDefaultPatternAdd
     * @param provider
     * @private
     */
    _confirmDefaultPatternAdd: function (provider) {
      var pattern = provider.getDefaultPattern();
      if (pattern && !pattern.hasAllThreadsIn(this.threads)) {
        var missingThreads = pattern.getMissingThreads(this.threads);
        var message = this._getConfirmMessage(pattern, provider, missingThreads);
        var promise = this._promptConfirmation(message);
        promise.then(_.bind(function (confirmed) {
            if (confirmed) {
              this._addDefaultPattern(missingThreads);
            }
          }, this))
          .done();
      }
    },

    /**
     * @method _promptConfirmation
     * @param message
     * @returns {*}
     * @private
     */
    _promptConfirmation: function (message) {
      return confirm.confirm(message);
    },

    /**
     * @method _getConfirmMessage
     * @param pattern
     * @param provider
     * @param missingThreads
     * @returns {string}
     * @private
     */
    _getConfirmMessage: function (pattern, provider, missingThreads) {
      var base = "You've just logged in to the " + provider.get('name') + " provider. " +
        "Do you want to Add its default pattern (" + pattern.get('name') + ")?\n" +
        "This will add the following Threads to the screen:\n\n";

      _.forEach(missingThreads, function (thread) {
        base += ' - ' + thread.get('name') + ',\n';
      });

      // Trim the additional ',\n' inserted by the forEach
      return base.slice(0, -2) + '.';
    },

    /**
     * @method _addDefaultPattern
     * @param threads
     * @private
     */
    _addDefaultPattern: function (threads) {
      var clonedThreads = _.map(threads, function (thread) {
        return thread.clone();
      });
      this.threads.add(clonedThreads);
      this.braidingController.get('threads').add(clonedThreads);
    }
  });

  return DefaultPatternLoader;
});
