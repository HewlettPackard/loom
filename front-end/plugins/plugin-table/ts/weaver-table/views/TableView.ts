import Backbone = require('backbone');
import _ = require('lodash');
import Element2 = require('weft/models/Element');
import AbstractThreadElementsView = require('plugins/common/views/AbstractThreadElementsView');
import Aggregation = require('weft/models/Aggregation');
import FibreBuilder = require('../layout/FibreBuilder');
import table_header = require('../layout/table_header');
import Builder = require('../layout/Builder');
import TableHeaderListener = require('../controller/TableHeaderListener');
import TableHeaderSortListener = require('../controller/TableHeaderSortListener');
import CollapseController = require('../controller/CollapseController');
import SortController = require('../controller/SortController');
import SelectorController = require('../controller/SelectorController');
import FibreView = require('./FibreView');
import DeltaOnResetElements = require('plugins/interfaces/DeltaOnResetElements');
import row_css = require('../util/row_css');
import $ = require('jquery');

import ViewOptions = Backbone.ViewOptions;
import TableHeaderValue = table_header.TableHeaderValue;


class TableView extends AbstractThreadElementsView {

  views: Array<FibreView>;

  selector: SelectorController;

  collapseListener: TableHeaderListener;
  collapseController: CollapseController;

  sortListener: TableHeaderSortListener;
  sortController: SortController;

  attributes_names: Array<TableHeaderValue>;
  fibreBuilder: FibreBuilder;

  constructor(options: ViewOptions<Thread>) {
    super(options);
    this.selector = new SelectorController(_.bind(this.$, this));
    this.fibreBuilder = new FibreBuilder(this.selector);
    this.render();
    this._attachEvents();
  }

  protected renderElements(elements: Array<Element2>): void {

    var builder = new Builder();
    var element = elements[0];

    this.attributes_names = _.map(element.getItemType().getVisibleAttributes(),
      (val: any) => new  TableHeaderValue(val.id, val.name)
    );

    var attributes = this.attributes_names.map((a) => {
      return a.attributeName;
    });

    this.collapseController = new CollapseController(attributes, this.collapseController);
    this.sortController = new SortController(this.selector, attributes);

    var filteredColumns = this.getCssForCollapsedColumn();

    var res = builder.build({
      values: this.attributes_names
    }, filteredColumns, elements, FibreView.factory, this.model, this.fibreBuilder);
    this.$el.append(res.el);
    this.views = res.views;

    // Event listener for collapse events
    this.collapseListener = res.collapseController;
    this.collapseListener.busEvent = this;

    // Event listener for sorting events
    this.sortListener = res.sortController;
    this.sortListener.busEvent = this;

    // Auto collapse if needed.
    setImmediate(_.bind(this._autoCollapseIfNeeded, this));
  }

  updateDiff(obj: DeltaOnResetElements<Element2>): void {
    // Converted removed elements into lookup table
    var removedTable = _.reduce(obj.delta.removed, (table, model) => {
      table[model.cid] = model;
      return table;
    }, {});

    var views_removed : Array<FibreView> = [];

    _.forEach(this.views, (view) => {
      if (removedTable[view.model.cid]) {
        view.$el.addClass('mas-row-deleted');
        setTimeout(() => { view.remove(); }, 2000 * Math.random());
      }
      views_removed.push(view);
    });

    this.views = _.difference(this.views, views_removed);

    var cols = this.getCssForCollapsedColumn();
    var table = this.$('.mas-table');

    _.forEach(obj.delta.added, (model) => {

      var el = this.fibreBuilder.buildFibre({ model: model, attribute_class: cols, row_css: ['mas-row-added']});
      this.views.push(FibreView.factory({
        el: el,
        model: model,
        builder: this.fibreBuilder,
        thread: this.model
      }));
      setTimeout(() => { $(el).removeClass('mas-row-added'); }, 10000);

      table.append(el);
    });

    this.sortController.refresh();
  }

  clear(): void {
    var view;
    if (this.views) {
      while (view = this.views.pop()) {
        view.remove();
      }
      this.views = undefined;
    }
    if (this.collapseListener) {
      this.collapseListener.remove();
    }
    if (this.sortListener) {
      this.sortListener.remove();
    }
    this.$el.empty();
  }

  protected onResetElements(collection: Backbone.Collection<Element2>, obj: DeltaOnResetElements<Element2>): void
  {
    // Used to check if the table has been cleared. If yes, redraw everything
    if (!this.views) {
      this.render();
    } else {
      this.updateDiff(obj);
    }

    if (_.any(collection.models, (m) => { return m instanceof Aggregation; })) {
      this.$el.children().addClass('has-aggregations');
    } else {
      this.$el.children().removeClass('has-aggregations');
    }
    _.forEach(obj.previousModels, (model) => { this.stopListening(model); });
    _.forEach(collection.models, (model: Element2) => {
      this.listenTo(model, 'change:related', (model, related) => {
        if (_.any(collection.models, (m) => { return m.get('related'); })) {
          this.$el.children().addClass('has-fibers-related');
        } else {
          this.$el.children().removeClass('has-fibers-related');
        }
      });
    });
  }

  private _attachEvents(): void {

    // Collapse events:
    this.listenTo(this, 'collapse:column', (col: number) => {
      this._collapse_column(col);
    });
    this.listenTo(this, 'expand:column', (col: number) => {
      this._expand_column(col);
    });

    // Sorting events
    this.listenTo(this, 'sort:column', (col: number) => {
      this._sort_column(col);
    });
  }

  ///
  /// Collapsed columns
  ///
  private getCssForCollapsedColumn(): Array<string> {
    var attributes = this.attributes_names.map((a) => {
      return a.attributeName;
    });
    return attributes.map((name) => {
      if (this.collapseController.is_attribute_collapsed(name)) {
        return 'is-collapsed';
      } else {
        return '';
      }
    });
  }

  ///
  /// Sorting mechanism
  ///

  private _sort_column(i: number): void {

    this.sortController.sort_column(i);
  }

  ///
  /// Collapse mechanism
  ///

  private _collapse_column(i: number): void {

    this.collapseController.collapse_column(i);
    this.selector.select_col(i).addClass('is-collapsed');
  }

  private _expand_column(i: number): void {

    this.collapseController.expand_column(i);
    this.selector.select_col(i).removeClass('is-collapsed');
  }

  private _autoCollapseIfNeeded(): void {
    var width = this.$el[0].getBoundingClientRect().width;
    var checkAgain = width == 0;

    if (this.$el[0].scrollWidth > width * 1.001 ) {
      checkAgain = true;
      var index = this.attributes_names.length - 1;
      while (index >= 0 && this.collapseController.is_column_collapsed(index)) {
        index -= 1;
      }
      this._collapse_column(index);
    }

    if (checkAgain) {
      setImmediate(_.bind(this._autoCollapseIfNeeded, this));
    }
  }
}

export = TableView;
