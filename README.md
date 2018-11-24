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


# Development Info
## 1.3:
### To Do
- Not set

### Done

## Future Feature Suggestions:
- Edit all icons to remove the two-tone design, make them all simple and flat

- Refactor settingsFragment code (things are called way more often than they need to be)

- Add separare bedtimes for weekdays and weekends (perhaps have an interface like the clock app which lets you add bedtimes much like alarms. Then have the ability to select which days it is active)

- Move all string resources to string value xml file, replace preference updating by creating a new string with two separate strings and adding them together around the value. Basically just make the app easy to translate to other languages by the community just by adding localized string values

- Use only vector files vs png if possible

- Add icons to about screen

- Revamp custom notifications screen, allow user to have infinite custom notifications. Also instead of having like "Notification 1, Notification 2" have a header saying custom notifications and simply list them, allowing more to fit on screen

- General performance optimizations

- Animate stars to gently blink

- Animate moon and stars to fade away as the sun rises

- Animate background to shift color as the day gets brighter

- Add app shortcut for go to sleep early feature

- AMOLED black theme option (maybe in premium category? idk)

- Automatic day/night mode

- Remove hours countdown when it is 0 possibly

- Refactor styles xml and possibly colors xml so that they are better organized/labelled and more compatible with everything

- Refactor preferences code to reduce dependence on 3rd party libraries

- Add background themeing (maybe)

