# Fix CyberKitty Screen UI Mismatch

Align the CyberKitty screen UI with the provided screenshot, focusing on vertical centering, button shapes, and icon tints.

## User Review Required

> [!IMPORTANT]
> The vertical centering is achieved by removing the hardcoded `layout_marginTop` and using `gravity="center"` in a `LinearLayout`. This will make the layout more adaptive to different screen sizes.

## Proposed Changes

### Resources

#### [NEW] [bg_circle_white.xml](file:///D:/Downloads/CyberVerseAndroid/app/src/main/res/drawable/bg_circle_white.xml)
Create a white circular background for the back button.

### Layouts

#### [MODIFY] [activity_cyberkitty.xml](file:///D:/Downloads/CyberVerseAndroid/app/src/main/res/layout/activity_cyberkitty.xml)
- Change the back button background to `bg_circle_white`.
- Center the main content vertically.
- Add tints to the chat bar icons to match the screenshot.

## Verification Plan

### Automated Tests
- Run `analyze_file` on the modified XML to ensure no new errors are introduced.

### Manual Verification
- Render the preview of `activity_cyberkitty.xml` to verify the visual alignment.
