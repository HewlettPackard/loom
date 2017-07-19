import _ = require('lodash');
import Element2 = require('weft/models/Element');

var dt = 300;
var timeShown = 10000;

interface AttributeChanged {
  [index: string]: number ;
}

class AttributeHasChangedMonitor {

  model: Element2;
  private attributesRecentlyChanged: AttributeChanged;
  private timeoutId: number;

  constructor(model: Element2) {
    this.model = model;
    this.model.on('change', this._addAttribute, this);
    this.attributesRecentlyChanged = {};
  }

  _addAttribute(): void {
    var newAttributes = this.model.getDisplayablePropertiesThatHasChanged();
    _.forEach(newAttributes, (attribute: string) => {
      // Time in ms.
      this.attributesRecentlyChanged[attribute] = timeShown;
    });
    this._triggerShortly();
  }

  _triggerShortly(): void {
    if (!this.timeoutId) {
      this.timeoutId = setTimeout(_.bind(this._refreshList, this), dt);
    }
  }

  _refreshList(): void {
    var newsAttributes: Array<string> = _.keys(
      _.omit<AttributeChanged, AttributeChanged>(this.attributesRecentlyChanged, (attribute) => attribute !== timeShown)
    );
    var oldAttributes: Array<string> = _.keys(
      _.omit<AttributeChanged, AttributeChanged>(this.attributesRecentlyChanged, (attribute) => attribute > 0)
    );
    this.attributesRecentlyChanged = _.omit<AttributeChanged, AttributeChanged>(
      this.attributesRecentlyChanged,
      (attribute) => attribute < 0
    );
    this.attributesRecentlyChanged = _.mapValues(this.attributesRecentlyChanged, (attribute) => attribute - dt);
    this.timeoutId = undefined;
    if (!_.isEmpty(this.attributesRecentlyChanged)) {
      this._triggerShortly();
    }
    if (newsAttributes.length > 0 || oldAttributes.length > 0) {
      this.model.trigger('attributesHaveChanged', { newAttributes: newsAttributes, notNewAnymoreAttributes: oldAttributes});
    }
  }
}

export = AttributeHasChangedMonitor;
