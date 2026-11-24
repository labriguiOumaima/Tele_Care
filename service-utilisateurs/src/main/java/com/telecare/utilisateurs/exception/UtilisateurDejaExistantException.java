package com.telecare.utilisateurs.exception;

/**
 * Levée quand un email est déjà utilisé par un autre utilisateur.
 */
public class UtilisateurDejaExistantException extends RuntimeException {

    public UtilisateurDejaExistantException(String email) {
        super("Un utilisateur avec l'email '" + email + "' existe déjà");
    }
}
