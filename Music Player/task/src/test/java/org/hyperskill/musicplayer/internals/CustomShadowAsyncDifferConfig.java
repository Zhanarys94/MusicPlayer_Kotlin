package org.hyperskill.musicplayer.internals;

import android.os.Handler;
import android.os.Looper;

import androidx.recyclerview.widget.AsyncDifferConfig;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.concurrent.Executor;

@Implements(AsyncDifferConfig.class)
@SuppressWarnings({"unused"})
public class CustomShadowAsyncDifferConfig {

    public static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable r) {
            handler.post(r);
        }
    }
    Executor mainExecutor;

    @Implementation
    public Executor getBackgroundThreadExecutor() {
        if(mainExecutor == null) {
            mainExecutor = new MainThreadExecutor();
        }
        return mainExecutor;
    }
}
