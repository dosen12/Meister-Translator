package com.squareround.meistertranslator;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.io.File;
import java.util.ArrayList;

public class ClientExecuter extends Service {
    private static final int PROGRESS_RATE_STT = 6;
    private static final int PROGRESS_RATE_TTT = 1;
    private static final int PROGRESS_RATE_PART = PROGRESS_RATE_STT + PROGRESS_RATE_TTT;
    private static final int FFMPEG_RETURN_CUT_FRONT = 0;
    private static final int FFMPEG_RETURN_CUT_BACK = 1;

    private FFMPEGLinker ffClient;
    private TextToTextClient tttClient;
    private SpeechToTextClient sttClient;
    private ClientAsyncListener< Client > clients;
    private ArrayList< SparseArray< String > > result;
    private SparseArray< String > syncMap;
    private ArrayList< Integer > syncTokenNum;
    private ArrayList< Integer > listKey;
    private ArrayList< String > listValue;
    private DBHelper database;
    private ClientLoadingView clientLoadingView;

    private String pathExtonal = Environment.getExternalStorageDirectory().getPath();
    private String pathProject = "/tmp/MeisterTranslator";
    private String pathDestination = "/demo.wav";
    private String pathTarget = "/demoEng.mp4";
    private int frameNum = 0;
    private int frameLast = -1;
    private boolean asyncEnd = false;
    private int progressMax = 0;
    private int progress = 0;
    private int[] progressPart = new int[] { 0, 0, 0, 0 };
    private int progressPosition = 0;
    private int progressSplit = 1;
    private static boolean executing = false;
    private ClientBinder binder;
    public class ClientBinder extends Binder {

        public ClientExecuter getService() {
            return ClientExecuter.this;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();

        binder = new ClientBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        executing = false;
    }

    @Nullable
    @Override
    public IBinder onBind( Intent intent ) {
        return binder;
    }

    @Override
    public boolean onUnbind( Intent intent ) {
        executing = false;

        return super.onUnbind( intent );
    }

    public void startClients( FFMPEGLinker ffClient, TextToTextClient tttClient, SpeechToTextClient sttClient, ClientLoadingView clientLoadingView1, String pathFile ) {
        this.ffClient = ffClient;
        this.tttClient = tttClient;
        this.sttClient = sttClient;
        this.clientLoadingView = clientLoadingView1;
        this.database = new DBHelper( this, "MeisterTranslator", null, 1 );
        executing = true;

        progressMax = 50;
        execute( pathFile );
    }

    public void execute( String pathFile ) {
        clientLoadingView.setProgress( 1, 300 );
        deleteDirectory( pathExtonal + pathProject );
        pathTarget = pathFile;
        if( loadDatabase( pathTarget ) <= 0 ) {
            new File( pathExtonal + pathProject ).mkdirs();
            ffClient.convertMP4toWAV( pathFile, pathExtonal + pathProject + pathDestination );
            progressSplit = ffClient.audioSplit( pathExtonal + pathProject + pathDestination, "15" );
            progressMax = PROGRESS_RATE_PART * progressSplit;
            progressPosition = 0;

            sttClient.resourceClear();
            for( int i = 0; i < progressSplit; i++ ) {
                sttClient.resourceAdd( pathExtonal + pathProject + pathDestination + "out" + i + ".wav" );
            }
            sttClient.setLanguageCode( "en-US" );
            clients = new ClientAsyncListener<>();
            clients.clientClear();
            clients.clientAdd( sttClient );
            clients.setResponser( new ClientAsyncListener.AsyncResponse() {

                @Override
                public void responseCommand() {
                    ( new Thread( new Runnable() {

                        @Override
                        public void run() {
                            result = sttClient.getResult();
                            frameNum = 0;
                            progressPosition = 1;

                            for( int num = 0; num < result.size(); num++ ) {
                                for( int test = 0; test < result.get( num ).size(); test++ ) { // 테스트 입니다.
                                    if( result.get( num ).get( test ) != null ) {
                                        frameNum += result.get( num ).get( test ).length();
                                    }
                                }
//                                frameNum += ( result.get( num ) ).size();
                                if( sttClient.getClientEndFrame( num ) > 0 ) {
                                    int endFrame = ( num * 15 * 1000 ) + sttClient.getClientEndFrame( num );
                                    frameLast = frameLast >= endFrame ? frameLast : endFrame;
                                }
                            }

                            sttClient.setLanguageCode( "ko-KR" );
                            clients = new ClientAsyncListener<>();
                            clients.clientClear();
                            clients.clientAdd( sttClient );
                            clients.setResponser( new ClientAsyncListener.AsyncResponse() {

                                @Override
                                public void responseCommand() {
                                    ( new Thread( new Runnable() {

                                        @Override
                                        public void run() {
                                            result = sttClient.getResult();
                                            progressPosition = 2;
                                            int frameCount = 0;
                                            int frameEnd = -1;

                                            for( int num = 0; num < result.size(); num++ ) {
                                                for( int test = 0; test < result.get( num ).size(); test++ ) { // 테스트 입니다.
                                                    if( result.get( num ).get( test ) != null ) {
                                                        frameCount += result.get( num ).get( test ).length();
                                                    }
                                                }
                                                frameCount += ( result.get( num ) ).size();
                                                if( sttClient.getClientEndFrame( num ) > 0 ) {
                                                    int endFrame = ( num * 15 * 1000 ) + sttClient.getClientEndFrame( num );
                                                    frameEnd = frameEnd >= endFrame ? frameEnd : endFrame;
                                                }
                                            }
                                            System.out.println( " >>> en = " + frameNum + " :: ko = " + frameCount );
                                            if( ( frameNum < frameCount )  ) { // || ( frameCount >= 10 )
                                                sttClient.setLanguageCode( "ko-KR" );
                                                frameLast = frameEnd;
                                            } else {
                                                sttClient.setLanguageCode( "en-US" );
                                            }

                                            ( new Thread( new Runnable() {

                                                @Override
                                                public void run() {
                                                    int ooffset = 0;
                                                    int plus = 0;

                                                    syncMap = new SparseArray<>();
                                                    syncTokenNum = new ArrayList<>();
                                                    while( ooffset < frameLast ) {
                                                        if( !executing ) {
                                                            return;
                                                        }
                                                        setProgress( true, ( float )frameLast, ( float )( ooffset * progressSplit ) );
                                                        ffClient.audioCut( pathExtonal + pathProject + pathDestination, pathExtonal + pathProject + "/slice.wav", Double.toString( ooffset / ( double )1000 ), "15", FFMPEG_RETURN_CUT_BACK );

                                                        sttClient.resourceClear();
                                                        sttClient.resourceAdd( pathExtonal + pathProject + "/slice.wav" );
                                                        clients = new ClientAsyncListener<>();
                                                        clients.clientClear();
                                                        clients.clientAdd( sttClient );
                                                        clients.execute();
                                                        while( !sttClient.getSuccess() ) {
                                                            if( !executing ) {
                                                                return;
                                                            }
                                                        }

                                                        result = sttClient.getResult();
                                                        for( SparseArray< String > sync : result ) {
                                                            for( int i = 0; i < sync.size(); i++ ) {
                                                                int key = sync.keyAt( i );
                                                                syncMap.put( key + ooffset, sync.get( key ) );
                                                                if( plus < key + ooffset ) {
                                                                    plus = key + ooffset;
                                                                }
                                                            }
                                                        }
                                                        for( ArrayList< Integer > tokens : sttClient.getSyncTokenNum() ) {
                                                            for( int token : tokens ) {
                                                                syncTokenNum.add( token + ooffset );
                                                            }
                                                        }
                                                        if( plus <= ooffset ) {
                                                            ooffset += 10 * 1000;
                                                        } else {
                                                            ooffset = plus;
                                                        }
                                                    }
                                                    asyncEnd = true;
                                                    setProgress( true, ( float )frameLast, ( float )( frameLast * progressSplit ) );
                                                }
                                            } ) ).start();
                                        }

                                    } ) ).start();
                                }

                            } );
                            clients.execute();
                        }

                    } ) ).start();
                }

            } );
            clients.execute();

            ( new Thread( new Runnable() {

                @Override
                public void run() {
                    while( !asyncEnd ) {
                        if( !executing ) {
                            clients.cancelListen();
                            return;
                        }
                        if( progressPosition < 2 ) {
                            setProgress( true, ( float )clients.getClientsNum(), ( float )clients.getProgress() );
                        }
                        int progressSum = 0;
                        for( int progresses : progressPart ) {
                            progressSum += progresses;
                        }
                        clientLoadingView.setProgress( progressSum, progressMax );
                    }

                    listKey = new ArrayList<>();
                    listValue = new ArrayList<>();
                    progressPosition = 3;

                    for( int i = 0; i < syncMap.size(); i++ ) {
                        int key = syncMap.keyAt( i );
                        String value = "";

                        setProgress( false, ( float )syncMap.size(), ( float )i );
                        int progressSum = 0;
                        for( int progresses : progressPart ) {
                            progressSum += progresses;
                        }
                        clientLoadingView.setProgress( progressSum, progressMax );
                        if( !executing ) {
                            clients.cancelListen();
                            return;
                        }
                        if( sttClient.getLanguageCode().equals( "en-US" ) ) {
                            int currentFrame = key;
                            int nextFrame = currentFrame;
                            int link = 0;
                            String source = syncMap.get( key );
                            ArrayList< Integer > tokens = new ArrayList<>();

                            if ( ( source == null ) || ( source.equals( "" ) ) ) {
                                continue;
                            }
                            tokens.add( currentFrame );
                            for( int j = 0; j < syncMap.size(); j++ ) {
                                int min = syncMap.keyAt( j );
                                if( ( min > currentFrame ) && ( ( min < nextFrame ) || ( currentFrame == nextFrame ) ) ) {
                                    nextFrame = min;
                                }
                            }
                            while( ( currentFrame != nextFrame ) && ( syncTokenNum.indexOf( nextFrame ) != -1 ) ) {
                                currentFrame = nextFrame;
                                link++;
                                source += syncMap.get( nextFrame );
                                tokens.add( nextFrame );
                                for( int j = 0; j < syncMap.size(); j++ ) {
                                    int min = syncMap.keyAt( j );
                                    if( ( min > currentFrame ) && ( ( min < nextFrame ) || ( currentFrame == nextFrame ) ) ) {
                                        nextFrame = min;
                                    }
                                }
                            }
                            i += link;
                            char[] adder = source.toCharArray();
                            adder[ source.length() - 1 ] = '.';
                            source = String.valueOf( adder );

                            tttClient = new TextToTextClient( source );
                            clients = new ClientAsyncListener<>();
                            clients.clientClear();
                            clients.clientAdd( tttClient );
                            clients.execute();
                            while( !tttClient.getSuccess() ) {
                                if( !executing ) {
                                    clients.cancelListen();
                                    return;
                                }
                            }

                            String[] subResult = tttClient.getResult().split( " " );
//                            String[] subResult = "안녕하세요. 어 안녕.".split( " " );
                            if ( subResult.length == 0 ) {
                                continue;
                            }
                            link = ( int )Math.ceil( subResult.length / ( double )( link + 1 ) );
                            String subString;
                            for( int j = 0, k, count = 0; j < subResult.length; j += k, count++ ) {
                                subString = "";
                                for ( k = 0; ( k < link ) && ( j + k < subResult.length ); k++ ) {
                                    subString += " " + subResult[ j + k ];
                                }
                                key = tokens.get( count );
                                value = subString;
                                listKey.add( key );
                                listValue.add( value );
                                key = tokens.get( count );
                            }
                        } else {
                            value = syncMap.get( key );
                            listKey.add( key );
                            listValue.add( syncMap.get( key ) );
                        }
                        if( !value.isEmpty() ) {
                            insertDatabase( getSquence( pathTarget ), pathTarget, key, value, null );
                        }
                    }
                    setProgress( false, ( float )syncMap.size(), ( float )syncMap.size() );
                    int progressSum = 0;
                    for( int progresses : progressPart ) {
                        progressSum += progresses;
                    }
                    clientLoadingView.setProgress( progressSum, progressMax );
                    clientLoadingView.stopOk();
                }

            } ) ).start();
        }
    }

    public static boolean getExecuting() {
        return executing;
    }

    public ArrayList< Integer > getListKey() {
        return this.listKey;
    }

    public ArrayList< String > getListValue() {
        return this.listValue;
    }

    public int getSquence( String uri ) {
        return database.getSQ( uri );
    }

    public void deleteDirectory( String filePath ) {
        File rootFile = new File( filePath );

        while( rootFile.exists() ) {
            if( rootFile.isDirectory() ) {
                File[] files = rootFile.listFiles();

                for( File file : files ) {
                    if( file.isDirectory() ) {
                        deleteDirectory( file.getPath() );
                    } else {
                        file.delete();
                    }
                }
                rootFile.delete();
            } else {
                rootFile.delete();
            }
        }
    }

    public int loadDatabase( String uri ) {
        int key;

        listKey = new ArrayList<>();
        listValue = new ArrayList<>();
        syncMap = database.loadData( uri );
        progressMax = syncMap.size();
        for( key = 0; key < syncMap.size(); key++ ) {
            progress = key;
            if( !executing ) {
                break;
            }
            listKey.add( syncMap.keyAt( key ) );
            listValue.add( syncMap.get( syncMap.keyAt( key ) ) );
            clientLoadingView.setProgress( progress, progressMax );
        }
        if( executing ) {
            progress = key;
            clientLoadingView.setProgress( progress, progressMax );
        } else {
            clientLoadingView.stopFailure();
        }

        return key;
    }

    public void insertDatabase( int index, String file, int startFrame, String subTitle, Integer endFrame ) {
        database.insert( index, file, startFrame, subTitle, endFrame );
    }



    public int[] getProgress() {
        return new int[] { progress, progressMax };
    }

    public void setProgress( boolean isClientStt, float clientMax, float clientSuccess ) {
        progressPart[ progressPosition ] = Math.round(
                (
                        (
                                ( float )progressMax / ( float )PROGRESS_RATE_PART
                        ) * (
                                isClientStt ?
                                        ( float )PROGRESS_RATE_STT / ( float )( progressSplit + 2 )
                                        :
                                        ( float )PROGRESS_RATE_TTT
                        )
                ) / (
                        clientMax / clientSuccess
                )
        );

    }

}
