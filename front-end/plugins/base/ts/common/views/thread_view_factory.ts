import Thread = require('weft/models/Thread');
import SelectionService = require('weft/services/SelectionService');
import ProvidersLegendService = require('weft/services/ProvidersLegendService')
import DisplayMode = require('../utils/DisplayMode');
import module_dm = require('../../features/utils/display_mode');
import Backbone = require('backbone');

import View = Backbone.View;
import displayModeAvailables = module_dm.displayModeAvailables;

export function main(thread: Thread, selectionService?: SelectionService, providerLegendService?: ProvidersLegendService): View<Thread> {
  var displayMode = thread.get('displayMode');
  displayMode = displayMode ? displayMode: DisplayMode.CLASSIC;

  return new displayModeAvailables[displayMode].threadViewClass({
      model: thread,
      selectionService: selectionService,
      providerLegendService: providerLegendService
  });
}

export function subThread(thread: Thread, otherOptions: any = {}): View<Thread> {
  var displayMode = thread.get('displayMode');
  displayMode = displayMode ? displayMode: DisplayMode.CLASSIC;

  otherOptions.model = thread;

  return new displayModeAvailables[displayMode].threadViewClass(otherOptions);
}
