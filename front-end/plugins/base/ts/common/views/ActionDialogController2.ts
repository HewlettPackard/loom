import ActionDialogController = require('weaver/views/ElementDetailsView/ActionDialogController');
import ActionDialogView2 = require('./ActionDialogView2');

class ActionDialogController2 extends ActionDialogController {

  dialog: ActionDialogView2;

  showDialog(actionDefinition): void {

    this.hideDialog();

    this.dialog = new ActionDialogView2({
      model: actionDefinition,
      element: this.model
    });

    // IMPROVE: Create `showAction()` and `showData()` method on the `ElementDetailsView`
    this.$('.mas-elementDetails--body').prepend(this.dialog.$el);
  }

}

export = ActionDialogController2;
