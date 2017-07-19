import AbstractFibreListener = require('plugins/common/views/AbstractFibreListener');
import Backbone = require('backbone');
import AlertNotificationView = require('weaver/views/AlertNotificationView');
import ViewFibreOptions = require('plugins/interfaces/ViewFibreOptions');
import FibreBuilder = require('../layout/FibreBuilder');

class FibreView extends AbstractFibreListener {

  static factory(options: ViewFibreOptions<FibreBuilder>): FibreView {
    return new FibreView(options);
  }

  thread: Thread;
  alertNotificationView: AlertNotificationView;

  constructor(options?: ViewFibreOptions<FibreBuilder>) {
    super(options);
    this.thread = options.thread;

    this.alertNotificationView = new AlertNotificationView({
      // TODO: Fix this once tsdgen annotations have been updated.
      model: this.model ? (<any>this.model).alert : undefined
    });

    this.alertNotificationView.$el.addClass('mas-fiberOverview--alert')
      .appendTo(this.$el);

      // TODO: Fix this once tsdgen annotations have been updated
    this.$('.mas-fiberOverview--label').append((<any>this.model).getTranslated('name'));
  }

  protected _updateElementDetails(selected: boolean): void {
    // TODO: FIXME
    if (selected) {
      this.dispatchCustomEvent('element:show-details', {
        args: true,
        model: this.model,
        thread: this.thread,
      });
    } else {
      this.dispatchCustomEvent('element:show-details', {
        args: false,
      });
    }
  }

  protected _updateChangedAttributesFullList(attributes: Array<string>): void {
    // Does nothing
  }
}

export = FibreView;
