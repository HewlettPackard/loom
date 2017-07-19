import Backbone = require('backbone');
import ViewOptions = Backbone.ViewOptions;
import Model = Backbone.Model;

interface ViewThreadOptions<T extends Model> extends ViewOptions<T> {
  thread?: Thread;
}

export = ViewThreadOptions;
