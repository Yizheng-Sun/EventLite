# EventLite
A web Application using Java Spring framework with MVC Model

## Desription 
EventLite is designed for users to orgnize their daily agenda. A user can create events with detailed infomation and update or delete it. Every event is linked with a venue. Users can create, update and delete venues as well. Venues are displayed on maps with pop-up windows. Users can also post tweets to the built-in twitter account. The five latest tweets will be displayed on the index page as a timeline. RESTFUL API is provided as well.
 
## Key Techniques
### The src repository includes three parts:
</br>Model: Contain Event Entity and Venue Entity. Behave like the database.
</br>Controller: Handle HTTP request and extract data from model. Insert data into front-end pages.
</br>View: HTML pages using thymeleaf to get data from back end.

MapBox API is used to display venues on map. Twitter4J API is used to post and retrieve tweets.
