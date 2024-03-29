# Scray Maven Archetype

This archetype supports writing Scray compatible jobs by creating:
- directory structure
- pom.xml that creates an ueberjar for the job
- bin directory with shell script to start the jobs
- minimal job structure

## Versions
| Java | Scala | Spark | Hadoop |
| ---- |-------|-------|--------|
| 11   | 2.12  | 3.2.0 | 2.7    |

## Usage:

### Building

To use the archetype artefacts they must either be pulled from a archetype-repo or installed in the local repo.

    mvn clean install

### Create project from archetype with maven
Archetypes are enhancements of maven ("plugins") that can generate new projects. To use them classical maven coordinates must be provided.
```
mvn archetype:generate                  \
  -DarchetypeGroupId=org.scray          \
  -DarchetypeArtifactId=scray-archetype \
  -DarchetypeVersion=1.1.5-SNAPSHOT	
```  
### Running the jobs:

#### Run in local Spark Standalone Mode  
  A local spark node will be started and this job will be executed on this node.  
  
  Example:  
```
./bin/submit-job.sh                      \ 
  --local-mode                           \
  --master spark://<YOUR_LOCAL_IP>:7077  \
  --total-executor-cores 4               \
  -b                                     \
  -m  spark://127.0.0.1:7077
```
The options <code>--master</code> with the Spark master URL and <code>--total-executor-cores</code> providing the number of cores are required by the runner script.

    
For the url of the master there are several options:
- <code>spark://&lt;IP&gt;:&lt;Port&gt;</code> (default port 7077)
- <code>yarn-client</code> (run a job with a local client but execute on a Hadoop yarn cluster of spark workers)
- <code>yarn-cluster</code> (run the client and the workers of the Spark job on a Hadoop yarn cluster)

##### Example start batch job
```
./bin/submit-job.sh                \
  --local-mode                     \
  --master spark://127.0.0.1:7077  \
  --total-executor-cores 2         \
  -b                               \
  -m spark://127.0.0.1:7077
```      
      
##### Example start streaming job  
```
./bin/submit-job.sh                 \
  --local-mode                      \
  --master spark://127.0.0.1:7077   \
  --total-executor-cores 2          \
  -m spark://127.0.0.1:7077         \
  -t db-nifi                        \
  -k www.db.opendata.s-node.de:9092 \
  -p /tmp/
```
