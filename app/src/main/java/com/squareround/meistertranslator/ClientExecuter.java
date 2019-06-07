package com.squareround.meistertranslator;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.SparseArray;

import java.io.File;
import java.util.ArrayList;

import javax.annotation.Nullable;

public class ClientExecuter {

    private Context context;
    private Intent intent;
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

    private String pathExtonal = Environment.getExternalStorageDirectory().getPath();
    private String pathProject = "/tmp/MeisterTranslator";
    private String pathDestination = "/demo.wav";
    private String pathTarget = "/demoEng.mp4";
    private int frameNum = 0;
    private int frameLast = -1;
    private boolean asyncEnd = false;
    private boolean executing = false;

    public ClientExecuter( Context context, FFMPEGLinker ffClient, TextToTextClient tttClient, SpeechToTextClient sttClient ) {
        this.context = context;
        this.ffClient = ffClient;
        this.tttClient = tttClient;
        this.sttClient = sttClient;
        this.database = new DBHelper( context, "MeisterTranslator", null, 1 );
    }

    public void execute( String pathFile ) {
        executing = true;
        pathTarget = pathFile;
        if( loadDatabase( pathTarget ) <= 0 ) { //&& false ) {
            new File( pathExtonal + pathProject ).mkdirs();
            ffClient.convertMP4toWAV( pathFile, pathExtonal + pathProject + pathDestination );
            int splits = ffClient.audioSplit( pathExtonal + pathProject + pathDestination, "15" );

            sttClient.resourceClear();
            for( int i = 0; i < splits; i++ ) {
                sttClient.resourceAdd( pathExtonal + pathProject + pathDestination + "out" + i + ".wav" );
            }
            sttClient.setLanguageCode( "en-US" );
            clients = new ClientAsyncListener<>();
            clients.clientClear();
            clients.clientAdd( sttClient );
            clients.setResponser( new ClientAsyncListener.AsyncResponse() {

                @Override
                public void responseCommand() {
                    result = sttClient.getResult();
                    frameNum = 0;

                    for( int num = 0; num < result.size(); num++ ) {
                        for( int test = 0; test < result.get( num ).size(); test++ ) { // 테스트 입니다.
                            if( result.get( num ).get( test ) != null ) {
                                frameNum += result.get( num ).get( test ).length();
                            }
                        }
//                        frameNum += ( result.get( num ) ).size();
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
                            result = sttClient.getResult();
                            int frameCount = 0;
                            int frameEnd = -1;
                            int offset = 0;
                            int plus = 0;

                            for( int num = 0; num < result.size(); num++ ) {
                                for( int test = 0; test < result.get( num ).size(); test++ ) { // 테스트 입니다.
                                    if( result.get( num ).get( test ) != null ) {
                                        frameCount += result.get( num ).get( test ).length();
                                    }
                                }
                                frameCount += ( result.get( num) ).size();
                                if( sttClient.getClientEndFrame( num ) > 0 ) {
                                    int endFrame = ( num * 15 * 1000 ) + sttClient.getClientEndFrame( num );
                                    frameEnd = frameEnd >= endFrame ? frameEnd : endFrame;
                                }
                            }
                            System.out.println( "en = " + frameNum + " :: ko = " + frameCount );
                            if( ( frameNum < frameCount )  ) { // || ( frameCount >= 10 )
                                sttClient.setLanguageCode( "ko-KR" );
                                frameLast = frameEnd;
                            } else {
                                sttClient.setLanguageCode( "en-US" );
                            }

                            syncMap = new SparseArray<>();
                            syncTokenNum = new ArrayList<>();
                            while( offset < frameLast ) {
                                ffClient.audioCut( pathExtonal + pathProject + pathDestination, pathExtonal + pathProject + "/slice.wav", Double.toString( offset / ( double )1000 ), Double.toString( ( offset / ( double )1000 ) + 15 ) );
                                sttClient.resourceClear();
                                sttClient.resourceAdd( pathExtonal + pathProject + "/slice.wav" );
                                clients = new ClientAsyncListener<>();
                                clients.clientClear();
                                clients.clientAdd( sttClient );
                                clients.execute();
                                while( !sttClient.getSuccess() );

                                result = sttClient.getResult();
                                for( SparseArray< String > sync : result ) {
                                    for( int i = 0; i < sync.size(); i++ ) {
                                        int key = sync.keyAt( i );
                                        syncMap.put( key + offset, sync.get( key ) );
                                        if( plus < key + offset ) {
                                            plus = key + offset;
                                        }
                                    }
                                }
                                for( ArrayList< Integer > tokens : sttClient.getSyncTokenNum() ) {
                                    for( int token : tokens ) {
                                        syncTokenNum.add( token + offset );
                                    }
                                }
                                if( plus <= offset ) {
                                    offset += 10 * 1000;
                                } else {
                                    offset = plus;
                                }
                            }
                            asyncEnd = true;
                        }

                    } );
                    clients.execute();
                }

            } );
            clients.execute();

            Thread detector = new Thread( new Runnable() {

                @Override
                public void run() {
                    while( !asyncEnd );
                    listKey = new ArrayList<>();
                    listValue = new ArrayList<>();

                    for( int i = 0; i < syncMap.size(); i++ ) {
                        int key = syncMap.keyAt( i );
                        String value = "";

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
                            source = String.valueOf( adder ); // adder

                            tttClient = new TextToTextClient( source );
                            clients = new ClientAsyncListener<>();
                            clients.clientClear();
                            clients.clientAdd( tttClient );
                            clients.execute();
                            while( !tttClient.getSuccess() );

                            String[] subResult = tttClient.getResult().split( " " );
//                            String[] subResult = source.split( " " );
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

                    executing = false;
                    intent = new Intent( context, VideoPlayerActivity.class );
                    intent.putExtra( "URI", pathTarget );
                    intent.putIntegerArrayListExtra( "SyncKeys", listKey );
                    intent.putStringArrayListExtra( "SyncValues", listValue );
                    context.startActivity( intent );
                }

            } );
            detector.start();
        } else {
            System.out.println( "성공적으로 불러왔습니다." );
            executing = false;
            intent = new Intent( context, VideoPlayerActivity.class );
            intent.putExtra( "URI", pathTarget );
            intent.putIntegerArrayListExtra( "SyncKeys", listKey );
            intent.putStringArrayListExtra( "SyncValues", listValue );
            context.startActivity( intent );
        }

    }

    public boolean getExecuting() {
        return executing;
    }

    public int getSquence( String uri ) {
        return database.getSQ( uri );
    }

    public int loadDatabase( String uri ) {
        int key;

        listKey = new ArrayList<>();
        listValue = new ArrayList<>();
        syncMap = database.loadData( uri );
        for( key = 0; key < syncMap.size(); key++ ) {
            listKey.add( syncMap.keyAt( key ) );
            listValue.add( syncMap.get( syncMap.keyAt( key ) ) );
        }

        return key;
    }

    public void insertDatabase( int index, String file, int startFrame, String subTitle, Integer endFrame ) {
        database.insert( index, file, startFrame, subTitle, endFrame );
    }

}
