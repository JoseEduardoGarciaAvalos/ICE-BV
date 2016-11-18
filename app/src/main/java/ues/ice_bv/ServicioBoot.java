package ues.ice_bv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by jose on 10-25-16.
 */

public class ServicioBoot extends BroadcastReceiver {

    // Evento que se ejecuta después del boot del celúlar.
    @Override
    public void onReceive(Context context, Intent intent) {

        // LANZAR SERVICIO
        Intent i = new Intent(context, Servicio.class);
        context.startService(i);

    }
}