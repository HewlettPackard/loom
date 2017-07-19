import ThreadView = require('weaver/views/ThreadView');
import TreeMapView = require('./TreeMapView');

class ThreadTreeMapView extends ThreadView {

  elementsView: TreeMapView;

  constructor(args: {model: Thread; selectionService: SelectionService}) {
    super(args);

    this.listenTo(this.model, 'didPressActionView', this._toggleThreadDisplay);
  }

  _createViewElements(): any {
    return new TreeMapView({
      className: 'mas-thread--fibers mas-flex',
      model: this.model
    });
  }

}

export = ThreadTreeMapView;
