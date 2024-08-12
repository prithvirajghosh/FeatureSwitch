package org.AutomateFeatureSwitchCleanups.PasswordHasher;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class HashPasswords {
    public static String EncryptPassword(String userPassword) {
        // Generate a salt and hash the password
        return BCrypt.withDefaults().hashToString(12, userPassword.toCharArray());
    }
}
