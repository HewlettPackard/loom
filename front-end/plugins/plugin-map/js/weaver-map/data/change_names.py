#!/usr/bin/env python

import codecs
import csv
import json
import difflib

country_code = {}
json_obj = {}
template = u"{0:35}{1:20}{2:20}{3:20}"

def findBetterMatches(name):
  if name in country_code:
    return country_code[name]
  matches = difflib.get_close_matches(name, country_code.keys())
  return map(lambda x: (x, country_code[x]), matches)


with codecs.open('country.csv', mode='r') as f:
  lines = csv.reader(f)
  test = False
  for row in lines:
    if test:
      country_code[row[1]]=row[0]
    test = True


with codecs.open('world-with-names.json', mode='r') as f:
  json_obj = json.load(f)
  geo = json_obj['objects']['countries']['geometries']
  for i in xrange(len(geo)):
      name = geo[i]['name']
      matches = findBetterMatches(name)
      if type(matches) == str:
        geo[i]['name'] = matches
      else:
        if len(matches) == 1:
          _, code = matches[0]
          geo[i]['name'] = code
        elif len(matches) == 0:
          # We're fucked.
          geo[i]['name'] = raw_input('Country code for ' + name + ' ?')
        else:
          j = raw_input('Ambiguity for "' + name + '" : ' + matches.__str__() + ' Code ?')
          if type(j) == int:
            _, code = matches[j]
            geo[i]['name'] = code
          else:
            geo[i]['name'] = j

      print template.format(name, 'transformed into ', geo[i]['name'], matches)

with codecs.open('world-with-country-codes.json', mode='w') as f:
  json.dump(json_obj, f)