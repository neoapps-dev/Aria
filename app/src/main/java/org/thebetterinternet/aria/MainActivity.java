package org.thebetterinternet.aria;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;

import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.Autocomplete;
import org.mozilla.geckoview.BasicSelectionActionDelegate;
import org.mozilla.geckoview.ContentBlocking;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSession.*;
import org.mozilla.geckoview.GeckoSession.PermissionDelegate;
import org.mozilla.geckoview.GeckoSession.PermissionDelegate.ContentPermission;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.GeckoWebExecutor;
import org.mozilla.geckoview.Image;
import org.mozilla.geckoview.MediaSession;
import org.mozilla.geckoview.OrientationController;
import org.mozilla.geckoview.SlowScriptResponse;
import org.mozilla.geckoview.TranslationsController;
import org.mozilla.geckoview.WebExtension;
import org.mozilla.geckoview.WebExtensionController;
import org.mozilla.geckoview.WebNotification;
import org.mozilla.geckoview.WebNotificationDelegate;
import org.mozilla.geckoview.WebRequest;
import org.mozilla.geckoview.WebRequestError;
import org.mozilla.geckoview.WebResponse;
import com.google.android.material.card.MaterialCardView;
import java.lang.*;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private GeckoView geckoView;
    private GeckoSession geckoSession;
    private EditText urlBar;
    private MaterialCardView bottomBar;
    private ImageButton goButton, backButton, forwardButton, homeButton, refreshButton;
    private ImageView lockIcon;
    private final String defaultUrl = "https://www.google.com";
    private static GeckoRuntime runtime;
    private boolean mCanGoBack;
    private boolean mCanGoForward;
    private LinearLayout tabBar;
    private List<GeckoSession> tabs = new ArrayList<>();
    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        geckoView = findViewById(R.id.geckoView);
        bottomBar = findViewById(R.id.bottomBar);
        urlBar = findViewById(R.id.urlBar);
        goButton = findViewById(R.id.goButton);
        backButton = findViewById(R.id.btnBack);
        forwardButton = findViewById(R.id.btnForward);
        homeButton = findViewById(R.id.btnHome);
        refreshButton = findViewById(R.id.btnRefresh);
        lockIcon = findViewById(R.id.lockIcon);
        tabBar = findViewById(R.id.tabBar);

        bottomBar.setAlpha(0.95f); // Ensure visibility
        findViewById(R.id.topBar).setAlpha(0.95f); // Ensure visibility

        initGecko();
        setupListeners();
        setupTabBar();
    }

    private void initGecko() {
        if (runtime == null) {
            GeckoRuntimeSettings.Builder runtimeSettings = new GeckoRuntimeSettings.Builder()
                .contentBlocking(new ContentBlocking.Settings.Builder()
                    .antiTracking(ContentBlocking.AntiTracking.DEFAULT)
                    .cookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_NON_TRACKERS)
                    .build())
                .aboutConfigEnabled(true);
            
            runtime = GeckoRuntime.create(this, runtimeSettings.build());
        }
        
        addNewTab(defaultUrl);
    }

    private void addNewTab(String url) {
        GeckoSessionSettings settings = new GeckoSessionSettings.Builder()
            .usePrivateMode(false)
            .useTrackingProtection(true)
            .build();
        GeckoSession session = new GeckoSession(settings);
        session.setContentDelegate(createContentDelegate(bottomBar));
        session.setNavigationDelegate(createNavigationDelegate());
        session.open(runtime);
        session.loadUri(url);

        tabs.add(session);
        switchToTab(tabs.size() - 1);
        updateTabBar();
    }

    private void switchToTab(int index) {
        currentTab = index;
        geckoSession = tabs.get(index);
        geckoView.setSession(geckoSession);
        urlBar.setText(geckoSession.getCurrentUri() != null ? geckoSession.getCurrentUri() : defaultUrl);
        updateNavButtons();
    }

    private void setupTabBar() {
        updateTabBar();
    }

    private void updateTabBar() {
        tabBar.removeAllViews();
        for (int i = 0; i < tabs.size(); i++) {
            ImageButton tabButton = new ImageButton(this);
            tabButton.setImageResource(R.drawable.ic_tab); // Use your tab icon
            tabButton.setBackgroundResource(android.R.color.transparent);
            int idx = i;
            tabButton.setOnClickListener(v -> switchToTab(idx));
            tabBar.addView(tabButton);
        }
        // "+" button to add new tab
        ImageButton addTabButton = new ImageButton(this);
        addTabButton.setImageResource(R.drawable.ic_add); // Use your add icon
        addTabButton.setBackgroundResource(android.R.color.transparent);
        addTabButton.setOnClickListener(v -> addNewTab(defaultUrl));
        tabBar.addView(addTabButton);
    }

    private GeckoSession.ContentDelegate createContentDelegate(MaterialCardView bottomBar) {
        return new GeckoSession.ContentDelegate() {
            private MaterialCardView topBar = findViewById(R.id.topBar);

            @Override
            public void onFirstComposite(GeckoSession session) {
                geckoView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    int dy = scrollY - oldScrollY;
                    if (Math.abs(dy) < 30) return;

                    float translationDistance = bottomBar.getHeight(); // Use consistent height
                    runOnUiThread(() -> {
                        if (dy > 0) {
                            bottomBar.animate().translationY(translationDistance).alpha(0f).setDuration(200).start();
                            topBar.animate().translationY(-topBar.getHeight()).alpha(0f).setDuration(200).start();
                        } else {
                            bottomBar.animate().translationY(0f).alpha(0.95f).setDuration(200).start();
                            topBar.animate().translationY(0f).alpha(0.95f).setDuration(200).start();
                        }
                    });
                });
            }
        };
    }

    private GeckoSession.NavigationDelegate createNavigationDelegate() {
        return new GeckoSession.NavigationDelegate() {
            public void onLocationChange(GeckoSession session, String url, List<GeckoSession.PermissionDelegate.ContentPermission> perms, boolean hasUserGesture) {
                runOnUiThread(() -> {
                    urlBar.setText(url);
                    updateNavButtons();
                });
            }
            
            @Override
            public void onCanGoBack(GeckoSession session, boolean canGoBack) {
                mCanGoBack = canGoBack;
                runOnUiThread(() -> {
                    backButton.setEnabled(canGoBack);
                    backButton.setAlpha(canGoBack ? 1.0f : 0.5f);
                });
            }
            
            @Override
            public void onCanGoForward(GeckoSession session, boolean canGoForward) {
                mCanGoForward = canGoForward;
                runOnUiThread(() -> {
                    forwardButton.setEnabled(canGoForward);
                    forwardButton.setAlpha(canGoForward ? 1.0f : 0.5f);
                });
            }
        };
    }

    private void setupListeners() {
        urlBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || 
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                loadUrl(urlBar.getText().toString());
                return true;
            }
            return false;
        });

        goButton.setOnClickListener(v -> loadUrl(urlBar.getText().toString()));
        
        backButton.setOnClickListener(v -> {
            if (mCanGoBack) {
                geckoSession.goBack();
            }
        });
        
        forwardButton.setOnClickListener(v -> {
            if (mCanGoForward) {
                geckoSession.goForward();
            }
        });
        
        homeButton.setOnClickListener(v -> {
            geckoSession.loadUri(defaultUrl);
            urlBar.setText(defaultUrl);
        });
        
        refreshButton.setOnClickListener(v -> geckoSession.reload());
    }
    
    private void loadUrl(String input) {
        if (input.isEmpty()) return;
        
        String url = input.trim();
        if (!URLUtil.isValidUrl(url) && !url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://www.google.com/search?q=" + url.replace(" ", "+");
        } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        
        geckoSession.loadUri(url);
        urlBar.clearFocus(); // Clear focus to hide the keyboard
    }
    
    private void updateNavButtons() {
        runOnUiThread(() -> {
            setButtonState(backButton, mCanGoBack);
            setButtonState(forwardButton, mCanGoForward);
        });
    }

    private void setButtonState(ImageButton button, boolean isEnabled) {
        button.setEnabled(isEnabled);
        button.setAlpha(isEnabled ? 1.0f : 0.5f); // Adjust alpha for visual feedback
    }
    
    @Override
    protected void onPause() {
        geckoSession.setActive(false);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        geckoSession.setActive(true);
    }
    
    @Override
    public void onBackPressed() {
        if (mCanGoBack) {
            geckoSession.goBack();
        } else {
            finish();
        }
    }
}