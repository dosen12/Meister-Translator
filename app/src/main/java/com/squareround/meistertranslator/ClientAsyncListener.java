package com.squareround.meistertranslator;

import android.os.AsyncTask;

import java.util.ArrayList;

public class ClientAsyncListener< Type extends Client > extends AsyncTask< Void, Void, Boolean > {

    private ArrayList< Type > clients;
    private AsyncResponse responser;
    public interface AsyncResponse {

        void responseCommand();

    }

    public ClientAsyncListener() {
        clients = new ArrayList<>();
        responser = null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        for( Client client : clients ) {
            client.clientExecute();
        }
    }

    @Override
    protected Boolean doInBackground( Void... voids ) {
        boolean asyncAllGiven = false;

        while( !asyncAllGiven ) {
            asyncAllGiven = true;
            for( Client client : clients ) {
                if( !client.getSuccess() ) {
                    asyncAllGiven = false;
                }
            }
        }

        return asyncAllGiven;
    }

    @Override
    protected void onPostExecute( Boolean aBoolean ) {
        super.onPostExecute( aBoolean );

        if( aBoolean && ( responser != null ) ) {
            responser.responseCommand();
        }
    }

    public void setResponser( AsyncResponse responser ) {
        this.responser = responser;
    }

    public void clientClear() {
        clients.clear();
    }

    public void clientAdd( Type client ) {
        clients.add( client );
    }

}
