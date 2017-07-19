import Backbone = require('backbone');

class TableHeaderMonitor extends Backbone.View<any> {

  busEvent: Backbone.Events;
  listeningTo: {[index: string]: boolean} = {};

  constructor(el: HTMLElement) {
    super({ el: el });
  }

  listenToColumn(class_col: string, col: number) {
    if (!this.listeningTo[class_col]) {
      this.$el.on('click', '.' + class_col + ':not(.is-collapsed)', () => {
        this.busEvent.trigger('collapse:column', col);
      });
      this.$el.on('click', '.is-collapsed.' + class_col, () => {
        this.busEvent.trigger('expand:column', col);
      });
      this.listeningTo[class_col] = true;
    }
  }

  remove(): TableHeaderMonitor {
    _.forEach(_.keys(this.listeningTo), (class_col) => {
      this.$el.off('click', '.' + class_col + ':not(.is-collapsed)');
      this.$el.off('click', '.is-collapsed.' + class_col);
    });
    super.remove();

    return this;
  }

}

export = TableHeaderMonitor;
