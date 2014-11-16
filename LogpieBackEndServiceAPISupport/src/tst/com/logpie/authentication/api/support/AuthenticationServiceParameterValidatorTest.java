package tst.com.logpie.authentication.api.support;

import junit.framework.TestCase;

import com.logpie.authentication.api.support.AuthenticationServiceParameterValidator;

public class AuthenticationServiceParameterValidatorTest extends TestCase
{

    public void testNonNullCheck()
    {
        Object nullObject = null;
        assertFalse(AuthenticationServiceParameterValidator.nonNullCheck(nullObject));
        Object[] nullStringArray1 = new String[] { "a", "b", null };
        Object[] nullStringArray2 = new String[] { null };
        Object[] nullStringArray3 = new String[] { null, "a", "b" };
        Object[] nullStringArray4 = new String[] { null, null };
        assertFalse(AuthenticationServiceParameterValidator.nonNullCheck(nullStringArray1));
        assertFalse(AuthenticationServiceParameterValidator.nonNullCheck(nullStringArray2));
        assertFalse(AuthenticationServiceParameterValidator.nonNullCheck(nullStringArray3));
        assertFalse(AuthenticationServiceParameterValidator.nonNullCheck(nullStringArray4));
    }

    public void testIsValidEmail()
    {
        String[] illegalEmails = { null, "1323", "aa", "aaa.aa", "aa@aa", "aaaa#a.com", "@a.com",
                "aa@", "aa@**.com", "1a1@1a2@112@" };

        for (String illegalEmail : illegalEmails)
        {
            assertFalse(AuthenticationServiceParameterValidator.isValidEmail(illegalEmail));
        }

        String[] legalEmails = { "a@a.com", "bbb.bb@aa.com", "aaaaaaaaaaa@bbc.com", "aa21@123.com",
                "aa21@123.com" };

        for (String legalEmail : legalEmails)
        {
            assertTrue(AuthenticationServiceParameterValidator.isValidEmail(legalEmail));
        }
    }

    public void testIsValidPassword()
    {
        String[] illegalPasswords = { null, "1323", "aa", "aaa.aa", "aa@aa", "aaaa#a.com",
                "243123423%#", "1111111111111111111111111111111", "sfads^A32423", "\\afadfd2" };

        for (String illegalPassword : illegalPasswords)
        {
            assertFalse(AuthenticationServiceParameterValidator.isValidPassword(illegalPassword));
        }

        String[] legalPasswords = { "111111", "111abc", "abcdefg", "ABCabc253", "baAC21",
                "21AAB21", "abd239572dsgAf" };

        for (String legalPassword : legalPasswords)
        {
            boolean valid = AuthenticationServiceParameterValidator.isValidPassword(legalPassword);
            System.out.println(legalPassword + ":" + valid);
            assertTrue(valid);
        }
    }
}
