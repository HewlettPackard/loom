
import ThreadTreeMapView = require('./weaver-treemap/views/ThreadTreeMapView');
import pluginAPI = require('plugins/pluginAPI');

require('../less/index.less');

pluginAPI.registerDisplayMode({
  name: 'treemap',
  humanReadableName: 'Tree Map',
  availability: pluginAPI.always,
  threadViewClass: ThreadTreeMapView,
});
