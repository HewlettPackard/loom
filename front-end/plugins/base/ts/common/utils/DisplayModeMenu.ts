import Menu = require('weaver/views/Menu');
import PropertySelector = require('weaver/views/PropertySelector');

import DisplayMode = require('./DisplayMode');

class DisplayModeMenu extends Menu {

    helper: DisplayMode;
    propertySelector: PropertySelector;
    toggleElement: HTMLElement;

    initialize(): void {
      Menu.prototype.initialize.apply(this, arguments);

      this.className = Menu.prototype.className + ' mas-displayMenu is-collapsed';
      // this.$el = template.clone();

      this.helper = new DisplayMode({
        thread: this.model
      });

      this.propertySelector = new PropertySelector({
        title: 'Display Options',
        preventDeselectionOnSameClick: true,
        model: this.helper.getPossibleDisplayModeWithReadableName()
      });

      this.propertySelector.select(this.model.get('displayMode'), false);

      this.listenTo(this.propertySelector, 'change:selection', this._updateDisplayMode);
      this.toggleElement = undefined;
      this.render();
    }

    render(): void{
      Menu.prototype.render.apply(this, arguments);
      this.stickit();

      this.$el.addClass(this.className);

      this.propertySelector.$el.addClass('mas-menu--content');
      this.el.appendChild(this.propertySelector.el);

      if (!this.helper.hasManyDisplayMode()) {
        this.$el.addClass('mas-is-visibility-hidden');
      }
    }

    _renderToggle(): void {
      var toggle = this.toggleElement = document.createElement('div');
      var globe = document.createElement('div');
      var list = document.createElement('div');
      var refresh = document.createElement('div');
      globe.classList.add('mas-displayMode--map');
      globe.classList.add('fa');
      globe.classList.add('fa-globe');
      list.classList.add('mas-displayMode--classic');
      list.classList.add('fa');
      list.classList.add('fa-th-list');
      refresh.classList.add('mas-displayMode--switch');
      refresh.classList.add('fa');
      refresh.classList.add('fa-refresh');
      toggle.appendChild(globe);
      toggle.appendChild(list);
      toggle.appendChild(refresh);
      toggle.classList.add('mas-displayMenu--toggle');
      this.$('.mas-menu--toggle').prepend(toggle);
    }

    _updateDisplayMode(displayMode): void {
      this.model.set('displayMode', displayMode);
    }
}

export = DisplayModeMenu;
