# GotoSleep

# TODO
First Release Feature Locked:

-Fix bug where minutes will equal 60 without adding to hours on the countdown. Experiment with using Math.round() with the hours finding function, otherwise just use and if statement to check if it is 60 and if so add 1 to hours.

-Investigate bug where turning off ads on 5.0 and possibly above does not disable the persistent notifications option.

-Investigate code to detect when a purchase has been refunded and disable the in-app purchase option.

-Add smart "I'm going to sleep" button where edit bedtime button appears that will appear 1 hour before your bedtime. When clicked it will enable do not disturb and reset next notification to tomorrow.

-Add automatic do not disturb. After the last notification or user clicking the im going to sleep button DnD will be enabled.

-Add notification button. If you click the "I'm going to sleep" button or whatever it will be called on the notification it will stop notifications, enable do not disturb mode, set next notification to tomorrow, and maybe turn off the device screen.

-Implement smart persistent notifications (keeps notifiying as long as user continues to use their device)

-Add admob advertisements BE CAREFUL NOT TO REVEAL PRIVATE KEY INFO IN GITHUB REPO

-Add credits for app icon, intro icons, open source libraries, etc


Next Major Version Features:

-AMOLED black theme option (maybe in premium category? idk)

-Refactor styles xml and possibly colors xml so that they are better organized/labelled and more compatible with everything

-Refactor preferences code to reduce dependence on 3rd party libraries

-Add background themeing (maybe)

