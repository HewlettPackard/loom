import Backbone = require('backbone');
import _ = require('lodash');
import $ = require('jquery');
import Element2 = require('weft/models/Element');
import ElementActionsView = require('weaver/views/ElementDetailsView/ElementActionsView');
import ViewThreadOptions = require('plugins/interfaces/ViewThreadOptions');
import ActionDialogController = require('weaver/views/ElementDetailsView/ActionDialogController');


class FibreContextMenu extends Backbone.View<Element2> {

  static className = 'mas-table-contextMenu'

  title = 'Actions';
  className = FibreContextMenu.className;
  elementActionView: ElementActionsView;
  actionDialogController: ActionDialogControllerSpec;
  thread: Thread;

  constructor(args: ViewThreadOptions<Element2>) {
    super(args);

    this.thread = args.thread;

    this.events = <any>{
      'click .mas-action--view': (event) => {
        event.preventDefault();
        this.thread.trigger('didPressActionView', this.model, event);
      },
      'click .mas-action--filter': (event) => {
        event.preventDefault();
        if (!this.model.get('disabled')) {
          this.model.set('isPartOfFilter', !this.model.get('isPartOfFilter'));
        }
      },
      'click': (event) => {
        event.originalEvent.ignoreSelect = true;
      }
    }

    this.delegateEvents();
  }

  render(): FibreContextMenu {

    var $buttons = $('<div>', {
      class: 'mas-table-contextMenu--actions'
    });

    this.elementActionView = new ElementActionsView({
      el: $buttons,
      model: this.model,
    });

    this.actionDialogController = new ActionDialogControllerSpec({
      model: this.model,
      el: this.el
    });

    var $header = $('<div>', {
      class: 'mas-table-contextMenu--title'
    }).append(this.title);

    this.$el.append($header);
    this.$el.append($buttons);

    return this;
  }

  show(): void {
    this.$el.addClass('mas-display-block');
  }

  hide(): void {
    this.$el.removeClass('mas-display-block');
    this.actionDialogController.hideDialog();
  }
}

class ActionDialogControllerSpec extends ActionDialogController {


  constructor(options: any) {
    _.assign(<any>this.events, ActionDialogController.prototype.events, {
      'click .mas-action-hideDialog': (event) => {
        event.preventDefault();
        this.hideDialog();
      }
    });
    super(options);
  }

  _manuallyResetHeightOfSiblings(): void {
    // Do nothing instead.
  }
}

export = FibreContextMenu;
