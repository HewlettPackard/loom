import Backbone = require('backbone');
import _ = require('lodash');
import FloatElementDetailsView = require('../views/FloatElementDetailsView');
import ViewTargetOptions = require('../../interfaces/ViewTargetOptions');

class FloatElementDetailsController extends Backbone.View<any> {

  view: FloatElementDetailsView;
  target: HTMLElement;
  attributesForView: { [index: string]: { [index: string]: string}};

  constructor(options?: ViewTargetOptions) {
    this.target = options.target;
    this.attributesForView = {};
    this.events = <any>{
      'element:show-details': function (event) {
        this._showDetails(event.originalEvent.args && event.originalEvent.model, event.originalEvent.thread);
      }
    }
    super(options);
  }

  remove(): FloatElementDetailsController {
    this.stopListening();
    return this;
  }

  hideView(): void {
    if (this.view) {
      this.view.remove();
      this.view = undefined;
    }
  }

  _showDetails(modelToPresent: any, thread: Thread):void {
    if (this.view) {
      this.view.remove();
      this.view = undefined;
    }

    if (modelToPresent) {
      this.view = new FloatElementDetailsView({
        model: modelToPresent,
        thread: thread
      });
      this.view.$el
        .appendTo(this.target);

      _.forEach(this.attributesForView, (attributes, selector: string) => {
        _.forEach(attributes, (value, attrName) => {
          this.view.$(selector).attr(attrName, value);
        });
      });
    }
  }

  attr(selector: string, attrName: string, value: string) {
    this.attributesForView[selector] = this.attributesForView[selector] || {};
    this.attributesForView[selector][attrName] = value;
  }
}


export = FloatElementDetailsController;
