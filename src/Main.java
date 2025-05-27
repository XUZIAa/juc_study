public class Main {
    public static void main(String[] args) {
        runnable runnable=new runnable();
        Thread Thread1=new Thread(runnable);
        thread thread1=new thread();
        thread1.start();
        Thread1.start();
        System.out.println("主线程");
    }

}
