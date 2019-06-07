//
// Created by zubako on 2019-05-27.
//

#include <jni.h>
#include <string.h>
#include <android/log.h>
#include "ffmpeg.h"
#include "ffmpeg/armeabi-v7a/include/libavformat/avformat.h"

#define LOG_TAG "FFmpegForAndroid"
#define LOGI(...) __android_log_print(4, LOG_TAG, __VA_ARGS__);
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG , "libnav", __VA_ARGS__)
#define LOGE(...) __android_log_print(6, LOG_TAG, __VA_ARGS__);

JNIEXPORT jint JNICALL Java_com_squareround_meistertranslator_FFMPEGLinker_information( JNIEnv* env, jobject instance, jstring url ) {
    const char* uri = ( *env )->GetStringUTFChars( env, url, 0 );

    AVFormatContext* avFormatContext = NULL;

    // muxer, demuxer, decoder, encoder 초기화
    av_register_all();

    // nativeFilepath로 avFormatContext 가져오기
    if( avformat_open_input( &avFormatContext, uri, NULL, NULL ) < 0 )
    {
        LOGE( "Can't open input file '%s'", uri );
        (*env)->ReleaseStringUTFChars( env, url, uri );
        return -1;
    }

    // 유효한 스트림 정보 찾기
    if( avformat_find_stream_info( avFormatContext, NULL ) < 0 )
    {
        LOGE( "Failed to retrieve input stream information" );
        ( *env )->ReleaseStringUTFChars( env, url, uri );
        return -2;
    }

    // avFormatContext->nb_streams : 비디오 파일의 전체 스트림 수
    for( unsigned int index = 0; index < avFormatContext->nb_streams; index++ )
    {
        AVCodecParameters* avCodecParameters = avFormatContext->streams[ index ]->codecpar;
        if( avCodecParameters->codec_type == AVMEDIA_TYPE_VIDEO )
        {
            LOGI( "------- Video info -------" );
            LOGI( "codec_id : %d", avCodecParameters->codec_id );
            LOGI( "bitrate : %lld", avCodecParameters->bit_rate );
            LOGI( "width : %d / height : %d", avCodecParameters->width, avCodecParameters->height );
        }
        else if( avCodecParameters->codec_type == AVMEDIA_TYPE_AUDIO )
        {
            LOGI( "------- Audio info -------" );
            LOGI( "codec_id : %d", avCodecParameters->codec_id );
            LOGI( "bitrate : %lld", avCodecParameters->bit_rate );
            LOGI( "sample_rate : %d", avCodecParameters->sample_rate );
            LOGI( "number of channels : %d", avCodecParameters->channels );
            LOGI( "length : %lld", avFormatContext->duration );
        }
    }

    AVCodec *c_temp = av_codec_next(NULL);
    while (c_temp != NULL) {
        switch (c_temp->type) {
            case AVMEDIA_TYPE_VIDEO:
                LOGI("[Video]:%s", c_temp->name);
                break;
            case AVMEDIA_TYPE_AUDIO:
                LOGI("[Audio]:%s", c_temp->name);
                break;
            default:
                LOGI("[Other]:%s", c_temp->name);
                break;
        }
        c_temp = c_temp->next;
    }

    // release
    if( avFormatContext != NULL )
    {
        avformat_close_input( &avFormatContext );
    }

    // release
    ( *env )->ReleaseStringUTFChars( env, url, uri );

    return 0;
}

JNIEXPORT jstring JNICALL Java_com_squareround_meistertranslator_FFMPEGLinker_convertMP4toWAV( JNIEnv* env, jobject instance, jstring url, jstring dst ) {
    const char* strUrl = ( *env )->GetStringUTFChars( env, url, 0 );
    const char* strDst = ( *env )->GetStringUTFChars( env, dst, 0 );
    char* cmdUrl = ( char* )strUrl;
    char* cmdDst = ( char* )strDst;
    char* commands[ 10 ];
    char** argv = &commands[ 0 ];

    commands[ 0 ] = "ffmpeg";
    commands[ 1 ] = "-i";
    commands[ 2 ] = cmdUrl;
    commands[ 3 ] = "-ac";
    commands[ 4 ] = "1";
    commands[ 5 ] = "-f";
    commands[ 6 ] = "wav";
    commands[ 7 ] = cmdDst;
    execute( 8, argv );

    return dst;
}

JNIEXPORT jint JNICALL Java_com_squareround_meistertranslator_FFMPEGLinker_audioCut( JNIEnv* env, jobject instance, jstring url, jstring dst, jstring startTime, jstring endTime ) {
    const char* strUrl = ( *env )->GetStringUTFChars( env, url, 0 );
    const char* strDst = ( *env )->GetStringUTFChars( env, dst, 0 );
    const char* strStartTime = ( *env )->GetStringUTFChars( env, startTime, 0 );
    const char* strEndTime = ( *env )->GetStringUTFChars( env, endTime, 0 );
    char* cmdUrl = ( char* )strUrl;
    char* cmdDst = ( char* )strDst;
    char* cmdStartTime = ( char* )strStartTime;
    char* cmdEndTime = ( char* )strEndTime;
    char* commands[ 10 ];
    char** argv = &commands[ 0 ];

    commands[ 0 ] = "ffmpeg";
    commands[ 1 ] = "-i";
    commands[ 2 ] = cmdUrl;
    commands[ 3 ] = "-ss";
    commands[ 4 ] = cmdStartTime;
    commands[ 5 ] = "-t";
    commands[ 6 ] = cmdEndTime;
    commands[ 7 ] = "-c";
    commands[ 8 ] = "copy";
    commands[ 9 ] = cmdDst;
    execute( 10, argv );

    return 0;
}

JNIEXPORT jint JNICALL Java_com_squareround_meistertranslator_FFMPEGLinker_audioCutFront( JNIEnv* env, jobject instance, jstring url, jstring dst, jstring time ) {
    const char* strUrl = ( *env )->GetStringUTFChars( env, url, 0 );
    const char* strDst = ( *env )->GetStringUTFChars( env, dst, 0 );
    const char* strTime = ( *env )->GetStringUTFChars( env, time, 0 );
    char* cmdUrl = ( char* )strUrl;
    char* cmdDst = ( char* )strDst;
    char* cmdTime = ( char* )strTime;
    char* commands[ 10 ];
    char** argv = &commands[ 0 ];

    commands[ 0 ] = "ffmpeg";
    commands[ 1 ] = "-i";
    commands[ 2 ] = cmdUrl;
    commands[ 3 ] = "-ss";
    commands[ 4 ] = cmdTime;
    commands[ 5 ] = "-c";
    commands[ 6 ] = "copy";
    commands[ 7 ] = cmdDst;
    execute( 8, argv );

    return 0;
}

JNIEXPORT jdouble JNICALL Java_com_squareround_meistertranslator_FFMPEGLinker_audioCutBack( JNIEnv* env, jobject instance, jstring url, jstring dst, jstring time ) {
    const char* strUrl = ( *env )->GetStringUTFChars( env, url, 0 );
    const char* strDst = ( *env )->GetStringUTFChars( env, dst, 0 );
    const char* strTime = ( *env )->GetStringUTFChars( env, time, 0 );
    char* cmdUrl = ( char* )strUrl;
    char* cmdDst = ( char* )strDst;
    char* cmdTime = ( char* )strTime;
    char* commands[ 10 ];
    char** argv = &commands[ 0 ];
    double cutPosition = 0;
    char cmdCutPodition[ 10 ] = "";
    AVFormatContext* context = NULL;

    if( avformat_open_input( &context, cmdUrl, NULL, NULL ) < 0 )
    {
        LOGE( "Can't open input file '%s'", cmdUrl );
        ( *env )->ReleaseStringUTFChars( env, url, cmdUrl );
        return -1;
    }
    if( avformat_find_stream_info( context, NULL ) < 0 )
    {
        LOGE( "Failed to retrieve input stream information" );
        ( *env )->ReleaseStringUTFChars( env, url, cmdUrl );
        return -2;
    }
    cutPosition = strtod( cmdTime, NULL );
    cutPosition = ( ( context->duration / ( double )1000000 ) - cutPosition < 0 ) ? 0 : ( ( context->duration / ( double )1000000 ) - cutPosition );
    sprintf( cmdCutPodition, "%f", cutPosition );

    commands[ 0 ] = "ffmpeg";
    commands[ 1 ] = "-i";
    commands[ 2 ] = cmdUrl;
    commands[ 3 ] = "-t";
    commands[ 4 ] = cmdCutPodition;
    commands[ 5 ] = "-c";
    commands[ 6 ] = "copy";
    commands[ 7 ] = cmdDst;
    execute( 8, argv );

    return cutPosition;
}

JNIEXPORT jint JNICALL Java_com_squareround_meistertranslator_FFMPEGLinker_audioSplit( JNIEnv* env, jobject instance, jstring url, jstring time ) {
    const char* strUrl = ( *env )->GetStringUTFChars( env, url, 0 );
    const char* strDst = ( *env )->GetStringUTFChars( env, url, 0 );
    const char* strTime = ( *env )->GetStringUTFChars( env, time, 0 );
    char* cmdUrl = ( char* )strUrl;
    char* cmdDst = ( char* )strDst;
    char* cmdTime = ( char* )strTime;
    char* commands[ 10 ];
    char** argv = &commands[ 0 ];
    AVFormatContext* context = NULL;
    double cutNum = 0;

    strcat( cmdDst, "out%d.wav" );
    if( avformat_open_input( &context, cmdUrl, NULL, NULL ) < 0 )
    {
        LOGE( "Can't open input file '%s'", cmdUrl );
        ( *env )->ReleaseStringUTFChars( env, url, cmdUrl );
        return -1;
    }
    if( avformat_find_stream_info( context, NULL ) < 0 )
    {
        LOGE( "Failed to retrieve input stream information" );
        ( *env )->ReleaseStringUTFChars( env, url, cmdUrl );
        return -2;
    }
    cutNum = strtod( cmdTime, NULL );
    cutNum = ceil( context->duration / ( double )1000000 / cutNum );

    commands[ 0 ] = "ffmpeg";
    commands[ 1 ] = "-i";
    commands[ 2 ] = cmdUrl;
    commands[ 3 ] = "-f";
    commands[ 4 ] = "segment";
    commands[ 5 ] = "-segment_time";
    commands[ 6 ] = cmdTime;
    commands[ 7 ] = "-c";
    commands[ 8 ] = "copy";
    commands[ 9 ] = cmdDst;
    execute( 10, argv );

    return ( int )cutNum;
}
