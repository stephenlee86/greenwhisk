# GreenWhisk
This repository contains the implimentation and simulation of the system described in the following paper: [papername](paperurl)

## Simulator

### Configuration
#### General
- Simulation length: Simulation length is controlled by a static final field in each simulators main class, `public static final int DAYS`. 
- Simulation start time: Start time is controlled in the simulators main class via the `public static final int START_TIME` field. The unit for this field is seconds.
- Max node queue size: setting `MAX_NODE_QUEUE_SIZE` in the main class limits the number of functions a node can have waiting to be ran at a given time.
- Node TDP: `Node.CPU` sets the TDP of all nodes in the system. For example, if your system has a maximum total power draw of 6.5W (ie. a Raspberry Pi 4) then this would be set in this field. 
- Node Container Count: `Node.CONTAINERS` sets the number of containers the node can have.
- Function utilization factor: `Workload.UTIL` sets the increased utilization to exhibit on the TDP.
- Function type amount: `Workload.NUM_TYPES` sets the number of types a function can be, with types being container types (ie. Ruby, Python, Node)

To choose the algorithm, specify one of the available algorithms on the command line when running the simulation:
- `weighted`
- `greedy`
- `consistent` (only available for grid simulation)
- `openwhisk` (default)
#### Grid-Isolated (Solar) Configuration
- Solar panel size: solar sizing is gathered from `misc/solar-sizing-server.csv` which contains a list of files indicating solar traces, and a list of corresponding panel sizes in Watts. This size is the number required **per node**. It will be multiplied by # of ndoes in the location.
- Battery size: battery size is contained in `Battery.CAPACITY`. This also represents a **per node** value. The size is in Ws. 
- RetryQueue length: `RetryQueue.MAX_QUEUE_SIZE` limits the maximum number of workloads that can be held in the queue before they begin to fail.
### Data Format
- Solar/Emissions Trace Directory: `Data/`
- Workload Trace Directory: `traces/`
- Solar panel size directory: `misc/`