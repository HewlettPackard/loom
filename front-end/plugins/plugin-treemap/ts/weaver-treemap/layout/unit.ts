
export enum Unit {
  Px,
  Vh,
  Vw,
  Pct,
}

export function to_css(unit: Unit): string {
  switch (unit) {
    case Unit.Px: return 'px';
    case Unit.Vh: return 'vh';
    case Unit.Vw: return 'vw';
    case Unit.Pct: return '%';
  }
}
