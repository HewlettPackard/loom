import $ = require('jquery');
import ThreadSettingsMenu = require('weaver/views/ThreadSettingsMenu');

//import MapMenu = require('./ThreadViewHeader/MapMenu');
import DisplayModeMenu = require('../../common/utils/DisplayModeMenu');

var originalInitialize = ThreadSettingsMenu.prototype.initialize;

function addButton(css: string, name: string, style: string): JQuery {
  style = style || "";
  return $('<div class="mas-threadSettingsMenu--item mas-menu ' + css + ' mas-menu-expandsDown mas-menu-rightAligned">' +
      // There is a div here because of the behavior of buttons in Firefox:
      // https://bugzilla.mozilla.org/show_bug.cgi?id=984869#c2
      '<div class="mas-menu--toggle mas-button-leftIcon" ' + style + '>' + name + '</div>' +
  '</div>');
};

ThreadSettingsMenu.prototype.initialize = function () {

  originalInitialize.apply(this, arguments);

  this.$('.mas-menu--content:first-child').prepend(
    addButton('mas-displayMenu', 'Display Mode', 'style="display: flex"')
  );
  /*this._addButton('mas-mapMenu', 'Distortion');

  this.mapMenu = new MapMenu({
    model: this.model,
    el: this.$('.mas-mapMenu'),
  });*/

  this.displayModeMenu = new DisplayModeMenu({
    model: this.model,
    el: this.$('.mas-displayMenu'),
  });
};

var decorate = () => {};
export = decorate;
