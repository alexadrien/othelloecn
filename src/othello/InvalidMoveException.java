package othello;

class InvalidMoveException extends RuntimeException {
    public InvalidMoveException(String mssg){
        super(mssg);
    }
}
