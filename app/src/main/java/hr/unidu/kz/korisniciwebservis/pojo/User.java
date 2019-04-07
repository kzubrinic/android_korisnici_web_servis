package hr.unidu.kz.korisniciwebservis.pojo;

/*
    POJO razred za spremanje podataka jednog korisnika
 */
public class User {
    private int id;
    private String username;
    private String name;

    public User() {}
    public User(int id, String korisnik, String ime) {
        this.id = id;
        this.username = korisnik;
        this.name = ime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String korisnik) {
        this.username = korisnik;
    }

    public String getName() {
        return name;
    }

    public void setName(String ime) {
        this.name = ime;
    }

}
