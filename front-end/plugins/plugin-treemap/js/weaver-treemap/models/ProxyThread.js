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
define(["require", "exports", 'lodash'], function (require, exports, _) {
    var ProxyThread = (function () {
        function ProxyThread(threadList, commonQuery) {
            this.threadList = threadList;
            this.commonQuery = commonQuery;
        }
        ProxyThread.prototype.set = function (key, value) {
            if (key === 'query') {
                this.commonQuery = value;
                _.forEach(this.threadList, function (thread) {
                    thread.set('query', value);
                });
            }
            else {
                throw new Error("Unreachable code.");
            }
        };
        ProxyThread.prototype.get = function (key) {
            if (key === 'query') {
                return this.commonQuery;
            }
            else {
                throw new Error("Unreachable code.");
            }
        };
        ProxyThread.prototype.getAttributesForOperation = function (operator) {
            return this.threadList[0].getAttributesForOperation(operator);
        };
        ProxyThread.prototype.getAttribute = function (attributeId) {
            return this.threadList[0].getAttribute(attributeId);
        };
        return ProxyThread;
    })();
    return ProxyThread;
});
