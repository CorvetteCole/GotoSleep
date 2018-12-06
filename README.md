# GotoSleep
I created this open-source, mostly ad-free app not for money but because I saw a problem that needed solving. Ironically, programming the app that was meant to help me not stay up too late kept me up more times than it helped me sleep but I am extremely satisfied with the end product. There are definitely things that need improvement (please don't look at SettingsFragment, it is a nightmare. That is the next thing on my to-do list for next release), but overall it turned out very nice. I encourage you to look at the source code of this app and see if you can improve anything. I would be thrilled if even one person compiled my code from scratch and tried to contribute to this project.

If you do contribute to this app, email me at corvettecole@gmail.com with proof that you are the github user who committed changes and I will give you a promo code to unlock advanced options in the app for free.

You can download this project on the Google Play Store: https://play.google.com/store/apps/details?id=com.corvettecole.gotosleep

Visit my website: https://corvettecole.com

View the license: https://sleep.corvettecole.com/license

View the privacy policy: https://sleep.corvettecole.com/privacy

View the credits: https://sleep.corvettecole.com/credits

## Donate to me

Ethereum address: 0x8eFF5600A23708EFa475Be2C18892c9c0C43373B

PayPal: http://paypal.me/CGerdemann

Google Pay: corvettecole@gmail.com

# Things to Investigate

- look in to migrating the code base in to Flutter to allow for unified app development across iOS and Android

- look in to creating a native iOS version outside of Flutter if not feasible


# Development Info
## 1.2.+ (Quality of Life Updates):
### To Do
- Create localized screenshots for supported languages in play store listing

- Change summaries for settings disabled by other settings to reflect why they are disabled (for example if auto do not disturb is disabled, change the summary of the interval setting to something like "auto do not disturb is disabled")

- Put this in somewhere... (https://www.reddit.com/r/Android/comments/9zvei4/i_just_released_my_opensource_app_to_help_you/eacdemo/)

- Edit all icons to remove the two-tone design, make them all simple and flat

- Add icons to about screen

- Remove hours countdown when it is 0 possibly

- Add dialog like the rating dialog that appears after 10 days of use or so if the user hasn't purchased the advanced options. Gently suggest that the user can purchase the advanced options to help support development of the app

### Done

- Set auto do not disturb to false if do not disturb access is not granted, and do the same for smart notifications with usage access (they can sometimes be enabled by restoring data from the play store even if permission is not granted). There is protection to stop crashes from this but it isn't clear to the user

- Move all string resources to string value xml file, replace preference updating by creating a new string with two separate strings and adding them together around the value. Basically just make the app easy to translate to other languages by the community just by adding localized string values

- Change default bedtime to something slightly more sensible

## 1.3 (Major Feature Update):
### To Do
- Add separare bedtimes for weekdays and weekends (perhaps have an interface like the clock app which lets you add bedtimes much like alarms. Then have the ability to select which days it is active)

- Revamp custom notifications screen, allow user to have infinite custom notifications. Also instead of having like "Notification 1, Notification 2" have a header saying custom notifications and simply list them, allowing more to fit on screen

- Refactor settingsFragment code (things are called way more often than they need to be)

- Put app settings in categories to make it more intuitive

- Add app shortcut for go to sleep early feature

- General performance optimizations

### Done

## Future Feature Suggestions:
- Native translation contribution screen and central database to help along adoption of many languages without expensive translation services

- Add sleep tracking. Perhaps as a direct competitor to Sleep as Android

- Use only vector files vs png if possible

- Animate stars to gently blink

- Animate moon and stars to fade away as the sun rises

- Animate background to shift color as the day gets brighter

- AMOLED black theme option (maybe in premium category? idk)

- Automatic day/night mode

- Refactor styles xml and possibly colors xml so that they are better organized/labelled and more compatible with everything

- Refactor preferences code to reduce dependence on 3rd party libraries

- Add background themeing (maybe)

