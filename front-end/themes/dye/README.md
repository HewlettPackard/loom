Dye - The theme creator for Weaver
==================================

Dye helps you build themes for Weaver using LESS and Grunt.

## Prerequisites

Dye is packaged as an NPM module, so you'll need [NodeJS](http://nodejs.org/)
and [NPM](http://npmjs.org) instaled on your machine. You'll also need Grunt's CLI
installed, which you can do with `npm install -g grunt-cli` (you might need to sudo on linux).

## Getting started

Dye is packaged as an NPM module, so it can be easily installed in your project with:
```
npm install <path-to-dye>
```

This will install a local copy of Dye in your project, as well as the Grunt modules it relies on.

Next you'll need to create the main LESS file for your theme, which **must** be `less/theme.less`.
You can then use the following command to create turn your LESS file into the theme's CSS:
```
grunt --base=. --gruntfile=node_modules/dye/Gruntfile.js css
```

## Using dye in your less files

Dye provides a few mixins and utitities to help you build you theme:

 - colors.less:              A list of color variables providing a default color palette
 - icons.less:               Loads font-awesome mixins (without inserting them in the CSS,
                             as the Weaver library already does that).
 - measures.less:            Common measures of different elements of the UI
 - mixins/foldedCorner.less: A mixin to help you create a beveled and/or folded corner
 - mixins/fontface.less:     A mixin for loading fonts
 - menu.less:                A mixin providing the positioning for the content of the menus
 - messages.less:            A mixin with default styling for messages
 - stripes.less:             A mixin to create stripe background images

To help importing those mixins, Dye provides the @DYE_PATH variable containing the path
to the root of the dye project. Make sure you use this variable when importing Dye's mixins:

```
@import "@{DYE_PATH}/less/colors.less";
```

> Note: The @DYE_PATH is necessary because NPM won't install modules that have already
> be installed by a parent package. So if you're building theme-B based on theme-A,
> theme-A wouldn't get a copy of dye in its own `node_modules` folder, which
> will prevent it to build properly. Additionally, this saves unnecessary copies
> of dye

## Recommended project structure

The only mandatory file is the main theme file, which must be `less/theme.less`.
Apart from that, you can organise your files the way that best suits you.
Remember that the generated CSS will sit in the `css` folder,
and that the paths to assets (fonts, images) in CSS are relative to the stylesheet.
That said, to keep things tidy, the following folder structure is recommended:
```
my-project
  |-- fonts        // Store font files there
  |-- images       // Store image assets there
  |-- less         // Store you LESS files there
    |-- theme.less // Mandatory file for LESS
    |-- ...        // Other LESS files
```

Regarding the organisation of the LESS files, breaking the theme into small files
focused on specific parts of the UI will make things easier to understand. It will
also help building upon your theme, as other developers will be able to import only
part of your theme to reuse.


## Available commands

&lt;command&gt; can be:

 - **css**:        Uses LESS to build the theme's CSS file and runs autoprefixer to add vendor prefixes when needed
 - **watch**:      To watch the LESS files and create theme
 - **dye-server**: Starts server to act as a proxy to an existing weaver deployment,
                   allowing you to develop your theme against it. Server configuration
                   can be tweaked with the following CLI parameters: 
                    + `dye-server.port`:           The port the server will run on
                    + `dye-server.themePath`:      The path to the theme's CSS file
                    + `dye-server.proxiedHost`:    Host of the existing weaver deployment
                    + `dye-server.proxiedPort`:    Port of the existing weaver deployment
                    + `dye-server.proxiedContext`: Context of the existing weaver deployment
 

## Advanced usage

### Building upon an existing theme

Import the theme you want to use in your project, preferably using NPM.
You can then use LESS to import part or the entirety of the theme you want to build upon,
and override whichever parts you feel necessary, or just update values of the parent's theme
variable if it has some.

### Adding tasks to the build

You can create your own Gruntfile to customize the build of your theme
(eg. if you want to pre-process images, add different pre/post processing steps
to the CSS processing...). You can `require()` Dye's Gruntfile to get a base configuration,
then update the tasks configuration using the Grunt API,
notably [`grunt.config.merge()`](http://gruntjs.com/api/grunt.config#grunt.config.merge).

## Ideas for the future

### Add packaging task to Gruntfile

 - assembles everything but the LESS folder in a tarball?
 - maybe use a file to list what to include in the tarball (include/exclude patterns?)

### Add a `proxy` task that would

 - ✔ serve the theme's folder
 - ✔ proxy to a Weaver deployment
 - ✔ override the weaver-config to load the theme
 - livereload/browsersync the theme's file when it changes

### Add a way to copy assets when building upon themes

 - add keyword in `package.json` to identify weaver themes (`weaver-theme` maybe?)
 - add a `copy:parent-assets` task to copy parent theme's asset

### Add a task to delete duplicate rules in generated CSS

 - `grunt-css-purge` might be an option