2-running-the-project
=====================

Usually the next step after building the project: running it. The situation will obviously depend on which app you've built.

> Note: When developing, it's more efficient to use the web app (unless building a windows 8 only feature, of course). The build script can watch your files and the server will reload automatically. Faster than rebuilding and reinstalling the windows app every time. Once things look OK in the web app, make the final checks in the windows 8 app.

## Web app

`npm start` will start a server hosting the web app. Once up, you can access it  on port 3000. The server will proxy requests to '/loom' to Loom server. By default, it will go to http://localhost:9099. It can be configured by setting the LOOM_SERVER environment variable before running the script.

Under the hood, this uses [BrowserSync](https://github.com/BrowserSync/browser-sync) (mostly because I couldn't get the Webpack Dev Server configuration to work, though)

The configuration is stored in the `browser-sync.config.js` folder.

You can pass any browsersync option to the command, however, be aware of that browsersync doesn't overide settings from the config file with the CLI arguments :( (https://github.com/BrowserSync/browser-sync/issues/796)

## Windows 8 app

After you've build the Windows 8 app, you'll need to install it before you run it.
If Weaver was already installed, make sure you uninstall it before installing the new version.

In the `AppPackages` folder (or the zip file downloaded from nexus), you'll find a `windows8.1<some.Version.Number>_AnyCPU_Test` folder, with the `Add-AppDevPackage.ps1` script that you need to run for installing the app.



