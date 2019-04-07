package hr.unidu.kz.korisniciwebservis.pojo;

/*
    POJO razred omotač za spremanje podataka više korisnika primljenih od web servisa
 */
public class Users {
    private User[] users;

    public User[] getUsers() {
        return users;
    }

    public void setUsers(User[] users) {
        this.users = users;
    }
}
