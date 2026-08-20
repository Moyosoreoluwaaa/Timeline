# Timeline

Timeline is a passive digital activity journal that records how a person actually spends time on their phone, reconstructing their day as a visual, chronological record of application sessions.

## Language

**Timeline**:
The primary visual, chronological record of the user's digital day.
_Avoid_: Dashboard, activity log, overview

**Session**:
A continuous period spent inside a single application.
_Avoid_: Activity, usage block, event

**Activity Event**:
The fundamental data object recording an application transition, including start time, end time, and duration.
_Avoid_: Log entry, record

**Session Detail**:
A short, draggable bottom sheet containing information about a specific selected session.

**Application Timeline**:
The expanded state of the Session Detail bottom sheet, showing the complete history of a specific application.
_Avoid_: App history, app overview

**Tracking Service**:
The persistent background process (foreground service on Android) responsible for recording activity transitions.
_Avoid_: Monitor, recorder, background agent
