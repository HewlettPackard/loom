import Query = require('weft/models/Query');
import module_dm = require('../../features/utils/display_mode');
import IQueryCleaner = module_dm.IQueryCleaner;

class QueryCleaner implements IQueryCleaner {

  transition(query: Query, to_display_mode: string): Query {
    // Does nothing
    return query;
  }
}

export = QueryCleaner;
