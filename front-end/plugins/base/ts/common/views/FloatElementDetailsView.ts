import $ = require('jquery');
import ElementDetailsView = require('weaver/views/ElementDetailsView');
import ActionDialogController2 = require('./ActionDialogController2');
import ViewThreadOptions = require('../../interfaces/ViewThreadOptions');

class FloatElementDetailsView extends ElementDetailsView {

  thread: Thread;
  actionDialogController: ActionDialogController2;

  constructor(options?: ViewThreadOptions<Element2>) {
    super(options);

    this.events = <any>{
      'click .mas-action--view': (event) => {
        event.preventDefault();
        this.thread.trigger('didPressActionView', this.model, event);
      },
      'click .mas-action--filter': () => {
        event.preventDefault();
        if (!this.model.get('disabled')) {
          this.model.set('isPartOfFilter', !this.model.get('isPartOfFilter'));
        }
      }
    }

    /// IE hack to fix LOOM-1632
    /// https://connect.microsoft.com/IE/feedbackdetail/view/951267/
    /// horizontal-scrolling-with-flex-grow-max-width
    if (window.screen.width * 0.66 > 800) {
        this.$el.attr('style', 'width: 400px');
    }

    this.delegateEvents();

    this.thread = options.thread;

    // ugly, would have been preferrable to have it in a css class.
    this.$el.removeClass('mas-elementDetails');
    this.$el.addClass('mas-element-' + this.model.cid);
    this.$el.addClass('mas-element');

    this.$el.addClass('mas-element--content');
    this.$el.addClass('mas-mapElementDetails');

    var movedView = this.$el.children().detach();
    var subView = $('<div class="mas-mapElementDetailsSubView mas-elementDetails"></div>');
    subView.append(movedView);
    this.$el.append(subView);

    if (this.model.get('displayMode')) {
      this.$el.addClass('is-displayed');
    }

    this.listenTo(this.model, 'change:displayMode', (model, displayMode) => {
      if (displayMode) {
        this.$el.addClass('is-displayed');
      } else {
        this.$el.removeClass('is-displayed');
      }
    });

    if (this.model.get('isMatchingFilter')) {
      this.$el.addClass('is-matchingFilter');
    }

    this.listenTo(this.model, 'change:isMatchingFilter', (model, match) => {
      if (match) {
        this.$el.addClass('is-matchingFilter');
      } else {
        this.$el.removeClass('is-matchingFilter');
      }
    });

    if (this.model.get('isPartOfFilter')) {
      this.$el.addClass('is-partOfFilter');
    }

    this.listenTo(this.model, 'change:isPartOfFilter', (model, value) => {
      if (value) {
        this.$el.addClass('is-partOfFilter');
      } else {
        this.$el.removeClass('is-partOfFilter');
      }
    });

    this.listenTo(this.model, 'change:displayed', (model, displayed) => {
      if (displayed) {
        this.$el.addClass('is-displayed');
      } else {
        this.$el.removeClass('is-displayed');
      }
    });

    this.$('.mas-elementDetails--properties').css('max-height', 'none');
    this.$('.mas-alertDetails').css('flex-shrink', '0');

    this.actionDialogController.stopListening();
    this.actionDialogController.undelegateEvents();
    this.actionDialogController = new ActionDialogController2({
      model: this.model,
      el: this.el
    });
  }

  remove(): FloatElementDetailsView {
    ElementDetailsView.prototype.remove.apply(this, arguments);
    this.thread.trigger('didRemoveDetailsView');
    return this;
  }

  _updateTitle(): void {

    var label = this.model.getTranslated('name');
    var thread = this.thread;
    if (thread && thread.isGrouped()) {
      var groupByOperation = thread.getLastGroupByOperation();
      var unit = thread.getUnit(groupByOperation.parameters.property);
      if (unit) {
        label += ' ' + unit;
      }
    }
    this.$('.mas-elementDetails--title').html(label);
  }

}

export = FloatElementDetailsView;
