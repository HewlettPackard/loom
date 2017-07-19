Scripts to support automated profiling on Loom Service

Usage:
	1. Run "./profileloom.sh </path/to/yourkit/> capture_performance <host> <port> " to capture a snapshot. This should be triggered at the end of each test (10 clients, 100 clients, and so on).

	#########Once all tests are done#########
	2. Run "./profileloom.sh </path/to/yourkit/> export_to_csv" to export raw snapshots to csv files
	3. Run "./profileloom.sh </path/to/yourkit/> generate_graphs"

	The graphs are in PNG format and saved to /src/test/profiling/graphs