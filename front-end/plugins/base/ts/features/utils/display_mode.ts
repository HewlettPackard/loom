
import _ = require('lodash');
import Thread = require('weft/models/Thread');
import Query = require('weft/models/Query');

export interface IQueryCleaner {
  transition(query: Query, to_display_mode: string): Query;
}

export interface DisplayModeData {
  readableName: string;
  available: (thread?: Thread) => boolean;
  threadViewClass: any;
  queryCleaner: IQueryCleaner;
}

export interface AvailableDisplayMode {
  [index:string]: DisplayModeData;
}

export interface DisplayModeReadable {
  [index: string]: string;
}

/**
 * This object has keys that correspond to the possible values for model.get('displayMode')
 * The value of each key contains a readable name and a boolean to express the possibility
 * to use this mode for the model.
 */
export var displayModeAvailables: AvailableDisplayMode = {};
