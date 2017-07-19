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
define(function (require) {

  "use strict";
  var _ = require('lodash');
  var Q = require('q');
  var WebWorker = require('./WebWorker');

  /**
    * Task runner.
    * @param taskName is the name of the task worker (located inside the workers folder)
    */
  var TaskRunner = function TaskRunner(taskPath) {
    this._taskList = [];
    // Load the worker.
    this._webWorker = new WebWorker();
    // With the custom code being loaded here.
    //var scriptPath = this._webWorker.convertPathForImport(taskURL);
    this._webWorker.postMessage({
      taskWorkerPath: taskPath
    });
    // Update of the promise when the worker has finished the work.
    var self = this;
    this._webWorker.onmessage = function (event) {
      if (self._taskList[event.data.id]) {
        self._taskList[event.data.id].resolve(event.data.result);
      }
      delete self._taskList[event.data.id];
    };
  };

  _.extend(TaskRunner.prototype,  {

    /*
     * Add a task to the existing queue.
     * The passed arguments are "forward" (there's a structured copy occuring here) 
     * to the task in its run method.
     * @return {Promise} Returns the promise resolved when the worker has finished.
     */
    runTaskWith: function () {
      var deferred = Q.defer();
      this._taskList.push(deferred);
      this._webWorker.postMessage({
        'id': this._taskList.length - 1,
        'params': Array.prototype.slice.call(arguments, 0)
      });
      return deferred.promise;
    }
  });

  return TaskRunner;
});