public class IllegalRegisterException extends RuntimeException{
    public IllegalRegisterException(String message){
        super(message);
    }

    public IllegalRegisterException(){
        super();
    }
}
