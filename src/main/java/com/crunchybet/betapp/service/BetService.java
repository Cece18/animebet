package com.crunchybet.betapp.service;

import com.crunchybet.betapp.model.Bet;
import com.crunchybet.betapp.repository.BetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BetService {
    @Autowired BetRepository betRepository;

    //Method to get bets of a user
    public List<Bet> getBetsByUserId(Long userId) {
        return betRepository.findByUserId(userId);
    }

}
