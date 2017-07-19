4-generating-the-documentation
==============================

An API Doc and a CSS styleguide can be generated for the project. A generic command, `npm run doc` will build both of them (into the `docs/js` and `docs/css` folder, respectively). Both can also be built separately.

### JS API Docs

You can generate JS API Docs with `npm run doc:js`. The resulting docs site will be available in the `docs/js` folder.

JS API Docs are generated using [Yuidoc](http://yui.github.io/yuidoc/)

> Note: There might be a way to visualise how the different classes are linked to one another using Webpack's JSON export and its [analyse](https://github.com/webpack/analyse) tool.

### CSS Styleguide

Before generating the CSS Styleguide, you'll need to build the web app, as the CSS files will be pulled from there.

With that done, you can generate the CSS Styleguide with `npm run doc:css`. The resulting docs site will be available in the `docs/css` folder.

The CSS styleguide is generated using [KSS](https://github.com/kss-node/kss-node)