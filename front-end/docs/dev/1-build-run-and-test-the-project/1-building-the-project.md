Building the project
====================

`npm run build` will build the project. This command will build any of the apps
in the `apps` folder. By default, it'll build the `web` one, but you can pick another on with the `--app` option.

The result of your build will sit in the `dist` folder

Under the hood, the build is handled by [webpack](http://webpack.github.io/).

## Options of interest

You can provide any of the webpack options to the build. Most useful is probably `--watch`, which will get webpack to rebuild the app anytime one of the files used to build it changes.

## Configuration

Configuration is in the `webpack.config.js` folder. It was a bit too complex
to fit in a single file, so it's been split into smaller manageable bits.

First bit is build vs test configuration. Obviously we want to use the same build
system when running the tests. This is why you'll find, in the configuration folder an `index.js` file used for building the app and a `karma.js` file used for building the tests.

Most of the configuration is common, though. The `app-base.js` holds the common parts of both build.

Because the apps actually consist of JS & default styles to which is added a theme, the apps build is also split into two. `app.js` for the JS & default styles part, `theme.js` for the theme.

This gives the following chains:

 - App: `index.js` <-- (`theme.js` + `app.js` <-- `app-base.js`)
 - Test: `karma.js` <-- `app-base.js`

## Additional build steps for the windows 8 app

Once you're done building the JS app, you're not quite there yet for the windows 8 app. You'll then need to bundle it as an actuall Windows 8 app. `npm run msbuild` is what you're looking for here. Results will sit in the `TODO: replace with actual path` of the windows8 app.

Alternative: open the project with Visual Studio and run from there.

## Two birds, one stone with Maven

If you want to quickly build both applications, Maven is there to help. `mvn install` will get you both applications built and packaged (as a WAR file for the web app and a zip file for the windows8 app).