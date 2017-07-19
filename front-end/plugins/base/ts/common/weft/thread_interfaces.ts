
import Query = require('weft/models/Query');


export interface IAttributeProperties {
  name: string;
  type: string;
  visible: boolean;
  mappable: boolean;
  plottable: boolean;
  ignoreUpdate: boolean;
  collectionType: string;
}

export interface IThreadSeenByQueryEditor {

  // Used by QueryEditor
  set(key: 'query', value: Query): void;
  set(key: string, value: any): void;
  get(key: 'query'): Query;
  get(key: string): any;

  // Used by SortByOperationView and DefaultOperationView
  getAttributesForOperation(operator: string): Array<string>;

  // Used by DefaultOperationView
  getAttribute(attributeId: string): IAttributeProperties;
}
