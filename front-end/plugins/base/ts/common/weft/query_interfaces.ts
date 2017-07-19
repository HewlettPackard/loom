
// An operator can be either a string (the operator's name)
// or an object verifying the following interface
export interface IsOperator {

  operator: string;
  parameters: { [index: string]: any };
}
