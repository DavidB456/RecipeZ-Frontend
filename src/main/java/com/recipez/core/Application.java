package com.recipez.core;

import com.recipez.ui.AuthenticationUI;
import com.recipez.ui.DashboardUI;
import com.recipez.ui.Window;
import com.recipez.user.User;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level orchestrator. Holds the singleton session state
 * (active user + apiClient) and switches between the auth screen
 * and the dashboard.
 */
public class Application {

    /** Change this if the backend runs somewhere other than localhost:8080. */
    public static final String API_BASE_URL = "http://localhost:8080";

    public static Application activeInstance;
    public static User activeUser;          // populated after login (with full profile)
    public static ApiClient apiClient;

    private final Window window;
    private final CardLayout rootLayout;
    private final JPanel rootPanel;
    private final AuthenticationUI authUI;
    private final DashboardUI dashboardUI;

    public Application() {
        activeInstance = this;
        apiClient = new ApiClient(API_BASE_URL);

        rootLayout = new CardLayout();
        rootPanel = new JPanel(rootLayout);
        rootPanel.setBackground(Color.decode("#121417"));

        authUI = new AuthenticationUI();
        dashboardUI = new DashboardUI();

        rootPanel.add(authUI.getAuthenticationPanel(), "auth");
        rootPanel.add(dashboardUI.getMainPanel(), "dashboard");

        window = new Window("Recipez", 1200, 800);
        window.setContentPane(rootPanel);
        window.display();

        rootLayout.show(rootPanel, "auth");
    }

    public void loginUser(User user) {
        activeUser = user;
        dashboardUI.refreshRecipes();
        rootLayout.show(rootPanel, "dashboard");
    }

    public void logout() {
        activeUser = null;
        rootLayout.show(rootPanel, "auth");
    }
}
