package com.recipez.core;

import javax.swing.SwingWorker;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class ApiTask {

    public static <T> void run(Callable<T> background,
                               Consumer<T> onSuccess,
                               Consumer<Throwable> onError) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return background.call();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (Exception e) {
                    Throwable cause = (e.getCause() != null) ? e.getCause() : e;
                    onError.accept(cause);
                }
            }
        }.execute();
    }
}
