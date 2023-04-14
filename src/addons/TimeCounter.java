package addons;

public class TimeCounter {
    private double startTime;

    public void start(){
        startTime = System.nanoTime();
    }

    public double stop_seconds(){
        return (System.nanoTime() - startTime) / 1000000000;
    }

    public double stop_miliseconds(){
        return (System.nanoTime() - startTime) / 1000000;
    }
}
