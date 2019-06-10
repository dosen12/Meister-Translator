package com.squareround.meistertranslator;

public class FFMPEGLinker {

    static {
        System.loadLibrary( "avutil-55" );
        System.loadLibrary( "avcodec-57" );
        System.loadLibrary( "avformat-57" );
        System.loadLibrary( "avfilter-6" );
        System.loadLibrary( "swscale-4" );
        System.loadLibrary( "swresample-2" );
        System.loadLibrary( "ffmpeglinker" );
    }

    public native int information( String url );

    public native String convertMP4toWAV( String url, String dst );

    public native double audioCut( String url, String dst, String startTime, String endTime, int flag );

    public native double audioCutFront( String url, String dst, String time );

    public native double audioCutBack( String url, String dst, String time );

    public native int audioSplit( String url, String time );

}
