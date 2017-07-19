Weaver web app
==============

Web application packaging for the Weaver application, based on the Weaver library.



## Prepping the ground

The application uses [Grunt](http://gruntjs.com/) to manage its build and [Bower](http://bower.io) for its dependencies.
Both tools run using [NodeJS](http://nodejs.org/) so you'll need to have it installed on your machine to fetch the dependencies and build the project:


### 1. Install [NodeJS](http://nodejs.org/).

If you are on Windows, simply download and install node on the [official web site](http://nodejs.org/).

If you are on a linux distribution, your packet manager might have an old version of node.
Besides, when using the package version of node, you will have to be root to install `bower` and `grunt`.

You should consider using [nvm](https://github.com/creationix/nvm) instead. Once `nvm is ready`:
 * `nvm install 0.12`
 * `nvm alias default 0.12`

Once installed, you should be able to run `node` and `npm` (Node Package Manager) from your command line.
Weaver requires npm to be at least version 2.0 which is the case when downloading node starting at version 0.11.


### 2. Install Grunt using NPM

If it's not already on your machine : `npm install -g grunt-cli`

> If you had already installed `grunt-cli` globally and just started using `nvm`,
> you should remove it first:
>
>   1. nvm use system
>   2. sudo npm uninstall -g grunt-cli
>   3. nvm use default
>   4. npm install -g grunt-cli


### 3. Install Bower using NPM

If it's not already on your machine : `npm install -g bower`

> If you had already installed `bower` globally and just started using `nvm`,
> you should remove it first:
>
>   1. nvm use system
>   2. sudo npm uninstall -g bower
>   3. nvm use default
>   4. npm install -g bower

### A word about proxies:

Bower uses the `HTTP_PROXY` and `HTTPS_PROXY` environment variables,
so you'll need to have them set to whatever proxy you're behind.

NPM doesn't seem to use the `HTTP_PROXY` (or its lowercase counterpart)
environment variables. To configure NPM's proxy, use `npm set proxy <your_proxy>`
for the plain HTTP one and `npm set https-proxy <your_https_proxy>` for HTTPS.

NPM and Bower clone Git repositories when installing some of the dependencies.
If your proxy doesn't accept the `git://` protocol, you can configure Git
to use HTTP(S) instead. For Github repositories (which is essentially where the Git
based dependencies are stored), you 'd use:
`git config --global url.https://github.com.insteadOf git://github.com`


### Finally

You've got Grunt and Bower setup! You'll then need to fetch the Grunt tasks for the build and the 3rd party libraries used in the project.
The former are listed in the `package.json` file, the latter in the `bower.json`.
You can install them with: `npm install` and `bower install`

This will create two new folders:

 - `node_modules`, containing the Grunt tasks NodeJS modules
 - `bower_components`, containing the 3rd party front-end Javascript libraries



## Building the application *( deployment )*

> You can skip this part if you don't intend any deployment of the application. The building
> task is not required to use weaver locally on you machine. See "Running the application" for more details.

Weaver comes with a number of plugins, all of them are optional. Building weaver will produce a folder called
dist that contains everything needed to deploy the web application. The following build options are:

 * `grunt build`        will build weaver without any plugins.
 * `grunt build:all`    will build weaver with all plugins currently available.
 * `grunt build:A:B`    will build weaver with plugin A and B only.

The list of plugins' name can be found inside the `weaver/plugins/` folder. They all start with the `plugin-` prefix.
With the following list of plugin folder:

    plugin-map, plugin-treemap, plugin-table

You can build weaver only with the map by doing:

    grunt build:map

And build weaver only with the table and the treemap by running:

    grunt build:map:treemap



## Running the application

The `grunt dev` command starts up a local server to host the project. It also automatically opens the project in your default browser. You can use `grunt dev:noBrowser` if you don't want it to launch the browser (eg. if your default browser is an old IE).

The server also proxies requests to the aggregator, so we don't run into crossdomain issues. Any request to `/warp` will go to the proxied aggregator.
You can configure which aggregator is proxied using those 3 command line parameters:

 - *--aggregator.host* sets the host of the aggregator (without http://). Defaults to `localhost`.
 - *--aggregator.port* sets the port of the aggregator. Defaults to `8080`
 - *--aggregator.context* sets the remote context the aggregator is deployed to. Defaults to `/loom`

If you aggregator runs on `http://mas-loom-br.hpl.hp.com:8080/loom-stable/loom`, you'd use `--aggregator.host=mas-loom-br.hpl.hp.com --aggregator.context=loom-stable\loom`.

The Gruntfile also contains a list of well-known aggregator that can be quickly accessed via the `--aggregator` option. If no option is given the `default` aggregator will be used.

 - node: `http://localhost:4000/loom`
 - loom: `http://loom.mas-dev-loom.ext8.sup.hpl.hp.com/loom`
 - loom-test: `http://loom.mas-test-loom.ext8.sup.hpl.hp.com/loom`
 - default: `http://localhost:8080/loom`

The `grunt dev` command will also monitor your files and reload your page when Javascript, HTML or CSS files change.

!! Bower has a cache which can prevent the latest changes being reflected in the version built into web/dist
!! and consequently served to the browser.  To clear the cached versions of weft and weaver and ensure
!! you are using the latest code do the following:
!!
!! Make any server started with `grunt dev` is stopped
!! Uninstall the `weft` and `weaver` dependencies using the following command: `bower uninstall weft weaver`
!! Remove weft and weaver from bower�s cache with the following command: `bower cache clean weft weaver`
!! Re-import the dependencies using `bower install`

## Developing against a working copy of Weft and the Weaver library

During development, you might need to update the features of Weft or the Weaver library. Bower will have copied those in `bower_components/weft` and `bower_components\weaver` respectively, but you can't push the changes you make there to Weft or Weaver library sources.
`bower update` would copy new versions of the libraries, but doing so as you're updating Weft will be a bit of a burden.

Bower's link feature provides an easy way to build the web app against your working copy of Weft and/or the weaver libraries. It replacing the corresponding folder in `bower_components` by a link to your working copy of the underlying library. This is a two steps operation (at least the first time you run it, then you should only need the second step):

1. Register your working copy of Weft or the Weaver library as a location Bower can create link to. In the folder with the library working copy, run `bower link`

2. Create the link in the Weaver web app project by runnin `bower link weft` or `bower link weaver`

The Grunt `watch` task is configured to watch the location of Weft and Weaver library souces when the project is linked, so livereload will get triggered when you update their files.

When the server is up, their test pages will also be available at the following URLs:

 - http://localhost:10000/bower_components/weaver/tests.html
 - http://localhost:10000/bower_components/weft/tests.html
