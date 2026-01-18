package com.ticketmaster.service;

import com.ticketmaster.model.ProofOfPayment;
import com.ticketmaster.repository.ProofRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProofService {

    private final ProofRepository repository;

    public ProofService(ProofRepository repository) {
        this.repository = repository;
    }

    public ProofOfPayment saveProof(ProofOfPayment proof) {
        return repository.save(proof);
    }

    public List<ProofOfPayment> getAllProofs() {
        return repository.findAll();
    }

    public ProofOfPayment getProofById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void deleteProof(Long id) {
        repository.deleteById(id);
    }

}
