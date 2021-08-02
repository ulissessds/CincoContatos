package com.example.main.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.example.cincocontatos.R;
import com.example.main.model.Contato;
import com.example.main.model.User;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;

public class NovoUsuarioActivity extends AppCompatActivity {

    /*
    boolean primeiraVezUser=true;
    boolean primeiraVezNome=true;
    boolean primeiraVezEmail=true;
    boolean primeiraVezSenha=true;
    */
    EditText edUser;
    EditText edPass;
    EditText edNome;
    EditText edEmail;
    Switch swLogado;
    Switch swTema;
    Button btCriar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_usuario);

        btCriar=findViewById(R.id.btCriar);
        edUser=findViewById(R.id.edT_Login2);
        edPass=findViewById(R.id.edt_Pass2);
        edNome=findViewById(R.id.edtNome);
        edEmail=findViewById(R.id.edEmail);
        swLogado=findViewById(R.id.swLogado);
        swTema= findViewById(R.id.swTema);

        setTitle(R.string.cadastrar_novo);

        /*
        //Evento de limpar Componente - User
        edUser.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (primeiraVezUser){
                    primeiraVezUser=false;
                    edUser.setText("");
                }

                return false;
            }
        });

        //Evento de limpar Componente - Senha
        edPass.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (primeiraVezSenha){
                    primeiraVezSenha=false;
                    edPass.setText("");
                    edPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                    );
                }
                return false;
            }
        });

        //Evento de limpar Componente - E-mail
        edEmail.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (primeiraVezEmail){
                    primeiraVezEmail=false;
                    edEmail.setText("");
                }

                return false;
            }
        });

        //Evento de limpar Componente - Nome
        edNome.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (primeiraVezNome){
                    primeiraVezNome=false;
                    edNome.setText("");
                }

                return false;
            }
        });
        */

        btCriar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String nome, login, senha;
                nome = edNome.getText().toString();
                login = edUser.getText().toString();
                senha = edPass.getText().toString();

                //Novos componentes
                String email;
                email = edEmail.getText().toString();
                boolean manterLogado;
                manterLogado= swLogado.isChecked();
                boolean temaEscuro=swTema.isChecked();

                SharedPreferences salvaUser= getSharedPreferences("usuarioPadrao", Activity.MODE_PRIVATE);
                SharedPreferences.Editor escritor= salvaUser.edit();

                escritor.putString("nome",nome);
                escritor.putString("senha",senha);
                escritor.putString("login",login);

                // Escrever no SharedPreferences
                escritor.putString("email",email);
                escritor.putBoolean("manterLogado",manterLogado);

                escritor.putBoolean("tema",temaEscuro);

                escritor.commit(); // Salva em Disco

                // Limpar o SharedPreference de contatos anterior...
                SharedPreferences recuperarContatos = getSharedPreferences("contatos", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = recuperarContatos.edit();

                int num = recuperarContatos.getInt("numContatos", 0);
                Contato contato;

                for (int i = 1; i <= num; i++) {
                    String objSel = recuperarContatos.getString("contato" + i, "");
                    if (objSel.compareTo("") != 0) {
                        try {
                            ByteArrayInputStream bis = new ByteArrayInputStream
                                    (objSel.getBytes(StandardCharsets.ISO_8859_1.name()));
                            ObjectInputStream oos = new ObjectInputStream(bis);
                            contato = (Contato) oos.readObject();

                            editor.remove("contato" + i);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                editor.commit();

                // Salvando o user

                User user =new User(nome,login,senha,email,manterLogado);

                Intent intent=new Intent(NovoUsuarioActivity.this, AlterarContatosActivity.class);
                intent.putExtra("usuario",user);
                startActivity(intent);

                // Mesmo após a chamar de um startActivity o método continuará execuntando
                // Por exemplo, aqui mataremos a Activity atual porém AlterarContatos será exibida
                finish();
            }
        });
    }
}