package com.crunchybet.betapp.repository;

import com.crunchybet.betapp.model.Bet;
import com.crunchybet.betapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BetRepository extends JpaRepository<Bet, Long> {

    List<Bet> findByUserId(Long userId);
    List<Bet> findByNomineeId(Long nomineeId);
    List<Bet> findByUserOrderByPlacedAtDesc(User user);;
}
