package app.controller;

public class SessionManager {

        // 1. Instancia estática de la clase SessionManager
        private static SessionManager instance;

        // 2. Variables de la sesión
        private String loggedInUserPassword;
        private String loggedInUsername;
        private int loggedInUserId;
        private int loggedInUserIdType;

        // 3. Constructor privado para evitar instanciación externa
        private SessionManager() {
            // Inicializa variables si es necesario
            this.loggedInUserPassword = null;
            this.loggedInUsername = null;
            this.loggedInUserId = -1;
            this.loggedInUserIdType = -1;
        }

        // 4. Método estático para obtener la única instancia (el corazón del Singleton)
        public static SessionManager getInstance() {
            if (instance == null) {
                instance = new SessionManager();
            }
            return instance;
        }

        // --- Getters y Setters para la información de la sesión ---

        public String getLoggedInUserPassword() {
            return loggedInUserPassword;
        }

        public void setLoggedInUserPassword(String loggedInUserPassword) {
            this.loggedInUserPassword = loggedInUserPassword;
        }

        public String getLoggedInUsername() {
            return loggedInUsername;
        }

        public void setLoggedInUsername(String loggedInUsername) {
            this.loggedInUsername = loggedInUsername;
        }

        public int getLoggedInUserId() {
            return loggedInUserId;
        }

        public int getLoggedInUserIdType() {
        return loggedInUserIdType;
    }

        public void setLoggedInUserId(int loggedInUserId) {
            this.loggedInUserId = loggedInUserId;
        }

        public void setLoggedInUserIdType(int loggedInUserIdType) {
        this.loggedInUserIdType = loggedInUserIdType;
    }

        public void clearSession() {
            this.loggedInUserPassword = null;
            this.loggedInUsername = null;
            this.loggedInUserId = -1;
        }
}
