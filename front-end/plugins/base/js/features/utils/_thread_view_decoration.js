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
define(["require", "exports", '../../common/models/QueryValidator', '../../common/models/QueryAutoUpdaterBraiding', 'weaver/views/QueryEditor', 'weaver/views/ThreadView', '../../common/utils/DisplayMode', 'lodash', '../../common/views/thread_view_factory'], function (require, exports, QueryValidator, QueryAutoUpdaterBraiding, QueryEditor, ThreadView, DisplayMode, _, thread_view_factory) {
    var originalInitialize = ThreadView.prototype.initialize;
    ThreadView.prototype._createQueryEditor = function () {
        return new QueryEditor({
            queryValidator: new QueryValidator({
                thread: this.model
            }),
            model: this.model,
            collapsed: true,
            el: this.$('.mas-thread--queryEditor')
        });
    };
    ThreadView.prototype.initialize = function () {
        originalInitialize.apply(this, arguments);
        this.model.set('queryUpdater', this._createQueryUpdater());
    };
    ThreadView.prototype._createQueryUpdater = function () {
        return new QueryAutoUpdaterBraiding({
            thread: this.model
        });
    };
    ThreadView.prototype._createSubThreadView = function (thread) {
        return thread_view_factory.subThread(thread);
    };
    ThreadView.prototype._toggleThreadDisplay = function (aggregation, value) {
        value = _.isString(value) ? value : DisplayMode.CLASSIC;
        aggregation.set('displayMode', value);
        if (this.subThread && this.subThread.model.get('aggregation') === aggregation) {
            // If the user is switching the display mode only.
            if (this.subThread.model.get('displayMode') !== aggregation.get('displayMode')) {
                this.stopListening(this.subThread.model, 'change:displayMode');
                _displayNestedThread(this, aggregation);
            }
            else {
                this.stopListening(this.subThread.model, 'change:displayMode');
                this.removeNestedThread();
            }
        }
        else {
            _displayNestedThread(this, aggregation);
        }
    };
    function _displayNestedThread(self, aggregation) {
        var displayMode = aggregation.get('displayMode');
        var handler = function () {
            aggregation.set('displayMode', displayMode);
        };
        self.$el.on('didRemoveThread', handler);
        self.displayNestedThread(aggregation).then(function (subThread) {
            self.listenTo(subThread.model, 'change:displayMode', function (thread, value) {
                var aggregation = thread.get('aggregation');
                thread.set('displayMode', aggregation.get('displayMode'), { silent: true });
                self._toggleThreadDisplay(aggregation, value);
            });
        }).done();
        self.$el.off('didRemoveThread', handler);
    }
    ;
    var decorate = function () {
    };
    return decorate;
});
//# sourceMappingURL=_thread_view_decoration.js.map