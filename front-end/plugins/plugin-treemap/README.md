# Plugin - Tree map -

Use case:

* The ThreadView is in classic mode.
* The user click on "multi-drill-down"
* The ThreadView is converted into a ThreadTreemapView
  -> we don't want to see the ThreadView collapse and reshow. Maybe decorate the thread view ? (certain)
  -> I think the switch transform the ThreadViewElements into ThreadViewTreemapElements instead.

* The thread header is also different. (later)


Problems:

* We need to have control over the sub threads created. Maybe a ListThread ?
  The threads will have identical queries in terms of operations.
  They only differ on input aggregation. Maybe simplify code to have only one pipeline here ?

* If the parent query is changed, the sub thread queries can be updated only
  after receiving the response for the parent thread. (the input will differ at that time)
  Note: the tapestry can't be incorrect so we have to remove the sub thread queries if
  the parent thread query is updated. (Will look a lot like what I did for the MetaThread)

* The tree map can be computed only when all responses have come back. But it will be painful
  to wait for all parsing to finish.
  We probably should use a web worker for that. Time to move the task work out of the map ?

* It look a lot like the weft model/api does not suit at all for my problem. Rewrite part of it
  for this particular case ?

* Mustache seems once again appropriate to generate the dom here. Try this based on a tree model ?
  -> Need to have a clear idea of the output of my data transformation.
  -> I'm converting a tree into a dom that look like what? << need to read more doc about what html can do.
  -> if needed to go to svg. Maybe reuse the TemplateSVGRenderer ?

## Typescript adding a template

// Template
/// <amd-dependency path="text!./TreeMapView.html" />
declare var require:(moduleId:string) => any;
var template = require('text!./TreeMapView.html');
