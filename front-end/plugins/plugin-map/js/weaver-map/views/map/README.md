Additions
=========

Here is a list of the modifications I made to add the map. It helps understanding how things works
to bring a map view.

Model:
  * ```this.model.getGeoAttributes()```:
  If not undefined, returns ```[{ latitude: 'name of the attribute', longitude: 'name of the attribute'}, ...]```
  * ```this.model.get('displayMode')```:
  If not undefined, stores the current display mode. Possible values are:
    - ```map```
    - ```classic```


ThreadListView:
  There is currently the following actions:
    * ```mas-action--close```
    * ```mas-action--clone```
  And I have added a new one:
    * ```mas-action--changeDisplayMode```
  