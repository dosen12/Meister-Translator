package com.squareround.meistertranslator;

import android.os.AsyncTask;

import java.util.ArrayList;

public class ClientAsyncListener< Type extends Client > extends AsyncTask< Void, Void, Boolean > {

    private ArrayList< Type > clients;
    private AsyncResponse responser;
    private int progress;
    private boolean canceled;
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
        canceled = false;
        boolean asyncAllGiven = false;
        int progressCount;

        while( !asyncAllGiven && !canceled ) {
            asyncAllGiven = true;
            progressCount = 0;
            for( Client client : clients ) {
                if( !client.getSuccess() ) {
                    asyncAllGiven = false;
                }
                for( Object progresses : client.getProgress() ) {
                    if( ( boolean )progresses ) {
                        progressCount++;
                    }
                }
            }
            progress = progressCount;
        }

        return asyncAllGiven;
    }

    @Override
    protected void onPostExecute( Boolean aBoolean ) {
        super.onPostExecute( aBoolean );

        if( aBoolean && ( responser != null ) && !canceled ) {
            responser.responseCommand();
        }
    }

    public int getProgress() {
        return this.progress;
    }

    public int getClientsNum() {
        int clientsNum = 0;

        for( Client client : clients ) {
            clientsNum += client.getProgress().size();
        }

        return clientsNum;
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

    public void cancelListen() {
        canceled = true;
    }

}
