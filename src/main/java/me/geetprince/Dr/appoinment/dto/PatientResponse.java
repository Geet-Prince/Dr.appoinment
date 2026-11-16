package me.geetprince.Dr.appoinment.dto;

public class PatientResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Integer age;
    private String gender;

    public PatientResponse() {
    }


    public PatientResponse(Long id, String fullName,
                           String email, String phoneNumber,
                           Integer age, String gender) {

        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.age = age;
        this.gender = gender;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
