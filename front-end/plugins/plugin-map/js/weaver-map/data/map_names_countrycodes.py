#!/usr/bin/env python

import codecs
import json

json_obj = {}
map_id_names = {}

with codecs.open('world-with-names.json', mode='r') as f:
  data = json.load(f)
  geo = data['objects']['countries']['geometries']
  for i in xrange(len(geo)):
    name = geo[i]['name']
    id = geo[i]['id']
    map_id_names[id] = {
      'name': name
    }


with codecs.open('world-with-country-codes.json', mode='r') as f:
  data = json.load(f)
  geo = data['objects']['countries']['geometries']
  for i in xrange(len(geo)):
    cc = geo[i]['name']
    id = geo[i]['id']
    map_id_names[id]['cc'] = cc


with codecs.open('map-countrycodes-names_en.json', mode='w') as f:
  for k, v in map_id_names.iteritems():
    json_obj[v['cc']] = v['name']
  json.dump(json_obj, f)