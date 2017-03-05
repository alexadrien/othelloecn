package utils;

/**
 *
 * @author Vincent
 */
public final class Timeout {
    private int value;
    private long start_time;
    
    public Timeout() {
        value = -1;
        start_time = -1;
    }
    
    public Timeout(int ms) {
        start(ms);
    }
    
    public void start(int ms) {
        value = ms;
        start_time = System.currentTimeMillis();
    }

    public void restart() {
        start_time = System.currentTimeMillis();
    }
    
    public void stop() {
        value = -1;
    }
    
    public boolean expired() {
        if(value < 0) {
            return false;
        }
        
        return (System.currentTimeMillis() - start_time) > value;
    }
    
    public long elapsed() {
        return System.currentTimeMillis() - start_time; 
    }
}
