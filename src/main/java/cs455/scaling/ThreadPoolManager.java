package cs455.scaling;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadPoolManager {
    private int poolSize;
    private ConcurrentLinkedQueue<Runnable> taskQueue;
    //private ConcurrentLinkedQueue<Thread> threadPool;
    
    ThreadPoolManager(int poolSize) {
        this.poolSize = poolSize;
        taskQueue = new ConcurrentLinkedQueue<>();
    }

    public void begin() {
        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker(taskQueue);
            worker.start();
        }
        System.out.println(Thread.activeCount());
    }

    public void printTasks() {
        System.out.println(taskQueue.size());
    }

    private class Worker extends Thread {
        private ConcurrentLinkedQueue<Runnable> taskBlock;

        public Worker(ConcurrentLinkedQueue<Runnable> taskBlock) {
            this.taskBlock = taskBlock;
        }

        @Override
        public void run() {
            while (true) {
                if (!taskBlock.isEmpty()) { 
                    Runnable task;
                    if ((task = taskBlock.poll()) != null) {
                        task.run();
                    }
                }
            }
        }
    }

    public void addTask(Runnable task) {
        taskQueue.add(task);
    }

    public static void main(String[] args) {
        ThreadPoolManager poolManager = new ThreadPoolManager(Integer.parseInt(args[0]));
    }
}
