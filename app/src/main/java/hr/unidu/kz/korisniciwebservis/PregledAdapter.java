package hr.unidu.kz.korisniciwebservis;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import hr.unidu.kz.korisniciwebservis.pojo.User;


/*
    Adapter koji pokazuje punjenje liste podacima kursora vraćenog iz baze
 */
public class PregledAdapter extends ArrayAdapter<User> {
    private final Context con;
    private final User[] kor;
    public PregledAdapter(Context context, User[] kor) {
        super(context, R.layout.redak_liste, kor);
        this.con = context;
        this.kor = kor;
    }

    @Override
    // ova metoda puni svaki redak liste podacima i vraća popunjeni redak listi
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.redak_liste, parent, false);
        TextView id = rowView.findViewById(R.id.id);
        TextView korisnik =  rowView.findViewById(R.id.korisnik);
        TextView naziv = rowView.findViewById(R.id.naziv);
        id.setText(String.valueOf(kor[position].getId()));
        korisnik.setText(kor[position].getUsername());
        naziv.setText(kor[position].getName());
        // metoda vraća u listu popunjeni redak
        return rowView;
    }
    @Override
    // ova metoda se odrađuje kada korisnik klikne na redak liste - vraća objekt tipa Pokemon
    // iz retka liste na koji je korisnik kliknuo
    public User getItem(int position) {
        return kor[position];
    }
}


