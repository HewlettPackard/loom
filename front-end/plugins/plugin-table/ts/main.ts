import ThreadTableView = require('weaver-table/views/ThreadTableView');
import pluginAPI = require('plugins/pluginAPI');

require('../less/index.less');

pluginAPI.registerDisplayMode({
  name: 'table',
  humanReadableName: 'Table',
  availability: pluginAPI.always,
  threadViewClass: ThreadTableView,
});
