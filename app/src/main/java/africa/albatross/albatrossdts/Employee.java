package africa.albatross.albatrossdts;

public class Employee {
    private String uid;
    private String email_address;
    private String first_name;
    private String last_name;
    private String group;

    public Employee(String uid, String email_address, String first_name, String last_name, String group) {
        this.uid = uid;
        this.email_address = email_address;
        this.first_name = first_name;
        this.last_name = last_name;
        this.group = group;
    }

    public Employee(){

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
