# GotoSleep

# TODO
## First Release Feature Locked TODO:

~~-Fix bug where minutes will equal 60 without adding to hours on the countdown. Experiment with using Math.round() with the hours finding function, otherwise just use and if statement to check if it is 60 and if so add 1 to hours~~ **DONE**

-Refactor settingsFragment code (things are called way more often than they need to be)

~~-Refactor variables regarding request codes for alarms and notifications. Notification request codes are particularly important since they let you dismiss and interact with notifications in different classes.~~ **DONE**

-Add broadcastreceiver to run when phone boots up so it can reset an alarm for the bedtime reminders 

-Add separate moon icon that is the right size for the notifications

-Implement smart persistent notifications (keeps notifiying as long as user continues to use their device)

-Add credits for app icon, intro icons, open source libraries, etc. Perhaps replace the Feedback button with Help or Info or About and have that open to a new activity? Maybe add a 3rd button called About

-Add separate content_main layout for devices with a low DPI so that the text background looks right

-Add prompt to rate the app after it has been opened like 15 times or something

-Make edit bedtime button disappear after returning from settings for the first time

-Add app shortcut for go to sleep early feature **UPDATE** moved to lower priority

-Investigate code to detect when a purchase has been refunded and disable the advanced options. **UPDATE:** still not sure how long it takes a revoked in-app purchase to be detected by the app code. Moving this to a lower priority



## Next Major Version Features (not confirmed):

-AMOLED black theme option (maybe in premium category? idk)

-Refactor styles xml and possibly colors xml so that they are better organized/labelled and more compatible with everything

-Refactor preferences code to reduce dependence on 3rd party libraries

-Add background themeing (maybe)

