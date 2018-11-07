# GotoSleep

# TODO
First Release Feature Locked:

-Fix bug where minutes will equal 60 without adding to hours on the countdown. Experiment with using Math.round() with the hours finding function, otherwise just use and if statement to check if it is 60 and if so add 1 to hours.

-Refactor settingsFragment code (things are called way more often than they need to be).

-Refactor variables regarding request codes for alarms and notifications. Notification request codes are particularly important since they let you dismiss and interact with notifications in different classes.

-Investigate code to detect when a purchase has been refunded and disable the in-app purchase option.

-Add separate moon icon that is the right size for the notifications

-Implement smart persistent notifications (keeps notifiying as long as user continues to use their device)

-Add admob advertisements BE CAREFUL NOT TO REVEAL PRIVATE KEY INFO IN GITHUB REPO

-Add credits for app icon, intro icons, open source libraries, etc. Perhaps replace the Feedback button with Help or Info or About and have that open to a new activity?. Maybe add a 3rd button called About

-Add app shortcut for go to sleep early feature

-Add separate content_main layout for devices with a low DPI so that the text background looks right

-Add prompt to rate the app after it has been opened like 15 times or something



Next Major Version Features:

-AMOLED black theme option (maybe in premium category? idk)

-Refactor styles xml and possibly colors xml so that they are better organized/labelled and more compatible with everything

-Refactor preferences code to reduce dependence on 3rd party libraries

-Add background themeing (maybe)

