/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.infygo.booking.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.infygo.booking.dto.BookingDetails;
import com.infygo.booking.dto.Flight;
import com.infygo.booking.dto.PassengerDetails;
import com.infygo.booking.entity.Passenger;
import com.infygo.booking.entity.Ticket;
import com.infygo.booking.exception.ARSServiceException;
import com.infygo.booking.exception.ExceptionConstants;
import com.infygo.booking.exception.InfyGoServiceException;
import com.infygo.booking.service.PassengerService;
import com.infygo.booking.service.TicketService;
import com.infygo.booking.utility.ClientErrorInformation;


@RestController
@RequestMapping("/book")
public class BookingController {

	protected Logger logger = Logger.getLogger(BookingController.class.getName());

//	@Autowired
//	private FlightService flightService;	
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private TicketService ticketService;
	@Autowired
	private PassengerService passengerService;
	private Ticket ticket;
	private int noOfSeats;
	

	public BookingController() {
		ticket = new Ticket();		
	}


	@PostMapping(value = "/{flightId}/{username}", produces = "application/json", consumes = "application/json")
	public ResponseEntity<BookingDetails> bookFlight(@PathVariable("flightId") String flightId,
		 @Valid @RequestBody PassengerDetails passengerDetails, @PathVariable("username") String username,Errors errors) throws InfyGoServiceException, ARSServiceException {
			System.out.println("1\n");
		    if (errors.hasErrors()) {
			return new ResponseEntity(new ClientErrorInformation(HttpStatus.BAD_REQUEST.value(),errors.getFieldError("passengerList").getDefaultMessage()), HttpStatus.BAD_REQUEST);
		    }
		    System.out.println("2\n");
		if(passengerDetails.getPassengerList().isEmpty())
        	throw new InfyGoServiceException(ExceptionConstants.PASSENGER_LIST_EMPTY.toString());
		System.out.println("3\n");
		List<Passenger> passengerList = new ArrayList<Passenger>();
		for (Passenger passengers : passengerDetails.getPassengerList()) {
			passengerList.add(passengers);
		    

		}
		System.out.println(passengerList.toString());

		logger.log(Level.INFO, "Book Flight method ");

		logger.log(Level.INFO, passengerDetails.toString());
		int pnr = (int) (Math.random() * 1858955);

		ticket.setPnr(pnr);
//		Date date = new Date();
//		Calendar calendar = Calendar.getInstance();
//		calendar.setTime(date);
          
		Flight flight = restTemplate.getForObject("http://localhost:9200/flights/"+flightId, Flight.class);
				//flightService.getFlights(flightId);

		double fare = Double.parseDouble(flight.getFare());
		System.out.println("Fare per person:****** " + fare);
		System.out.println("List size:****** " + passengerDetails.getPassengerList().size());
		double totalFare = fare * (passengerDetails.getPassengerList().size());

		BookingDetails bookingDetails = new BookingDetails();
		bookingDetails.setPassengerList(passengerDetails.getPassengerList());
		bookingDetails.setPnr(pnr);
		bookingDetails.setTotalFare(totalFare);
		ticket.setBookingDate(new Date());
		System.out.println(ticket.getBookingDate());
		ticket.setDepartureDate(flight.getJourneyDate());
		ticket.setDepartureTime(flight.getDepartureTime());
		ticket.setFlightId(flight.getFlightId());
		ticket.setUserId(username);		
		ticket.setTotalFare(totalFare);
		noOfSeats = passengerDetails.getPassengerList().size();
		ticket.setNoOfSeats(noOfSeats);
	    ticketService.createTicket(ticket);
    
		addPassengers(bookingDetails.getPassengerList());
		
		restTemplate.put("http:localhost:9200/flights"+flightId+"/"+noOfSeats, bookingDetails);
//		flightService.updateFlight(flightId, noOfSeats);

		return new ResponseEntity<BookingDetails>(bookingDetails, HttpStatus.OK);

	}

	private void addPassengers(List<Passenger> passengers) {
		
		for (Passenger passenger : passengers) {
			passenger.setTicketId(passenger.getTicketId());;	    

		}

		passengerService.createPassenger(passengers);

	}

}
