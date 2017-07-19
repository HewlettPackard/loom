Task System
===========

The role of the task system is to ease the use of workers inside weaver while providing path
consistency between a call to ```require``` for a task, and ```require``` to a classic weaver class.

How to use
----------

Create a js file following this convention:

    define(function (require) {

      // Your requirements for your task. They will be run inside the worker
      // so they can't manipulate the DOM.
      var something = require('something');

      var myTask = {

        // [optional]
        // This function will be called once, before any run.
        // It allows you to performs any kind of initialization
        // for you task runner.
        init: function () {
            ...
        }

        // [mandatory]
        // This method will be called every time a run is requested.
        // The arguments are passed from the main thread by structured cloning,
        // that is no function or inheritance. Be sure of what your passing.
        // (Note we could implement a sort of reference cloning to preserve inhereted data fields,
        //  but this hasn't been implemented yet)
        run: function(arg1, arg2, ...) {
          ...
        }
      }

      return myTask;
    });

That's it, you've written your first task handler. Now let's see how to use it:

    define(function (require) {
      ...
      var myTaskRunner = require('task!path/to/myTask');
      ...

      // Somewhere in the code:
      myTaskRunner.runTaskWith(arg1, arg2, ...) // Start the task in the web worker
                                                // and copy the args with structured cloning.
      .then(function (result) {                 // Callback using the 'q' library.

      })
      .done();
    })

That's all.

How does it work
----------------

Even if it works for the web project both in dev and production mode. All of this rely heavily on the ```Gruntfile.js```
behavior. Basically a second grunt task is added that creates a ```worker.js``` file for production mode, while in dev mode
the behavior is by running the local worker file.

When you require a task, a plugin that you can find at ```weaver/js/weaver/taskutils/taskPlugin.js``` is used to
load the task runner. The idea is to hide the fact to the user that he's not directly requesting the dependency on its task
but on the wrapper that is job for him.

What could be improved
----------------------

  * The ```WebWorker.js``` file contains the hardcoded path for the worker both in prod and dev mode. 
    (We could maybe access those path through the configuration file.)
  * The ```TaskWorkerBase.js``` file which correspond the worker file in dev mode, include 
    in dev mode requirejs with ```importScripts```