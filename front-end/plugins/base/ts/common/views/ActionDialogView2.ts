import ActionDialogView = require("weaver/views/ActionDialogView");
import Backbone = require('backbone');
import ViewOptions = Backbone.ViewOptions;

class ActionDialogView2 extends ActionDialogView {

  constructor(options?: ViewOptions<any>) {
    super(options);

    this.$el.css('padding-top', '20px');
    this.$el.css('height', '100%');
    this.$el.css('width', '100%');
    this.$el.css('position', 'absolute');
  }

}

export = ActionDialogView2;
