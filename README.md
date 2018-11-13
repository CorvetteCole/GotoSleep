# GotoSleep

# TODO
## First Release Feature Locked TODO:
- Refactor settingsFragment code (things are called way more often than they need to be)

- General performance optimizations

- See if you can change the behavior of the in-app purchase pop up. right now it hides the status bar which sucks because it makes the entire app shift. If this can't be fixed oh well

- Fix bug where number of notifications to send isnt always disabled if smart notifications are on when you open settings.

- Fix bug where "clesr GDPR preferences" is visible to US users

- Fix bug where the rating box doesn't disappear smoothly

- Fix bug where minutes on the main screen can sometimes get cut off. set the side constraints to fixed and see if that fixes it

~~- Add in-app purchase for a donation in about page~~ **DONE**

~~- Add separate content_main layout for devices with a low DPI so that the text background looks right~~ **DONE**

~~- Make edit bedtime button disappear after returning from settings for the first time~~ **DONE**

~~- Speed up launch of MainActivity after pressing the DONE button in Intro slides~~ **DONE** (further optimization may still be possible)

~~- Add setting for do not disturb delay~~ **DONE**

~~- Edit settings order so perhaps the persistent notification option will go in to the notification category but will say it requires ads enabled or the in app purchase in the summary when neither exists~~ **DONE** (tested but decided against)

~~- If notification minute is 1, say minute in notification vs minutes~~ **DONE**

~~- Add broadcastreceiver to run when phone boots up so it can reset an alarm for the bedtime reminders~~ **DONE**

~~- Add credits for app icon, intro icons, open source libraries to about~~ **DONE**

~~- Re-do about page to use a custom layout vs the about page library it is using now~~ **DONE** (change not needed)

~~-Fix bug where minutes will equal 60 without adding to hours on the countdown. Experiment with using Math.round() with the hours finding function, otherwise just use and if statement to check if it is 60 and if so add 1 to hours~~ **DONE** 

~~-Refactor variables regarding request codes for alarms and notifications. Notification request codes are particularly important since they let you dismiss and interact with notifications in different classes.~~ **DONE**

~~-Add separate moon icon that is the right size for the notifications~~ **DONE** (not confirmed fixed)

~~-Implement smart persistent notifications (keeps notifiying as long as user continues to use their device)~~ **DONE** (Confirmed working)

~~- Fix bug where notification amount isn't always disabled when enabling persistent notifications~~ **DONE**

~~- Add setting to change GDPR preference. Make it only display if user is in the EU~~ **DONE**

~~- Add Google Admob consent SDK for European users (https://developers.google.com/admob/android/eu-consent)~~ **DONE**

~~- Constrain rating box to bottom of the sleepmessage so they dont interfere~~ **DONE**

~~-Add prompt to rate the app after it has been opened like 15 times or something~~ **DONE** 

~~- Investigate code to detect when a purchase has been refunded and disable the advanced options. **UPDATE:** still not sure how long it takes a revoked in-app purchase to be detected by the app code. Moving this to a lower priority~~ **DONE** (confirmed working, takes a couple of days to update though)



## Next Major Version Features (not confirmed):
-Animate stars to gently blink

- Animate moon and stars to fade away as the sun rises

- Animate background to shift color as the day gets brighter

- Add app shortcut for go to sleep early feature

- AMOLED black theme option (maybe in premium category? idk)

- Automatic day/night mode

- Remove hours countdown when it is 0 possibly

- Refactor styles xml and possibly colors xml so that they are better organized/labelled and more compatible with everything

- Refactor preferences code to reduce dependence on 3rd party libraries

- Add background themeing (maybe)

