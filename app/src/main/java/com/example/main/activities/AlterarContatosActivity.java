package com.example.main.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.cincocontatos.R;
import com.example.main.model.Contato;
import com.example.main.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

public class AlterarContatosActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    //Boolean primeiraVezUser=true;
    EditText edtNome;
    ListView lv;
    BottomNavigationView bnv;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_contatos);
        edtNome = findViewById(R.id.edtBusca);
        bnv = findViewById(R.id.bnv);
        bnv.setOnNavigationItemSelectedListener(this);
        bnv.setSelectedItemId(R.id.anvMudar);

        //Dados da Intent Anterior
        Intent quemChamou=this.getIntent();
        if (quemChamou!=null) {
            Bundle params = quemChamou.getExtras();
            if (params!=null) {
                //Recuperando o Usuario
                user = (User) params.getSerializable("usuario");
                setTitle("Alterar Contatos de Emergência");
            }
        }
        lv = findViewById(R.id.listContatosDoCell);

        /*
        //Evento de limpar Componente
        edtNome.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (primeiraVezUser){
                    primeiraVezUser=false;
                    edtNome.setText("");
                }

                return false;
            }
        });
        */
    }

    public void salvarContato (Contato w){
        SharedPreferences salvaContatos =
                getSharedPreferences("contatos",Activity.MODE_PRIVATE);

        int num = salvaContatos.getInt("numContatos", 0); //checando quantos contatos já tem
        SharedPreferences.Editor editor = salvaContatos.edit();

        Contato contato;
        boolean jaExiste = false;

        try {
            for (int i = 1; i <= num; i++) {
                String objSel = salvaContatos.getString("contato" + i, "");
                if (objSel.compareTo("") != 0) {
                    try {
                        ByteArrayInputStream bis = new ByteArrayInputStream
                                (objSel.getBytes(StandardCharsets.ISO_8859_1.name()));
                        ObjectInputStream oos = new ObjectInputStream(bis);
                        contato = (Contato) oos.readObject();

                        // Checar se o número já existe e sair
                        if (contato != null && w.getNome().equals(contato.getNome())) {
                            jaExiste = true;
                            break;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            if (!jaExiste) {
                ByteArrayOutputStream dt = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(dt);
                dt = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(dt);
                oos.writeObject(w);
                String contatoSerializado = dt.toString(StandardCharsets.ISO_8859_1.name());
                editor.putString("contato" + (num + 1), contatoSerializado);
                editor.putInt("numContatos", num + 1);
                editor.commit();
                user.getContatos().add(w);
                Toast.makeText(this, "Novo contato adicionado!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Contato já existe!", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("PDM","Matando a Activity Lista de Contatos");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("PDM","Matei a Activity Lista de Contatos");
    }

    public void onClickBuscar(View v){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
            Log.v("PDM", "Pedir permissão");
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 3333);
            return;
        }
        Log.v("PDM", "Tenho permissão");

        ContentResolver cr = getContentResolver();
        String consulta = ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";
        String [] argumentosConsulta= {"%"+edtNome.getText()+"%"};
        Cursor cursor= cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                consulta,argumentosConsulta, null);
        final String[] nomesContatos = new String[cursor.getCount()];
        final String[] telefonesContatos = new String[cursor.getCount()];
        Log.v("PDM","Tamanho do cursor:"+cursor.getCount());

        int i=0;
        while (cursor.moveToNext()) {
            int indiceNome = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME);
            String contatoNome = cursor.getString(indiceNome);
            Log.v("PDM", "Contato " + i + ", Nome:" + contatoNome);
            nomesContatos[i]= contatoNome;
            int indiceContatoID = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID);
            String contactID = cursor.getString(indiceContatoID);
            String consultaPhone = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID;
            Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, consultaPhone, null, null);

            while (phones.moveToNext()) {
                String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                telefonesContatos[i]=number; //Salvando só último telefone
            }
            i++;
        }

        if (nomesContatos !=null) {
            for(int j=0; j<=nomesContatos.length; j++) {
                ArrayAdapter<String> adaptador;
                adaptador = new ArrayAdapter<String>(this, R.layout.list_view_layout, nomesContatos);
                lv.setAdapter(adaptador);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Contato c= new Contato();
                        c.setNome(nomesContatos[i]);
                        c.setNumero("tel:+"+telefonesContatos[i]);
                        salvarContato(c);
                        Intent intent = new Intent(getApplicationContext(), ListaContatosActivity.class);
                        intent.putExtra("usuario", user);
                        startActivity(intent);
                        finish();

                    }
                });
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Checagem de o Item selecionado é o do perfil
        if (item.getItemId() == R.id.anvPerfil) {
            //Abertura da Tela Mudar Dados Usario
            Intent intent = new Intent(this, PerfilActivity.class);
            intent.putExtra("usuario", user);
            startActivity(intent);

        }
        // Checagem de o Item selecionado é o do perfil
        if (item.getItemId() == R.id.anvLigar) {
            //Abertura da Tela Mudar Contatos
            Intent intent = new Intent(this, ListaContatosActivity.class);
            intent.putExtra("usuario", user);
            startActivity(intent);

        }
        return true;
    }
}