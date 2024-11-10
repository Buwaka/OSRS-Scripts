package Utilities;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.logging.Logger;

/**
 * Authenticator which keeps credentials to be passed to the requestor based on authority of the requesting URL. The
 * authority is <pre>user:password@host:port</pre>, where all parts are optional except the host.
 * <p>
 * If the configured credentials are not found, the Authenticator will use the credentials embedded in the URL, if
 * present. Embedded credentials are in the form of <pre>user:password@host:port</pre>
 *
 * @author Michael Fortin 2011-09-23
 */
public final class DefaultAuthenticator extends Authenticator
{

    private static final Logger               LOG = Logger.getLogger(DefaultAuthenticator.class.getName());
    private static       DefaultAuthenticator instance;

    private PasswordAuthentication Current = null;

    private DefaultAuthenticator()
    {
    }

    public void SetUserPass(String user, String pass)
    {
        Current = (new PasswordAuthentication(user, pass.toCharArray()));
    }

    public static synchronized DefaultAuthenticator getInstance()
    {
        if(instance == null)
        {
            instance = new DefaultAuthenticator();
            Authenticator.setDefault(instance);
        }
        return instance;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication()
    {

        return Current;
    }

    // unit testing
    static void reset()
    {
        instance = null;
        Authenticator.setDefault(null);
    }

}



