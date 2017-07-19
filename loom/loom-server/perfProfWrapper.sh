#!/bin/bash
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
REPORT_DIR="target/jmeter/results"
PROF_DIR="/var/lib/jenkins/Snapshots/graphs"
YOURKIT_DIR="/home/suksant/yourkit/"

mv target/*.csv $REPORT_DIR
mv target/*.html $REPORT_DIR
mv target/*.png $REPORT_DIR
mv $PROF_DIR/*.png $REPORT_DIR


noEndingPerf=$(head -n -2 $REPORT_DIR/Loomusers.html)

ADD_OVERALL_JMETER=' <h1>Aggregated Data Summary</h1> <div class="aggregations"><h2>Overall PERFORMANCE view</h2> <div class="images"> <h3>Number of threads</h3>  <img src="SimpleLoomPerformanceTest-ThreadsStateOverTime.png" tooltip="# of threads"/>  <h3>Transactions</h3>  <im
g src="SimpleLoomPerformanceTest-TransactionsPerSecond.png
" tooltip="Transactions"/>      <h3>Response time</h3>      <img src="SimpleLoomPerformanceTest-ResponseTimesOverTime.png" tooltip="Response time"/>    </div>   </div></body><html>'
echo $noEndingPerf > Loomusers.html
echo $ADD_OVERALL_JMETER >> Loomusers.html
mv Loomusers.html $REPORT_DIR

# Add profiling results to jmeter graphs
noEndingProf=$(more $REPORT_DIR/Loomusers.html)

ADD_OVERALL_YOURKIT='<h2>Overall PROFILING view</h2> <div class="images"> <h3>Memory Usage</h3>  <img src="memory.png" tooltip="memUsage"/>  <h3>CPU Usage (user)</h3>  <img src="cpu_kernel.png
" tooltip="userCPU"/>      <h3>CPU usage(kernel)</h3>      <img src="cpu_user_kernel.png" tooltip="kernelCPU"/>   <h3>Garbage collection CPU</h3>      <img src="cpu_gc.png" tooltip="gcCPU"/> </div>   </div></body></html>'
echo ${noEndingProf%</body>*} > Loomusers.html
echo $ADD_OVERALL_YOURKIT >> Loomusers.html
mv Loomusers.html $REPORT_DIR

now=`date +"%m_%d_%Y"`
_file="perfResults_$now.tar.gz"
tar czvf "$_file" --exclude=\*.{csv,jtl} $REPORT_DIR

