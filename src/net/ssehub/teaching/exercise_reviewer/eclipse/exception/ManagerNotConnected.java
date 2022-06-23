package net.ssehub.teaching.exercise_reviewer.eclipse.exception;

/**
 * This Exception is thrown if the manager can't connect to the StuMgmt.
 * 
 * @author lukas
 *
 */
public class ManagerNotConnected extends Exception {
    
    
    public static final String NOINTERNETCONNECTION = "Connection to the serves can not be estabilished";

    
    private static final long serialVersionUID = -6740266154116763086L;
    
    /**
     * Creates an instance of {@link ManagerNotConnected}.
     * @param message
     */
    public ManagerNotConnected(String message) {
        super(message);
    }
    
    
    

}
