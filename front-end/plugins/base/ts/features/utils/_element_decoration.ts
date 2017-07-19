import _ = require('lodash');
import Translator = require('../../common/models/Translator');
import Element = require('weft/models/Element');

var originalDefaults = Element.prototype.defaults;

//Element.prototype.idAttribute = 'weaverId';

Element.prototype.defaults = function () {
  var def = originalDefaults.call(this);
  return _.extend(def, {

    /**
     * Translator reference, Allows to show better names than
     * the one outputed by loom. Currently essentially used for countries.
     */
    translator: new Translator(),
  });
};

Element.prototype.getTranslated = function () {
  var res = Element.prototype.get.apply(this, arguments);

  if (_.isString(res)) {
    return this.get('translator').translate(res);
  }

  return res;
};

var decorate = () => {};
export = decorate;
