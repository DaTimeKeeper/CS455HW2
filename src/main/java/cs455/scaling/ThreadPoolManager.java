package cs455.scaling;

import java.util.concurrent.ConcurrentLinkedQueue;

/*
    The ThreadPoolManager maintains a ConcurrentLinkedQueue of runnable tasks. 
    A Runnable is basically the run() method of a thread as an object. It is code that
    will be executed in a thread.
    
    During initilization, it will create a set amount of worker threads that constantly check
    the thread safe queue for tasks to do and return to polling once they finish a task
*/
public class ThreadPoolManager {
    private int poolSize;
    private ConcurrentLinkedQueue<Runnable> taskQueue;
    
    ThreadPoolManager(int poolSize) {
        this.poolSize = poolSize;
        taskQueue = new ConcurrentLinkedQueue<>();
    }

    /*
        Make [poolSize] amount of workers and start them polling the taskQueue.
        
        start() causes a new thread to be made and then run() is executed, which is what we want
        run() causes the thread to be ran *IN THE CURRENT THREAD* so this would cause the PoolManager to execute the Worker run() as a method call

        start() can only be called once, else it throws an exception. This means each worker thread can only be created once
    */
    public void begin() {
        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker();
            worker.start();
        }
        System.out.println("Num threads running:" + Thread.activeCount());
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
                        System.out.print(currentThread().getName() + ": ");
                        task.run();
                    }
                }
            }
        }
    }

    public void addTask(Runnable task) {
        taskQueue.add(task);
    }

    //For testing thread pool by itself
    //Not acutally gonna use this
    public static void main(String[] args) {
        ThreadPoolManager poolManager = new ThreadPoolManager(Integer.parseInt(args[0]));
    }
}
