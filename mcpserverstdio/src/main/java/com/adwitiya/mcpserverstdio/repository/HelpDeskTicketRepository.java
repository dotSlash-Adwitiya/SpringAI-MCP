package com.adwitiya.mcpserverstdio.repository;

import com.adwitiya.mcpserverstdio.entity.HelpDeskTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpDeskTicketRepository extends JpaRepository<HelpDeskTicket, Long> {

    List<HelpDeskTicket> findByUsername(String username);
}