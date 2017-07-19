import Thread = require('weft/models/Thread');
import Query = require('weft/models/Query');
import _ = require('lodash');
import QueryAutoUpdaterBraiding = require('../../common/models/QueryAutoUpdaterBraiding');
import Translator = require('../../common/models/Translator');
import thread_attributes_to_clone = require('./thread_attributes_to_clone');
import DisplayMode = require('../../common/utils/DisplayMode');

var originalDefaults = Thread.prototype.defaults;

Thread.prototype.defaults = function () {
  var def = originalDefaults.call(this);
  return _.extend(def, {

    /**
     * The query updater is a per-thread controller that is allow to update
     * the query operations based on external events. The default one, provided here,
     * does nothing.
     *
     * @see QueryAutoUpdaterMapOperations
     * @see QueryAutoUpdaterBraiding
     *
     * @attribute queryUpdater
     * @type {QueryAutoUpdater}
     */
    queryUpdater: new QueryAutoUpdaterBraiding({
      thread: this
    }),

    /**
     * The string contains an indication for representing the data.
     * To see the full list of displayModes see DisplayMode in Weaver.
     * @property displayMode
     * @default undefined (The thread is not displayed yet)
     */
    displayMode: DisplayMode.CLASSIC,

    /**
     * Contains the different cluster operations.
     * @type {Object}
     */
    itemClusterBy: {},

    /**
     * Translator reference, Allows to show better names than
     * the one outputed by loom. Currently essentially used for countries.
     */
    translator: new Translator(),

    /**
     * Attribute storing view-specific data.
     * @attribute viewData
     */
    viewData: undefined,
  });
};

Thread.prototype.clone = function () {
  var items = {};

  _.forEach(thread_attributes_to_clone, (attribute) => {
    items[attribute] = this.get(attribute);
  });

  items['query'] = this.get('query').clone();
  items['viewData'] = this.get('viewData') ? this.get('viewData').clone() : undefined;

  return new Thread(items);
};

Thread.prototype.createNestedThread = function (aggregation) {

  var nestedThread = this.clone();
  var displayMode = aggregation.get('displayMode');
  displayMode = displayMode ? displayMode: DisplayMode.CLASSIC;
  nestedThread.set({
    parent:  this,
    aggregation: aggregation,
    name: aggregation.get('name'),
    displayMode: displayMode,
    query: this.createNestedThreadQuery(aggregation),
  });

  return nestedThread;
};

// Ignored: Warning 'getTranslated' does not exist on type Thread
Thread.prototype.getTranslated = function () {
  var res = Thread.prototype.get.apply(this, arguments);

  if (_.isString(res)) {
    return this.get('translator').translate(res);
  }

  return res;
};

var decorate = () => {};
export = decorate;
