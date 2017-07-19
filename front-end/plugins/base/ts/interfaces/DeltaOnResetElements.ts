import Backbone = require('backbone');
import Model = Backbone.Model;


interface DeltaOnResetElements<T extends Model> {
  delta : {
    added: Array<T>;
    removed: Array<T>;
  }
  previousModels: Array<T>;
}

export = DeltaOnResetElements;
