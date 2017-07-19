import AbstractElementView = require('weaver/views/AbstractElementView');
import ElementStateController = require('weaver/views/ElementView/ElementStateController');
import AttributeHasChangedMonitor = require('../utils/AttributeHasChangedMonitor');
import Backbone = require('backbone');
import ViewOptions = Backbone.ViewOptions;

class AbstractFibreListener extends AbstractElementView {

  bindings: { [index: string]: { classes: { [index:string]: string } } };
  selected: boolean;
  elementStateController: ElementStateController;
  monitor: AttributeHasChangedMonitor;
  model: Element2;
  private attributesChanged: Array<string>;

  constructor(options?: ViewOptions<any>) {
    this.bindings =  {
      ':el': {
        classes: {
          'is-related': 'related',
          'is-highlighted': 'highlighted',
          'is-fromHighlightedProvider': 'isFromHighlightedProvider'
        }
      }
    }
    this.events = <any>{
      'click': (event) => {
        if (!event.isDefaultPrevented() && !event.originalEvent.ignoreSelect) {
          if (this.selected) {
            this.unselectElement(event);
          } else {
            this.selectElement(event);
          }
        }
      }
    }
    super(options);
    this.attributesChanged = [];
    this.elementStateController = new ElementStateController({
      el: this.el,
      model: this.model
    });
    this.monitor = new AttributeHasChangedMonitor(this.model);

    this.listenTo(this.model, 'change:isMatchingFilter', (model, matchesFilter) => {
        this._updateFilterState(matchesFilter);
    });
    this.listenTo(this.model, 'change:isPartOfFilter', this.setFilter);
    this.listenTo(this.model.alert, 'change:level', (model, level) => this._updateAlertState(model.previous('level'), level));
    //this.listenTo(this.model.updatedAttributesMonitor, 'change:updatedAttributes', this._updateUpdatedAttributes);
    this.listenTo(this.model, 'attributesHaveChanged', this._updateChangedAttributes);
    //this.listenTo(this.model.alert, 'change:level', () => this._updateChangedAttributes({ newAttributes: ['alertLevel']}));
  }

  selectElement(event: any): void {
    if (this.selected) {
      return;
    }
    this.dispatchCustomEvent('willSelectElement', {
       view: this
    });
    if (!(event && event.isDefaultPrevented())) {
      this.selected = true;
      this._updateSelectedState(true);
      this.dispatchCustomEvent('didSelectElement', {
        view: this
      });
    }
  }

  unselectElement(event: any): void {
    this.selected = false;
    this._updateSelectedState(false);
    this.dispatchCustomEvent('didUnselectElement', undefined);
    if (event) {
      // Prevent default behaviour to avoid selecting the element back
      // Other option would be do enable/disable listeners according to
      // if element is selected or not
      event.preventDefault();
    }
  }

  remove(): AbstractFibreListener {
    this.elementStateController.deactivate();
    super.remove();
    return this;
  }

  render(): AbstractElementView {
    this.stickit();
    super.render();
    this.$el.removeClass('mas-element');
    this._updateAlertState(this.model.alert.get('level'), this.model.alert.get('level'));
    return this;
  }

  protected _updateElementDetails(selected: boolean): void {
    throw new Error("Unimplemented error");
  }

  protected _updateChangedAttributes(changes: { newAttributes: Array<string>; notNewAnymoreAttributes: Array<string>; }): void {
    this.attributesChanged = _.union(this.attributesChanged, changes.newAttributes);
    this.attributesChanged = _.bind(_.without, _, this.attributesChanged).apply(_, changes.notNewAnymoreAttributes);
    this._updateChangedAttributesFullList(this.attributesChanged);
  }

  protected _updateChangedAttributesFullList(attributes: Array<string>): void {
    throw new Error("Unimplemented error");
  }

  private _updateSelectedState(selected) {
    if (selected) {
      this.$el.addClass('is-selected');
    } else {
      this.$el.removeClass('is-selected');
    }
    this._updateElementDetails(selected);
  }

  private _updateAlertState(oldlevel:number, level: number): void {
    this.$el.removeClass('mas-alertNotification-' + oldlevel);
    this.$el.addClass('mas-alertNotification-' + level);
  }

  private setFilter(): void {
    if (this.model.get('isPartOfFilter')) {
      this._dispatchEvent('action:addFilterElement', null);
      this.$el.addClass('is-partOfFilter');
    } else {
      this._dispatchEvent('action:removeFilterElement', null);
      this.$el.removeClass('is-partOfFilter');
    }
  }
}

export = AbstractFibreListener;
