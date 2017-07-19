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
var __extends = this.__extends || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    __.prototype = b.prototype;
    d.prototype = new __();
};
define(["require", "exports", 'backbone'], function (require, exports, Backbone) {
    //var emptyElementTemplate = require('weaver/views/ThreadViewElements/EmptyElement.html');
    var AbstractThreadElementsView = (function (_super) {
        __extends(AbstractThreadElementsView, _super);
        function AbstractThreadElementsView(options) {
            _super.call(this, options);
            this.listenTo(this.model, 'reset:elements', this._onResetElements);
            this.listenTo(this.model, 'change:query', this.clear);
        }
        AbstractThreadElementsView.prototype.render = function () {
            // Either render an empty tags, because the thread does not have elements at the moment
            // or render the elements.
            if (this.model.get('elements').models.length > 0) {
                this.renderElements(this.model.get('elements').models);
            }
            else {
                this.renderEmpty();
            }
            // Notify
            setImmediate(_.bind(this._notifyRenderingComplete, this));
            return this;
        };
        // =================================================================== //
        //                      Sub classes interface                          //
        // =================================================================== //
        /**
         * This method is called whenever the 'reset:elements' event
         * is fired on the Thread model.
         */
        AbstractThreadElementsView.prototype.onResetElements = function (collection, obj) {
            throw new Error("Unimplemented exception");
        };
        /**
         * This method is only called when elements.length > 0,
         * and should render into this.$el the HTML content.
         */
        AbstractThreadElementsView.prototype.renderElements = function (elements) {
            throw new Error("Unimplemented exception");
        };
        /**
         * This method is called whenever the query changed.
         * It can also be manually called to clear the view.
         */
        AbstractThreadElementsView.prototype.clear = function () {
            throw new Error("Unimplemented exception");
        };
        ///
        /// Private interface
        ///
        AbstractThreadElementsView.prototype._onResetElements = function (collection, obj) {
            // SubClass treatment.
            this.onResetElements(collection, obj);
            // Notify
            setImmediate(_.bind(this._notifyRenderingComplete, this));
        };
        AbstractThreadElementsView.prototype.renderEmpty = function () {
            // FIXME:
            /*this.$emptyElement = $(emptyElementTemplate);
        
            this.removedElementsView.$el.addClass('mas-elements--removedElements')
              .appendTo(this.$el);*/
        };
        AbstractThreadElementsView.prototype.refreshElementsLabels = function () {
            // Does nothing.
            // This method is here to work with ThreadView maximize function.
        };
        AbstractThreadElementsView.prototype._notifyRenderingComplete = function () {
            var event = document.createEvent('Event');
            event.initEvent('didRender', true, true);
            this.el.dispatchEvent(event);
        };
        return AbstractThreadElementsView;
    })(Backbone.View);
    return AbstractThreadElementsView;
});
//# sourceMappingURL=AbstractThreadElementsView.js.map