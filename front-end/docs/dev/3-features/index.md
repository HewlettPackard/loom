3-Features
==========

Let's have a look at the features of the application and discover how they're tied to loom functionalities.

The app is composed of two main screens, the login screen and the tapestry screen.
The later comprises multiple components providing specific functionnalities, screens within the screens of sorts.

## Login

### Login form

The login form will greet users launching Weaver. It'll let them pick which provider (from the list provided by Loom server) they wish to authenticate with and enter their credentials.

### Choice of Loom server on Windows8 app

For the windows 8 app, users will have to pick which Loom server their app should
connect to before they can autheticate. This screen will appear everytime the app starts. However, the last URL will be remembered.

### Offline warning

When Weaver cannot load the list of providers from Loom server, it will display a message to warn the user.

## Tapestry screen

Once logged in users will see the Tapestry screen, letting browse the data available on the Loom server. Weaver will load the providers default list of Threads after login.

### List of threads

The main space of the screen shows the list of Threads currently displayed by the user. For each Thread, it shows the data resulting from its current query, as well as a Query editor letting users customize the query, and options to configure the display of the results.

#### Display content of Threads

Each Thread displays
##### Header

The header shows the user the title of the Thread, as well as a count of the items
it currently display. It also lets users reorder Threads and toggle the QueryEditor.

##### Fibers

Each Thread displays the fibers corresponding to the result of their Query.

###### Fiber states (item/aggregation, new, updated, alert)

Fibers will show in a different fashion according to their internal state. Users will be able to distinguish between:
 - items and aggregations
 - new fibers in the thread
 - fibers whose data have been updated
 - fibers with alerts

###### Fiber details

Upon selecting a fiber, users will be able to access the values of the attributes of this fibers, as well as details on the alert if it has one. Only attributes listed as "visible" will be displayed in the Thread.

If the ItemType provides a `unit` for the attribute, the value will display with this unit. 

A specific `format` can also be provided for Time values, as a [Java DateTimeFormat string](http://joda-time.sourceforge.net/apidocs/org/joda/time/format/DateTimeFormat.html) to format time. For this property, the special value `human` will display the date in a "XYZ ago" format.

###### Actions

Providers can define actions to be executed on Items and/or Aggregations of specific ItemTypes. An icon for each action will appear on the fiber details, letting the user fill in whatever information is needed to execute the action before sending it to Loom. 

Note: Need to review UX to scale with multiple actions
Note: Fire and forget on actions

###### Filter related

To help reduce the amount of information on screen and focus on the project at hand, users can filter the other Threads to display only fibers related to selected fiber.

###### Bookmark fiber

To help focus on a specific subset of fibers on the screen, users can bookmark a fiber to fade out all fibers that are not related to it, but leaving them visible on the screen. Users can combine multiple fibers as a bookmark to narrow down further which data they're interested in.

#### Query Editor

Theads present the result of a Query over a specific ItemType. The Query editor lets users view and edit which Query is applied. 

##### Adding, removing and reordering operations

Users can add,remove reconfigure and reorder operations available for this specific ItemType. The description of the operations is provided by Loom and Weaver provides a generic display to let user configure the operations. Specific views can be added (by plugins for example) to provide customo displays for certain operations.

##### Pre-set query

To help users access commonly used operations quickly, the QueryEditor displays a set of suggested operations.

Note: List of suggested operations is hardcoded in Weaver at the moment. Maybe there'd be use for letting Loom provide a custom list for specific ItemTypes (best analysed through such and such operations).

### Thread settings menu

Each Thread displays a menu letting the user access additional settings.

#### Clone

Adds a copy of the Thread to the Tapestry.

##### Close

Removes the Thread for the Tapestry

##### Metrics

ItemTypes can list some of their attributes as "plottable". This will let Weaver know that it is interesting for users to view them as bar charts. Users will then be able to pick which of these to display in the "Metrics" menu.

##### Quick sort

Loom provides a Sort operation to sort the Items in a Thread, but it requires to update the Query of the Thread. The QuickSort lets users reorder the _fibers in the result_ according to some of their properties.

When sorted, the Fibers will display the value used for sorting as part of their label so users can navigate the data more easily.

##### Thread actions

Some ItemTypes will provide actions that can be executed without a specific fiber (eg. create a new file). This menu allows user to pick which action to execute. As for actions targetting a specific Fiber, users will be able to fill whichever parameters are necessary before pushing the action to Loom.

##### Change of display (plugins)

Some plugins might introduce alternate way to display the data of the Threads. This menu will let users pick which representation they want to use.

### Toolbar

#### Bookmarked fibers

After fibers are bookmarked, they get listed in the toolbar so users can see which ones are used to focus on specific fibers and remove the ones they no longer need.

#### Relations panel

The relations panel will presents users features to help them understanding relations.

When typed relations are used on the screen, it allows users to highlight specific ones and control how deep to pursue those relations.

Note: is that still usefull with the feature showing the path from one fiber to another?

It also lets users highlight how a fiber is related to the selected fiber.

#### Patterns pannel

The patterns pannel lists the patterns available from the different providers. It lets users know which ones are already on screen (based on the ItemTypes of the Threads) and add new ones to the tapestry.

#### Providers pannel

The providers pannel lets users connect and disconnect from the different providers deployed on the Loom server. When losing connection to a provider (due to a session timeout, for example), it will display automatically to let users log-in again.

It also lets users highlight which fibers come from a specific provider.

#### Relations graph

The relation's graph button allows users to visualise a graph of the different item types offered by the providers they are logged into. Users can zoom in and out the graph to focus on specific parts. They can tap on a specific provider to fit the graph to the item types of that provider only.

Note: This should probably come from either the relations panel (it's about relations after all)
Note: A reduced version of that could be used to display informations about the schema offered by providers before users log in. 

## Plugins

### DMA

The DMA plugin brings in features specific to "The machine" and the visualisation of discrepancies with expected states. 

#### Updated view for fiber attributes 

When the DMA plugin is loaded, the attributes displayed when users are looking at the details of a fiber show if they match the value they're expected to have. If not users can access the value the attribute is expected to have.

#### Filter DMA state 

The plugin also adds a toggle allowing users to quickly group threads according to wether fibers match their expected state or not.

### Table

The table plugin adds an alternative visualisation letting users display the attributes of the fibers in a table.

### Treemap

The treemap plugin adds an alternative visualisation that displays the fibers as a treemap, allowing an easier view of the difference of data in aggregations

### Map

