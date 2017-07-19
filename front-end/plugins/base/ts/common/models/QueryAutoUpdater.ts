import _ = require('lodash');
import Backbone = require('backbone');
import Thread = require('weft/models/Thread');

/**
 * The query auto-updater is a per-thread controller
 * that automatically update operations that are in "auto" mode.
 * If you want to create your own, just subclass this one
 * and set it in your subclass of ThreadView.
 *
 * Currently they are two updaters availables:
 *   - QueryAutoUpdaterMapOperations
 *   - QueryAutoUpdaterBraiding
 *
 * @class QueryAutoUpdater
 * @namespace weaver-map.models
 * @module weaver-map
 */
class QueryAutoUpdater extends Backbone.Events {

  thread: Thread;

  constructor(options) {
    if (_.isFunction(Backbone.Events)) {
      super();
    }
    options = options || {};

    this.thread = options.thread;

    return this.creator.apply(this, arguments);
  }

  /**
   * Contructor called that is allow to return an object on construction.
   * Usefull to allow caching.
   *
   * The default constructor should always be called, it takes care
   * of removing itself when the query updater is changed.
   *
   * @param {Object} options is the traditional arguments object map like.
   * @return {Object} Returns the constructed object.
   */
  creator(options?: any): any {

    this.listenTo(this.thread, 'change:queryUpdater', function (thread, newQueryUpdater) {

      if (newQueryUpdater !== this) {
        this.detach();
      }
    });

    // Unnecessary but self documenting.
    return this;
  }

  /**
   * Detach the query updater that has been replaced by a new one.
   * By default call, the stopListening method, that should be more than
   * sufficient in most of cases.
   */
  detach(): void {

    this.stopListening();
  }

  /**
   * Force the update of the query.
   * You have to override this method to allow users of query Updater
   * to force an update of their query.
   */
  forceUpdate(): void {

    throw "This function should be overriden";
  }
}

export = QueryAutoUpdater;
/**
* Offer Backbone.Events facilities to QueryAutoUpdaters.
*/
_.extend(QueryAutoUpdater.prototype, Backbone.Events);
