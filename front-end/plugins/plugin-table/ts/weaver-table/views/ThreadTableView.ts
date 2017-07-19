import ThreadView = require('weaver/views/ThreadView');
import TableView = require('./TableView');
import Thread = require('weft/models/Thread');
import ViewOptions = Backbone.ViewOptions;

class ThreadTableView extends ThreadView {

  constructor(options: ViewOptions<Thread>) {
    super(options);

    this.listenTo(this.model, 'didPressActionView', this._toggleThreadDisplay);
  }

  _createViewElements(): any {
    return new TableView({
      className: 'mas-thread--fibers',
      model: this.model
    });
  }
}

export = ThreadTableView;
