Weaver developer docs
=====================

## Necessary software

Before you start tinkering with Weaver, you'll need a few things installed on your machine.

The bulk of the build is done by Javascript build tools, so you'll need [NodeJS and NPM](https://nodejs.org). A few Grunt tasks have survived the modernisation of the build tools, so you'll still need [Grunt](http://gruntjs.com) too.

If you plan to work with the Windows 8 app, you'll obviously want to be on a _Windows_ machine. You'll also need _Visual Studio_ 2013 or 2015 (to get MSBuild.exe).

Last, if you intend to deploy the apps on Nexus or make a release, you'll need _Java_ and _Maven_ (which you'll also need if you want to build/run Loom server).

The project uses additional tools for building the JS app, running tests... They will be downloaded when you install the NPM dependencies, so you don't need to install them globally.

> _A word about proxies_:
>
> NPM doesn't seem to use the `HTTP_PROXY` (or its lowercase counterpart)
> environment variables. To configure NPM's proxy, use `npm set proxy <your_proxy>`
> for the plain HTTP one and `npm set https-proxy <your_https_proxy>` for HTTPS.
>
> NPM clones Git repositories when installing some of the dependencies.
> If your proxy doesn't accept the `git://` protocol, you can configure Git
> to use HTTP(S) instead. For Github repositories (which is essentially where the Git
> based dependencies are stored), you 'd use:
> `git config --global url.https://github.com.insteadOf git://github.com`
>
> Maven's proxy is [configured](https://maven.apache.org/guides/mini/guide-proxies.html) in the `settings.xml` file located in the `.m2`
> directory (which should be in your personal folder).
