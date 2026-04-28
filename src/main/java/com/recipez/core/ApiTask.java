package com.recipez.core;

import javax.swing.SwingWorker;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Thin wrapper over SwingWorker so button handlers stay readable.
 *
 * Usage:
 *   ApiTask.run(
 *       () -> Application.apiClient.login(user, pass),     // runs on background thread
 *       response -> { ... update UI ... },                 // runs on EDT
 *       error    -> { ... show error dialog ... }          // runs on EDT
 *   );
 */
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
