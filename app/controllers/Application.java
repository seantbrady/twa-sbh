package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import services.FlightService;
import views.html.index;

public class Application extends Controller {
  
    public static Result index() {
        final FlightService flightService = new FlightService();
        flightService.execute();
        return ok(index.render("Your new application is ready."));
    }
  
}
