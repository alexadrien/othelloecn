package othello;


class NotYourTurnException extends RuntimeException {
    public NotYourTurnException(String message){
        super(message);
    }
}
