package es.danielumpierrez.simpleedit;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class NoteEditorFragment extends Fragment {

    private String nombreNota = "";
    // private String nota = "";
    private EditText txtNotas;
    private String key;

    public NoteEditorFragment() {
        nombreNota = "";
    }

    public String getKey() {
        return key;
    }

    public EditText getTxtNotas() {
        return txtNotas;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_note_editor, container, false);
        txtNotas = (EditText) v.findViewById(R.id.txtNota);
        SharedPreferences shared = getActivity().getSharedPreferences(getString(R.string.fichero_preferencias), Context.MODE_PRIVATE);
        Map mapNotas = shared.getAll();
        Iterator it = mapNotas.keySet().iterator();
        key = "";
        while (it.hasNext()) {
            String tmp = String.valueOf(it.next());
            String nota = shared.getString(tmp, "");
            if (nota == this.nombreNota) {
                key = tmp;
                break;
            }
        }
        if (!key.isEmpty() && !nombreNota.isEmpty())
            txtNotas.setText(leerNota(String.format("%s-%s.txt", key, nombreNota)));
        return v;
    }

    public String getNota() {
        return nombreNota;
    }

    public void setNota(String nombreNota) {
        this.nombreNota = nombreNota;
    }

    private String leerNota(String strFileNota) {
        File path = getActivity().getFilesDir();
        File fileNota = new File(path, strFileNota);
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileNota));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();    //You'll need to add proper error handling here
        }
        return text.toString();
    }
}
