# [WIP]

## Fixed
- **Scroll Behavior**: Fixed inconsistent animations for `topBar` and `bottomBar` during scrolling by ensuring consistent height values are used.
- **EditText Focus**: Resolved issue where the `urlBar` retained focus after loading a URL, causing the keyboard to remain open.
- **Button States**: Improved visual feedback for `backButton` and `forwardButton` by adjusting their alpha values when disabled.
- **Layout Overlap**: Fixed `GeckoView` overlapping with `topBar` and `bottomBar` by adjusting margins in `activity_main.xml`.
- **Button Click Feedback**: Added ripple effect to all buttons for better visual feedback on click.
- **Accessibility**: Added `contentDescription` attributes to buttons and other interactive elements to improve accessibility.
- **Initial Visibility**: Ensured `topBar` and `bottomBar` are fully visible when the app starts by setting their alpha values during initialization.

## Added
- **Strings for Accessibility**: Added new strings in `strings.xml` for button descriptions to enhance accessibility support.
