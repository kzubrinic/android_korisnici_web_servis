package hr.unidu.kz.korisniciwebservis;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hr.unidu.kz.korisniciwebservis.pojo.Praznik;

public class PrazniciAdapter extends RecyclerView.Adapter<PrazniciAdapter.MyViewHolder>{
    private List<Praznik> mDataset;
    private Context con;
    // Prima se referenca na izvor podataka - u ovom slučaju polje stringova
    public PrazniciAdapter(List<Praznik> myDataset, Context c) {
        mDataset = myDataset;
        con = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // stvara se novi view iz standardnog jednostavnog layouta retka
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        // Unutarnja klasa tipa ViewHolder čuva referencu na view
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Kada se korisnik pozicionira na određeni redak sadržaj retka viewa se ažurira
        // podatkom iz dataseta koji se nalazi na istoj poziciji.
        holder.datum.setText(mDataset.get(position).getDate());
        holder.naziv.setText(mDataset.get(position).getLocalName());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();

    }

    // Unutarnja klasa tipa ViewHolder čuva referencu na view
    // U njoj se obrađuje događaj pritiska/klika na stavku
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView datum;
        public TextView naziv;
        public MyViewHolder(View itemView) {
            super(itemView);
            datum = itemView.findViewById(android.R.id.text1);
            naziv = itemView.findViewById(android.R.id.text2);
            // referenca na objekt tipa MyViewHolder se sprema kao tag elementa liste
            itemView.setTag(this);
            // registrira se listener
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // vrati poziciju na koju je korisnik kliknuo
            int pos = getAdapterPosition();
            notifyDataSetChanged();

            // Dohvat podataka iz adaptera
            // Check if an item was deleted, but the user clicked it before the UI removed it
            if (pos != RecyclerView.NO_POSITION) {
                // Dohvaća se podatak iz dataseta koji se nalazi na poziciji kliknutog retka liste
                String pok = mDataset.get(pos).getLocalName();
                Toast.makeText(con, pok, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
