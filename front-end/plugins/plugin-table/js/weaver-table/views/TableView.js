/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
define(["require", "exports", 'lodash', 'plugins/common/views/AbstractThreadElementsView', 'weft/models/Aggregation', '../layout/FibreBuilder', '../layout/table_header', '../layout/Builder', '../controller/CollapseController', '../controller/SortController', '../controller/SelectorController', './FibreView', 'jquery'], function (require, exports, _, AbstractThreadElementsView, Aggregation, FibreBuilder, table_header, Builder, CollapseController, SortController, SelectorController, FibreView, $) {
    var TableHeaderValue = table_header.TableHeaderValue;
    var TableView = (function (_super) {
        __extends(TableView, _super);
        function TableView(options) {
            _super.call(this, options);
            this.selector = new SelectorController(_.bind(this.$, this));
            this.fibreBuilder = new FibreBuilder(this.selector);
            this.render();
            this._attachEvents();
        }
        TableView.prototype.renderElements = function (elements) {
            var builder = new Builder();
            var element = elements[0];
            this.attributes_names = _.map(element.getItemType().getVisibleAttributes(), function (val) { return new TableHeaderValue(val.id, val.name); });
            var attributes = this.attributes_names.map(function (a) {
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
            this.collapseListener = res.collapseController;
            this.collapseListener.busEvent = this;
            this.sortListener = res.sortController;
            this.sortListener.busEvent = this;
            setImmediate(_.bind(this._autoCollapseIfNeeded, this));
        };
        TableView.prototype.updateDiff = function (obj) {
            var _this = this;
            var removedTable = _.reduce(obj.delta.removed, function (table, model) {
                table[model.cid] = model;
                return table;
            }, {});
            var views_removed = [];
            _.forEach(this.views, function (view) {
                if (removedTable[view.model.cid]) {
                    view.$el.addClass('mas-row-deleted');
                    setTimeout(function () { view.remove(); }, 2000 * Math.random());
                }
                views_removed.push(view);
            });
            this.views = _.difference(this.views, views_removed);
            var cols = this.getCssForCollapsedColumn();
            var table = this.$('.mas-table');
            _.forEach(obj.delta.added, function (model) {
                var el = _this.fibreBuilder.buildFibre({ model: model, attribute_class: cols, row_css: ['mas-row-added'] });
                _this.views.push(FibreView.factory({
                    el: el,
                    model: model,
                    builder: _this.fibreBuilder,
                    thread: _this.model
                }));
                setTimeout(function () { $(el).removeClass('mas-row-added'); }, 10000);
                table.append(el);
            });
            this.sortController.refresh();
        };
        TableView.prototype.clear = function () {
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
        };
        TableView.prototype.onResetElements = function (collection, obj) {
            var _this = this;
            if (!this.views) {
                this.render();
            }
            else {
                this.updateDiff(obj);
            }
            if (_.any(collection.models, function (m) { return m instanceof Aggregation; })) {
                this.$el.children().addClass('has-aggregations');
            }
            else {
                this.$el.children().removeClass('has-aggregations');
            }
            _.forEach(obj.previousModels, function (model) { _this.stopListening(model); });
            _.forEach(collection.models, function (model) {
                _this.listenTo(model, 'change:related', function (model, related) {
                    if (_.any(collection.models, function (m) { return m.get('related'); })) {
                        _this.$el.children().addClass('has-fibers-related');
                    }
                    else {
                        _this.$el.children().removeClass('has-fibers-related');
                    }
                });
            });
        };
        TableView.prototype._attachEvents = function () {
            var _this = this;
            this.listenTo(this, 'collapse:column', function (col) {
                _this._collapse_column(col);
            });
            this.listenTo(this, 'expand:column', function (col) {
                _this._expand_column(col);
            });
            this.listenTo(this, 'sort:column', function (col) {
                _this._sort_column(col);
            });
        };
        TableView.prototype.getCssForCollapsedColumn = function () {
            var _this = this;
            var attributes = this.attributes_names.map(function (a) {
                return a.attributeName;
            });
            return attributes.map(function (name) {
                if (_this.collapseController.is_attribute_collapsed(name)) {
                    return 'is-collapsed';
                }
                else {
                    return '';
                }
            });
        };
        TableView.prototype._sort_column = function (i) {
            this.sortController.sort_column(i);
        };
        TableView.prototype._collapse_column = function (i) {
            this.collapseController.collapse_column(i);
            this.selector.select_col(i).addClass('is-collapsed');
        };
        TableView.prototype._expand_column = function (i) {
            this.collapseController.expand_column(i);
            this.selector.select_col(i).removeClass('is-collapsed');
        };
        TableView.prototype._autoCollapseIfNeeded = function () {
            var width = this.$el[0].getBoundingClientRect().width;
            var checkAgain = width == 0;
            if (this.$el[0].scrollWidth > width * 1.001) {
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
        };
        return TableView;
    })(AbstractThreadElementsView);
    return TableView;
});
