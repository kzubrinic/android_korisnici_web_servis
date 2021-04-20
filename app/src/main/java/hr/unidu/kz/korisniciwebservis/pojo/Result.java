package hr.unidu.kz.korisniciwebservis.pojo;
/*
    POJO razred omotač za spremanje podataka više korisnika primljenih od web servisa
 */
public class Result {
    private int code;
    private String status;
    private String message;
    private User[] data;

    public User[] getData() {
        return data;
    }

    public void setData(User[] data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}