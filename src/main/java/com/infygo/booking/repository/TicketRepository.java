package com.infygo.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.infygo.booking.entity.Ticket;



@Repository
public interface TicketRepository extends JpaRepository<Ticket, Double> {

}
