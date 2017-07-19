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
define(["require", "exports", '../../features/utils/_braiding_controller_decoration', 'weft/models/Operation'], function (require, exports, optimalBraiding, Operation) {
    /**
     * A QueryValidator is used by the QueryEditor to
     * perform check on the query being validated. It is allowed
     * to modify the query before the new query is set to the thread.
     *
     * Subclasses of QueryValidator can override the following method:
     *   - initialize     is called after construction.
     *   - validateQuery  is called whenever the editor generate the query.
     *
     * @see QueryValidatorMap
     *
     * @param {Object} options must contains the referenced thread.
     */
    var QueryValidator = (function () {
        function QueryValidator(options) {
            this.thread = options.thread;
            this.initialize.apply(this, arguments);
        }
        /**
         * Function that can modify the given query following test.
         *
         * @param  {Query} query is the query to validate.
         * @return {Query}       Returns the new query modified.
         */
        QueryValidator.prototype.validateQuery = function (query) {
            // The query has no limit ?
            if (!query.hasLimit()) {
                query = query.limitWith({
                    operator: Operation.BRAID_ID,
                    parameters: {
                        maxFibres: optimalBraiding.getValue()
                    }
                });
            }
            return query;
        };
        QueryValidator.prototype.initialize = function (options) {
            var query = this.thread.get('query');
            query = this.validateQuery(query);
            this.thread.set('query', query);
        };
        return QueryValidator;
    })();
    return QueryValidator;
});
//# sourceMappingURL=QueryValidator.js.map