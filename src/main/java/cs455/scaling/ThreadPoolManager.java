package cs455.scaling;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*
    The ThreadPoolManager maintains a ConcurrentLinkedQueue of runnable tasks. 
    A Runnable is basically the run() method of a thread as an object. It is code that
    will be executed in a thread.
    
    During initilization, it will create a set amount of worker threads that constantly check
    the thread safe queue for tasks to do and return to polling once they finish a task
*/
public class ThreadPoolManager implements Runnable{
    private int poolSize, batchSize;
    private ConcurrentLinkedQueue<Runnable> taskQueue;
    private LinkedBlockingQueue<Message> hashBatch;
    
    ThreadPoolManager(int poolSize, int batchSize) {
        this.poolSize = poolSize;
        this.taskQueue = new ConcurrentLinkedQueue<>();
        this.hashBatch = new LinkedBlockingQueue<>(batchSize * 2);
        this.batchSize = batchSize;
    }

    @Override
    public void run() {
        startPool();
        ArrayList<Message> messages = new ArrayList<>();
        
        while(true) {           
            if (batchReady()) {
                System.out.println("batching!");
                hashBatch.drainTo(messages, batchSize);
                addTask(new HashProcessor(messages));
            }
        }
    }

    /*
        Make [poolSize] amount of workers and start them polling the taskQueue.
        
        start() causes a new thread to be made and then run() is executed, which is what we want
        run() causes the thread to be ran *IN THE CURRENT THREAD* so this would cause the PoolManager to execute the Worker run() as a method call

        start() can only be called once, else it throws an exception. This means each worker thread can only be created once
    */
    public void startPool() {
        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker();
            worker.start();
        }
        //Ignore main thread
        int count = Thread.activeCount() - 1;
        System.out.println("Num threads running:" + count);
    }

    public void printTasks() {
        System.out.println(taskQueue.size());
    }

    /*
        This is what makes up the thread pool
        A worker is a thread that takes a Runnable task from the queue
        and runs it. This is why there are 2 run methods, one for the Worker to pull tasks
        and the other to run the task it has polled.

        When done with the task it resumes to poll the queue for more
    */
    private class Worker extends Thread {

        @Override
        public void run() {
            while (true) {
                if (!taskQueue.isEmpty()) {
                        Runnable task = taskQueue.poll();
                        if ((task ) != null) {
                            task.run();
                    }
                }
            }
        }
    }

    public void addTask(Runnable task) {
        taskQueue.add(task);
    }

    public void addHash(Message hashTask) throws InterruptedException {
        hashBatch.offer(hashTask, 250, TimeUnit.MILLISECONDS);
    }

    public boolean batchReady() {
        return (hashBatch.size() == batchSize);
    }
}
