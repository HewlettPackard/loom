
var NB_ROW_NOT_REVERSED = 7;

function row_css(col: number): Array<string> {
  return (col >= NB_ROW_NOT_REVERSED) ? ['mas-last-row']: [];
}

export = row_css;
