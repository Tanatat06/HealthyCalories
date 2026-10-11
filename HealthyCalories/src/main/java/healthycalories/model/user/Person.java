package main.java.healthycalories.model.user;

public abstract class Person {
    private String email;
    private String passwordHash;

    public Person() {
        this("", "", "");
    }

    public Person(String email, String password) {
        this(email, password, password);
    }

    public Person(String email, String password, String passwordHash) {
        this.email = email;
        this.passwordHash = hashPassword(passwordHash == null ? password : passwordHash);
    }

    public String getEmail(String email) {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = hashPassword(password);
    }

    public boolean checkPassword(String password) {
        return passwordHash != null && passwordHash.equals(password);
    }

    protected String hashPassword(String password) {
        return password == null ? "" : password;
    }

    public void information() {
        System.out.println("อีเมล" + email);
    }
}