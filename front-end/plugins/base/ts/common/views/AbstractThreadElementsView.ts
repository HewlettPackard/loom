import Backbone = require('backbone');

import Element2 = require('weft/models/Element');
import DeltaOnResetElements = require('../../interfaces/DeltaOnResetElements');

//var emptyElementTemplate = require('weaver/views/ThreadViewElements/EmptyElement.html');

class AbstractThreadElementsView extends Backbone.View<Thread> {

  constructor(options: any) {
    super(options);

    this.listenTo(this.model, 'reset:elements', this._onResetElements);
    this.listenTo(this.model, 'change:query', this.clear);
  }

  render(): AbstractThreadElementsView {

    // Either render an empty tags, because the thread does not have elements at the moment
    // or render the elements.
    if (this.model.get('elements').models.length > 0) {
      this.renderElements(this.model.get('elements').models);
    } else {
      this.renderEmpty();
    }

    // Notify
    setImmediate(_.bind(this._notifyRenderingComplete, this));

    return this;
  }

  // =================================================================== //
  //                      Sub classes interface                          //
  // =================================================================== //

  /**
   * This method is called whenever the 'reset:elements' event
   * is fired on the Thread model.
   */
  protected onResetElements(collection: Backbone.Collection<Element2>, obj: DeltaOnResetElements<Element2>): void {
    throw new Error("Unimplemented exception");
  }

  /**
   * This method is only called when elements.length > 0,
   * and should render into this.$el the HTML content.
   */
  protected renderElements(elements: Array<Element2>): void {
    throw new Error("Unimplemented exception");
  }

  /**
   * This method is called whenever the query changed.
   * It can also be manually called to clear the view.
   */
  clear(): void {
    throw new Error("Unimplemented exception");
  }

  ///
  /// Private interface
  ///

  private _onResetElements(collection: Backbone.Collection<Element2>, obj: DeltaOnResetElements<Element2>): void {

    // SubClass treatment.
    this.onResetElements(collection, obj);

    // Notify
    setImmediate(_.bind(this._notifyRenderingComplete, this));
  }

  protected renderEmpty(): void {
    // FIXME:
    /*this.$emptyElement = $(emptyElementTemplate);

    this.removedElementsView.$el.addClass('mas-elements--removedElements')
      .appendTo(this.$el);*/
  }

  private refreshElementsLabels(): void {
    // Does nothing.
    // This method is here to work with ThreadView maximize function.
  }

  private _notifyRenderingComplete(): void {

    var event = document.createEvent('Event');
    event.initEvent('didRender', true, true);
    this.el.dispatchEvent(event);
  }

}

export = AbstractThreadElementsView;
