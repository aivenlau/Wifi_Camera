
package com.joyhonest.wifination;

import android.media.AudioFormat;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

public interface AudioCodec {
    String MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AAC;
    int KEY_CHANNEL_COUNT = 1;
    int KEY_SAMPLE_RATE = 44100;
    int KEY_BIT_RATE = KEY_SAMPLE_RATE * KEY_CHANNEL_COUNT * 16;
    int KEY_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;

    int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    int CHANNEL_MODE =  (KEY_CHANNEL_COUNT ==1?AudioFormat.CHANNEL_IN_MONO:AudioFormat.CHANNEL_IN_STEREO);
    int BUFFFER_SIZE = 1024*2*KEY_CHANNEL_COUNT;

}
