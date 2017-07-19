import QueryValidator = require('../../common/models/QueryValidator');
import QueryAutoUpdaterBraiding = require('../../common/models/QueryAutoUpdaterBraiding');
import QueryEditor = require('weaver/views/QueryEditor');
import ThreadView = require('weaver/views/ThreadView');
import DisplayMode = require('../../common/utils/DisplayMode');
import _ = require('lodash');

import thread_view_factory = require('../../common/views/thread_view_factory');

var originalInitialize = ThreadView.prototype.initialize;

ThreadView.prototype._createQueryEditor = function () {
  return new QueryEditor({
    queryValidator: new QueryValidator({
      thread: this.model
    }),
    model: this.model,
    collapsed: true,
    el: this.$('.mas-thread--queryEditor')
  });
};

ThreadView.prototype.initialize = function () {
  originalInitialize.apply(this, arguments);

  this.model.set('queryUpdater', this._createQueryUpdater());
};

ThreadView.prototype._createQueryUpdater = function () {
  return new QueryAutoUpdaterBraiding({
    thread: this.model,
  });
};

ThreadView.prototype._createSubThreadView = function (thread) {
  return thread_view_factory.subThread(thread);
};

ThreadView.prototype._toggleThreadDisplay = function (aggregation: any, value?: string) {

  value = _.isString(value) ? value : DisplayMode.CLASSIC;
  aggregation.set('displayMode', value);
  if (this.subThread && this.subThread.model.get('aggregation') === aggregation) {

    // If the user is switching the display mode only.
    if (this.subThread.model.get('displayMode') !== aggregation.get('displayMode')) {

      this.stopListening(this.subThread.model, 'change:displayMode');
      _displayNestedThread(this, aggregation);

    } else {

      this.stopListening(this.subThread.model, 'change:displayMode');
      this.removeNestedThread();
    }
  } else {

    _displayNestedThread(this, aggregation);
  }
};

function _displayNestedThread (self: ThreadView, aggregation: any): void {
  var displayMode = aggregation.get('displayMode');
  var handler = function () {
    aggregation.set('displayMode', displayMode);
  };
  self.$el.on('didRemoveThread', handler);
  self.displayNestedThread(aggregation)
    .then(function (subThread) {
      self.listenTo(subThread.model, 'change:displayMode', function (thread, value: string) {

        var aggregation = thread.get('aggregation');
        thread.set('displayMode', aggregation.get('displayMode'), {silent: true});
        self._toggleThreadDisplay(aggregation, value);
      });
    })
    .done();
  self.$el.off('didRemoveThread', handler);
};

var decorate = () => {};
export = decorate;
