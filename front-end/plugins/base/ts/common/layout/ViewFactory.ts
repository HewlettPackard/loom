import Backbone = require('backbone');

import ViewOptions = Backbone.ViewOptions;
import Model = Backbone.Model;

interface ViewFactory<V, M extends Model> {
  (options: ViewOptions<M>) : V;
}

export = ViewFactory;
