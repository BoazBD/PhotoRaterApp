package com.bardavid.boaz.photorater;

import java.util.List;

public class PersonModel {
    private String id;
    private String name;
    private int age;
    private String gender;
    private List<String> pairCodes;
    private String genderPreference;
    private String agePreference;
    //constructors
    public PersonModel() {
    }

    public PersonModel(String name, int age, String gender,String genderPreference,String agePreference) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.genderPreference=genderPreference;
        this.agePreference=agePreference;
    }

    @Override
    public String toString() {
        return "PersonModel{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", imageUrl='" + "urls" + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGenderPreference() {
        return genderPreference;
    }

    public void setGenderPreference(String genderPreference) {
        this.genderPreference = genderPreference;
    }

    public String getAgePreference() {
        return agePreference;
    }

    public void setAgePreference(String agePreference) {
        this.agePreference = agePreference;
    }

    public void addPairID(String id){
        pairCodes.add(id);
    }
    public List<String> getPairCodes(){return this.pairCodes;}
    public void setPairCodes(List<String> pairCodes){
        this.pairCodes = pairCodes;
    }
    public String getUrl(int index){
        if(pairCodes.size()>index)
            return pairCodes.get(index);
        else
            return null;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void removePairCode(String code){
        pairCodes.remove(code);
    }
}
