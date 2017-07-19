
import QueryAutoUpdater = require('./QueryAutoUpdater');
import Operation = require('weft/models/Operation');

/**
 * A QueryAutoUpdaterBraiding update the braid limit
 *
 * @class QueryAutoUpdaterBraiding
 * @namespace weaver-map.models
 * @module weaver-map
 */
class QueryAutoUpdaterBraiding extends QueryAutoUpdater {

  optimalBraiding: number;

  constructor(options?: any) {
    super(options);
  }

  creator(options?: any): any {

    super.creator.apply(this, arguments);

    // --------------------------------------------------------
    // We listen to optimal braiding change
    //
    // @see BraidingControllerDecorator
    //
    this.listenTo(this.thread, 'optimalBraiding', function (optimalBraiding) {

      // Update
      this._updateBraiding(optimalBraiding);

      // Stored to allow forced update.
      this.optimalBraiding = optimalBraiding;
    });
  }

  forceUpdate(): void {

    if (this.optimalBraiding) {
      this._updateBraiding(this.optimalBraiding);
    }
  }

  private _updateBraiding(optimalBraiding: number): void {

    // We update appropriately the thread.
    if (this.thread.hasLimit(Operation.BRAID_ID)) {

      this.thread.limitWith({
        operator: Operation.BRAID_ID,
        parameters: {
          maxFibres: optimalBraiding
        }
      });
    }
  }

}

export = QueryAutoUpdaterBraiding;
