
/// This interface models the minimum requirement
/// to replace a ThreadView.
interface IThreadView {
  //constructor(args: { model: Thread; selectionService: SelectionService});
  render(): void;
  remove():void;
  dispatchCustomEvent(str: string, args: any): void;
}

export = IThreadView;
