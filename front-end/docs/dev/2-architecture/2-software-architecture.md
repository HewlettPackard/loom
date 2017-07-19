2-software-architecture
=======================

## Javascript

### General principles

The project relies on Backbone.js for its architecture.

> Note: Updating Backbone to its latest version (1.2.3) sounds tempting
> but they made changes in the way parameters are set in the `options` properties
> of the views (amongst other change), so the project still uses version 1.0.0

The app is bootstrapped by the `Weaver` class, that handles the routing between the different sreens of the application.

The data is stored in `Model`s. Though you can instanciate `Model`s at will, you'll probably want to access data from a Loom server. The `AggregatorClient` is your entry point for that. It will help you connect to Loom server and retrieve `Model`s from the various API calls you can make.

> Note: The `AggregatorClient` relies himself of the `LoomClient` to communicate with Loom. The `AggregatorClient` provides parsed `Model`s while the `LoomClient` returns plain Javascript objects.

`services` manage functionnalities spanning across multiple `Model`s (actions affecting only one `Model` usually have their own method on the model class).

The screens are composed of a variety of `Views`, built as small components responsible for rendering the data from `Models` and turning user events into the appropriate calls on `Models` or `Services`.

To avoid bulking the code of `Views` and encapsulate specific features some event handling is delegated to `Controller`s. They are Backbone `View`s which won't render anything and just handle events. They usually are instanciated on the DOM element of one of the `View`s.

### Details of interest

Updates to `View`s should be driven by reacting to an update of some `Model`. Even when an event will update the same view that's handling it, the idea is to have the `View` update its `Model` and then react to the change to update its render. This should help with consistency between what's displayed and the state of the data.

The app uses DOM event bubbling a lot to handle events. Instead of passing tons of data down nested views, it lets event bubble up until it reaches the appropriate data higher up in the DOM tree. Views can trigger custom events to communicate with their parents that way (in addition to standard DOM events) and data can be attached to event targets through jQuery's `data()` method.

DOM elements used for triggering actions on controller follow one of these two nomenclatures generally. They have a `.mas-action--<nameOfTheAction>` class or a `data-action=<nameOfTheAction>` attribute.

A common source of memory leaks is references being kept because of event listeners. Views need to make sure they stop listening to events appropriately (usually when removed from screen). Using Backbone `View`'s `listenTo` method instead of the `Model`s' `on` methods will automatically remove listener when the view is removed. This should leave you with only the controllers and child views to handle.

Each `View` adds a `.mas-<nameOfTheView>` CSS class to its DOM Element to help with styling, as well as debugging. `Views` inheriting `BaseView` also expose the view through `jQuery`'s `data()` method.

Some views use HTML templates to keep the component structure readable when the rendered DOM is a bit complex. `BaseView`s will automatically generate appropriate DOM from its template property. You can then use jQuery to manipulate whatever part needs update when rendering the view.


## CSS

The styles of the applications are split between layout styles (controlling where elements sit on the screen) and theme styles (controlling the look and feel). The former are bundled in the library (and plugins) (in the `css` folder of the `weaver` project). Themes get build separately and sit in the `themes` folder.

Classes follow a [BEM](https://en.bem.info/)-like nomenclature:

 - each class is prefixed with `mas-` to avoid collisions if 3rd party stylesheets get brought into the project
 - `Views` attach a `mas-<nameOfTheView>` class to their elements (eg. `mas-graphBar`)
 - classes representing specific states of the views can be named two ways:
   - `mas-<nameOfTheView>-<nameOfTheState>` (eg. `mas-graphBar-notZero`)
   - `is-<nameOfTheState>` or `has-<nameOfTheState>` (eg. `is-loggingIn`)
 - to identify specific child elements within a View (eg. to pick them up with jQuery or adjust their styling), you can use classes with the following syntax: `mas-<nameOfTheView>--<nameOfTheElement>`.

This system makes for longer names of selectors, but allows to keep specificity low. Ideally, you'll want to use one perhaps two classes to apply styles. This will make it easier to override style when composing `View`s.

### "Layout" styles

The styles responsible for the layout of the application (ie. those in the weaver library) are organised the following way:

 - base.less - base styles, ie styles applying directly to elements or generic components
 - icons.less - legacy icon classes (still used by some of the providers to use an icon for their action). Most icons are applied using font-awesome classes, either directly or as mixins. This file could be revived into an inventory of all icons in the application, which would help knowing which icons are needed (eg. when designing an icon set for the app)
 - layouts - generic layouts
 - components - styles of specific views. Usually the styles for ComponentABC.js will be in ComponentABC.less. Sometimes multiple component are aggregated in one file, so when looking for styles of a specific component, it's easier to do a search on the `.mas-componentABC` class it add to its DOM element
 - screens - style of screens, assembling multiple views
 - mas-width-480 - Media query code for adjusting the view

### Theme styles

Some common mixins are provided in the `dye` project. The HPE theme was built extending the legacy "flat" theme. New components have only had their themes added to the HPE theme, however so:
 - if having a `flat` theme is still usefull, the styles for the new components should be ported to the `flat` theme
 - if not, both projects should be merged in order to avoid duplication

The structure of the flat theme follows the same general idea as the "layout" styles.
Because the HPE them is leaner, it is mostly hold in its theme.less file.

When building themes depending on other, it is useful for the parent theme to expose variables for overidding the values of CSS properties. Obviously, doing so for all values would be really unproductive, but key settings (eg. main colors, main dimensions) can be exposed that way. Further overriding can be done in the child theme by adding styles for the same selector.

## Plugins

The plugins system was introduced to have new functionnalities added without impacting the main library. It doesn't really introduce an API with well delimited extension points. Instead it uses Javascript's ability to edit objects prototypes in order to override existing behaviour.

Each plugin must have a `js/main.js` file that Webpack will use for adding the plugin to the app. This file doesn't need to be handwritten, and could be, for example, the result of TypeScript compilation.

From this `js/main.js` file, you can then `require()` whichever class you want to override the behaviour and alter its prototype accordingly.

Layout styles can be added to the app automatically too by `require()`ing any necessary LESS files from your plugin. For themes, however, the styles will need to be added directly in the themes folder.

> Note: To facilitate maintenance when making changes to the main library, it would be good to introduce a proper API for extending Weaver and Weft, with properly documented extension points.
> Note: If keeping on with prototype overriding, plugins might benefit from an AOP library (eg. meld.js).

