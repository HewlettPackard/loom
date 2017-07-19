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
/* global self, importScripts */
"use strict";

//////////////////
// Ugly details //
//////////////////

var DevMode = false;
// Import requirejs when in dev mode. Ugly code, only working with the web project.
if (!(typeof define === 'function' && typeof define.amd === 'object' && define.amd)) {
  importScripts('/bower_components/requirejs/require.js');
  DevMode = true;
}

self.tasksAvailable = {};


// Let's fool requirejs :)
if (DevMode) {
  var requireOld = require;
  var tasksDeps;
  var tasksCallback;
  require = function (deps, callback) {
    tasksDeps = deps;
    tasksCallback = callback;
  };
}

function convertConfig(config) {
  config.baseUrl = '../../../';
  for (var dep in config.paths) {
    config.paths[dep] = '../../../' + config.paths[dep];
  }
  return config;
}

self.onmessage = function (event) {
  var data = event.data;
  if (data.taskWorkerPath) {
    self.currentTask = data.taskWorkerPath;
  }
  if (data.requireConfig && DevMode) {
    require = requireOld;
    require.config(convertConfig(data.requireConfig));
    // TODO: FIXME
    // Preveting task to be used with phantomjs.
    if (self.console) {
      require(tasksDeps, tasksCallback);
    }
  }
};

////////////////////////////
// Implementation details //
////////////////////////////

function toArguments(obj) {
  var i = 0;
  var tab = [];
  while (obj.hasOwnProperty(i) || (obj.length && i < obj.length)) {
    tab[i] = obj[i];
    ++i;
  }
  return tab;
}

function getKeysFromObject(obj) {
  var keys = "";
  for (var k in obj) {
    keys += k + ", ";
  }
  return keys.substr(0, keys.length - 2);
}

function setTaskWorker(path) {
  var taskWorker = self.tasksAvailable[path];

  if (taskWorker) {
    self.taskWorker = taskWorker;
  } else {
    console.error("Couldn't load Task: " + path,
    " Availables Tasks: " + getKeysFromObject(self.tasksAvailable));
  }
}

function setWorkerToReady() {

  if (self.currentTask) {
    setTaskWorker(self.currentTask);
  }

  self.onmessage = function (event) {
    var data = event.data;

    if (data.taskWorkerPath) {
      setTaskWorker(data.taskWorkerPath);

    } else if (data.id !== undefined) {

      if (self.taskWorker) {

        var result = self.taskWorker.run.apply(self.taskWorker, toArguments(data.params));
        self.postMessage({
          'id': data.id,
          'result': result
        });
      }
    }
  };
}

///////////
// Tasks //
///////////

/**
 * Helper to add task worker defined in a separate file.
 * @param  {String} name is the requirejs path of the task.
 * @param  {Object} task is the task object containing an init method and a run method.
 */
function defineTaskWorker(name, task) {
  if (task.init) {
    task.init();
  }
  self.tasksAvailable[name] = task;
}

require(
  [ // Cartogram task.
    'weaver-map/tasks/cartogram-task'
  ],
  function (cartogramtask) {

    // Available tasks
    defineTaskWorker('weaver-map/tasks/cartogram-task', cartogramtask);

    // load
    setWorkerToReady();
  }
);
