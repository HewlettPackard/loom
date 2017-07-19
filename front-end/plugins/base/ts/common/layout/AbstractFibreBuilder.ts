import HasModel = require('../../interfaces/HasModel');
import AbstractBuilder = require('./AbstractBuilder');

class AbstractFibreBuilder extends AbstractBuilder {

  buildFibre(context: HasModel): HTMLElement {
    throw new Error("Unimplemented error");
  }
}

export = AbstractFibreBuilder;
