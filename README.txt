To Run Server:
    java -jar {jar} cs455.scaling.main server {port} {numThreads} {batchSize} {batchTime}

    The server must run with a minimum of 2 threads as one thread is dedicated to manage incoming connections

To Run Client:
    java -jar {jar} cs455.scaling.main client {hostName} {hostPort} {rate}

**All values are int so no floating point paramaters!**

Runner.sh runs 100 clients at a set rate

SocketProcessor.java:
    This thread manages all incoming connections and data and passes off new messages to the ThreadPoolManager

HashProcessor.java:
    This Runnable task hashes an Arraylist of messages, the amount being defined by batchSize. It will then send
    a message to its host client. 

ThreadPoolManager.java:
    On start, it created a specified number of threads for the thread pool. These worker threads continuously poll
    a task queue of Runnables for them process. The Manager will then oversee the creation of message batches to be added
    as tasks to the TaskQueue. It will create a new batch if there is enough messages in the hashQueue or if batchTime has
    passed.

Message.java:
    Data transfer object for unhashed messages

Client + Server.java:
    I hope you know what these do... 