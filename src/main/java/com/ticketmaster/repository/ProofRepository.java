package com.ticketmaster.repository;

import com.ticketmaster.model.ProofOfPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProofRepository extends JpaRepository<ProofOfPayment, Long> {
}
