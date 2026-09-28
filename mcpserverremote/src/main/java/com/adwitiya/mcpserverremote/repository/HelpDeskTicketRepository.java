package com.adwitiya.mcpserverremote.repository;

import com.adwitiya.mcpserverremote.entity.HelpDeskTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpDeskTicketRepository extends JpaRepository<HelpDeskTicket, Long> {

    List<HelpDeskTicket> findByUsername(String username);
}