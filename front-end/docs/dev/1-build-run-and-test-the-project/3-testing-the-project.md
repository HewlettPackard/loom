3 - Testing the project
===================

The project comes with automated tests ensuring features behave the way they're supposed to. The execution of the tests is managed by the `test` script (`npm run test`).

Aside from the console output, the commande yields two reports in the `reports` folder:
 - an XUnit report of the results (`test-results.xml`)
 - a coverage report in the `coverage` folder (HTML and Cobertura formats).

Under the hood, it uses [Karma](http://karma-runner.github.io/0.13/index.html) to set up a server hosting the tests and run them in a browser (PhantomJS). Tests are written using a framework called [mocha](https://mochajs.org/) (with some help of [chai](http://chaijs.com/) for the assertions and [SinonJS](http://sinonjs.org/) for mocking).

By default, the script only runs the tests once, printing the results in the console.
Not the most handy for debugging. The `--no-single-run` flag
will have _Karma_ keep the server up (same as before, don't forget the `--` to let NPM pass the flag to the script).
You can then debug the tests in a browser by accessing `http://localhost:9876/debug.html`.

Additionaly, while the _Karma_ server is up, you can connect additional browsers (eg. Chrome, Firefox, IE)
by opening `http://localhost:9876`. This will allow you to run the tests simultaneously
in different environments. _Karma_ will also watch the files used to run the test
and re-execute the test once they change. You'll have to reload the `debug.html` page manually, though.

> _Adding new tests_
>
> Karma will add any file suffixed in .test.js from the lib/weft/test
> and lib/weaver-lib/test folders. If you need to add new tests files
> for these libraries, add a new MyClass.test.js at the path corresponding
> to the class you're testing. For example, if your class is at
> lib/weft/js/weft/models/MyModel.js, your test should be at
> lib/weft/test/weft/models/MyModel.js.
>
> If you need to add tests for another part of the project (eg. a plugin),
> create a test folder with the same krama.test.js file as weft or weaver-lib.
> Add it to the list of files loaded by Karma in karma.config.js and you're set up
> for writing new tests.

## Configuration

The configuration sits in the `karma.config.js` folder. As for the build of the app, the processing of the files is delegated to Webpack. The specific configuration for Webpack sits in the `webpack.config.js/karma.js` file.

## Options of interest

`--no-single-run`: Get Karma to watch the files and re-run the tests
`--no-browsers`: By default, attach a PhantomJS that will run the tests in the background. If you run an actuall browser, no need to this.
`--no-coverage`: Prevents instrumentation of JS for code coverage, will help when debugging in the browser (code instrumentation for coverage makes sources pretty much illegible :/).

You can also add any other Karma option, of course.

