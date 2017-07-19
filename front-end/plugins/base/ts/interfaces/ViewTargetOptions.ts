import Backbone = require('backbone');
import ViewOptions = Backbone.ViewOptions;

interface Options extends ViewOptions<any> {
  target?: HTMLElement;
}

export = Options;
