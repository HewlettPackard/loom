import Backbone = require('backbone');
import optimalBraiding = require('../../features/utils/_braiding_controller_decoration');
import Operation = require('weft/models/Operation');

/**
 * A QueryValidator is used by the QueryEditor to
 * perform check on the query being validated. It is allowed
 * to modify the query before the new query is set to the thread.
 *
 * Subclasses of QueryValidator can override the following method:
 *   - initialize     is called after construction.
 *   - validateQuery  is called whenever the editor generate the query.
 *
 * @see QueryValidatorMap
 *
 * @param {Object} options must contains the referenced thread.
 */
class QueryValidator {

  thread: Thread;

  constructor(options: {thread: Thread}) {

    this.thread = options.thread;

    this.initialize.apply(this, arguments);
  }

  /**
   * Function that can modify the given query following test.
   *
   * @param  {Query} query is the query to validate.
   * @return {Query}       Returns the new query modified.
   */
  validateQuery(query: Query): Query {

    // The query has no limit ?
    if (!query.hasLimit()) {

      query = query.limitWith({
        operator: Operation.BRAID_ID,
        parameters: {
          maxFibres: optimalBraiding.getValue(),
        }
      });
    }

    return query;
  }

  initialize(options?: { thread: Thread }): void {
    var query = this.thread.get('query');

    query = this.validateQuery(query);

    this.thread.set('query', query);
  }

}

export = QueryValidator;
