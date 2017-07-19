Weaver CSS Styleguide
=====================

This documentation presents the different CSS components involved in the styling of the Weaver app'.

## File organisation

 - `style.less`: main LESS file, used to import all the other stylesheets
 - `base.less`: base styles for HTML elements. Those styles aim at getting a consistent default
   behaviour across browsers (eg. having consistent margin and paddings on `<ul>`). They should not be
   used to define a "visual style" for those elements. This will be the role of the stylesheets in the
   `component` folder.
 - `mixins`: stores LESS mixins to help with development
 - `icons.less`: provides style for displaying an icon before an element
 - `layout.less`: stores the rules related to the overall layout of the application (think laying out
   the whole page, not a specific component)
 - `components`: stores the styles for the different components of the application