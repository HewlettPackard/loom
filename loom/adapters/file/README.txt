The file adapter includes an example of user-defined function that they want Loom to include in the "library" of 
available functions.

Adding these custom functions to Loom makes them immediately available for usage from our REST API.

#######################################################################################################################

# create empty tapestry, it returns a tapestry id 5b7bd299-0b0d-46ea-9fc2-4352dbe688fa
POST /tapestries with body: {"id":null,"threads":[]}

# add new thread to tapestry containing a thread with a query invoking the Adapter-defined operation (declared in the FileSystemAdapter.java as 'extractFileValues'"
# Note that {tapestryId} in the URL below should be replaced with the id returned in the previous call: 5b7bd299-0b0d-46ea-9fc2-4352dbe688fa
POST /tapestries{tapestryId}/threads with body: {"id":"file-10","itemType":"file-file","query":{"inputs":["file/files"],"operationPipeline":[{"operator":"file/file/extractFileValue", "parameters": {}}]},"name":"addValue"}

# get the results of the query specified in the thread
# Note: {tapestryId} in the URL below should be replaced with the id returned in the previous call: 5b7bd299-0b0d-46ea-9fc2-4352dbe688fa
# Note 2: {threadId} is the id we setup in the thread above: 'file-10' 
GET /tapestries/{tapestryId}/threads/{threadId}/results


#######################################################################################################################

For a quick validation this custom operation was successfully registered in Loom, we'll use the (still experimental) nodejs CLI:

0. Boot your loom server with the file adapter properties file in your adapters file
1. rm $HOME/.loom; cd loom/weaver/client-node-loom
2. register -n http://localhost:9099/loom
3. bin/loomCli.js providers
4. bin/loomCli.js login -u test -p test -t file -i file
5. bin/loomCli.js patterns -p File
6. bin/loomCli.js threads -a '{"id":"file-10","itemType":"file-file","query":{"inputs":["file/files"],"operationPipeline":[{"operator":"file/file/extractFileValue", "parameters": {}}]},"name":"addValue"}'
7. bin/loomCli.js query -p file-10


