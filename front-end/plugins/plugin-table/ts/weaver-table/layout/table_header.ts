import builder_interfaces = require('plugins/common/layout/builder_interfaces');
import Header = builder_interfaces.Header;

export class TableHeaderValue {
  attributeName: string;
  humanReadableName: string;

  constructor(a: string, h: string) {
    this.attributeName = a;
    this.humanReadableName = h;
  }

  as_str(): string {
    return this.humanReadableName;
  }
}

export interface TableHeader<T> extends Header<T, TableHeaderValue> {
}
