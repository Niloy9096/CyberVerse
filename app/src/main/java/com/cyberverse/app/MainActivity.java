package com.cyberverse.app;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberverse.app.BuildConfig;
import com.cyberverse.app.network.GroqRequest;
import com.cyberverse.app.repository.CyberKittyRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int HOME = 0;
    private static final int SHOP = 1;
    private static final int FREE = 2;
    private static final int PAID = 3;
    private static final int CERTIFICATION = 4;
    private static final int COURSE_DETAIL = 5;
    private static final int PRODUCT_DETAIL = 6;
    private static final int CYBERKITTY = 7;
    private static final int PROFILE = 8;
    private static final int LOGIN = 9;
    private static final int SIGNUP = 10;

    private int currentScreen = HOME;
    private FirebaseAuth mAuth;

    private static final String CYBERKITTY_SYSTEM_PROMPT =
            "You are CyberKitty, an intelligent, friendly, and helpful AI assistant for CyberVerse. " +
            "You assist students and users with cybersecurity, ethical hacking, programming, computer science, study guides, course information, and general technology topics. " +
            "Be natural, conversational, and encouraging. Keep your responses concise for simple questions, and detailed when asked for explanations or step-by-step guidance. " +
            "If you do not know something, clearly say so. Never claim to have performed actions or tasks that you did not perform.";

    private List<ChatMessage> chatMessages = new ArrayList<>();

    private static final String CHAT_PREFS = "cyberkitty_chat_prefs";
    private static final String KEY_CHAT_SESSIONS = "chat_sessions_json";

    public static class ChatMessage {
        public String text;
        public boolean isUser;

        public ChatMessage(String text, boolean isUser) {
            this.text = text;
            this.isUser = isUser;
        }
    }

    public static class ChatSession {
        public String id;
        public String title;
        public long timestamp;
        public List<ChatMessage> messages;

        public ChatSession(String id, String title, long timestamp, List<ChatMessage> messages) {
            this.id = id;
            this.title = title;
            this.timestamp = timestamp;
            this.messages = (messages != null) ? messages : new ArrayList<>();
        }
    }

    private List<ChatSession> chatSessions = new ArrayList<>();
    private ChatSession currentChatSession = null;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        showScreen(HOME);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentScreen == CYBERKITTY) {
                    View overlay = findViewById(R.id.chatHistoryOverlay);
                    if (overlay != null && overlay.getVisibility() == View.VISIBLE) {
                        overlay.setVisibility(View.GONE);
                        return;
                    }
                }
                if (currentScreen == HOME) {
                    finish();
                } else {
                    showScreen(HOME);
                }
            }
        });
    }

    private void loadChatSessionsFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(CHAT_PREFS, MODE_PRIVATE);
        String json = prefs.getString(KEY_CHAT_SESSIONS, null);
        chatSessions.clear();
        if (!TextUtils.isEmpty(json)) {
            try {
                Type type = new TypeToken<List<ChatSession>>(){}.getType();
                List<ChatSession> loaded = gson.fromJson(json, type);
                if (loaded != null) {
                    chatSessions.addAll(loaded);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveChatSessionsToPrefs() {
        SharedPreferences prefs = getSharedPreferences(CHAT_PREFS, MODE_PRIVATE);
        try {
            String json = gson.toJson(chatSessions);
            prefs.edit().putString(KEY_CHAT_SESSIONS, json).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initCurrentChatSession() {
        loadChatSessionsFromPrefs();
        if (!chatSessions.isEmpty()) {
            if (currentChatSession == null) {
                currentChatSession = chatSessions.get(0);
            } else {
                boolean found = false;
                for (ChatSession s : chatSessions) {
                    if (s.id.equals(currentChatSession.id)) {
                        currentChatSession = s;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    currentChatSession = chatSessions.get(0);
                }
            }
        } else {
            createNewChatSession();
            return;
        }

        chatMessages.clear();
        if (currentChatSession.messages != null) {
            chatMessages.addAll(currentChatSession.messages);
        }
    }

    private void createNewChatSession() {
        String newId = UUID.randomUUID().toString();
        ChatSession session = new ChatSession(newId, getString(R.string.default_chat_title), System.currentTimeMillis(), new ArrayList<>());
        chatSessions.add(0, session);
        currentChatSession = session;
        chatMessages.clear();
        saveChatSessionsToPrefs();
    }

    private void saveCurrentSessionState() {
        if (currentChatSession != null) {
            currentChatSession.messages = new ArrayList<>(chatMessages);
            currentChatSession.timestamp = System.currentTimeMillis();

            if (getString(R.string.default_chat_title).equals(currentChatSession.title) || TextUtils.isEmpty(currentChatSession.title)) {
                for (ChatMessage msg : chatMessages) {
                    if (msg.isUser && !TextUtils.isEmpty(msg.text)) {
                        String t = msg.text.trim();
                        if (t.length() > 25) {
                            t = t.substring(0, 25) + "...";
                        }
                        currentChatSession.title = t;
                        break;
                    }
                }
            }
            saveChatSessionsToPrefs();
        }
    }

    private void showScreen(int screen) {
        currentScreen = screen;

        switch (screen) {
            case SHOP:
                setContentView(R.layout.activity_shop);
                bindBottomNavigation();
                bindShopCards();
                bindKittyFab();
                break;
            case FREE:
                setContentView(R.layout.activity_free_courses);
                bindBottomNavigation();
                bindCourseCards();
                bindKittyFab();
                break;
            case PAID:
                setContentView(R.layout.activity_paid_courses);
                bindBottomNavigation();
                bindCourseCards();
                bindKittyFab();
                break;
            case CERTIFICATION:
                setContentView(R.layout.activity_certification);
                bindBottomNavigation();
                bindCertification();
                bindKittyFab();
                break;
            case COURSE_DETAIL:
                setContentView(R.layout.activity_course_detail);
                bindBottomNavigation();
                bindCourseDetail();
                break;
            case PRODUCT_DETAIL:
                setContentView(R.layout.activity_product_detail);
                bindBottomNavigation();
                bindProductDetail();
                break;
            case CYBERKITTY:
                setContentView(R.layout.activity_cyberkitty);
                bindKittyScreen();
                break;
            case PROFILE:
                setContentView(R.layout.activity_profile);
                bindBottomNavigation();
                bindProfile();
                bindKittyFab();
                break;
            case LOGIN:
                setContentView(R.layout.activity_login);
                bindLogin();
                break;
            case SIGNUP:
                setContentView(R.layout.activity_signup);
                bindSignup();
                break;
            default:
                setContentView(R.layout.activity_home);
                bindHome();
                break;
        }
    }

    private void bindHome() {
        ImageButton menu = findViewById(R.id.menuButton);
        LinearLayout drawer = findViewById(R.id.drawer);

        menu.setOnClickListener(v -> drawer.setVisibility(
                drawer.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
        ));

        View closeDrawer = findViewById(R.id.closeDrawer);
        if (closeDrawer != null) closeDrawer.setOnClickListener(v -> drawer.setVisibility(View.GONE));

        findViewById(R.id.navShop).setOnClickListener(v -> showScreen(SHOP));
        findViewById(R.id.navHome).setOnClickListener(v -> showScreen(HOME));

        findViewById(R.id.kittyFab).setOnClickListener(v -> showScreen(CYBERKITTY));
        findViewById(R.id.talkToKitty).setOnClickListener(v -> showScreen(CYBERKITTY));

        findViewById(R.id.drawerShop).setOnClickListener(v -> showScreen(SHOP));
        findViewById(R.id.drawerDevices).setOnClickListener(v -> showScreen(SHOP));
        findViewById(R.id.drawerFree).setOnClickListener(v -> showScreen(FREE));
        findViewById(R.id.drawerPaid).setOnClickListener(v -> showScreen(PAID));
        findViewById(R.id.drawerCertification).setOnClickListener(v -> showScreen(CERTIFICATION));
        findViewById(R.id.drawerClaim).setOnClickListener(v -> showScreen(CERTIFICATION));

        findViewById(R.id.btnAllHome).setOnClickListener(v -> showScreen(HOME));
        findViewById(R.id.btnFreeHome).setOnClickListener(v -> showScreen(FREE));
        findViewById(R.id.btnShopHome).setOnClickListener(v -> showScreen(SHOP));
        findViewById(R.id.btnLearnHome).setOnClickListener(v -> showScreen(PAID));


        View hackrf = findViewById(R.id.itemHackRFHome);
        View ubertooth = findViewById(R.id.itemUbertoothHome);
        View hackypi = findViewById(R.id.itemHackyPiHome);
        View kali = findViewById(R.id.itemKaliHome);

        View[] productViews = {hackrf, ubertooth, hackypi, kali};
        for (View v : productViews) {
            if (v != null) v.setOnClickListener(view -> showScreen(PRODUCT_DETAIL));
        }

        View heroContainer = findViewById(R.id.heroContainer);
        if (heroContainer != null) heroContainer.setOnClickListener(v -> showScreen(SHOP));

        View heroShop = findViewById(R.id.heroShopButton);
        if (heroShop != null) heroShop.setOnClickListener(v -> showScreen(SHOP));

        View heroLearn = findViewById(R.id.heroLearnButton);
        if (heroLearn != null) heroLearn.setOnClickListener(v -> showScreen(PAID));
    }

    private void bindBottomNavigation() {
        View home = findViewById(R.id.navHome);
        View shop = findViewById(R.id.navShop);
        View profile = findViewById(R.id.navProfile);

        if (home != null) home.setOnClickListener(v -> showScreen(HOME));
        if (shop != null) shop.setOnClickListener(v -> showScreen(SHOP));
        if (profile != null) profile.setOnClickListener(v -> showScreen(PROFILE));
    }

    private static final String PREFS_NAME = "cyberverse_user_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    private boolean isUserLoggedIn() {
        if (mAuth.getCurrentUser() != null) {
            return true;
        }
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private void saveUserLocalAccount(String name, String email, String password) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString("account_name_" + email.toLowerCase(), name)
                .putString("account_password_" + email.toLowerCase(), password)
                .apply();
    }

    private boolean validateLocalLogin(String email, String password) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedPassword = prefs.getString("account_password_" + email.toLowerCase(), null);
        return savedPassword != null && savedPassword.equals(password);
    }

    private boolean hasLocalAccount(String email) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.contains("account_password_" + email.toLowerCase());
    }

    private String getLocalAccountName(String email) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString("account_name_" + email.toLowerCase(), email.split("@")[0]);
    }

    private void saveUserSession(String name, String email) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .apply();
    }

    private void clearUserSession() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .remove(KEY_IS_LOGGED_IN)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .apply();
        if (mAuth != null) {
            mAuth.signOut();
        }
    }

    private boolean isFirebaseConfigError(Exception e) {
        if (e == null) return false;
        String msg = e.getMessage();
        return msg != null && (msg.contains("API key not valid") || msg.contains("internal error") || msg.contains("An internal error has occurred"));
    }

    private void bindProfile() {
        View guest = findViewById(R.id.guestContainer);
        View user = findViewById(R.id.userContainer);
        View back = findViewById(R.id.profileBack);

        if (back != null) back.setOnClickListener(v -> showScreen(HOME));

        if (isUserLoggedIn()) {
            guest.setVisibility(View.GONE);
            user.setVisibility(View.VISIBLE);

            TextView profileUsername = findViewById(R.id.profileUsername);
            TextView profileEmail = findViewById(R.id.profileEmail);

            FirebaseUser currentUser = mAuth.getCurrentUser();
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            String displayName = currentUser != null ? currentUser.getDisplayName() : null;
            if (TextUtils.isEmpty(displayName)) {
                displayName = prefs.getString(KEY_USER_NAME, "Cyber Defender");
            }
            if (TextUtils.isEmpty(displayName)) {
                displayName = "Cyber Defender";
            }

            String email = currentUser != null ? currentUser.getEmail() : null;
            if (TextUtils.isEmpty(email)) {
                email = prefs.getString(KEY_USER_EMAIL, "defender@cyberverse.com");
            }

            if (profileUsername != null) {
                profileUsername.setText(displayName);
            }

            if (profileEmail != null) {
                profileEmail.setText(email);
            }

            findViewById(R.id.logoutButton).setOnClickListener(v -> {
                clearUserSession();
                showScreen(PROFILE);
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            });
        } else {
            guest.setVisibility(View.VISIBLE);
            user.setVisibility(View.GONE);
            findViewById(R.id.gotoLogin).setOnClickListener(v -> showScreen(LOGIN));
            findViewById(R.id.gotoSignup).setOnClickListener(v -> showScreen(SIGNUP));
        }
    }

    private void bindLogin() {
        findViewById(R.id.loginBack).setOnClickListener(v -> showScreen(PROFILE));
        findViewById(R.id.switchToSignup).setOnClickListener(v -> showScreen(SIGNUP));

        EditText emailField = findViewById(R.id.loginEmail);
        EditText passwordField = findViewById(R.id.loginPassword);
        View submitBtn = findViewById(R.id.loginSubmit);

        submitBtn.setOnClickListener(v -> {
            String email = emailField != null ? emailField.getText().toString().trim() : "";
            String password = passwordField != null ? passwordField.getText().toString().trim() : "";

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
                return;
            }

            submitBtn.setEnabled(false);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        submitBtn.setEnabled(true);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String name = (user != null && !TextUtils.isEmpty(user.getDisplayName())) ? user.getDisplayName() : email.split("@")[0];
                            saveUserLocalAccount(name, email, password);
                            saveUserSession(name, email);
                            Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                            showScreen(PROFILE);
                        } else {
                            Exception e = task.getException();
                            if (isFirebaseConfigError(e)) {
                                if (validateLocalLogin(email, password)) {
                                    String name = getLocalAccountName(email);
                                    saveUserSession(name, email);
                                    Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                    showScreen(PROFILE);
                                } else if (hasLocalAccount(email)) {
                                    Toast.makeText(this, "Login Failed: Incorrect password", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(this, "Login Failed: Account not found. Please Sign Up first.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                String errorMsg = e != null ? e.getMessage() : "Authentication failed.";
                                Toast.makeText(this, "Login Failed: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });
    }

    private void bindSignup() {
        findViewById(R.id.signupBack).setOnClickListener(v -> showScreen(PROFILE));
        findViewById(R.id.switchToLogin).setOnClickListener(v -> showScreen(LOGIN));

        EditText nameField = findViewById(R.id.signupName);
        EditText emailField = findViewById(R.id.signupEmail);
        EditText passwordField = findViewById(R.id.signupPassword);
        View submitBtn = findViewById(R.id.signupSubmit);

        submitBtn.setOnClickListener(v -> {
            String name = nameField != null ? nameField.getText().toString().trim() : "";
            String email = emailField != null ? emailField.getText().toString().trim() : "";
            String password = passwordField != null ? passwordField.getText().toString().trim() : "";

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            saveUserLocalAccount(name, email, password);

            submitBtn.setEnabled(false);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            saveUserSession(name, email);
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && !TextUtils.isEmpty(name)) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                                user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                    submitBtn.setEnabled(true);
                                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                    showScreen(PROFILE);
                                });
                            } else {
                                submitBtn.setEnabled(true);
                                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                showScreen(PROFILE);
                            }
                        } else {
                            submitBtn.setEnabled(true);
                            Exception e = task.getException();
                            if (isFirebaseConfigError(e)) {
                                saveUserSession(name, email);
                                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                showScreen(PROFILE);
                            } else {
                                String errorMsg = e != null ? e.getMessage() : "Registration failed.";
                                Toast.makeText(this, "Signup Failed: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });
    }

    private void bindKittyFab() {
        View kitty = findViewById(R.id.kittyFab);
        if (kitty != null) kitty.setOnClickListener(v -> showScreen(CYBERKITTY));
    }

    private void bindShopCards() {
        View back = findViewById(R.id.shopBack);
        if (back != null) back.setOnClickListener(v -> showScreen(HOME));

        View hackrf = findViewById(R.id.itemHackRF);
        View ubertooth = findViewById(R.id.itemUbertooth);
        View hackypi = findViewById(R.id.itemHackyPi);
        View kali = findViewById(R.id.itemKali);

        View[] views = {hackrf, ubertooth, hackypi, kali};
        for (View v : views) {
            if (v != null) v.setOnClickListener(view -> showScreen(PRODUCT_DETAIL));
        }
    }

    private void bindCourseCards() {
        View freeBack = findViewById(R.id.freeBack);
        View paidBack = findViewById(R.id.paidBack);
        if (freeBack != null) freeBack.setOnClickListener(v -> showScreen(HOME));
        if (paidBack != null) paidBack.setOnClickListener(v -> showScreen(HOME));

        View cs = findViewById(R.id.itemCourseCS);
        View pentest = findViewById(R.id.itemCoursePentest);
        View full = findViewById(R.id.itemCourseFull);
        View info = findViewById(R.id.itemCourseInfo);
        View skill = findViewById(R.id.itemCourseSkillLogic);
        View isc2 = findViewById(R.id.itemCourseISC2);
        View skill2 = findViewById(R.id.itemCourseSkillLogic2);
        View isc2_2 = findViewById(R.id.itemCourseISC2_2);

        View[] views = {cs, pentest, full, info, skill, isc2, skill2, isc2_2};
        for (View v : views) {
            if (v != null) v.setOnClickListener(view -> showScreen(COURSE_DETAIL));
        }
    }

    private void bindCertification() {
        View back = findViewById(R.id.certBack);
        if (back != null) back.setOnClickListener(v -> showScreen(HOME));

        View cert1 = findViewById(R.id.itemCert1);
        View cert2 = findViewById(R.id.certEthical);
        if (cert1 != null) cert1.setOnClickListener(v -> showScreen(COURSE_DETAIL));
        if (cert2 != null) cert2.setOnClickListener(v -> showScreen(COURSE_DETAIL));
    }

    private void bindCourseDetail() {
        findViewById(R.id.detailBack).setOnClickListener(v -> showScreen(CERTIFICATION));
        findViewById(R.id.learnMore).setOnClickListener(
                v -> Toast.makeText(this, "Course information", Toast.LENGTH_SHORT).show()
        );
        findViewById(R.id.takeExam).setOnClickListener(
                v -> Toast.makeText(this, "Exam started", Toast.LENGTH_SHORT).show()
        );
    }

    private void bindProductDetail() {
        findViewById(R.id.productBack).setOnClickListener(v -> showScreen(SHOP));
        findViewById(R.id.askCyberkitty).setOnClickListener(v -> showScreen(CYBERKITTY));
        findViewById(R.id.addToCart).setOnClickListener(
                v -> Toast.makeText(this, "HackRF One added to cart", Toast.LENGTH_SHORT).show()
        );
    }

    private void bindKittyScreen() {
        findViewById(R.id.kittyBack).setOnClickListener(v -> {
            saveCurrentSessionState();
            showScreen(HOME);
        });

        initCurrentChatSession();

        RecyclerView recyclerView = findViewById(R.id.chatRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ChatAdapter adapter = new ChatAdapter(chatMessages);
        recyclerView.setAdapter(adapter);
        if (!chatMessages.isEmpty()) {
            recyclerView.scrollToPosition(chatMessages.size() - 1);
        }

        View overlay = findViewById(R.id.chatHistoryOverlay);
        RecyclerView historyRecyclerView = findViewById(R.id.historyRecyclerView);
        TextView tvEmptyHistory = findViewById(R.id.tvEmptyHistory);

        Runnable refreshHistoryList = new Runnable() {
            @Override
            public void run() {
                if (chatSessions.isEmpty()) {
                    tvEmptyHistory.setVisibility(View.VISIBLE);
                    historyRecyclerView.setVisibility(View.GONE);
                } else {
                    tvEmptyHistory.setVisibility(View.GONE);
                    historyRecyclerView.setVisibility(View.VISIBLE);
                    historyRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    HistoryAdapter historyAdapter = new HistoryAdapter(chatSessions, new HistoryAdapter.OnHistoryClickListener() {
                        @Override
                        public void onItemClick(ChatSession session) {
                            saveCurrentSessionState();
                            currentChatSession = session;
                            chatMessages.clear();
                            if (session.messages != null) {
                                chatMessages.addAll(session.messages);
                            }
                            adapter.notifyDataSetChanged();
                            if (!chatMessages.isEmpty()) {
                                recyclerView.scrollToPosition(chatMessages.size() - 1);
                            }
                            overlay.setVisibility(View.GONE);
                        }

                        @Override
                        public void onDeleteClick(ChatSession session) {
                            chatSessions.remove(session);
                            saveChatSessionsToPrefs();
                            if (currentChatSession != null && currentChatSession.id.equals(session.id)) {
                                if (!chatSessions.isEmpty()) {
                                    currentChatSession = chatSessions.get(0);
                                    chatMessages.clear();
                                    if (currentChatSession.messages != null) {
                                        chatMessages.addAll(currentChatSession.messages);
                                    }
                                } else {
                                    createNewChatSession();
                                }
                                adapter.notifyDataSetChanged();
                            }
                            run();
                        }
                    });
                    historyRecyclerView.setAdapter(historyAdapter);
                }
            }
        };

        findViewById(R.id.kittyNewChat).setOnClickListener(v -> {
            saveCurrentSessionState();
            createNewChatSession();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, R.string.new_chat_started, Toast.LENGTH_SHORT).show();
            if (overlay.getVisibility() == View.VISIBLE) {
                overlay.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.kittyHistory).setOnClickListener(v -> {
            saveCurrentSessionState();
            refreshHistoryList.run();
            overlay.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.kittyCloseHistory).setOnClickListener(v -> overlay.setVisibility(View.GONE));

        findViewById(R.id.kittyNewChatDrawer).setOnClickListener(v -> {
            saveCurrentSessionState();
            createNewChatSession();
            adapter.notifyDataSetChanged();
            overlay.setVisibility(View.GONE);
            Toast.makeText(this, R.string.new_chat_started, Toast.LENGTH_SHORT).show();
        });

        View sendButton = findViewById(R.id.sendMessage);
        EditText input = findViewById(R.id.chatInputText);
        sendButton.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                chatMessages.add(new ChatMessage(text, true));
                adapter.notifyItemInserted(chatMessages.size() - 1);
                recyclerView.scrollToPosition(chatMessages.size() - 1);
                input.setText("");

                saveCurrentSessionState();
                sendMessageToAi(text, adapter, recyclerView, sendButton);
            }
        });
    }

    private void sendMessageToAi(String userText, ChatAdapter adapter, RecyclerView recyclerView, View sendButton) {
        if (sendButton != null) {
            sendButton.setEnabled(false);
            sendButton.setAlpha(0.5f);
        }

        ChatMessage thinkingMsg = new ChatMessage("CyberKitty is thinking...", false);
        chatMessages.add(thinkingMsg);
        adapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);

        List<GroqRequest.Message> historyMessages = new ArrayList<>();
        for (ChatMessage msg : chatMessages) {
            if (msg == thinkingMsg) continue;
            if (msg.text != null && !msg.text.trim().isEmpty()) {
                historyMessages.add(new GroqRequest.Message(
                        msg.isUser ? "user" : "assistant",
                        msg.text.trim()
                ));
            }
        }

        String apiKey = BuildConfig.GROQ_API_KEY;

        CyberKittyRepository.sendChatMessage(MainActivity.this, apiKey, CYBERKITTY_SYSTEM_PROMPT, historyMessages, new CyberKittyRepository.ResponseCallback() {
            @Override
            public void onSuccess(String replyText) {
                chatMessages.remove(thinkingMsg);
                chatMessages.add(new ChatMessage(replyText, false));
                adapter.notifyDataSetChanged();
                if (!chatMessages.isEmpty()) {
                    recyclerView.scrollToPosition(chatMessages.size() - 1);
                }

                if (sendButton != null) {
                    sendButton.setEnabled(true);
                    sendButton.setAlpha(1.0f);
                }
                saveCurrentSessionState();
            }

            @Override
            public void onError(int statusCode, String friendlyErrorMessage) {
                chatMessages.remove(thinkingMsg);

                String fallback = getCyberkittySmartResponse(userText);
                chatMessages.add(new ChatMessage(fallback, false));

                adapter.notifyDataSetChanged();
                if (!chatMessages.isEmpty()) {
                    recyclerView.scrollToPosition(chatMessages.size() - 1);
                }

                Toast.makeText(MainActivity.this, friendlyErrorMessage, Toast.LENGTH_LONG).show();

                if (sendButton != null) {
                    sendButton.setEnabled(true);
                    sendButton.setAlpha(1.0f);
                }
                saveCurrentSessionState();
            }
        });
    }

    private String getCyberkittySmartResponse(String prompt) {
        if (prompt == null) return "Meow! How can I assist your cybersecurity journey today?";
        String query = prompt.toLowerCase().trim();
        if (query.equals("hi") || query.equals("hello") || query.contains("hey") || query.contains("greetings")) {
            return "Meow! Hello there, Cyber Defender! How can I assist your security journey today? Feel free to ask me about cybersecurity concepts, ethical hacking, courses, or hardware tools like HackRF!";
        } else if (query.contains("hackrf") || query.contains("rf") || query.contains("sdr")) {
            return "The HackRF One is a versatile Software Defined Radio (SDR) transceiver operating from 1 MHz to 6 GHz. It's fantastic for RF analysis, signal replay, and wireless auditing!";
        } else if (query.contains("ubertooth") || query.contains("bluetooth")) {
            return "Ubertooth One is an open-source 2.4 GHz wireless development platform suitable for Bluetooth experimentation and packet sniffing.";
        } else if (query.contains("hackypi") || query.contains("raspberry")) {
            return "HackyPi is a compact, USB-based hardware hacking tool built for quick script injection, badUSB emulation, and cybersecurity learning!";
        } else if (query.contains("course") || query.contains("learn") || query.contains("certification")) {
            return "CyberVerse offers top cybersecurity courses! Check out our Available Courses section for Network Security, Ethical Hacking, and ISC2 Certification prep.";
        } else if (query.contains("who are you") || query.contains("what can you do")) {
            return "I'm CyberKitty! Your friendly CyberVerse AI assistant. I can guide you through cybersecurity topics, hardware tools, ethical hacking roadmaps, and course recommendations!";
        } else {
            return "Meow! In cybersecurity, continuous learning and hands-on practice with tools like Kali Linux, Wireshark, and Nmap are essential for defense. How can I help you explore \"" + prompt + "\"?";
        }
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
        private List<ChatMessage> messages;

        public ChatAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatMessage msg = messages.get(position);
            holder.text.setText(msg.text);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.text.getLayoutParams();
            if (msg.isUser) {
                params.gravity = Gravity.END;
                holder.text.setBackgroundResource(R.drawable.bg_chat_user);
                holder.text.setTextColor(getResources().getColor(R.color.cv_white));
            } else {
                params.gravity = Gravity.START;
                holder.text.setBackgroundResource(R.drawable.bg_chat_ai);
                holder.text.setTextColor(getResources().getColor(R.color.cv_text));
            }
            holder.text.setLayoutParams(params);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;
            ViewHolder(View v) {
                super(v);
                text = v.findViewById(R.id.messageText);
            }
        }
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<ChatSession> sessions;
        private OnHistoryClickListener listener;

        public interface OnHistoryClickListener {
            void onItemClick(ChatSession session);
            void onDeleteClick(ChatSession session);
        }

        public HistoryAdapter(List<ChatSession> sessions, OnHistoryClickListener listener) {
            this.sessions = sessions;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatSession session = sessions.get(position);
            holder.title.setText(session.title != null ? session.title : getString(R.string.default_chat_title));
            int msgCount = session.messages != null ? session.messages.size() : 0;
            String countStr = msgCount + (msgCount == 1 ? " message" : " messages");
            holder.subtitle.setText(countStr);

            holder.container.setOnClickListener(v -> listener.onItemClick(session));
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(session));
        }

        @Override
        public int getItemCount() {
            return sessions.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            View container;
            TextView title, subtitle;
            ImageButton btnDelete;

            ViewHolder(View v) {
                super(v);
                container = v.findViewById(R.id.itemHistoryContainer);
                title = v.findViewById(R.id.historyTitle);
                subtitle = v.findViewById(R.id.historySubtitle);
                btnDelete = v.findViewById(R.id.btnDeleteHistory);
            }
        }
    }
}
