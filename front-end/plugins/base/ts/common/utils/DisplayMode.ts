import display_mode = require('../../features/utils/display_mode');

import AvailableDisplayMode = display_mode.AvailableDisplayMode;
import DisplayModeReadable = display_mode.DisplayModeReadable;
import displayModeAvailables = display_mode.displayModeAvailables;



class DisplayMode {

  static CLASSIC = 'classic';

  private thread: Thread;

  constructor(options: { thread: Thread }) {
    this.thread = options.thread;
  }

  hasManyDisplayMode(): boolean {
    return this.getPossibleDisplayModeArray().length > 1;
  }

  getPossibleDisplayModeObject(): AvailableDisplayMode {
    return _.omit<AvailableDisplayMode, AvailableDisplayMode>(displayModeAvailables, (context) => {
      return context.available(this.thread) !== true;
    });
  }

  getPossibleDisplayModeWithReadableName(): DisplayModeReadable {
    return _.mapValues(this.getPossibleDisplayModeObject(), function (context) {
      return context.readableName;
    });
  }

  getPossibleDisplayModeArray(): Array<string> {
    return _.keys(this.getPossibleDisplayModeObject());
  }
}

export = DisplayMode;
