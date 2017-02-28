package othello;

public class NotGameOverException extends RuntimeException {
    
    public NotGameOverException(String mssg){
        super(mssg);
    }
    
}
