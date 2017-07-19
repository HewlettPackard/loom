
import _ = require('lodash');
import thread_attributes_to_clone = require('./features/utils/thread_attributes_to_clone');
import dictionary = require('./common/models/dictionary');
import DefaultQueryCleaner = require('./common/utils/DefaultQueryCleaner');
import display_mode = require('./features/utils/display_mode');
import displayModeAvailables = display_mode.displayModeAvailables;
import IQueryCleaner = display_mode.IQueryCleaner;

// Required decorations
import displayModeFeature = require('./features/displayMode');
displayModeFeature();
import translatorFeature = require('./features/translator');
translatorFeature();
import thread_view_decoration = require('./features/utils/_thread_view_decoration');
thread_view_decoration();
import query_editor_decoration = require('./features/utils/_query_editor_decoration');
query_editor_decoration();
import thread_list_view_decoration = require('./features/utils/_thread_list_view_decoration');
thread_list_view_decoration();
import braiding_controller_decoration = require('./features/utils/_braiding_controller_decoration');
braiding_controller_decoration.getValue();

require('../less/style.less');

export interface Dictionary {
  [index: string]: string;
}

export function addTranslations(str: string, traductions: Dictionary): void {
  _.assign(dictionary[str], traductions);
}

export interface DisplayModeRegistration {
  name: string;
  humanReadableName: string;
  availability: (thread: Thread) => boolean;
  threadViewClass: any;
  queryCleaner?: IQueryCleaner;
}

export var always = () => true;

export function registerDisplayMode(register: DisplayModeRegistration): void
{
  displayModeAvailables[register.name] = {
    readableName: register.humanReadableName,
    available: register.availability,
    threadViewClass: register.threadViewClass,
    queryCleaner: register.queryCleaner ? register.queryCleaner: new DefaultQueryCleaner(),
  }
}

export function registerThreadAttributes(...attributes: string[]): void {
  var test = _.uniq(thread_attributes_to_clone.concat(attributes));
  // Useless but to avoid complain from tsc.
  while(thread_attributes_to_clone.pop());
  thread_attributes_to_clone.push.apply(thread_attributes_to_clone, test);
}
