1-high-level-view
=================

## Overal project structure

Project actually serves to build 2 apps: a web app and a windows 8 app. They just contain bootstrapping code (and windows 8 specific module).
Both rely on two libraries:
 - Weaver, providing UI components
 - Weft, providing a model layer to communicate with Loom
Themes allow to customise the look an feel of your app
Plugins can be added to bring in new features (eg. experimental stuff, domain specific renderings...)

This leads to the following overal structure for the front-end project:

  - *apps*: Stores the parts that are specific to each app, mainly a couple of JS files to tune how they start.
    _apps/web_ hosts the sources of the Web app
    _apps/windows8_ hosts the sources of the Windows 8 app
  - *lib*: The common code of the apps is split into two layers, each one provided by a specific library.
    _lib/weft_ provides a low level API for interacting with Loom (models and common interactions).
    _lib/weaver_ provides the UI layer that is used to create the apps
  - *plugins*: Additional functionalities can be added to the apps via a system of plugin.
    _plugins/base_ provides base alterations to Weaver and Weft (most)
    _plugins/plugin-map_ adds a visualisation showing fibers on a map
    _plugins/plugin-table_ allows user to explore content of Threads in a table
    _plugins/plugin-treemap_ adds a treemap visualisation
  - *themes*: The apps can be styled via themes
    _themes/dye_ provides LESS mixins and classes to be reused by themes
    _themes/weaver-theme-flat_ is a legacy theme used as base for the HPE theme
    _themes/weaver-theme-hpe_ is the HPE theme for Weaver
  - *vendor*: Though dependencies are managed via NPM, some 3rd party scripts couldn't be loaded that way. They reside in the vendor folder. A few vendor scripts also sit in the `lib/weaver/js/weaver/utils`.

The `browser-sync.config.js` and `webpack.config.js` folder store build tools configuration.

All these parts are assembled together using webpack, taking advantage of `alias`es to point Webpack to the right files.

## General file organisation

For each project, the files are organised as such:

 - `js`: Javascript files
 - `less`: Less files (appart for the weaver library, where they sit in `css`)
 - `test`: Test files
   - `karma.test.js`: Main file that will be loaded by Karma
   - The rest of the tests follows the structure of js files and are suffixed with `.test.js` (ie. tests for `js/weft/models/Item.js` are located in the `tests/weft/models/Item.test.js` file)
 - `ts`: Typescript files (for some plugins)
 - `images`/`fonts`: Images, fonts