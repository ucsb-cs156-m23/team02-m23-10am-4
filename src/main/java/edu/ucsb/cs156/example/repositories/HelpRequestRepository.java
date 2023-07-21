package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.HelpRequest;

import java.time.LocalDate;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface HelpRequestRepository extends CrudRepository<UCSBDate, Long> {
  Iterable<HelpRequest> findAllByRequester(String requester);
  Iterable<HelpRequest> findAllByDate(LocalDate requestDate);
}