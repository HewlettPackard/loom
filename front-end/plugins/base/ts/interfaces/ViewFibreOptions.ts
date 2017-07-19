import ViewThreadOptions = require('./ViewThreadOptions');
import AbstractFibreBuilder = require('../common/layout/AbstractFibreBuilder');

// This interface introduce a FibreBuilder responsible
// of building the fibre HTMLElement instance.
interface ViewFibreOptions<FB extends AbstractFibreBuilder> extends ViewThreadOptions<Element2> {
  builder: FB;
}

export = ViewFibreOptions;
