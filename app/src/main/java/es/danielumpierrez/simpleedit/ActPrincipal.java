package es.danielumpierrez.simpleedit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ActPrincipal extends AppCompatActivity {

    ActPrincipal ctx = null;
    EditText txtNota;
    ListView lstNotas;
    FloatingActionButton fab;
    private StableArrayAdapter listAdapter;

    public static void inputDialog(final Fragment appAct, final String data, final View view) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(appAct.getActivity());
        builder.setTitle("Nombre");
        // Set up the input
        final EditText input = new EditText(appAct.getActivity());
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SharedPreferences sharedPref = appAct.getActivity().getSharedPreferences(
                        appAct.getString(R.string.fichero_preferencias), Context.MODE_PRIVATE);
                String key = String.valueOf(System.currentTimeMillis());
                String value = input.getText().toString();
                sharedPref.edit().putString(key, value).apply();
                File path = appAct.getActivity().getFilesDir();
                File nota = new File(path, String.format("%s-%s.txt", key, value));
                escribirNota(nota, data, view);


            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private static void escribirNota(File nota, String data, View view) {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(nota);
            stream.write(data.getBytes());
            stream.flush();
            Snackbar.make(view, "Nota guardada correctamente.", Snackbar.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_principal);
        ctx = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        txtNota = (EditText) findViewById(R.id.txtNota);
        lstNotas = (ListView) findViewById(R.id.lstNotas);
        fab = (FloatingActionButton) findViewById(R.id.fbtnGuardar);
        fab.setImageResource(android.R.drawable.ic_menu_edit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment tmp = getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1);
                if (tmp instanceof NoteEditorFragment) {
                    guardarNota(view);
                } else {
                    if (tmp instanceof ActPrincipalFragment) {
                        fab.setImageResource(android.R.drawable.ic_menu_save);
                        nuevaNota();
                    }
                }


            }
        });
        SharedPreferences shared = getSharedPreferences(getString(R.string.fichero_preferencias), Context.MODE_PRIVATE);
        Map notas = shared.getAll();
        Iterator it = notas.values().iterator();
        ArrayList<String> arrayNotas = new ArrayList<>();

        while (it.hasNext()) {
            arrayNotas.add(String.valueOf(it.next()));
        }
        listAdapter = new StableArrayAdapter(this, R.layout.simplerow, arrayNotas);
        lstNotas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                FragmentTransaction frgTrans = getSupportFragmentManager().beginTransaction();
                NoteEditorFragment frgEdit = new NoteEditorFragment();
                frgEdit.setNota(item);
                frgTrans.add(R.id.fragment, frgEdit);
                frgTrans.addToBackStack(null);
                fab.setImageResource(android.R.drawable.ic_menu_save);
                frgTrans.commit();
            }
        });
        lstNotas.setAdapter(listAdapter);

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {

                        Fragment tmp = getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getBackStackEntryCount());
                        if (tmp instanceof NoteEditorFragment) {
                            fab.setImageResource(android.R.drawable.ic_menu_save);
                        }
                        if (tmp instanceof ActPrincipalFragment) {
                            fab.setImageResource(android.R.drawable.ic_menu_edit);
                        }
                    }
                });


    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            alertaGuardarCambios();
            return;
        }

        // Otherwise defer to system default behavior.
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_act_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_nuevo:
                nuevaNota();
                break;
            case R.id.action_guardar:
                guardarNota(item.getActionView());
                break;
            case R.id.action_compartir:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, txtNota.getText().toString());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Compartir"));
                break;
        }
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void nuevaNota() {
        FragmentTransaction frgTrans = getSupportFragmentManager().beginTransaction();
        frgTrans.add(R.id.fragment, new NoteEditorFragment());
        frgTrans.addToBackStack(null);
        frgTrans.commit();
    }

    private void guardarNota(View view) {
        Fragment tmp = this.getSupportFragmentManager().getFragments().get(this.getSupportFragmentManager().getFragments().size() - 1);
        if (tmp instanceof NoteEditorFragment) {
            NoteEditorFragment noteEditorFrg = (NoteEditorFragment) tmp;
            String nota = noteEditorFrg.getNota();
            EditText etNota = noteEditorFrg.getTxtNotas();
            if (nota.isEmpty()) {
                inputDialog(noteEditorFrg, etNota.getText().toString(), view);
            } else {
                File path = ctx.getFilesDir();
                String strFileNota = String.format("%s-%s.txt", noteEditorFrg.getKey(), nota);
                escribirNota(new File(path, strFileNota), noteEditorFrg.getTxtNotas().getText().toString(), view);
            }
        }
    }

    private void alertaGuardarCambios() {

        final Context context = this;
        String title = "Guardar";
        String message = "¿Desea salir sin guardar la nota actual?";
        String button1String = "Salir";
        String button2String = "Guardar";

        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle(title);
        ad.setMessage(message);

        ad.setNegativeButton(
                button1String,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        getSupportFragmentManager().popBackStackImmediate();
                    }
                }
        );

        ad.setPositiveButton(
                button2String,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        guardarNota(ctx.findViewById(R.id.fbtnGuardar));
                        getSupportFragmentManager().popBackStackImmediate();
                    }
                }
        );

        //
        ad.show();
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
