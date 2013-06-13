OpenMash Calendar Fixture Generator
===================================

The OpenMash calendar fixture generator is a test tool to aid manual testing of
the [OpenMash Saturn]() calendar frontend. The fixture generator handles generating
a large number of events in both user and shared calendars, with some of the events
spanning multiple days, and some of the events associated with multiple users.


Installation
------------


### Prerequisites

You will need to install [maven](http://maven.apache.org/) version 3 or higher
to build and run mashmesh.


### Configuring API Access

Before running the project, you will need to allow it to access several
Google APIs.

First, visit the [Google API Console](https://code.google.com/apis/console/‎).
If this is your first Google API project, click "Create project..." on the
landing page to get started. Otherwise, if you have an existing project,
select "Create..." from the projects dropdown in the upper left of the window.

Next, select "Services" from the left-hand navigation bar. Click the toggle
switch (shown below) in the "Status" column of the "Calendar API" row of the
services table to enable the Google Calendar API.

After this, you will need to create and configure service account.
Select "API access" from the left-hand navigation bar, and then click
"Create an OAuth 2.0 client ID...".
Give the product an appropriate product name, and optionally configure a
product logo and home page URL.

Click "Next", and on the next page select "Web application" as the client
type. You can safely ignore the "site or hostname" field - it is not used
by the application. Click "Create client ID" to generate a web application ID.

Finally, you will need to configure the application to use the API
credentials. Copy `oauth.properties.template` to `oauth.properties`. Edit
oauth.properties and fill in the missing fields as follows:

- Configure `google.apiKey` with the "API key" value listed in the "Simple
  API Access" section.
- Set `google.oauth.consumerKey` to the "Client ID" value listed under
  the "Client ID for web applications" section.
- Set `google.oauth.consumerSecret` to the "Client secret" value listed
  in the "Client ID for web applications" section.

The following image shows where each field is found in the API Console:

![API Access Fields](https://raw.github.com/openmash/calendar-fixture-generator/master/doc/client-credentials.png)


### Granting Two-legged OAuth Access

Log into the Google Apps control panel for your domain as an administrator,
select "Advanced Tools" from the navigation bar, and then click on the
"Manage third party OAuth Client access" link.

![Apps Control Panel](https://raw.github.com/openmash/calendar-fixture-generator/master/doc/control-panel.png)

In the "Manage API client access" screen, enter the value of the
`google.oauth.consumerKey` property in the "Client Name" field,
enter `http://www.google.com/calendar/feeds/` in the "One or More API Scopes"
field, and then click "Authorize".

![Third Party OAuth](https://raw.github.com/openmash/calendar-fixture-generator/master/doc/third-party-oauth.png)

The calendar fixture generator should now be able to connect to Google Calendar
and create events as any user in the configured domain.


Running the Fixture Generator
-------------------------------------------

Execute the command `mvn compile exec:java` to clear and repopulate the calendars for
users user20@test3.sheepdoginc.ca through test50@test3.sheepdoginc.ca. The logic to
generate the calendars can be found in the source code file
`src/main/groovy/com/sheepdog/calendar/CalendarFixtureGenerator.groovy`.
