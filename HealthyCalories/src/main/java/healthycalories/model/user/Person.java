package main.java.healthycalories.model.user;

/** ข้อมูลบุคคลพื้นฐานที่ใช้เก็บอีเมลและค่ารหัสผ่านของสมาชิก */
public abstract class Person {
    private String email;
    private String passwordHash;

    /** สร้างบุคคลที่ยังไม่มีอีเมลหรือรหัสผ่าน */
    public Person() {
        this("", "", "");
    }

    /** สร้างบุคคลโดยแปลงรหัสผ่านที่รับมาเป็นค่าที่จัดเก็บ */
    public Person(String email, String password) {
        this(email, password, password);
    }

    /** สร้างบุคคลโดยเลือกค่ารหัสผ่านสำหรับจัดเก็บแยกจากรหัสผ่านต้นทาง */
    public Person(String email, String password, String passwordHash) {
        this.email = email;
        this.passwordHash = hashPassword(passwordHash == null ? password : passwordHash);
    }

    /** คืนอีเมลของบุคคล */
    public String getEmail() {
        return email;
    }

    /** กำหนดอีเมลของบุคคล */
    public void setEmail(String email) {
        this.email = email;
    }

    /** คืนค่ารหัสผ่านที่จัดเก็บไว้ */
    public String getPassword() {
        return this.passwordHash;
    }

    /** จัดเก็บรหัสผ่านหลังผ่านเมธอดแฮชของคลาส */
    public void setPassword(String password) {
        this.passwordHash = hashPassword(password);
    }

    /** ตรวจว่ารหัสผ่านที่ได้รับตรงกับค่าที่จัดเก็บหรือไม่ */
    public boolean checkPassword(String password) {
        return passwordHash != null && passwordHash.equals(password);
    }

    /** จุดปรับแต่งสำหรับแปลงรหัสผ่าน; คลาสพื้นฐานคืนค่าเดิมหรือค่าว่าง */
    protected String hashPassword(String password) {
        return password == null ? "" : password;
    }

    /** แสดงอีเมลของบุคคลโดยไม่เปิดเผยรหัสผ่าน */
    public void information() {
        System.out.println("อีเมล: " + email);
    }
}