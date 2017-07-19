export class Rect {
  x: number;
  y: number;
  width: number;
  height: number;

  constructor(x: Rect);
  constructor(x: number);
  constructor(x: number, y: number, width: number, height: number);
  constructor(x, y = 0, width = 0, height = 0) {
    if (typeof x == 'number') {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
    } else if (typeof x == 'object') {
      this.x = x.x;
      this.y = x.y;
      this.width = x.width;
      this.height = x.height;
    }
  }

  shorter_direction(): Direction {
    if (this.width > this.height) {
      return Direction.Vertical;
    } else {
      return Direction.Horizontal;
    }
  }

  set(d: Direction, value: number): void {
    if (d == Direction.Horizontal) {
      this.width = value;
    } else {
      this.height = value;
    }
  }

  get(d: Direction): number {
    if (d == Direction.Vertical) {
      return this.height;
    } else {
      return this.width;
    }
  }
}

export enum Direction {
  Horizontal,
  Vertical
}

export function opposite(d: Direction): Direction {
  if (d == Direction.Horizontal) {
    return Direction.Vertical;
  }
  return Direction.Horizontal;
}
