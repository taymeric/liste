# Grocery List

## Description
A simple Android app for managing a grocery list. 

### Main features
* The app's main screen contains a single list.
* Every element added to the list is also saved into a history.
* The user can then: edit the list, access the history, send a notification, send an email or access settings.

More information as well as screenshots on the Google Play Store page of the app (link below). 

### Implementation
The app was partly conceived for practicing Android development basics and its implementation contains:
* Activities as well as a Settings Fragment.
* RecyclerViews with Adapters to display and populate lists.
* A SQLite database with two tables for data storage, accessed through a Content Provider.
* The use of System Services like Alarm Manager and Notification Manager.
* A Broadcast Receiver to react to those alarm triggers.
* IntentService as well as Loaders to perform several background tasks.
* SharedPreferences to store and access users preferences.

## Installation
Two ways:
* Import the project into Android Studio and create your APK.

* <a href='https://play.google.com/store/apps/details?id=com.athebapps.android.list'>
  <img src='google-play-badge.png' height='80dp' alt='Get it on Google Play'>
</a>

## License
The contents of this repository are covered under the [MIT License](https://opensource.org/licenses/MIT).
