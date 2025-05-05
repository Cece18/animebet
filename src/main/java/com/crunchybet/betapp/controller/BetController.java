package com.crunchybet.betapp.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bets")
public class BetController {

    @GetMapping("/user-bets")
    public String getUserBets() {
        return "List of user bets";
    }

    //Place a bet
    @GetMapping("/place-bet")
    public String placeBet() {
        return "Place a bet";
    }


    //update a bet
    @GetMapping("/update-bet")
    public String updateBet() {
        return "Update a bet";
    }


}
