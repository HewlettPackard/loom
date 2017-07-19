Weaver library
==============

Javascript library used as base for building the Windows 8 and Web client.

## Prepping the ground

The application uses [Grunt](http://gruntjs.com/) to manage its build and [Bower](http://bower.io) for its dependencies.
Both tools run using [NodeJS](http://nodejs.org/) so you'll need to have it installed on your machine to fetch the dependencies and build the project:

1. *Download and install [NodeJS 0.10.12](http://nodejs.org/dist/v0.10.12/).* As of now, Bower seems to have trouble with version 0.10.13. Once installed, you should be able to run `node` and `npm` (Node Package Manager) from your command line.
2. *Install Grunt using NPM*, if it's not already on your machine : `npm install -g grunt-cli`
3. *Install Bower using NPM*, if it's not already on your machine : `npm install -g bower`

> _A word about proxies_:
>
> Bower uses the `HTTP_PROXY` and `HTTPS_PROXY` environment variables,
> so you'll need to have them set to whatever proxy you're behind.
>
> NPM doesn't seem to use the `HTTP_PROXY` (or its lowercase counterpart)
> environment variables. To configure NPM's proxy, use `npm set proxy <your_proxy>`
> for the plain HTTP one and `npm set https-proxy <your_https_proxy>` for HTTPS.
>
> NPM and Bower clone Git repositories when installing some of the dependencies.
> If your proxy doesn't accept the `git://` protocol, you can configure Git
> to use HTTP(S) instead. For Github repositories (which is essentially where the Git
> based dependencies are stored), you 'd use:
> `git config --global url.https://github.com.insteadOf git://github.com`

You've got Grunt and Bower setup! You'll then need to fetch the Grunt tasks for the build and the 3rd party libraries used in the project. The former are listed in the `package.json` file, the latter in the `bower.json`. You can install them with: `npm install` and `bower install`

This will create two new folders:

 - `node_modules`, containing the Grunt tasks NodeJS modules
 - `bower_components`, containing the 3rd party front-end Javascript libraries

## Building the project

So far, the project has no concatenation and minification build. The build only validates the Javascript with JSHint. Use `grunt` to run the validation, generate the documentation and run the tests.

Note: The JSHint rules are described in the `.jshintrc` file.

## Testing the project

### Tools of the trade

Testing is done with the [Mocha](http://visionmedia.github.io/mocha/) library using [Chai](http://chaijs.com/api/bdd/) assertions. Tests also use two additional Chai plugins to make assertions a bit easier:
 - [chai-jquery](http://chaijs.com/plugins/chai-jquery) for validations related to the DOM (checking an element has a given class for example)
 - [sinon-chai] for validations related to Sinon objects (see below)

You can use [Sinon](http://sinonjs.org/) to mock objects when you need to isolate the object you're testing, check for function calls (rather than the result of the calls) or validate HTTP requests sent by the application.

The `test-with-coverage.html` file also adds code coverage analysis using [Blanket.js](http://blanketjs.org/).

### Writing new tests

The tests are located in the `test` folder (sooo original). The structure replicates the structure of the application source code. Tests for the `views/ThreadView` class, will be in the `views` folder and be named `ThreadView.test.js`.

Once you've created your file in the appropriate folder, you'll need to add it to the list of tests in the `main.js` file. Otherwise, the tests you've written wouldn't be run :(

### Running the tests

There are two way to run the project's tests: from your browser (when developing and debugging) or in a shell (for use on CI).

To run tests in you browser, make sure you have the server running (see Running the application, below) go to [http://localhost:10000/test.html](http://localhost:10000/test.html). You should see a GUI listing the tests of the projects and their results.

For running the tests in a shell, use the `grunt test` command. It will run a temporary server to run the tests from and output the tests result in the console.

## Developing against a working copy of Weft

During development, you might need to update the features of Weft. Bower will have copied the library in `bower_components/weft`, but you can't push the changes you make there to Weft sources.
`bower update` would copy new versions of the Weft library, but doing so as you're updating Weft will be a bit of a burden.

Bower's link feature provides an easy way to build Weaver against your working copy of Weft, by replacing the `bower_components/weft` folder by a link to your working copy of Weft. This is a two steps operation (at least the first time you run it, then you should only need the second step):

1. Register your working copy of Weft as a location Bower can create link to. In the folder with your Weft working copy, run `bower link`

2. Create the link in the Weaver project. In the folder with the Weaver project, run `bower link weft`.

The Grunt `watch` task is configured to watch the location of Weft souces when the project is linked, so livereload will get triggered when you update Weft files.