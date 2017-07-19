import _ = require('lodash');
import ThreadListView = require('weaver/views/ThreadListView');

import thread_view_factory = require('../../common/views/thread_view_factory');
import module_dm = require('./display_mode');
import DisplayMode = require('../../common/utils/DisplayMode');

import displayModeAvailables = module_dm.displayModeAvailables;

//var originalDisplayThread = ThreadListView.prototype.displayThread;
var originalRegisterModelListeners = ThreadListView.prototype._registerModelListeners;

var createNewThread = function (thread) {

  var newthread = thread.clone();
  var from_display_mode = thread._previousAttributes.displayMode;
  var to_display_mode = thread.get('displayMode');

  var query = displayModeAvailables[from_display_mode]
    .queryCleaner.transition(newthread.get('query'), to_display_mode);
  // Set the new query.
  newthread.set('query', query, {silent: true});

  return newthread;
};

var changeViewForThread = function (thread) {

  var newthread = createNewThread(thread);
  var oldPosition = this.threadViews[thread.id].$el;

  var view = this.createView(newthread);

  view.$el.insertAt(oldPosition.index(), this.$el);
  view.render();

  this.model.remove(thread);
  this.model.add(newthread, {
    silent: true
  });

  view.dispatchCustomEvent('didDisplayThread', {
    thread: newthread
  });

  this.threadViews[newthread.id] = view;
};


/*ThreadListView.prototype.displayThread = function (thread) {
  // A thread must have a displayMode from this point.
  if (_.isUndefined(thread.get('displayMode'))) {

  }

  return originalDisplayThread.apply(this, arguments);
};*/

ThreadListView.prototype.createView = function (thread) {
  return thread_view_factory.main(thread, this.options.selectionService, this.options.providerLegendService);
};

ThreadListView.prototype._registerModelListeners = function () {

  originalRegisterModelListeners.apply(this, arguments);
  this.listenTo(this.model, 'change:displayMode', changeViewForThread);
};

var decoration = () => {};
export = decoration;
