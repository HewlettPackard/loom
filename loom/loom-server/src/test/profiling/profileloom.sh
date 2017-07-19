#!/bin/sh
#*******************************************************************************
# (c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License,
# Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
# may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
# or implied. See the License for the specific language governing permissions and limitations under
# the License.
#*******************************************************************************

YOURKIT_API="$1/lib/yjp-controller-api-redist.jar"
YOURKIT_APP="$1/lib/yjp.jar"
R_PATH=$(dirname $0)
OPERATION=$2
HOST=$3
PORT=$4
ARGUMENTS=$#
SNAPSHOTS_DIR="$HOME/Snapshots"
CSV_DIR="$SNAPSHOTS_DIR/csv"
PRO_CSV_DIR="$SNAPSHOTS_DIR/processed_csv"
GRAPHS_DIR="$SNAPSHOTS_DIR/graphs"

capture_performance() {
	echo "Capture performance snapshots"
	java -jar $YOURKIT_API $HOST $PORT capture-performance-snapshot
}

export_to_csv() {
	# verify the existence of $HOME/Snapshots directory
	if [ -d "$SNAPSHOTS_DIR" ]
	then
		echo "Export snapshots from directory $SNAPSHOTS_DIR"
	else
		mkdir $SNAPSHOTS_DIR
	fi

	# verify the existence of $HOME/Snapshots/csv directory
	if [ -d $CSV_DIR ]
	then
		rm -R $CSV_DIR
		mkdir $CSV_DIR
	else
		mkdir $CSV_DIR
	fi
	echo "Export snapshots into $CSV_DIR"


	NO_OF_SNAPSHOTS=`ls -1 $SNAPSHOTS_DIR/*.snapshot 2>/dev/null | wc -l`
	if [ "$NO_OF_SNAPSHOTS" = "0" ]
	then
		echo "There is no snapshot to export"
		exit 0
	fi

	COUNT=1
	for f in `ls -tr $SNAPSHOTS_DIR/*.snapshot`
	do
		DIR="$CSV_DIR/$COUNT"
		mkdir $DIR
		java -jar -Dexport.csv -jar $YOURKIT_APP -export $f $DIR
		COUNT=$((COUNT+1))
	done
}

clear_data() {
	echo "Clear all snapshots"
	rm -R $SNAPSHOTS_DIR/*.snapshot
	if [ -d $SNAPSHOTS_DIR/csv ]
	then
		rm -R $SNAPSHOTS_DIR/csv
	fi
	if [ -d $SNAPSHOTS_DIR/processed_csv ]
	then
		rm -R $SNAPSHOTS_DIR/processed_csv
	fi
	if [ -d $SNAPSHOTS_DIR/graphs ]
	then
		rm -R $SNAPSHOTS_DIR/graphs
	fi
}

remove_duplicate_and_create() {
	# create directory for processed csv
	FILE=$1
	PRO_DIR=$2

	LINE=2
	COUNT=1

	mkdir $PRO_CSV_DIR/$PRO_DIR

	for f in `ls -dtr $CSV_DIR/*/`
	do
		if [ -f  $f/$FILE ]
		then
			NO_LINE=`wc -l $f/$FILE | grep -o "^[0-9]\+"`
			# create processed file and copy header
			sed -n -e '1,1p' $f/$FILE > $PRO_CSV_DIR/$PRO_DIR/${PRO_DIR}_$COUNT.csv
			# copy non-duplicated entries
			ARG="$((LINE+1)),${NO_LINE}p"
			LINE=$NO_LINE
			sed -n -e $ARG $f/$FILE >> $PRO_CSV_DIR/$PRO_DIR/${PRO_DIR}_$COUNT.csv
			COUNT=$((COUNT+1))
		else
			echo "File $f/$FILE does not exist in $f"
			exit 1
		fi
	done
}

process_csv() {
	remove_duplicate_and_create "Chart--CPU-Usage.csv" "cpu"
	remove_duplicate_and_create "Chart--Heap-Memory.csv" "memory"
	# remove_duplicate_and_create "Chart--JSP-Servlet-Requests--Count.csv" "count"
	# remove_duplicate_and_create "Chart--JSP-Servlet-Requests--Duration.csv" "duration"
}

run_R() {
	METRIC=$1
	DATE=$2
	TIME=$3
	R_ARGS="$METRIC $DATE $TIME $GRAPHS_DIR "
	for f in `ls -dtr $PRO_CSV_DIR/${METRIC}/*`
	do
		R_ARGS="$R_ARGS $f"
	done
	Rscript $R_PATH/process_csv.R $R_ARGS
}

generate_graphs() {
	# verify the existence of $HOME/Snapshots/graphs directory
	if [ -d $GRAPHS_DIR ]
	then
		rm -R $GRAPHS_DIR
		mkdir $GRAPHS_DIR
	else
		mkdir $GRAPHS_DIR
	fi
	echo "Generate graphs"

	# verify the existence of $HOME/Snapshots/processed_csv directory
	if [ -d $PRO_CSV_DIR ]
	then
		rm -R $PRO_CSV_DIR
		mkdir $PRO_CSV_DIR
	else
		mkdir $PRO_CSV_DIR
	fi

	process_csv

	DATETIME=`date +"%d-%m-%y %H:%M:%S"`
	run_R "memory" $DATETIME
	run_R "cpu" $DATETIME
}

perform_operation() {
	if [ "$OPERATION" = "capture_performance" ]
	then
		capture_performance
	elif [ "$OPERATION" = "export_to_csv" ]
	then
		export_to_csv
	elif [ "$OPERATION" = "clear_data" ]
	then
		clear_data
	elif [ "$OPERATION" = "generate_graphs" ]
	then
		generate_graphs
	fi
}

verify_arguments() {

	# verify the number of arguments and validity of the operation
	if [ "$OPERATION" != "capture_performance" ] && [ "$OPERATION" != "export_to_csv" ] && [ "$OPERATION" != "clear_data" ] && [ "$OPERATION" != "generate_graphs" ]
	then
		echo "Usage: ./profileloom.sh </path/to/yourkit/> <capture_performance|export_to_csv|clear_data|generate_graphs>"
		exit 1
	elif [ "$OPERATION" = "capture_performance" ] && [ "$ARGUMENTS" -ne 4 ]
	then
		echo "Usage: ./profileloom.sh </path/to/yourkit/commandline/api> capture_performance <host> <port>"
		exit 1
	elif [ "$OPERATION" = "export_to_csv" ] && [ "$ARGUMENTS" -ne 2 ]
	then
		echo "Usage: ./profileloom.sh </path/to/yourkit/commandline/api> export_to_csv"
		exit 1
	elif [ "$OPERATION" = "clear_data" ] && [ "$ARGUMENTS" -ne 2 ]
	then
		echo "Usage: ./profileloom.sh </path/to/yourkit/commandline/api> clear_data"
		exit 1
	elif [ "$OPERATION" = "generate_graphs" ] && [ "$ARGUMENTS" -ne 2 ]
	then
		echo "Usage: ./profileloom.sh </path/to/yourkit/commandline/api> generate_graphs"
		exit 1
	fi

	# verify the existence of yourkit commandline api
	if [ -f "$YOURKIT_API" ]
	then
	        echo "Using yourkit commandline api from $YOURKIT_API"
	else
        	echo "Yourkit commandline api does not exist"
	        exit 1
	fi

	# verify the existence of yourkit application
	if [ -f "$YOURKIT_APP" ]
	then
		echo "Using yourkit application from $YOURKIT_APP"
	else
		echo "Yourkit application does not exist"
		exit 1
	fi
}

verify_arguments
perform_operation
