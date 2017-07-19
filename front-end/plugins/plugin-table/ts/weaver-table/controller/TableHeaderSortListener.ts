import Backbone = require('backbone');

class TableHeaderSortMonitor extends Backbone.View<any> {

  busEvent: Backbone.Events;
  listeningTo: {[index: string]: boolean} = {};

  constructor(el: HTMLElement) {
    super({ el: el });
  }

  listenToColumn(class_col: string, col: number) {
    if (!this.listeningTo[class_col]) {
      this.$el.on('click', '.' + class_col, () => {
        this.busEvent.trigger('sort:column', col);
      });
      this.listeningTo[class_col] = true;
    }
  }

  remove(): TableHeaderSortMonitor {
    _.forEach(_.keys(this.listeningTo), (class_col) => {
      this.$el.off('click', '.' + class_col);
    });
    super.remove();

    return this;
  }
}

export = TableHeaderSortMonitor;
