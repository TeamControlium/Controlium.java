package TeamControlium.Controlium;

public class Credentials {
    private String _Username;
    private String _Password;

    public Credentials(String Username, String Password) {
        _Username = Username;
        _Password = Password;
    }

    public String getUsername() {
        return _Username;
    }

    public String setUsername(String value) {
        _Username = value;
        return _Username;
    }

    public String getPassword() {
        return _Password;
    }

    public String setPassword(String value) {
        _Password = value;
        return _Password;
    }

    public void setCredentials(String Username, String Password) {
        _Username = Username;
        _Password = Password;
    }
}

