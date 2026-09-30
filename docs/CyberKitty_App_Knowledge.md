# CyberVerse — CyberKitty App Knowledge Base

## 1. Application Overview
CyberVerse is a comprehensive Android application designed for cybersecurity education, ethical hacking learning roadmaps, certification preparation, and hardware security device discovery (such as HackRF One, Ubertooth One, and HackyPi). It includes built-in user authentication, course management, hardware shopping, and an intelligent AI assistant named CyberKitty.

## 2. Application Purpose
To provide learners, students, and cybersecurity professionals with a centralized platform to learn cybersecurity concepts, explore trusted hardware tools, prepare for certifications, and interact with an AI-powered assistant (CyberKitty) for guidance and tutoring.

## 3. Target Users
Students, IT professionals, hobbyists, ethical hackers, and cybersecurity enthusiasts seeking practical learning materials and hardware guides.

## 4. Core Features
- **CyberKitty AI Assistant**: Powered by Groq API (`openai/gpt-oss-20b`) with multi-turn chat history, persistent session storage, and new chat management.
- **Cybersecurity Courses**: Free and paid courses covering network security, penetration testing, and information security.
- **Hardware Shop & Showcase**: Detailed information on hardware auditing devices (HackRF One, Ubertooth One, HackyPi, Kali Linux Box).
- **Certification Prep**: ISC2 and ethical hacking certification courses and exam workflows.
- **User Authentication**: Firebase Auth and local credential management with login, signup, and profile management.

## 5. Complete Screen Inventory
1. **Home Screen**: `activity_home.xml` (Managed by `MainActivity.kt`) — Dashboard featuring top banner, quick category filters, featured products, navigation drawer, and bottom navigation.
2. **Shop Screen**: `activity_shop.xml` — Product catalog showing trusted security hardware.
3. **Free Courses Screen**: `activity_free_courses.xml` — Listing of free cybersecurity courses.
4. **Paid Courses Screen**: `activity_paid_courses.xml` — Listing of advanced paid cybersecurity courses.
5. **Certification Screen**: `activity_certification.xml` — ISC2 and professional certification prep.
6. **Course Detail Screen**: `activity_course_detail.xml` — Detailed course description, stats, learning objectives, and exam actions.
7. **Product Detail Screen**: `activity_product_detail.xml` — Product specs, pricing, add to cart, and "Ask Cyberkitty" shortcut.
8. **CyberKitty Chat Screen**: `activity_cyberkitty.xml` — Real-time AI chat with message bubbles, input field, new chat button, and chat history drawer.
9. **Profile Screen**: `activity_profile.xml` — User account details, session management, and logout/login actions.
10. **Login Screen**: `activity_login.xml` — Email & password login.
11. **Signup Screen**: `activity_signup.xml` — Account creation with full name, email, and password.

## 6. Navigation Structure
Splash / Launch
↓
Home Screen (`MainActivity`)
├── Shop / Devices Tab & Drawer Links
├── Free Courses Tab & Drawer Links
├── Paid Courses Tab & Drawer Links
├── Certification Tab & Drawer Links
├── Profile Tab
└── CyberKitty AI Chat (via Floating Action Button, Talk to Kitty buttons, or Product/Course prompts)
      ├── Chat View
      └── Chat History Drawer

## 7. User Workflows
- **Explore & Learn**: Open app → Browse Home / Courses → Select Course → View Course Details.
- **Hardware Discovery**: Browse Shop → Select Product (e.g. HackRF One) → View Specs → Ask CyberKitty about the device.
- **AI Tutoring**: Tap CyberKitty FAB or "Talk to Kitty" → Type question → Receive Groq AI response with multi-turn context retention → Access past chats via History drawer or start a New Chat.

## 8. Authentication
- Powered by Firebase Authentication (`FirebaseAuth`) with local fallback storage in `SharedPreferences` (`cyberverse_user_session`).
- Supports user login (`activity_login.xml`), user registration (`activity_signup.xml`), and secure logout.

## 9. User Profile
- Displays authenticated user name and email.
- Allows logging out and returning to guest or login state.

## 10. Learning Features
- Free and paid curated cybersecurity courses with descriptions, difficulty levels, learning hours, and ratings.

## 11. Quiz / Exam Features
- Course detail and certification screens include "Take Exam" and "Learn More" interactive placeholders.

## 12. CyberKitty AI
- **AI Provider**: Groq API
- **Endpoint**: `https://api.groq.com/openai/v1/chat/completions`
- **Model**: `openai/gpt-oss-20b`
- **Request Format**: JSON payload with `model` and `messages` (`system`, `user`, `assistant`).
- **Response Format**: `choices[0].message.content`.
- **History Management**: Stored locally in `SharedPreferences` (`cyberkitty_chat_prefs`) as JSON-serialized chat sessions.

## 13. Database / Data Storage
- Firebase Firestore / Auth for cloud account management.
- `SharedPreferences` for local user sessions (`cyberverse_user_session`) and chat session history (`cyberkitty_chat_prefs`).

## 14. APIs and External Services
- Groq Cloud API for AI Chat Completions.
- Firebase Authentication for user identity.

## 15. Important Business Rules
- Input messages cannot be empty.
- Send button disables during active network generation to prevent duplicate requests.
- Chat history is limited to last 12 messages per request payload to manage token limits.

## 16. Validation Rules
- Email and password fields require non-empty validation during login and signup.

## 17. Error Handling
- Network exceptions, timeouts, and HTTP error codes (400, 401, 403, 429, 500+, 404) are handled with user-friendly messages and Logcat diagnostics (`CyberKittyRepo`).

## 18. Permissions
- `<uses-permission android:name="android.permission.INTERNET" />`
- `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />`

## 19. Settings
- App theme configuration (`Theme.CyberVerse`).

## 20. Frequently Asked User Questions
- Q: How do I ask CyberKitty a question? A: Tap the CyberKitty floating action button from Home, Shop, or Courses.
- Q: Can I view past chats? A: Yes, tap the History icon in the top right of the CyberKitty screen.

## 21. Things CyberKitty Must Never Assume
- Never assume the user is logged in without checking session state.
- Never assume an API key is hardcoded in source code (it is loaded securely via `BuildConfig.GROQ_API_KEY`).

## 22. Source Code Reference Map
- **MainActivity**: `MainActivity.java`
- **Groq Repository**: `CyberKittyRepository.java`
- **Network Client**: `GroqClient.java`, `GroqApiService.java`
- **Layouts**: `activity_home.xml`, `activity_cyberkitty.xml`, etc.
