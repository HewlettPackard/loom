
class CollapseController {

  private collapsed_attributes: {[index: string]: boolean};
  private attributes: Array<string>;

  constructor(attributes: Array<string>, collapse?: CollapseController) {
    if (collapse) {
      this.collapsed_attributes = collapse.collapsed_attributes;
    } else {
      this.collapsed_attributes = {};
    }
    this.attributes = attributes;
  }

  is_attribute_collapsed(attributeName: string): boolean {
    return this.collapsed_attributes[attributeName];
  }

  is_column_collapsed(col: number): boolean {
    return this.is_attribute_collapsed(this.attributes[col]);
  }

  collapse_column(col: number): void {

    this.collapsed_attributes[this.attributes[col]] = true;
  }

  expand_column(col: number): void {
    this.collapsed_attributes[this.attributes[col]] = false;
  }
}

export = CollapseController;
