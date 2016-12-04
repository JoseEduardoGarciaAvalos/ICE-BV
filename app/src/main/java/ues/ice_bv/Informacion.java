package ues.ice_bv;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;

public class Informacion extends AppCompatActivity {

    private static final String TAG = "LOGi"; //Identificador en el log.
    private Utilidad utilidad = new Utilidad();
    private final int LIM = 3;

    private ListView jilv1;
    private ArrayAdapter datosLista;

    private Button jib1;
    private EditText jiet1;
    private Switch jis1;

    String simbolo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.informacion);

        // 1. Relacionar objetos
        jiet1 = (EditText) findViewById(R.id.jiet1);
        jis1 = (Switch) findViewById(R.id.jis1);
        jib1 = (Button) findViewById(R.id.jib1);
        jilv1 = (ListView) findViewById(R.id.jilv1);
        // 2. Recuperar parametros
        Bundle parametros = getIntent().getExtras();
        simbolo = parametros.getString("simbolo");
        // 3. Conseguir la información del internet
        datosLista = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        jilv1.setAdapter(datosLista);
        actualizar(simbolo);
        // 4. Habilitar switch
        String minimo = utilidad.getMinimo(this, simbolo);
        if (minimo.equals("")) {
            jis1.setChecked(false);
            jiet1.setText("");
        } else {
            jis1.setChecked(true);
            jiet1.setText(minimo);
        }
        setJib1(false);
    }

    // Métodos para la actualización de datos
    public void actualizar(final String simbolo) {
        // Android tiene dos reglas que se debe respetar, sino se cumplen lanzan excepción (se traba la aplicación) y manda un mensaje ARN ("Aplicación no responde")
        // 1- Cualquier consulta a la red (internet) deberá ejecutarse en un hilo aparte del principal
        // 2- Nunca deben llamarse objetos de vista dentro de los hilos.
        new Thread(new Runnable() {

            @Override
            public void run() {// El hilo se finaliza a sí mismo cuando termina su trabajo.
                Log.d(TAG, "inicio");
                // 1. Iniciar
                utilidad.iniciarPeticion(Utilidad.A_UNOxUNO, simbolo, Utilidad.C_INFORMACION, LIM);
                clearDatosLista();
                Log.d(TAG, "simbolos " + simbolo);
                // 2.1 Conseguir valor.
                String simbolo = utilidad.getPeticionParcial();
                String nombre = utilidad.getPeticionParcial();
                String precio = utilidad.getPeticionParcial();
                simbolo = simbolo == null ? "N/A" : simbolo;
                nombre = nombre == null ? "N/A" : nombre;
                precio = precio == null ? "N/A" : precio;
                // 2.1.1 Iniciar hilo UI para actualizarTodo la UI
                setDatosLista("Símbolo: " + simbolo);
                setDatosLista("Nombre: " + nombre);
                setDatosLista("Precio: " + (precio.equals("N/A") ? precio:"$ " + precio  + " (millones)"));
                try {

                    Double.parseDouble(precio); // Convertir string a numero
                    setJib1(true);
                } catch (NumberFormatException e) {
                    // Si no se puede convertir no se habilitaran los botones.
                }
                Log.d(TAG, "Fin");
            }
        }).start();
    }

    private synchronized void setDatosLista(final String valor) {
        runOnUiThread(new Runnable() { // Cuando se necesita modificar una variable de vista en un hilo, solo se hará por este hilo.
            @Override
            public void run() {
                datosLista.add(valor);
            }
        });
    }

    private synchronized void clearDatosLista() {
        runOnUiThread(new Runnable() { // Cuando se necesita modificar una variable de vista en un hilo, solo se hará por este hilo.
            @Override
            public void run() {
                datosLista.clear();
            }
        });
    }

    private synchronized void setJib1(final boolean habilitado) {
        runOnUiThread(new Runnable() { // Cuando se necesita modificar una variable de vista en un hilo, solo se hará por este hilo.
            @Override
            public void run() {
                jib1.setEnabled(habilitado);
            }
        });
    }

    public void guardar(View v) {

        if (jis1.isChecked()) {
            String minimo = jiet1.getText().toString();
            utilidad.setAccion(this, simbolo, minimo);
        } else {
            utilidad.eliminarAccion(this, simbolo);
        }

        finish();
    }
}
