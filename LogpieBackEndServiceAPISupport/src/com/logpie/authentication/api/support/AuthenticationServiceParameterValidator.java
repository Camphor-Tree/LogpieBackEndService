package com.logpie.authentication.api.support;

/**
 * This class is used to handle the parameters validation
 * 
 * @author yilei
 * 
 */
public class AuthenticationServiceParameterValidator
{
    private static String sEmailMatchReg = "[a-zA-Z0-9\\.]+@[a-zA-Z0-9\\-\\_\\.]+\\.[a-zA-Z0-9]{3}";
    private static String sPasswordMatchReg = "^[A-Za-z0-9]+";

    /**
     * Check whether parameters are null
     * 
     * @param parameters
     * @return true, if none of the parameters are null false, if one of the
     *         parameters are null
     */
    public static boolean nonNullCheck(final Object... parameters)
    {
        if (parameters == null || parameters.length == 0)
        {
            return false;
        }

        for (Object parameter : parameters)
        {
            if (parameter == null)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether it matches email pattern
     * 
     * @param email
     * @return
     */
    public static boolean isValidEmail(final String email)
    {
        if (email == null || email.length() == 0)
        {
            return false;
        }
        return email.matches(sEmailMatchReg);
    }

    /**
     * Check whether it matches password pattern. Password must just contain
     * numbers or letters, length must more than 6, less than 20
     * 
     * @param password
     * @return true, if it is a valid password
     */
    public static boolean isValidPassword(final String password)
    {
        if (password == null)
        {
            return false;
        }
        int length = password.length();
        if (length < 6 || length > 20)
        {
            return false;
        }
        if (password.matches(sPasswordMatchReg))
        {
            return true;
        }
        return false;
    }
}
