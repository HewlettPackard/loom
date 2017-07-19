
// T should be either string or Array<string>
export interface ColCSSClasses<T> {
  col: number;
  classes: T;
}

// T should be either string or Array<string>
export interface Classes<T> {
  separator: string;
  generic_cell: string;
  row: Array<string>;
  cols?: Array<ColCSSClasses<T>>;
}

// T should be either string or Array<string>
export interface Header<T, V> {
  values: Array<V>;
  col?: Array<T>
}

export interface LineValue {
  as_str(): string;
}

export class StringAsLineValue {
  private str: string;

  constructor(str: string) {
    this.str = str;
  }

  as_str(): string {
    return this.str;
  }
}

export function as_line_value(str: string): StringAsLineValue {
  return new StringAsLineValue(str);
}
