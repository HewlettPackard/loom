Windows 8 UI
============

The Windows 8 UI provides the same functionnalities as the web UI, packaged as a native Windows 8 application.
It uses the exact same files as the web UI and its build system provides a utility to import them

## Prepping the ground

The application uses [Bower](http://bower.io) to manage its dependencies.
Bower runs using [NodeJS](http://nodejs.org/) so you'll need to have it installed on your machine to fetch the dependencies and build the project:

1. *Download and install [NodeJS 0.10.12](http://nodejs.org/dist/v0.10.12/).* As of now, Bower seems to have trouble with version 0.10.13. Once installed, you should be able to run `node` and `npm` (Node Package Manager) from your command line.
3. *Install Bower using NPM*, if it's not already on your machine : `npm install -g bower`

> _A word about proxies_:
>
> Bower uses the `HTTP_PROXY` and `HTTPS_PROXY` environment variables,
> so you'll need to have them set to whatever proxy you're behind.
>
> Bower clones Git repositories when installing some of the dependencies.
> If your proxy doesn't accept the `git://` protocol, you can configure Git
> to use HTTP(S) instead. For Github repositories (which is essentially where the Git
> based dependencies are stored), you 'd use:
> `git config --global url.https://github.com.insteadOf git://github.com`

You've got Bower setup! You'll need to fetch the project dependencies by running `bower install` from a command prompt opened in the project's folder.
This will creates a `bower_components` folder containing the different libraries the project uses.

> Quick note about `bower link`
>
> Bower link allows to replace dependencies with a symlink to a folder on your machine. It's great when you need to develop against a custom working copy
> of one of the dependencies of the project, BUT... Visual Studio seems to be struggling a bit when using linked dependencies (probably trying to index the full content
> of the linked folder). So it seems to be not much of help here. You'll probably want to test any modification you want to make in the Web app (in IE10/11),
> for which `bower link` works wonders, before importing these modifications using standard `bower install` or `bower update`

## Controlling what's included in the app

The Windows 8 app will only embed the files specified listed by the `<Content>` elements in the `.jsproj` file. Make sure any new file you need embedded is listed there :)

## Packaging the app in Visual Studio & deploying it on a machine

!! NOTE: you will need to create your own Temporary Windows8 key for Visual Studio to build and deploy this App.  See here for some convenient instructions: http://blogs.remobjects.com/blogs/jim/2012/07/05/p4503

In Visual Studio, you can package the app so it's ready to be deployed without uploading it to the Windows Store by going in Project > Store > Create App Packages... and answering "No" to the first question ("Do you want to build packages to upload to the Windows Store?").

You'll find your packaged application in the AppPackages folder. The package contains a PowerShell script that deploys the app. Note that if the app is already installed on the machine you want to deploy, you'll need to uninstall it beforehand.