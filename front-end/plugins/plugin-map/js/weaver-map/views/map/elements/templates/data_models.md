# Template data models

## Elements displayed

This is the required data to render the `countries.svg` and `markers.svg` files.

```json
{
  idGroup,
  scene : {
    transform
  },
  marker: {
    stroke
  },
  models: [{
    id, // used by markers only
    classes,
    idRef,
    inset: {
      idMask,
    },
    isUpdated,
    hasAlert,
    title // used by country only
  }],
  hatch: {
    id,
    idMask
  },
  inset: {
    stroke,
    strokeMask // used by the inset effect def
  }
}
```json

Note that a mapping must be made then between the process of re-rendering
and change that happend within the model.

## Definitions (svg `defs` tag)

For countries:
```json
{
  paths: [{
    data,
    id
  }]
}
```

```json
{
  inset: {
    strokeMask
  }
}
```

## Changes to files

### TemplateSVGRenderer impact

```js
var countryDefTemplate = require('weaver-map/views/map/elements/templates/countries/countries-defs.svg');

...

// MapRenderer is defined by MapViewElement
// and added to mapData:

this.mapRenderer = new TemplateSVGRender();
this.mapCountriesDef = this.mapRenderer.addDefs({
  template: countryDefTemplate,
  data: convertTopoJson(topojson),
});

convertTopoJson: function(topojson) {
  return {
    paths: _.map(topojson, function() {
      ...
      return {
        d: ...,   // data
        id: ...,  // country name + uuid
      }
    })
  }
},


//
// In MapComponentMarkers definition of old defsMarkers
//

var markerDefsTemplate = require('weaver-map/views/map/elements/templates/markers/markers-defs.svg');

...

this.markerDefs = this.mapRenderer.addDefs({
  template: markerDefsTemplate,
  data: {
    idGroup: _.uniqueId()
  }
});


//
// In a common class of MapComponentMarkers and MapComponentCountries
//


var insetEffectDefsTemplate = require('weaver-map/views/map/elements/templates/effects/inset-effect-defs.svg');

var hatchEffectDefsTemplate = require('weaver-map/views/map/elements/templates/effects/hatch-effect-defs.svg');

...

this.mapRenderer.addDefs({
  template: insetEffectDefsTemplate,
  refresh: true,
});

this.mapRenderer.addDefs({
  template: hatchEffectDefsTemplate,
});

getTemplateModel --> cf BaseModel


//
// In MapComponentMarkers
//

var markersTemplate = require('weaver-map/views/map/elements/templates/markers/markers.svg');

...

this.mapRenderer.add({
  template: markersTemplate,
  refresh: true,
});

...

this.templateModel.setElements(this.markersList);
this.mapRenderer.updateModel(this.rendererModel.getContext());
this.mapRenderer.refresh();

//
// In MapComponentCountries
//

var countriesTemplate = require('weaver-map/views/map/elements/templates/countries/countries.svg');

...

this.mapRenderer.add({
  template: countriesTemplate,
  refresh: true,
});

...

this.templateModel.setElements(this.countriesView);
this.mapRenderer.updateModel(this.rendererModel.getContext());
this.mapRenderer.refresh();
```

### TemlateModel impact
