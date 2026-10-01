package main.java.healthycalories.model.user;

public abstract class Person {
    private String email;
    private String passwordHash;

    public Person(String email, String password){

    }
    public void getEmail(String email){

    }
    public void setEmail(String email){

    }
    public String getPassword(){
        return this.passwordHash;
    }
    public void setPassword(String password){

    }
    public boolean checkPassword(String password){
        return false;
    }
    protected String hashPassword(String password){
        return password;
    }
    public void information(){
        
    }
}