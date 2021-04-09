# UssdDemo
A demo project implemented to interact with ussd alert dialog programmatically in android.

This app will auto input "0" and click "Cancel" Button in USSD Alert Dialog(s) starting with "Today's usage".

Please make one of your sim cards as default from the Sim Settings, so Call button on the app won't take you outside of the app to select sim cards!

Also enable Accessibility Permission beforehand for this app by going to Settings > Additional Settings > Accessibility; This path may be different in your phone.

Kill and restart the app if it doesn't work as expected even after enabling the Accessibility Permission.


# ShafinKhadem's notes

## motivation
My mobile operator Banglalink was randomly pushing USSD popup starting with "Today's usage" and giving me options to buy net packages.
Screen never turns off automatically in samsung galaxy when such USSD popups are waiting for input.

## idea
Got the idea from: https://stackoverflow.com/questions/22057625/prevent-ussd-dialog-and-read-ussd-response