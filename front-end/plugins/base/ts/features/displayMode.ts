
// Force import resolution by tsc
import thread_decoration = require('./utils/_thread_decoration');
thread_decoration();
import thread_settings_menu_decoration = require('./utils/_thread_settings_menu_decoration');
thread_settings_menu_decoration();
import display_mode = require('./utils/display_mode');
import displayModeAvailables = display_mode.displayModeAvailables;
import DefaultQueryCleaner = require('../common/utils/DefaultQueryCleaner');
import ThreadView = require('weaver/views/ThreadView');

var always = () => true;

displayModeAvailables[
  'classic'
] = {
  readableName: 'Classic',
  available: always,
  threadViewClass: ThreadView,
  queryCleaner: new DefaultQueryCleaner(),
};

var feature = () => {};
export = feature;
