This is a test application that demos how to start new activities and send information between activities.

It also implements a list view that is populated using an array of strings. A callback method is demonstrated which is called when an item in the ListView is clicked. Currently the callback method prints information to the console about which item in the list was selected.

The functionality demonstrated here is most of what we need in order to expand the single-screen Vocal application into one that allows the user to select from a list of optional songs.

The "change screen" button does a simple screen change in which information is not expected to be returned from the new activity.

The "change screen 2" button utilizes a slightly-modified method to start the new activitiy so that when the new activity finishes, information is passed back to the main activity.

The ListView contains two items: "foo" and "bar".

![alt tag](http://i.imgur.com/Cftipl0.png)
