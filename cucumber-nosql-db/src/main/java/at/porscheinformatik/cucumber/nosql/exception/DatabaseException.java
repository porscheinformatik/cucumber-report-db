package at.porscheinformatik.cucumber.nosql.exception;

/**
 * @author Stefan Mayer (yms)
 */
public class DatabaseException extends RuntimeException
{
    private static final long serialVersionUID = -6694656142998668250L;

    public DatabaseException(String message)
    {
        super(message);
    }
}
