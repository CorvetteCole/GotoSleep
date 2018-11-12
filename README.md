# GotoSleep

# TODO
## First Release Feature Locked TODO:

~~-Fix bug where minutes will equal 60 without adding to hours on the countdown. Experiment with using Math.round() with the hours finding function, otherwise just use and if statement to check if it is 60 and if so add 1 to hours~~ **DONE** 

- Refactor settingsFragment code (things are called way more often than they need to be)

~~-Refactor variables regarding request codes for alarms and notifications. Notification request codes are particularly important since they let you dismiss and interact with notifications in different classes.~~ **DONE**

- Add broadcastreceiver to run when phone boots up so it can reset an alarm for the bedtime reminders 

~~-Add separate moon icon that is the right size for the notifications~~ **DONE** (not confirmed fixed)

~~-Implement smart persistent notifications (keeps notifiying as long as user continues to use their device)~~ **DONE** (Confirmed working)

- Fix bug where notification amount isn't always disabled when enabling persistent notifications

- Add setting for do not disturb delay

- Edit settings order so perhaps the persistent notification option will go in to the notification category but will say it requires ads enabled or the in app purchase in the summary when neither exists

- Add Google Admob consent SDK for European users (https://developers.google.com/admob/android/eu-consent)

- Re-do about page to use a custom layout vs the about page library it is using now

- Constrain rating box to bottom of the sleepmessage so they dont interfere

- If notification minute is 1, say minute in notification vs minutes

- Add credits for app icon, intro icons, open source libraries

- Add separate content_main layout for devices with a low DPI so that the text background looks right

~~-Add prompt to rate the app after it has been opened like 15 times or something~~ **DONE** (still needs polishing in terms of words)

- Make edit bedtime button disappear after returning from settings for the first time

- Add app shortcut for go to sleep early feature **UPDATE** moved to lower priority

~~- Investigate code to detect when a purchase has been refunded and disable the advanced options. **UPDATE:** still not sure how long it takes a revoked in-app purchase to be detected by the app code. Moving this to a lower priority~~ **DONE** (confirmed working, takes a couple of days to update though)



## Next Major Version Features (not confirmed):

-AMOLED black theme option (maybe in premium category? idk)

-Refactor styles xml and possibly colors xml so that they are better organized/labelled and more compatible with everything

-Refactor preferences code to reduce dependence on 3rd party libraries

-Add background themeing (maybe)

