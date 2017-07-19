import ITagOptions = require('./ITagOptions');
import _ = require('lodash');
import builder_interfaces = require('./builder_interfaces');

import Classes = builder_interfaces.Classes;
import LineValue = builder_interfaces.LineValue;

class AbstractBuilder {

  buildTag(options: ITagOptions<string>): HTMLElement;
  buildTag(options: ITagOptions<Array<string>>): HTMLElement;
  buildTag(options): HTMLElement {
    var div = document.createElement(options.tag ? options.tag: "div");
    if (typeof options.classes === "object") {
      div.setAttribute("class", options.classes.join(" "));
    } else if (typeof options.classes === "string") {
      div.setAttribute("class", options.classes);
    }
    if (options.style) {
      div.setAttribute("style", options.style);
    }
    return div;
  }

  protected buildText(content: number): Text;
  protected buildText(content: string): Text;
  protected buildText(content): Text {
    if (_.isString(content) || _.isNumber(content)) {
      return document.createTextNode(content);
    } else {
      return document.createTextNode(this.nullTextMessage(content));
    }
  }

  protected nullTextMessage(content?: any): string {
    return 'None';
  }

}

export = AbstractBuilder;
