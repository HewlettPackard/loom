import AbstractFibreListener = require('plugins/common/views/AbstractFibreListener');
import ViewFibreOptions = require('plugins/interfaces/ViewFibreOptions');
import FibreBuilder = require('../layout/FibreBuilder');
import FibreContextMenu = require('./FibreContextMenu');
import $ = require('jquery');

class FibreView extends AbstractFibreListener {

  static factory(options: ViewFibreOptions<FibreBuilder>): FibreView {
    return new FibreView(options);
  }

  thread: Thread;
  builder: FibreBuilder;
  fibreContextMenu: FibreContextMenu;

  constructor(options?: ViewFibreOptions<FibreBuilder>) {
    super(options);
    this.thread = options.thread;
    this.builder = options.builder;
    this.$el.data('view', this);

    this.fibreContextMenu = new FibreContextMenu({
      el: this.$('.' + FibreContextMenu.className),
      model: this.model,
      thread: this.thread,
    });

    this.fibreContextMenu.render();
  }

  selectElement(event: any): void {
    super.selectElement(event);
    this.fibreContextMenu.show();
  }

  unselectElement(event: any): void {
    super.unselectElement(event);
    this.fibreContextMenu.hide();
  }

  // Does nothing as they're already being shown all the time.
  protected _updateElementDetails(selected: boolean): void {}

  protected _updateChangedAttributesFullList(attributes: Array<string>): void {
    var attribute_class: Array<string> = [];
    var children  = this.$el.children('.mas-table-cell');
    var row_css = _.filter(this.$el[0].className.split(' '), (css) => css === 'mas-last-two-row');
    for (var i = 0; i < children.length; ++i) {
      var child = children[i];
      var className = _.without(child.className.split(' '), 'mas-has-changed').join(' ');
      attribute_class.push(className);
    }
    var el = this.builder.buildFibre({ model: this.model, attribute_class: attribute_class, row_css: row_css}, attributes);
    this.$el.html($(el).html());
    this.fibreContextMenu.setElement(this.$('.' + FibreContextMenu.className));
    this.fibreContextMenu.render();
  }
}

export = FibreView;
