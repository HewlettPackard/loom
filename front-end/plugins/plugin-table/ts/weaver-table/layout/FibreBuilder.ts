import ATableBuilder = require('./ATableBuilder');
import helper = require('plugins/common/layout/builder_interfaces');
import HasModel = require('plugins/interfaces/HasModel');
import SelectorController = require('../controller/SelectorController');

interface BuildContext extends HasModel {
  attribute_class: Array<string>;
  row_css: Array<string>;
}

class FibreBuilder extends ATableBuilder {

  selector: SelectorController;

  constructor(selector: SelectorController) {
    super();
    this.selector = selector;
  }

  buildFibre(ctxt: BuildContext, attributes_changed?: Array<string>): HTMLElement {

    var keys = _.pluck(ctxt.model.getItemType().getVisibleAttributes(), 'id');
    var classes = [];
    for (var i = 0; i < keys.length; ++i) {
      var css_class = ctxt.attribute_class[i];
      classes.push({ classes: css_class ? [css_class]: [], col: i});
    }
    _.forEach(attributes_changed, (attribute) => {
      classes[keys.indexOf(attribute)].classes.push('mas-has-changed');
    });
    var el = this.build_line(_.map(keys, (val) => {
      return helper.as_line_value(ctxt.model.get(val));
    }), {
      row: ctxt.row_css.concat(['mas-table-row']),
      generic_cell: 'mas-table-cell',
      separator: 'mas-table-separator',
      cols: classes
    });

    // see: http://stackoverflow.com/questions/8501727/table-row-wont-contain-elements-with-positionabsolute
    var wrapper_relative = this.buildTag({
      tag: 'div',
      style: 'position:relative; overflow: visible; height: 100%;'
    });
    wrapper_relative.appendChild(this.buildTag({
      tag: 'div',
      classes: 'mas-table-contextMenu',
    }));

    el.firstChild.insertBefore(wrapper_relative, el.firstChild.firstChild);

    return el;
  }

  protected col_css(i: number) {
    return this.selector.col_css(i);
  }

}

export = FibreBuilder;
