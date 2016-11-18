package ues.ice_bv;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

/**
 * Created by jose on 11-02-16.
 */

public class Utilidad {
    private static final String TAG = "LOGu"; //Identificador en el log.

    // GUARDAR VARIABLES POR LA CLASE SharedPreferences
    private String base = "configuracion";
    private String lista = "acciones"; // lista de simbolos guardadas por el usuario.

    private SharedPreferences getConfiguracion(Context e) {
        return e.getSharedPreferences(base, 0);
    }

    public String getAcciones(Context e) {
        return getConfiguracion(e).getString(lista, ""); //conseguir la lista de simbolos
    }

    public String getMinimo(Context e, String simbolo) {
        return getConfiguracion(e).getString(simbolo, ""); // Conseguir el minimo de uma empresas dada.
    }

    public void setAccion(Context e, String simbolo, String minimo) {
        SharedPreferences.Editor editor = getConfiguracion(e).edit();
        editor.putString(simbolo, minimo);

        if (getMinimo(e, simbolo).equals("")) {
            String simbolos = getConfiguracion(e).getString(lista, "");
            String mas = simbolos.equals("") ? "" : "+";
            editor.putString(lista, simbolos + mas + simbolo); // Agregar el simbolo en la lista de simbolos
        }
        editor.commit();
    }

    public void eliminarAccion(Context e, String simbolo) {
        SharedPreferences.Editor editor = getConfiguracion(e).edit();
        String simbolos = getConfiguracion(e).getString(lista, "");
        // PRIMERO+SEGUNDO+TERCERO
        simbolos = simbolos.replace(simbolo + "+", ""); // Eliminar si es el primero.
        simbolos = simbolos.replace("+" + simbolo, ""); // Eliminar si esta en medio o último (el segundo o tercero).
        simbolos = simbolos.replace(simbolo, ""); // Eliminar si es el unico.
        editor.remove(simbolo);
        editor.putString(lista, simbolos); // Actualizar la lista de simbolos
        editor.commit();
    }

    public void eliminarTodo(Context e) {
        SharedPreferences.Editor editor = getConfiguracion(e).edit();
        editor.clear();
        editor.commit();
    }

    // REALIZAR PETICIONES
    private String dato = new String();
    private boolean contenedorLleno = false;
    private String[] datos = new String[]{};
    private boolean estaCompleta = false;
    private boolean estaErronea = false;
    private boolean estaCancelado = false;
    private int tipo;

    public static final int A_UNOxUNO = 1;
    public static final int A_LINEAxLINEA = 2;

    public static final String B_TODO = Simbolo.SIMBOLOS;

    public static final String C_LISTA = "ns"; //ns nombre y simbolo,
    public static final String C_INFORMACION = "snl1"; //snl1 simbolo, nombre y precio
    public static final String C_SERVICIO = "sl1"; //l1 simbolo y precio

    public void iniciarPeticion(int tipo, String simbolos, String parametros, int... numero) {
        int NUM = numero.length == 0 ? 10 : numero[0];

        datos = new String[]{};
        contenedorLleno = false;
        estaCompleta = false;
        estaCancelado = false;
        estaErronea = false;
        this.tipo = tipo;

        // Partir de 10 a 10 los simbolos.
        StringTokenizer token = new StringTokenizer(simbolos, "+");
        int lim = token.countTokens();
        int a1 = (int) lim / NUM;
        int a2 = lim % NUM == 0 ? 0 : 1;
        String[] url = new String[a1 + a2];
        Log.d(TAG, "total " + (a1 + a2));
        String aux2 = "";
        int j = 0;
        for (int i = 1; i <= lim; i++) {
            aux2 += token.nextToken() + "+";
            if (i % NUM == 0 || lim == i) {
                url[j] = ("http://finance.yahoo.com/d/quotes.csv?s=" + aux2 + "&f=" + parametros);
                Log.d(TAG, "url " + url[j]);
                aux2 = "";
                j++;
            }
        }

        new PeticionYahoo().execute(url);
    }

    private class PeticionYahoo extends AsyncTask<String, Integer, String[]> {

        protected String[] doInBackground(String... urls) {
            Log.d(TAG, "inicio");

            String[] aux = new String[]{};
            String cadena = "";
            try {
                for (int a = 0; a < urls.length; a++) {
                    // 1. Inicio. Crear el enlace de comunicación entre nuestra aplicación y una URL
                    URL url = new URL(urls[a]);
                    URLConnection conexion = url.openConnection();
                    // 2. Crear un buffer
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                    // 3. Si existe caracteres...Entonces repetir
                    while (buffer.read() != -1) {
                        String linea = buffer.readLine();
                        if (linea == null || linea.equals("")) {
                            break;
                        }
                        linea = linea.replaceAll("\"", ""); // reemplazar " (comilla doble) por caracter vacio
                        linea = linea.replaceAll(", ", ". "); // reemplazar
                        Log.d(TAG, "linea " + linea);
                        // 3.1 Realizamos la descomposición de la cadena que está separada por comas
                        // Por ejemplo: "MMI","Mm Mm Inst",17.10
                        StringTokenizer token = new StringTokenizer(linea, ",");
                        // 3.2 Conseguir límites.
                        int lim = token.countTokens();
                        // 3.3 Crear espacio.
                        aux = new String[lim];
                        for (int i = 0; i < lim; i++) {
                            switch (tipo) {
                                case Utilidad.A_UNOxUNO:
                                    // 3.3.a Se mandará uno por uno
                                    aux[i] = token.nextToken();
                                    setPeticionParcial(aux[i]);
                                    break;
                                case Utilidad.A_LINEAxLINEA:
                                    // 3.3.b Se mandará línea a linea
                                    if (i < (lim - 1)) {
                                        cadena += token.nextToken() + "\n";
                                    } else {
                                        aux[i] = cadena + token.nextToken();
                                        setPeticionParcial(aux[i]);
                                        cadena = "";
                                    }
                                    break;
                            }
                            if (estaCancelado) {
                                return aux;
                            }
                        }
                    }
                    // 4. Se cierra el buffer
                    buffer.close();
                }
            } catch (MalformedURLException emfurl) {
                Log.d(TAG, "¿Has escrito bien la URL? : esto es lo que escribiste: " + emfurl.toString());
                setPeticionErronea(aux);
            } catch (IOException eio) {
                Log.d(TAG, "No se puede leer desde internet: " + eio.toString());  //System.out.println();
                setPeticionErronea(aux);
            }
            if (!estaErronea) {
                setPeticionCompleta(aux);
            }
            Log.d(TAG, "fin");
            return aux;
        }


    }

    /**
     * Obtiene de forma concurrente o síncrona el elemento que hay en el contenedor.
     *
     * @return String el dato.
     */
    public synchronized String getPeticionParcial() {
        // 1. Si No esta lleno... entonces repetir
        while (!contenedorLleno) {
            try {
                //1.1 Esperar 50 microsegundos
                wait(50);
                if (estaCompleta || estaCancelado || estaErronea) {
                    return null;
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "Contenedor: Error en get -> " + e.getMessage()); //System.err.println();
            }
        }
        // 2. Se consumio el recurso.
        contenedorLleno = false;
        // 3. Despertar el hilo "productor" para que coloque otro recurso.
        notify();
        return dato;
    }

    /**
     * Introduce de forma concurrente o síncrona un elemento en el contenedor
     *
     * @param value Elemento a introducir en el contenedor
     */
    public synchronized void setPeticionParcial(String value) {
        // 1. Esta lleno...entonces
        while (contenedorLleno) {
            try {
                //1.1 Esperar 50 microsegundos
                wait(50);
                if (estaCompleta || estaCancelado || estaErronea) {
                    return;
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "Contenedor: Error en set -> " + e.getMessage());
            }
        }
        // 2. Se produjo otro recurso.
        dato = value;
        contenedorLleno = true;
        // 3. Despertar el hilo "consumidor" para que consuma el recurso.
        notify();
    }

    public String[] getPeticionCompleta() {
        return datos;
    }

    private synchronized void setPeticionCompleta(String[] aux) {
        estaCompleta = true;
        dato = null;
        datos = aux;
        notify();
    }

    public boolean isPeticionCompleta() {
        return estaCompleta;
    }

    private synchronized void setPeticionErronea(String[] aux) {
        estaErronea = true;
        dato = null;
        datos = aux;
        notify();
    }

    public boolean isPeticionErronea() {
        return estaErronea;
    }


    public void setPeticionCancelada(boolean cancelada) {
        estaCancelado = cancelada;
    }

    public boolean isPeticionCancelada() {
        return estaCancelado;
    }
}