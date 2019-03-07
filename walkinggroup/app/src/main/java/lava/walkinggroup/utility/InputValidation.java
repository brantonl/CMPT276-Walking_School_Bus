package lava.walkinggroup.utility;

/**
 * Collection of validation functions for user input
 */
public class InputValidation {

    public static boolean isValidEmail(String email) {
        if(email == null){
            return false;
        }
        return email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    }

    public static boolean isValidName(String name) {
        if(name == null){
            return false;
        }
        return name.matches("([a-zA-Z]+\\.)?([a-zA-Z]+ )*[a-zA-Z]*");
    }

    public static boolean isValidPassword(String password) {
        if(password == null){
            return false;
        }
        return password.length() >=8;
    }

    //TODO:: Better error conditions for following methods
    public static boolean isValidBirthYear(Integer birthYear) {
        return birthYear != null;
    }

    public static boolean isValidBirthMonth(Integer birthMonth){
        return (birthMonth != null && birthMonth <= 12 && birthMonth > 0);
    }

    public static boolean isValidPhoneNumber(String homePhone) {
        return homePhone != null;
    }

    public static boolean isValidAddress(String address) {
        return address != null;
    }

    public static boolean isValidGrade(String grade) {
        return grade != null;
    }

    public static boolean isValidEmergencyContactInfo(String emergencyContactInfo) {
        return emergencyContactInfo != null;
    }
}
