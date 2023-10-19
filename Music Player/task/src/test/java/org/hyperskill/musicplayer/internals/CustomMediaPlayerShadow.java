package org.hyperskill.musicplayer.internals;

import static org.junit.Assert.assertEquals;
import static org.robolectric.shadows.ShadowMediaPlayer.State.INITIALIZED;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.SurfaceHolder;

import org.hyperskill.musicplayer.R;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowMediaPlayer;
import org.robolectric.shadows.util.DataSource;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
// version 1.3
@Implements(MediaPlayer.class)
public class CustomMediaPlayerShadow extends ShadowMediaPlayer {

    private static SongFake fakeSong = new SongFake(
            -1,
            "Guggenheim grotto",
            "Wisdom",
            215_000
    );

    public static boolean wasResetOrRecreated = false;


    @Implementation
    public static MediaPlayer create(Context context, int resid){
        if(resid == R.raw.wisdom) {
            DataSource dataSource = DataSource.toDataSource(String.valueOf(resid));
            MediaInfo info = new MediaInfo(fakeSong.getDuration(), 0);

            addMediaInfo(dataSource, info);

            MediaPlayer mediaPlayer = new MediaPlayer();
            ShadowMediaPlayer shadow = Shadow.extract(mediaPlayer);
            try {
                shadow.setDataSource(dataSource);
                shadow.setState(INITIALIZED);
                mediaPlayer.prepare();
            } catch (Exception e) {
                return null;
            }
            wasResetOrRecreated = true;
            return mediaPlayer;
        }
        throw new AssertionError("invalid resid provided to MediaPlayer.create(Context context, int resid)");
    }

    @Implementation
    protected static MediaPlayer create(
            Context context, int resid, AudioAttributes audioAttributes, int audioSessionId) {

        return create(context, resid);
    }

    @Implementation
    protected static MediaPlayer create(Context context, Uri trackUri){
        if(trackUri.getPath().equals("/raw/wisdom")) {
            return create(context, R.raw.wisdom);
        }

        Uri expectedSongUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                fakeSong.getId()
        );

        assertEquals("Media player created with incorrect uri", expectedSongUri.getPath(), trackUri.getPath());

        DataSource dataSource = DataSource.toDataSource(context, trackUri);

        MediaInfo info = new MediaInfo((int) fakeSong.getDuration(), 0);
        addMediaInfo(dataSource, info);

        MediaPlayer mediaPlayer = new MediaPlayer();
        ShadowMediaPlayer shadow = Shadow.extract(mediaPlayer);
        try {
            shadow.setDataSource(dataSource);
            shadow.setState(INITIALIZED);
            mediaPlayer.prepare();
        } catch (Exception e) {
            return null;
        }
        wasResetOrRecreated = true;
        return mediaPlayer;
    }

    @Implementation
    protected static MediaPlayer create(Context context, Uri uri, SurfaceHolder holder) {
        return create(context, uri);
    }

    @Implementation
    protected static MediaPlayer create(Context context, Uri uri, SurfaceHolder holder,
                                        AudioAttributes audioAttributes, int audioSessionId) {
        return create(context, uri);
    }

    @Implementation
    @Override
    protected void setDataSource(
            Context context,
            Uri trackUri,
            Map<String, String> headers,
            List<HttpCookie> cookies) throws IOException {

        if(trackUri.getPath().equals("/raw/wisdom")) {
            DataSource dataSource = DataSource.toDataSource(context, trackUri);
            MediaInfo info = new MediaInfo(fakeSong.getDuration(), 0);
            addMediaInfo(dataSource, info);
        } else {
            Uri expectedSongUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    fakeSong.getId()
            );

            assertEquals(
                    "Media player created with incorrect uri",
                    expectedSongUri.getPath(), trackUri.getPath()
            );

            DataSource dataSource = DataSource.toDataSource(context, trackUri);
            MediaInfo info = new MediaInfo((int) fakeSong.getDuration(), 0);
            addMediaInfo(dataSource, info);
        }

        super.setDataSource(context, trackUri, headers, cookies);
    }

    @Implementation
    @Override
    protected void setDataSource(Context context, Uri trackUri) throws IOException {
        setDataSource(context, trackUri, null, null);
    }

    @Implementation
    @Override
    protected void setDataSource(
            Context context, Uri uri, Map<String, String> headers) throws IOException {
        setDataSource(context, uri, null, null);
    }

    @Implementation
    @Override
    protected void setDataSource(String uri, Map<String, String> headers) throws IOException {
        throw new AssertionError(
                "tests do not support the method " +
                        ".setDataSource(String uri, Map<String, String> headers), " +
                        "use .setDataSource(Context context, Uri uri) instead"
        );
    }

    @Implementation
    @Override
    protected void setDataSource(FileDescriptor fd, long offset, long length) throws IOException {
        throw new AssertionError(
                "tests do not support the method " +
                        ".setDataSource(FileDescriptor fd, long offset, long length), " +
                        "use .setDataSource(Context context, Uri uri) instead"
        );
    }

    @Implementation
    @Override
    protected void setDataSource(MediaDataSource mediaDataSource) throws IOException {
        throw new AssertionError(
                "tests do not support the method " +
                        ".setDataSource(MediaDataSource mediaDataSource), " +
                        "use .setDataSource(Context context, Uri uri) instead"
        );
    }

    @Implementation
    @Override
    protected void setDataSource(AssetFileDescriptor assetFileDescriptor) throws IOException {
        throw new AssertionError(
                "tests do not support the method " +
                        ".setDataSource(AssetFileDescriptor assetFileDescriptor), " +
                        "use .setDataSource(Context context, Uri uri) instead"
        );
    }

    @Implementation
    @Override
    protected void _reset() {
        wasResetOrRecreated = true;
        super._reset();
    }

    public static void setFakeSong(SongFake fakeSong) {
        CustomMediaPlayerShadow.fakeSong = fakeSong;
    }
}